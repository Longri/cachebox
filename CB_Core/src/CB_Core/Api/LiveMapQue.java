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

package CB_Core.Api;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_Core.CoreSettingsForward;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CacheListChangedEventList;
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheListLive;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Map.Descriptor;
import CB_Utils.Lists.CB_List;
import CB_Utils.Lists.CB_Stack;
import CB_Utils.Lists.CB_Stack.iCompare;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.LoopThread;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.files.FileHandle;

/**
 * @author Longri
 */
public class LiveMapQue
{
	public static final SearchForGeocaches_Core SEARCH_API = new SearchForGeocaches_Core();
	public static final String LIVE_CACHE_NAME = "Live_Request";
	public static final String LIVE_CACHE_EXTENTION = ".txt";

	public static final byte DEFAULT_ZOOM_14 = 14;
	public static final int MAX_REQUEST_CACHE_RADIUS_14 = 1060;

	public static final byte DEFAULT_ZOOM_13 = 13;
	public static final int MAX_REQUEST_CACHE_RADIUS_13 = 2120;

	public static final int MAX_REQUEST_CACHE_COUNT = 200;
	private static final ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
	private static final ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
	public static CacheListLive LiveCaches;
	public static Live_Radius radius = CB_Core.Settings.CB_Core_Settings.LiveRadius.getEnumValue();

	private static GpxFilename gpxFilename;

	static
	{
		CB_Core.Settings.CB_Core_Settings.LiveRadius.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				radius = CB_Core.Settings.CB_Core_Settings.LiveRadius.getEnumValue();

				switch (radius)
				{
				case Zoom_13:
					Used_Zoom = DEFAULT_ZOOM_13;
					Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_13;
					break;
				case Zoom_14:
					Used_Zoom = DEFAULT_ZOOM_14;
					Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
					break;
				default:
					Used_Zoom = DEFAULT_ZOOM_14;
					Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
					break;

				}
			}

		});

		radius = CB_Core.Settings.CB_Core_Settings.LiveRadius.getEnumValue();

		switch (radius)
		{
		case Zoom_13:
			Used_Zoom = DEFAULT_ZOOM_13;
			Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_13;
			break;
		case Zoom_14:
			Used_Zoom = DEFAULT_ZOOM_14;
			Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
			break;
		default:
			Used_Zoom = DEFAULT_ZOOM_14;
			Used_max_request_radius = MAX_REQUEST_CACHE_RADIUS_14;
			break;

		}

		int maxLiveCount = CB_Core.Settings.CB_Core_Settings.LiveMaxCount.getValue();
		LiveCaches = new CacheListLive(maxLiveCount);
		CB_Core.Settings.CB_Core_Settings.LiveMaxCount.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				int maxLiveCount = CB_Core.Settings.CB_Core_Settings.LiveMaxCount.getValue();
				LiveCaches = new CacheListLive(maxLiveCount);
			}
		});

	}

	public static byte Used_Zoom;
	public static int Used_max_request_radius;

	public static AtomicBoolean DownloadIsActive = new AtomicBoolean(false);
	public static CB_List<QueStateChanged> eventList = new CB_List<LiveMapQue.QueStateChanged>();
	public static CB_Stack<Descriptor> descStack = new CB_Stack<Descriptor>();

	public enum Live_Radius
	{
		Zoom_13, Zoom_14
	}

	public interface QueStateChanged
	{
		public void stateChanged();
	}

	private static LoopThread loop = new LoopThread(2000)
	{

		@Override
		protected boolean LoopBraek()
		{
			return descStack.empty();
		}

		@Override
		protected void Loop()
		{
			Descriptor desc;
			synchronized (descStack)
			{
				do
				{
					desc = descStack.get();
				}
				while (LiveCaches.contains(desc));
			}

			if (desc == null) return;

			for (int i = 0; i < eventList.size(); i++)
				eventList.get(i).stateChanged();
			DownloadIsActive.set(true);

			SearchLiveMap requestSearch = new SearchLiveMap(MAX_REQUEST_CACHE_COUNT, desc, Used_max_request_radius);
			requestSearch.excludeFounds = CB_Core_Settings.LiveExcludeFounds.getValue();
			requestSearch.excludeHides = CB_Core_Settings.LiveExcludeOwn.getValue();

			CB_List<Cache> apiCaches = null;
			String result = "";

			if (descExistLiveCache(desc))
			{
				apiCaches = loadDescLiveFromCache(requestSearch);
			}
			if (apiCaches == null)
			{

				if (gpxFilename == null)
				{
					CategoryDAO categoryDAO = new CategoryDAO();
					Category category = categoryDAO.GetCategory(CoreSettingsForward.Categories, "API-Import");
					gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
				}

				apiCaches = new CB_List<Cache>();
				CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
				result = t.SearchForGeocachesJSON(requestSearch, apiCaches, apiLogs, apiImages, gpxFilename.Id, null);
			}

			if (result.equals("download limit"))
			{
				GroundspeakAPI.setDownloadLimit();
			}

			final CB_List<Cache> removedCaches = LiveCaches.add(desc, apiCaches);

			// log.debug("LIVE_QUE: add " + apiCaches.size() + "from Desc:" + desc.toString() + "/ StackSize:" + descStack.getSize());

			Thread callThread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{

					synchronized (Database.Data.Query)
					{
						Database.Data.Query.removeAll(removedCaches);
					}

					CacheListChangedEventList.Call();
					for (int i = 0; i < eventList.size(); i++)
						eventList.get(i).stateChanged();
				}
			});
			callThread.start();
			DownloadIsActive.set(false);

		}
	};

	public void AddStateChangedListner(QueStateChanged listner)
	{
		eventList.add(listner);
	}

	public static CB_List<Cache> loadDescLiveFromCache(SearchLiveMap requestSearch)
	{
		String path = requestSearch.descriptor.getLocalCachePath(LIVE_CACHE_NAME) + LIVE_CACHE_EXTENTION;
		String result = null;

		FileHandle fh = new FileHandle(path);

		try
		{

			BufferedReader br = fh.reader(1000);
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null)
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			result = sb.toString();
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// parseResult
		CB_List<Cache> cacheList = new CB_List<Cache>();
		if (result != null && result.length() > 0)
		{
			if (gpxFilename == null)
			{
				CategoryDAO categoryDAO = new CategoryDAO();
				Category category = categoryDAO.GetCategory(CoreSettingsForward.Categories, "API-Import");
				gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
			}

			SEARCH_API.ParseJsonResult(requestSearch, cacheList, apiLogs, apiImages, gpxFilename.Id, result, (byte) 1, true);
			return cacheList;
		}

		return null;
	}

	protected static boolean descExistLiveCache(Descriptor desc)
	{
		String path = desc.getLocalCachePath(LIVE_CACHE_NAME) + LIVE_CACHE_EXTENTION;
		return FileIO.FileExists(path, CB_Core_Settings.LiveCacheTime.getEnumValue().getMinuten());
	}

	static public void quePosition(Coordinate coord)
	{
		// no request for invalid Coords
		if (coord == null || !coord.isValid()) return;

		// no request if disabled
		if (CB_Core.Settings.CB_Core_Settings.DisableLiveMap.getValue()) return;

		final Descriptor desc = new Descriptor(coord, Used_Zoom);
		queDesc(desc);
		return;
	}

	private static Descriptor lastLo, lastRu;
	private static Byte count = 0;

	public static void queScreen(Descriptor lo, Descriptor ru)
	{
		if (GroundspeakAPI.ApiLimit()) return;

		// check last request don't Double!
		if (lastLo != null && lastRu != null)
		{
			// all Descriptor are into the last request?
			if (lastLo.getX() == lo.getX() && lastRu.getX() == ru.getX() && lastLo.getY() == lo.getY() && lastRu.getY() == ru.getY())
			{
				// Still run every 15th!
				if (count++ < 15) return;
				count = 0;
			}

		}

		lastLo = lo;
		lastRu = ru;

		CB_List<Descriptor> descList = new CB_List<Descriptor>();
		for (int i = lo.getX(); i <= ru.getX(); i++)
		{
			for (int j = lo.getY(); j <= ru.getY(); j++)
			{
				Descriptor desc = new Descriptor(i, j, lo.getZoom(), lo.NightMode);

				CB_List<Descriptor> descAddList = desc.AdjustZoom(Used_Zoom);

				for (int k = 0; k < descAddList.size(); k++)
				{
					if (!descList.contains(descAddList.get(k))) descList.add(descAddList.get(k));
				}
			}
		}

		// remove all descriptor are ready loaded at LiveCaches
		descList.removeAll(LiveCaches.getDescriptorList());

		if (!loop.Alive()) loop.start();

		synchronized (descStack)
		{
			descStack.addAll_removeOther(descList);

			// Descriptor MapCenter=MapViewBase.center

			Coordinate center = null;
			if ((lo.Data != null) && (lo.Data instanceof Coordinate)) center = (Coordinate) lo.Data;
			if (center != null)
			{
				final Descriptor mapCenterDesc = new Descriptor(center, lo.getZoom());
				descStack.sort(new iCompare<Descriptor>()
				{
					@Override
					public int compare(Descriptor item1, Descriptor item2)
					{
						int distanceFromCenter1 = item1.getDistance(mapCenterDesc);
						int distanceFromCenter2 = item2.getDistance(mapCenterDesc);
						if (distanceFromCenter1 == distanceFromCenter2) return 0;
						if (distanceFromCenter1 > distanceFromCenter2) return 1;
						return -1;
					}
				});
			}
		}
	}

	public static void setCenterDescriptor(CoordinateGPS center)
	{
		LiveCaches.setCenterDescriptor(new Descriptor(center, Used_Zoom));
	}

	private static void queDesc(Descriptor desc)
	{
		if (GroundspeakAPI.ApiLimit()) return;
		if (!loop.Alive()) loop.start();
		if (LiveCaches.contains(desc)) return; // all ready for this descriptor
		synchronized (descStack)
		{
			descStack.add(desc);
		}
	}
}
