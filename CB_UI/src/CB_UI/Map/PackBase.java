package CB_UI.Map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;

import CB_Core.Map.Descriptor;
import CB_Utils.Util.FileIO;

public abstract class PackBase implements Comparable<PackBase>
{

	public long MaxAge = 0;
	public static boolean Cancel = false;
	public Layer Layer = null;
	public boolean IsOverlay = false;
	public String Filename = "";
	public ArrayList<BoundingBox> BoundingBoxes = new ArrayList<BoundingBox>();

	public abstract byte[] LoadFromBoundingBoxByteArray(BoundingBox bbox, Descriptor desc);

	/**
	 * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that starts at <tt>offset</tt>.
	 */
	protected static byte[] get(byte[] array, int offset, int length)
	{
		byte[] result = new byte[length];
		System.arraycopy(array, offset, result, 0, length);
		return result;
	}

	public PackBase(Layer layer)
	{
		this.Layer = layer;
	}

	public PackBase(ManagerBase manager, String file) throws IOException
	{
		Filename = file;

		File queryFile = new File(file);
		FileInputStream stream = new FileInputStream(queryFile);
		DataInputStream reader = new DataInputStream(stream);

		/*
		 * DataInputStream reader = new DataInputStream() Stream stream = new FileStream(file, FileMode.Open); BinaryReader reader = new
		 * BinaryReader(stream);
		 */
		String layerName = readString(reader, 32);
		String friendlyName = readString(reader, 128);
		String url = readString(reader, 256);
		Layer = manager.GetLayerByName(layerName, friendlyName, url);

		long ticks = Long.reverseBytes(reader.readLong());
		MaxAge = ticks;

		int numBoundingBoxes = Integer.reverseBytes(reader.readInt());
		for (int i = 0; i < numBoundingBoxes; i++)
			BoundingBoxes.add(new BoundingBox(reader));

		reader.close();
		stream.close();

	}

	// make a new one from the existing BoundingBoxes
	// WritePackFromBoundingBoxes();
	public void WritePackFromBoundingBoxes() throws IOException
	{
		/*
		 * FileStream stream = new FileStream(filename, FileMode.Create); BinaryWriter writer = new BinaryWriter(stream);
		 */
		FileOutputStream stream = new FileOutputStream(Filename + ".new");
		DataOutputStream writer = new DataOutputStream(stream);

		Write(writer);
		writer.flush();
		writer.close();

		if (Cancel)
		{
			File file = new File(Filename);
			file.delete();
		}
	}

	// / <summary>
	// / �berpr�ft, ob der Descriptor in diesem Map Pack enthalten ist und liefert
	// / die BoundingBox, falls dies der Fall ist, bzw. null
	// / </summary>
	// / <param name="desc">Deskriptor, dessen </param>
	// / <returns></returns>
	public BoundingBox Contains(Descriptor desc)
	{
		for (BoundingBox bbox : BoundingBoxes)
			if (bbox.Zoom == desc.Zoom && desc.X <= bbox.MaxX && desc.X >= bbox.MinX && desc.Y <= bbox.MaxY && desc.Y >= bbox.MinY) return bbox;

		return null;
	}

	public int NumTilesTotal()
	{
		int result = 0;
		for (BoundingBox bbox : BoundingBoxes)
			result += bbox.NumTilesTotal();

		return result;
	}

	/*
	 * public delegate void ProgressDelegate(String msg, int zoom, int x, int y, int num, int total);
	 */
	protected void writeString(String text, DataOutputStream writer, int length) throws IOException
	{
		if (text.length() > length) text = text.substring(0, length);
		else
			while (text.length() < length)
				text += " ";
		byte[] asciiBytes = EncodingUtils.getAsciiBytes(text);
		for (int i = 0; i < length; i++)
			writer.write(asciiBytes[i]);
	}

	protected String readString(DataInputStream reader, int length) throws IOException
	{
		byte[] asciiBytes = new byte[length];
		int last = 0;
		for (int i = 0; i < length; i++)
		{
			asciiBytes[i] = reader.readByte();
			if (asciiBytes[i] > 32) last = i;
		}
		return EncodingUtils.getAsciiString(asciiBytes, 0, last + 1).trim();
	}

	public void CreateBoudingBoxesFromBounds(int minZoom, int maxZoom, double minLat, double maxLat, double minLon, double maxLon)
	{
		for (int zoom = minZoom; zoom <= maxZoom; zoom++)
		{
			int minX = (int) Descriptor.LongitudeToTileX(zoom, minLon);
			int maxX = (int) Descriptor.LongitudeToTileX(zoom, maxLon);

			int minY = (int) Descriptor.LatitudeToTileY(zoom, maxLat);
			int maxY = (int) Descriptor.LatitudeToTileY(zoom, minLat);

			BoundingBoxes.add(new BoundingBox(zoom, minX, maxX, minY, maxY, 0));
		}
	}

	public void GeneratePack(String filename, long maxAge, int minZoom, int maxZoom, double minLat, double maxLat, double minLon,
			double maxLon) throws IOException
	{
		MaxAge = maxAge;
		Filename = filename;

		CreateBoudingBoxesFromBounds(minZoom, maxZoom, minLat, maxLat, minLon, maxLon);
		/*
		 * FileStream stream = new FileStream(filename, FileMode.Create); BinaryWriter writer = new BinaryWriter(stream);
		 */
		FileOutputStream stream = new FileOutputStream(filename);
		DataOutputStream writer = new DataOutputStream(stream);

		Write(writer);
		writer.flush();
		writer.close();

		if (Cancel)
		{
			File file = new File(filename);
			file.delete();
		}
	}

	// / <summary>
	// / Speichert ein im lokalen Dateisystem vorliegendes Pack in den writer
	// / </summary>
	// / <param name="writer"></param>
	public void Write(DataOutputStream writer) throws IOException
	{
		// int numTilesTotal = NumTilesTotal();

		// Header
		writeString(Layer.Name, writer, 32);
		writeString(Layer.FriendlyName, writer, 128);
		writeString(Layer.Url, writer, 256);
		writer.writeLong(Long.reverseBytes(MaxAge));
		writer.writeInt(Integer.reverseBytes(BoundingBoxes.size()));

		// Offsets berechnen
		long offset = 32 + 128 + 256 + 8 + 4 + 8 + BoundingBoxes.size() * 28 /* BoundingBox.SizeOf */;
		for (int i = 0; i < BoundingBoxes.size(); i++)
		{
			BoundingBoxes.get(i).OffsetToIndex = offset;
			offset += BoundingBoxes.get(i).NumTilesTotal() * 8;
		}

		// Bounding Boxes schreiben
		for (int i = 0; i < BoundingBoxes.size(); i++)
			BoundingBoxes.get(i).Write(writer);

		// Indexe erzeugen
		for (int i = 0; i < BoundingBoxes.size(); i++)
		{
			BoundingBox bbox = BoundingBoxes.get(i);

			for (int y = bbox.MinY; y <= bbox.MaxY && !Cancel; y++)
			{
				for (int x = bbox.MinX; x <= bbox.MaxX && !Cancel; x++)
				{
					// Offset zum Bild absaven
					writer.writeLong(Long.reverseBytes(offset));

					// Dateigr��e ermitteln
					String local = Layer.GetLocalFilename(new Descriptor(x, y, bbox.Zoom));

					if (FileIO.FileExists(local))
					{
						File info = new File(local);
						if (info.lastModified() < MaxAge) Layer.DownloadTile(new Descriptor(x, y, bbox.Zoom));
					}
					else
						Layer.DownloadTile(new Descriptor(x, y, bbox.Zoom));

					// Nicht vorhandene Tiles haben die L�nge 0
					if (!FileIO.FileExists(local)) offset += 0;
					else
					{
						File info = new File(local);
						offset += info.length();
					}

					/*
					 * if (OnProgressChanged != null) OnProgressChanged("Building index...", cnt++, numTilesTotal);
					 */
				}
			}
		}

		// Zur L�ngenberechnung
		writer.writeLong(Long.reverseBytes(offset));

		// So, und nun kopieren wir noch den Mist rein
		for (int i = 0; i < BoundingBoxes.size() && !Cancel; i++)
		{
			BoundingBox bbox = BoundingBoxes.get(i);

			for (int y = bbox.MinY; y <= bbox.MaxY && !Cancel; y++)
			{
				for (int x = bbox.MinX; x <= bbox.MaxX && !Cancel; x++)
				{
					String local = Layer.GetLocalFilename(new Descriptor(x, y, bbox.Zoom));
					File f = new File(local);
					if (!f.exists() || f.lastModified() < MaxAge) if (!Layer.DownloadTile(new Descriptor(x, y, bbox.Zoom))) continue;
					FileInputStream imageStream = new FileInputStream(local);
					int anzAvailable = (int) f.length();
					byte[] temp = new byte[anzAvailable];
					imageStream.read(temp);
					writer.write(temp);
					imageStream.close();

					// if (OnProgressChanged != null) OnProgressChanged("Linking package...", cnt++, numTilesTotal);

				}
			}
		}
	}

	@Override
	public int compareTo(PackBase arg0)
	{
		if (this.MaxAge < arg0.MaxAge) return -1;

		if (this.MaxAge > arg0.MaxAge) return 1;

		return 0;
	}

}