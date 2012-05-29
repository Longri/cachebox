package de.Map;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import CB_Core.FileIO;
import CB_Core.Map.BoundingBox;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.PackBase;

public class Manager extends ManagerBase {
	
	public Manager()
	{
		// for the Access to the manager in the CB_Core
		CB_Core.Map.ManagerBase.Manager = this;
		// Layers.add(new Layer("MapsForge", "MapsForge", ""));
		Layers.add(new Layer("Mapnik", "Mapnik", "http://a.tile.openstreetmap.org/"));
		Layers.add(new Layer("OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/"));
//		Layers.add(new Layer("TilesAtHome", "Osmarender", "http://a.tah.openstreetmap.org/Tiles/tile/"));
	}

	
	@Override
	public byte[] LoadLocalPixmap(Layer layer, Descriptor desc)
	{
		// Mapsforge 3.0
		if (layer.isMapsForge)
		{
			return null;
		}
		
		
		try
		{
			// Schauen, ob Tile im Cache liegt
			String cachedTileFilename = layer.GetLocalFilename(desc);

			long cachedTileAge = 0;

			if (FileIO.FileExists(cachedTileFilename))
			{
				File info = new File(cachedTileFilename);
				cachedTileAge = info.lastModified();
			}

			// Kachel im Pack suchen
			for (int i = 0; i < mapPacks.size(); i++)
			{
				PackBase mapPack = mapPacks.get(i);
				if ((mapPack.Layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge))
				{
					BoundingBox bbox = mapPacks.get(i).Contains(desc);

					if (bbox != null) return mapPacks.get(i).LoadFromBoundingBoxByteArray(bbox, desc);
				}
			}
			// Kein Map Pack am Start!
			// Falls Kachel im Cache liegt, diese von dort laden!
			if (cachedTileAge != 0)
			{
				File myImageFile = new File(cachedTileFilename);
				BufferedImage img = ImageIO.read(myImageFile);
				ByteArrayOutputStream bas =
				new ByteArrayOutputStream();
				ImageIO.write(img, "png", bas);
				byte[] data = bas.toByteArray();
				return data;
			}
		}
		catch (Exception exc)
		{
			/*
			 * #if DEBUG Global.AddLog("Manager.LoadLocalBitmap: " +
			 * exc.ToString()); #endif
			 */
		}
		return null;
		}
	
	
	// / <summary>
		// / Läd die Kachel mit dem übergebenen Descriptor
		// / </summary>
		// / <param name="layer"></param>
		// / <param name="tile"></param>
		// / <returns></returns>
		public boolean CacheTile(Layer layer, Descriptor tile)
		{
			// Gibts die Kachel schon in einem Mappack? Dann kann sie übersprungen
			// werden!
			for (PackBase pack : mapPacks)
				if (pack.Layer == layer) if (pack.Contains(tile) != null) return true;

			String filename = layer.GetLocalFilename(tile);
			String path = layer.GetLocalPath(tile);
			String url = layer.GetUrl(tile);

			// Falls Kachel schon geladen wurde, kann sie übersprungen werden
			synchronized (this)
			{
				if (FileIO.FileExists(filename)) return true;
			}

			// Kachel laden
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = null;
	
			try
			{
				response = httpclient.execute(new HttpGet(url));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK)
				{
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();

					String responseString = out.toString();

					// Verzeichnis anlegen
					synchronized (this)
					{
						if (!FileIO.DirectoryExists(path)) return false;
					}
					// Datei schreiben
					synchronized (this)
					{
						FileOutputStream stream = new FileOutputStream(filename, false);

						out.writeTo(stream);
						stream.close();
					}

					NumTilesLoaded++;
					// Global.TransferredBytes += result.Length;

					// ..more logic
				}
				else
				{
					// Closes the connection.
					response.getEntity().getContent().close();
					// throw new IOException(statusLine.getReasonPhrase());
					return false;
				}
				
			}
			catch (Exception ex)
			{
				return false;
			}
			/*
			 * finally { if (stream != null) { stream.Close(); stream = null; } if
			 * (responseStream != null) { responseStream.Close(); responseStream =
			 * null; } if (webResponse != null) { webResponse.Close(); webResponse =
			 * null; } if (webRequest != null) { webRequest.Abort(); webRequest =
			 * null; } GC.Collect(); }
			 */
			return true;
		}
	
	
	
}
