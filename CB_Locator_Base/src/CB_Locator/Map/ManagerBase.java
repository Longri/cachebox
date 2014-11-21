/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_Locator.Map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.layer.renderer.GL_DatabaseRenderer;
import org.mapsforge.map.layer.renderer.IDatabaseRenderer;
import org.mapsforge.map.layer.renderer.MF_DatabaseRenderer;
import org.mapsforge.map.layer.renderer.MixedDatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.rule.CB_RenderThemeHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import CB_Locator.LocatorSettings;
import CB_Locator.Tag;
import CB_Locator.Map.Layer.Type;
import CB_UI_Base.Global;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.GL_GraphicFactory;
import CB_UI_Base.graphics.GL_RenderType;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.LogLevel;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.Gdx;

/**
 * @author ging-buh
 * @author Longri
 */
public abstract class ManagerBase
{

	public static int PROCESSOR_COUNT;
	private final DisplayModel DISPLAY_MODEL;

	public static final String INTERNAL_CAR_THEME = "internal-car-theme";
	protected final int CONECTION_TIME_OUT = 15000;// 15 sec
	protected final int CONECTION_TIME_OUT_MESSAGE_INTERVALL = 60000;// 1min

	public static boolean RenderThemeChanged = true;

	public static ManagerBase Manager = null;

	public static long NumBytesLoaded = 0;

	public static int NumTilesLoaded = 0;

	public static int NumTilesCached = 0;

	public ArrayList<PackBase> mapPacks = new ArrayList<PackBase>();

	public ArrayList<TmsMap> tmsMaps = new ArrayList<TmsMap>();

	private final ArrayList<Layer> Layers = new ArrayList<Layer>();

	private final DefaultLayerList DEFAULT_LAYER = new DefaultLayerList();

	private boolean mayAddLayer = false; // add only during startup (GetLayerByName)

	protected String RenderTheme;

	public boolean isRenderThemeSetted()
	{
		if (RenderTheme != null && RenderTheme.length() > 0) return true;
		return false;
	}

	public ManagerBase(DisplayModel displaymodel)
	{
		// for the Access to the manager in the CB_Core
		CB_Locator.Map.ManagerBase.Manager = this;
		// PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
		PROCESSOR_COUNT = 1;
		DISPLAY_MODEL = displaymodel;

		LocatorSettings.MapsforgeRenderType.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				ManagerBase.this.setRenderTheme(getCurrentRenderTheme());
			}
		});
	}

	public abstract PackBase CreatePack(String file) throws IOException;

	// / <summary>
	// / L�d ein Map Pack und f�gt es dem Manager hinzu
	// / </summary>
	// / <param name="file"></param>
	// / <returns>true, falls das Pack erfolgreich geladen wurde, sonst
	// false</returns>
	public boolean LoadMapPack(String file)
	{
		try
		{
			PackBase pack = CreatePack(file);
			mapPacks.add(pack);

			// Nach Aktualit�t sortieren
			Collections.sort(mapPacks);
			return true;
		}
		catch (Exception exc)
		{
		}
		return false;
	}

	public Layer GetLayerByName(String Name, String friendlyName, String url)
	{
		if (Name == "OSM" || Name == "") Name = "Mapnik";

		for (Layer layer : Layers)
		{
			if (layer.Name.equalsIgnoreCase(Name)) return layer;
		}

		if (mayAddLayer)
		{
			Layer newLayer = new Layer(Type.normal, Name, Name, url);
			Layers.add(newLayer);
			return newLayer;
		}
		else
		{
			if (Layers != null && Layers.size() > 0)
			{
				LocatorSettings.CurrentMapLayer.setValue(Layers.get(0).Name);
				return Layers.get(0); // ist wahrscheinlich Mapnik und sollte immer tun
			}
			return null;
		}
	}

	private boolean useInvertedNightTheme;

	protected boolean mapsforgeNightThemeExist()
	{
		return !useInvertedNightTheme;
	}

	public void setUseInvertedNightTheme(boolean value)
	{
		useInvertedNightTheme = value;
	}

	public class ImageData
	{
		public int[] PixelColorArray;
		public int width;
		public int height;
	}

	public TileGL LoadLocalPixmap(String layer, Descriptor desc, int ThreadIndex)
	{
		return LoadLocalPixmap(GetLayerByName(layer, layer, ""), desc, ThreadIndex);
	}

	public abstract TileGL LoadLocalPixmap(Layer layer, Descriptor desc, int ThreadIndex);

	protected abstract ImageData getImagePixel(byte[] img);

	protected abstract byte[] getImageFromData(ImageData imgData);

	/**
	 * Load Tile from URL and save to MapTile-Cache
	 * 
	 * @param layer
	 * @param tile
	 * @return
	 */
	public boolean CacheTile(Layer layer, Descriptor tile)
	{

		if (layer == null) return false;

		// Gibts die Kachel schon in einem Mappack? Dann kann sie �bersprungen
		// werden!
		for (PackBase pack : mapPacks)
			if (pack.Layer == layer) if (pack.Contains(tile) != null) return true;

		String filename = layer.GetLocalFilename(tile);
		// String path = layer.GetLocalPath(tile);
		String url = layer.GetUrl(tile);

		// Falls Kachel schon geladen wurde, kann sie �bersprungen werden
		synchronized (this)
		{
			if (FileIO.FileExists(filename)) return true;
		}

		// Kachel laden
		// set the connection timeout value to 15 seconds (15000 milliseconds)
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONECTION_TIME_OUT);

		HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpResponse response = null;

		HttpGet GET = new HttpGet(url);

		try
		{

			response = httpclient.execute(GET);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();

				synchronized (this)
				{
					// Verzeichnis anlegen
					if (!FileIO.createDirectory(filename)) return false;

					// Datei schreiben
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
			/*
			 * webRequest = (HttpWebRequest)WebRequest.Create(url); webRequest.Timeout = 15000; webRequest.Proxy = Global.Proxy; webResponse
			 * = webRequest.GetResponse(); if (!webRequest.HaveResponse) return false; responseStream = webResponse.GetResponseStream();
			 * byte[] result = Global.ReadFully(responseStream, 64000); // Verzeichnis anlegen lock (this) if (!Directory.Exists(path))
			 * Directory.CreateDirectory(path); // Datei schreiben lock (this) { stream = new FileStream(filename, FileMode.CreateNew);
			 * stream.Write(result, 0, result.Length); } NumTilesLoaded++; Global.TransferredBytes += result.Length;
			 */
		}
		catch (Exception ex)
		{
			// Check last Error for this URL and post massage if the last > 1 min.

			String URL = GET.getURI().getAuthority();

			boolean PostErrorMassage = false;

			if (LastRequestTimeOut.containsKey(URL))
			{
				long last = LastRequestTimeOut.get(URL);
				if ((last + CONECTION_TIME_OUT_MESSAGE_INTERVALL) < System.currentTimeMillis())
				{
					PostErrorMassage = true;
					LastRequestTimeOut.remove(URL);
				}
			}
			else
			{
				PostErrorMassage = true;
			}

			if (PostErrorMassage)
			{
				LastRequestTimeOut.put(URL, System.currentTimeMillis());
				ConnectionError INSTANCE = new ConnectionError(layer.Name + " - Provider");
				GL.that.Toast(INSTANCE);
			}

			return false;
		}
		/*
		 * finally { if (stream != null) { stream.Close(); stream = null; } if (responseStream != null) { responseStream.Close();
		 * responseStream = null; } if (webResponse != null) { webResponse.Close(); webResponse = null; } if (webRequest != null) {
		 * webRequest.Abort(); webRequest = null; } GC.Collect(); }
		 */

		return true;
	}

	HashMap<String, Long> LastRequestTimeOut = new HashMap<String, Long>();

	/**
	 * The matrix is stored in a single array, and its treated as follows: [ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ] <br>
	 * <br>
	 * When applied to a color [r, g, b, a], the resulting color is computed as (after clamping) <br>
	 * R' = a*R + b*G + c*B + d*A + e;<br>
	 * G' = f*R + g*G + h*B + i*A + j;<br>
	 * B' = k*R + l*G + m*B + n*A + o;<br>
	 * A' = p*R + q*G + r*B + s*A + t;<br>
	 * 
	 * @param src
	 * @param matrix
	 * @return
	 */
	public static ImageData getImageDataWithColormatrixManipulation(float[] matrix, ImageData imgData)
	{

		int[] data = imgData.PixelColorArray;
		for (int i = 0; i < data.length; i++)
		{
			data[i] = colorMatrixManipulation(data[i], matrix);
		}
		return imgData;
	}

	public static int colorMatrixManipulation(int c, float[] matrix)
	{
		int[] color = new int[4];

		color[0] = (c >> 24) & (0xff);
		color[1] = ((c << 8) >> 24) & (0xff);
		color[2] = ((c << 16) >> 24) & (0xff);
		color[3] = ((c << 24) >> 24) & (0xff);

		int R = color[1];
		int G = color[2];
		int B = color[3];
		int A = color[0];

		color[1] = Math.max(0, Math.min(255, (int) ((matrix[0] * R) + (matrix[1] * G) + (matrix[2] * B) + (matrix[3] * A) + matrix[4])));
		color[2] = Math.max(0, Math.min(255, (int) ((matrix[5] * R) + (matrix[6] * G) + (matrix[7] * B) + (matrix[8] * A) + matrix[9])));
		color[3] = Math.max(0, Math.min(255, (int) ((matrix[10] * R) + (matrix[11] * G) + (matrix[12] * B) + (matrix[13] * A) + matrix[14])));
		color[0] = Math.max(0, Math.min(255, (int) ((matrix[15] * R) + (matrix[16] * G) + (matrix[17] * B) + (matrix[18] * A) + matrix[19])));

		return ((color[0] & 0xFF) << 24) | ((color[1] & 0xFF) << 16) | ((color[2] & 0xFF) << 8) | ((color[3] & 0xFF));
	}

	/**
	 * Night Color Matrix <br>
	 * <br>
	 * R= -1.0f, 0.0f, 0.0f, 0.0f, 255.0f, <br>
	 * G= 0.0f, -1.5f, 0.0f, 0.0f, 200.0f, <br>
	 * B= 0.0f, 0.0f, -1.5f, 0.0f, 0.f, <br>
	 * A= 0.0f, 0.0f, 0.0f, 0.0f, 255f <br>
	 */
	public static final float[] NIGHT_COLOR_MATRIX =
		{ /* */
		-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, /* */
		0.0f, -1.5f, 0.0f, 0.0f, 200.0f, /* */
		0.0f, 0.0f, -1.5f, 0.0f, 0.f, /* */
		0.0f, 0.0f, 0.0f, 0.0f, 255f };

	public void LoadTMS(String string)
	{
		try
		{
			TmsMap tmsMap = new TmsMap(string);
			if ((tmsMap.name == null) || (tmsMap.url == null))
			{
				return;
			}
			tmsMaps.add(tmsMap);
			Layers.add(new TmsLayer(Type.normal, tmsMap));
		}
		catch (Exception ex)
		{

		}

	}

	public void LoadBSH(String string)
	{
		try
		{
			BshLayer layer = new BshLayer(Type.normal, string);
			Layers.add(layer);
		}
		catch (Exception ex)
		{

		}

	}

	private void getFiles(ArrayList<String> files, ArrayList<String> mapnames, String directory)
	{
		File dir = new File(directory);
		String[] dirFiles = dir.list();
		if (dirFiles != null && dirFiles.length > 0)
		{
			for (String tmp : dirFiles)
			{
				String FilePath = directory + "/" + tmp;
				String ttt = tmp.toLowerCase();
				if (ttt.endsWith("pack") || ttt.endsWith("map") || ttt.endsWith("xml") || ttt.endsWith("bsh"))
				{
					if (!mapnames.contains(tmp))
					{
						files.add(FilePath);
						mapnames.add(tmp);
						Gdx.app.debug(Tag.TAG, "add: " + tmp);
					}
				}
			}
		}
	}

	public void initialMapPacks()
	{
		Layers.clear();

		mayAddLayer = true;

		// add default layer
		Layers.addAll(DEFAULT_LAYER);

		ArrayList<String> files = new ArrayList<String>();
		ArrayList<String> mapnames = new ArrayList<String>();

		Gdx.app.debug(Tag.TAG, "dirOwnMaps = " + LocatorSettings.MapPackFolderLocal.getValue());
		getFiles(files, mapnames, LocatorSettings.MapPackFolderLocal.getValue());

		Gdx.app.debug(Tag.TAG, "dirDefaultMaps = " + LocatorSettings.MapPackFolder.getDefaultValue());
		getFiles(files, mapnames, LocatorSettings.MapPackFolder.getDefaultValue());

		Gdx.app.debug(Tag.TAG, "dirGlobalMaps = " + LocatorSettings.MapPackFolder.getValue());
		getFiles(files, mapnames, LocatorSettings.MapPackFolder.getValue());

		if (!(files == null))
		{
			if (files.size() > 0)
			{
				for (String file : files)
				{
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("pack"))
					{
						ManagerBase.Manager.LoadMapPack(file);
					}
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("map"))
					{

						String Name = FileIO.GetFileNameWithoutExtension(file);
						Layer layer = new Layer(Type.normal, Name, Name, file);
						layer.isMapsForge = true;
						ManagerBase.Manager.Layers.add(layer);
					}
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("xml"))
					{
						ManagerBase.Manager.LoadTMS(file);
					}
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("bsh"))
					{
						ManagerBase.Manager.LoadBSH(file);
					}
				}
			}
		}
		mayAddLayer = false;
	}

	public ArrayList<Layer> getLayers()
	{
		return Layers;
	}

	// ##########################################################################
	// Mapsforge 0.4.0
	// ##########################################################################

	MapDatabase mapDatabase[] = null;
	IDatabaseRenderer databaseRenderer[] = null;
	Bitmap tileBitmap = null;
	File mapFile = null;
	private String mapsForgeFile = "";
	XmlRenderTheme renderTheme;
	public float textScale = 1;
	public static float DEFAULT_TEXT_SCALE = 1;

	/**
	 * Returns the loaded MapfileInfo or NULL
	 * 
	 * @param newLayer
	 * @return
	 */
	public MapFileInfo getMapsforgeLodedMapFileInfo(Layer layer)
	{
		if (!layer.isMapsForge) return null;

		if (!mapsForgeFile.equalsIgnoreCase(layer.Name))
		{
			// layer is not Loaded, so we load first
			LoadMapsforgeMap(layer);
		}

		try
		{
			MapFileInfo info = mapDatabase[0].getMapFileInfo();

			return info;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void clearRenderTheme()
	{
		RenderTheme = null;
		RenderThemeChanged = true;
	}

	public void setRenderTheme(String theme)
	{
		RenderTheme = theme;
		RenderThemeChanged = true;
	}

	public XmlRenderTheme getCurrentRenderTheme()
	{
		return renderTheme;
	}

	public void setRenderTheme(XmlRenderTheme RenderTheme)
	{
		renderTheme = RenderTheme;

		// Check RenderTheme valid
		try
		{
			CB_RenderThemeHandler.getRenderTheme(getGraphicFactory(DISPLAY_MODEL.getScaleFactor()), DISPLAY_MODEL, renderTheme);
		}
		catch (SAXException e)
		{
			String ErrorMsg = e.getMessage() + Global.br + "Line: " + ((SAXParseException) e).getLineNumber();
			GL.that.Toast(ErrorMsg, 8000);
			Gdx.app.error(Tag.TAG, "databaseRenderer: " + ErrorMsg);
			renderTheme = CB_InternalRenderTheme.OSMARENDER;
		}
		catch (ParserConfigurationException e)
		{
			String ErrorMsg = e.getMessage();
			GL.that.Toast(ErrorMsg, 8000);
			Gdx.app.error(Tag.TAG, "databaseRenderer: " + ErrorMsg);
			renderTheme = CB_InternalRenderTheme.OSMARENDER;
		}
		catch (IOException e)
		{
			String ErrorMsg = e.getMessage();
			GL.that.Toast(ErrorMsg, 8000);
			Gdx.app.error(Tag.TAG, "databaseRenderer: " + ErrorMsg);
			renderTheme = CB_InternalRenderTheme.OSMARENDER;
		}
		catch (Exception e)
		{
			String ErrorMsg = e.getMessage();
			if (GL.that != null) GL.that.Toast(ErrorMsg, 8000);
			Gdx.app.error(Tag.TAG, "databaseRenderer: " + ErrorMsg);
			renderTheme = CB_InternalRenderTheme.OSMARENDER;
		}

		databaseRenderer = new IDatabaseRenderer[PROCESSOR_COUNT];
		RenderThemeChanged = false;
	}

	public TileGL getMapsforgePixMap(Layer layer, Descriptor desc, int ThreadIndex)
	{
		// Mapsforge 0.4.0

		if ((mapDatabase == null) || (!mapsForgeFile.equalsIgnoreCase(layer.Name)))
		{
			LoadMapsforgeMap(layer);
		}

		if (RenderThemeChanged)
		{
			if (RenderTheme == null)
			{
				renderTheme = CB_InternalRenderTheme.OSMARENDER;
			}
			else if (RenderTheme.equals(INTERNAL_CAR_THEME))
			{
				renderTheme = CB_InternalRenderTheme.DAY_CAR_THEME;
			}
			else
			{
				try
				{
					Gdx.app.debug(Tag.TAG, "Suche RenderTheme: " + RenderTheme);
					if (RenderTheme == null)
					{
						Gdx.app.debug(Tag.TAG, "RenderTheme not found!");
						renderTheme = CB_InternalRenderTheme.OSMARENDER;

					}
					else
					{
						File file = new File(RenderTheme);
						if (file.exists())
						{
							Gdx.app.debug(Tag.TAG, "RenderTheme found!");
							renderTheme = new ExternalRenderTheme(file);

						}
						else
						{
							Gdx.app.debug(Tag.TAG, "RenderTheme not found!");
							renderTheme = CB_InternalRenderTheme.OSMARENDER;
						}
					}

				}
				catch (FileNotFoundException e)
				{
					Gdx.app.error(Tag.TAG, "Load RenderTheme Error loading RenderTheme!", e);
					renderTheme = CB_InternalRenderTheme.OSMARENDER;
				}
			}

			// Check RenderTheme valid
			try
			{
				CB_RenderThemeHandler.getRenderTheme(getGraphicFactory(DISPLAY_MODEL.getScaleFactor()), DISPLAY_MODEL, renderTheme);
			}
			catch (SAXException e)
			{
				String ErrorMsg = e.getMessage() + Global.br + "Line: " + ((SAXParseException) e).getLineNumber();
				GL.that.Toast(ErrorMsg, 8000);
				Gdx.app.error(Tag.TAG, "databaseRenderer: " + ErrorMsg);
				renderTheme = CB_InternalRenderTheme.OSMARENDER;
			}
			catch (ParserConfigurationException e)
			{
				String ErrorMsg = e.getMessage();
				GL.that.Toast(ErrorMsg, 8000);
				Gdx.app.error(Tag.TAG, "databaseRenderer: " + ErrorMsg);
				renderTheme = CB_InternalRenderTheme.OSMARENDER;
			}
			catch (IOException e)
			{
				String ErrorMsg = e.getMessage();
				GL.that.Toast(ErrorMsg, 8000);
				Gdx.app.error(Tag.TAG, "databaseRenderer: " + ErrorMsg);
				renderTheme = CB_InternalRenderTheme.OSMARENDER;
			}

			if (databaseRenderer != null)
			{
				for (int i = 0; i < PROCESSOR_COUNT; i++)
				{
					databaseRenderer[i] = null;
				}
			}

			RenderThemeChanged = false;
		}

		Tile tile = new Tile(desc.getX(), desc.getY(), (byte) desc.getZoom());

		// chk if MapDatabase Loded a Map File
		if (!this.mapDatabase[ThreadIndex].hasOpenFile())
		{
			return null;
		}

		RendererJob job = new RendererJob(tile, mapFile, renderTheme, DISPLAY_MODEL, textScale, false);

		if (databaseRenderer == null) databaseRenderer = new IDatabaseRenderer[PROCESSOR_COUNT];

		if (databaseRenderer[ThreadIndex] == null)
		{
			GL_RenderType RENDERING_TYPE = LocatorSettings.MapsforgeRenderType.getEnumValue();

			switch (RENDERING_TYPE)
			{
			case Mapsforge:
				databaseRenderer[ThreadIndex] = new MF_DatabaseRenderer(this.mapDatabase[ThreadIndex], getGraphicFactory(DISPLAY_MODEL.getScaleFactor()));
				break;
			case Mixing:
				databaseRenderer[ThreadIndex] = new MixedDatabaseRenderer(this.mapDatabase[ThreadIndex], getGraphicFactory(DISPLAY_MODEL.getScaleFactor()), ThreadIndex);
				break;
			case OpenGl:
				databaseRenderer[ThreadIndex] = new GL_DatabaseRenderer(this.mapDatabase[ThreadIndex], new GL_GraphicFactory(DISPLAY_MODEL.getScaleFactor()), DISPLAY_MODEL);
				break;
			default:
				databaseRenderer[ThreadIndex] = new MF_DatabaseRenderer(this.mapDatabase[ThreadIndex], getGraphicFactory(DISPLAY_MODEL.getScaleFactor()));
				break;

			}

		}

		if (databaseRenderer[ThreadIndex] == null)
		{
			Gdx.app.error(Tag.TAG, "databaseRenderer are NULL");
			return null;
		}

		try
		{
			TileGL Tile = databaseRenderer[ThreadIndex].execute(job);
			if (Tile == null)
			{
				Gdx.app.error(Tag.TAG, "databaseRenderer provid no Tile for: " + job.tile.toString());
			}

			return Tile;

		}
		catch (Exception e)
		{
			Gdx.app.error(Tag.TAG, "", e);
		}

		return null;
	}

	private void LoadMapsforgeMap(Layer layer)
	{
		// RenderThemeChanged = true;
		mapFile = new File(layer.Url);

		if (mapDatabase == null) mapDatabase = new MapDatabase[PROCESSOR_COUNT];
		Gdx.app.debug(Tag.TAG, "Open MapsForge Map: " + mapFile);
		for (int i = 0; i < PROCESSOR_COUNT; i++)
		{
			if (mapDatabase[i] == null) mapDatabase[i] = new MapDatabase();
			mapDatabase[i].closeFile();
			FileOpenResult result = mapDatabase[i].openFile(mapFile);

			if (result.isSuccess())
			{
				Gdx.app.debug(Tag.TAG, "Open Map success");
				if (i == 0) writeDebugMapInfo(mapDatabase[0].getMapFileInfo());
			}
			else
			{
				Gdx.app.error(Tag.TAG, "Open Map faild");
				Gdx.app.error(Tag.TAG, result.getErrorMessage());
			}
		}

		MapFileInfo info = mapDatabase[0].getMapFileInfo();
		if (info.comment == null)
		{
			LoadadMapIsFreizeitkarte = false;
		}
		else
			LoadadMapIsFreizeitkarte = info.comment.contains("FZK project");

		if (!LoadadMapIsFreizeitkarte) Gdx.app.debug(Tag.TAG, "Map is no FZK map");

		mapsForgeFile = layer.Name;
	}

	private final String NULL = "NULL";
	private final String TRUE = "TRUE";
	private final String FALSE = "FALSE";

	private void writeDebugMapInfo(MapFileInfo info)
	{

		// only if DebugLevel
		if (CB_UI_Base_Settings.AktLogLevel.getEnumValue() != LogLevel.debug) return;

		String comment = info.comment != null ? info.comment : NULL;
		String createdBy = info.createdBy != null ? info.createdBy : NULL;
		String debugFile = info.debugFile ? TRUE : FALSE;
		String fileSize = String.valueOf(info.fileSize / 1024) + " kB";
		String fileVersion = String.valueOf(info.fileSize);
		String boundingBox = info.boundingBox.toString();
		String startPosition = info.startPosition != null ? info.startPosition.toString() : NULL;

		StringBuilder sb = new StringBuilder();
		sb.append("Map file info:");
		sb.append(Global.br);

		sb.append("comment: ");
		sb.append(comment);
		sb.append(Global.br);

		sb.append("createdBy: ");
		sb.append(createdBy);
		sb.append(Global.br);

		sb.append("debugFile: ");
		sb.append(debugFile);
		sb.append(Global.br);

		sb.append("fileSize: ");
		sb.append(fileSize);
		sb.append(Global.br);

		sb.append("fileVersion: ");
		sb.append(fileVersion);
		sb.append(Global.br);

		sb.append("boundingBox: ");
		sb.append(boundingBox);
		sb.append(Global.br);

		sb.append("startPosition: ");
		sb.append(startPosition);
		sb.append(Global.br);

		Gdx.app.debug(Tag.TAG, sb.toString());
		sb = null;
	}

	private boolean LoadadMapIsFreizeitkarte = false;

	/**
	 * @return True, if the loaded map from Freizeitkarte
	 */
	public boolean isFreizeitKarteLoaded()
	{
		return LoadadMapIsFreizeitkarte;
	}

	public abstract GraphicFactory getGraphicFactory(float ScaleFactor);

}
