package CB_Core.Api;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import CB_Core.DAO.CacheDAO;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Cache;
import CB_Core.Types.DLong;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Utils.Log.Logger;

public class ApiGroundspeak_SearchForGeocaches extends ApiGroundspeak
{
	private Search search;
	private long gpxFilenameId;
	private ArrayList<Cache> cacheList;
	private ArrayList<LogEntry> logList;
	private ArrayList<ImageEntry> imageList;

	public ApiGroundspeak_SearchForGeocaches(Search search, ArrayList<Cache> cacheList, ArrayList<LogEntry> logList,
			ArrayList<ImageEntry> imageList, long gpxFilenameId)
	{
		super();
		this.search = search;
		this.gpxFilenameId = gpxFilenameId;
		this.cacheList = cacheList;
		this.logList = logList;
		this.imageList = imageList;
	}

	@Override
	protected queryType getQueryType()
	{
		return queryType.POST;
	}

	@Override
	protected String getApiFunction()
	{
		return "SearchForGeocaches";
	}

	@Override
	protected boolean getRequest(JSONObject request)
	{
		// Generate the request Object
		try
		{
			search.getRequest(request, isLite);
		}
		catch (JSONException e1)
		{
			Logger.Error("ApiGroundspeak - SearchForGeocaches:JSONException", e1.getMessage());
			return false;
		}
		// isLite vom SearchObjekt auswerten, da dies darin ge�ndert worden sein k�nnte
		if (search.getIsLite())
		{
			isLite = search.getIsLite();
			apiStatus = 2; // voll laden
		}
		else
		{
			isLite = search.getIsLite();
			apiStatus = 1; // nicht voll laden
		}

		return true;
	}

	@Override
	protected ApiGroundspeakResult parseJson(JSONObject json) throws JSONException
	{
		// Parse Result object
		ApiGroundspeakResult result = new ApiGroundspeakResult(-1, "");

		JSONArray caches = json.getJSONArray("Geocaches");
		Logger.LogCat("got " + caches.length() + " Caches from gc");
		for (int i = 0; i < caches.length(); i++)
		{
			JSONObject jCache = (JSONObject) caches.get(i);
			String gcCode = jCache.getString("Code");
			Logger.DEBUG("handling " + gcCode);
			String name = jCache.getString("Name");

			Boolean CacheERROR = false;

			Cache cache = new Cache();
			cache.Archived = jCache.getBoolean("Archived");
			cache.setAttributesPositive(new DLong(0, 0));
			cache.setAttributesNegative(new DLong(0, 0));
			JSONArray jAttributes = jCache.getJSONArray("Attributes");
			for (int j = 0; j < jAttributes.length(); j++)
			{
				JSONObject jAttribute = jAttributes.getJSONObject(j);
				int AttributeTypeId = jAttribute.getInt("AttributeTypeID");
				Boolean isOn = jAttribute.getBoolean("IsOn");
				Attributes att = Attributes.getAttributeEnumByGcComId(AttributeTypeId);
				if (isOn)
				{
					cache.addAttributePositive(att);
				}
				else
				{
					cache.addAttributeNegative(att);
				}
			}
			cache.Available = jCache.getBoolean("Available");
			cache.DateHidden = new Date();
			try
			{
				String dateCreated = jCache.getString("DateCreated");
				int date1 = dateCreated.indexOf("/Date(");
				int date2 = dateCreated.indexOf("-");
				String date = (String) dateCreated.subSequence(date1 + 6, date2);
				cache.DateHidden = new Date(Long.valueOf(date));
			}
			catch (Exception exc)
			{
				Logger.Error("API", "SearchForGeocaches_ParseDate", exc);
			}
			cache.Difficulty = (float) jCache.getDouble("Difficulty");

			CacheDAO dao = new CacheDAO();
			// Ein evtl. in der Datenbank vorhandenen "Favorit" nicht �berschreiben
			Boolean Favorite = dao.loadBooleanValue(gcCode, "Favorit");
			cache.setFavorit(Favorite);

			// Ein evtl. in der Datenbank vorhandenen "Found" nicht �berschreiben
			Boolean Found = dao.loadBooleanValue(gcCode, "found");
			if (!Found)
			{
				cache.Found = jCache.getBoolean("HasbeenFoundbyUser");
			}
			else
			{
				cache.Found = true;
			}

			cache.GcCode = jCache.getString("Code");
			try
			{
				cache.GcId = jCache.getString("ID");
			}
			catch (Exception e)
			{
				// CacheERROR = true; gibt bei jedem Cache ein
				// Fehler ???
			}
			cache.GPXFilename_ID = gpxFilenameId;

			// Ein evtl. in der Datenbank vorhandenen "Found" nicht �berschreiben
			Boolean userData = dao.loadBooleanValue(gcCode, "HasUserData");

			cache.hasUserData = userData;
			try
			{
				cache.hint = jCache.getString("EncodedHints");
			}
			catch (Exception e1)
			{
				cache.hint = "";
			}
			cache.Id = Cache.GenerateCacheId(cache.GcCode);
			cache.listingChanged = false;

			try
			{
				cache.longDescription = jCache.getString("LongDescription");
			}
			catch (Exception e1)
			{
				Logger.Error("API", "SearchForGeocaches_LongDescription:" + cache.GcCode, e1);
				cache.longDescription = "";
			}
			if (jCache.getBoolean("LongDescriptionIsHtml") == false)
			{
				cache.longDescription = cache.longDescription.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
			}
			cache.Name = jCache.getString("Name");
			cache.noteCheckSum = 0;
			cache.NumTravelbugs = jCache.getInt("TrackableCount");
			JSONObject jOwner = (JSONObject) jCache.getJSONObject("Owner");
			cache.Owner = jOwner.getString("UserName");
			cache.PlacedBy = cache.Owner;
			try
			{
				cache.Pos = new Coordinate(jCache.getDouble("Latitude"), jCache.getDouble("Longitude"));
			}
			catch (Exception e)
			{

			}
			cache.Rating = 0;
			// cache.Rating =
			try
			{
				cache.shortDescription = jCache.getString("ShortDescription");
			}
			catch (Exception e)
			{
				Logger.Error("API", "SearchForGeocaches_shortDescription:" + cache.GcCode, e);
				cache.shortDescription = "";
			}
			if (jCache.getBoolean("ShortDescriptionIsHtml") == false)
			{
				cache.shortDescription = cache.shortDescription.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
			}
			JSONObject jContainer = jCache.getJSONObject("ContainerType");
			int jSize = jContainer.getInt("ContainerTypeId");
			cache.Size = CacheSizes.parseInt(GroundspeakAPI.getCacheSize(jSize));
			cache.solverCheckSum = 0;
			cache.Terrain = (float) jCache.getDouble("Terrain");
			cache.Type = CacheTypes.Traditional;
			JSONObject jCacheType = jCache.getJSONObject("CacheType");
			cache.Type = GroundspeakAPI.getCacheType(jCacheType.getInt("GeocacheTypeId"));
			cache.Url = jCache.getString("Url");
			cache.ApiStatus = apiStatus;

			// Chk if Own or Found
			Boolean exclude = false;
			if (search.excludeFounds && cache.Found) exclude = true;
			if (search.excludeHides && cache.Owner.equalsIgnoreCase(CB_Core_Settings.GcLogin.getValue())) exclude = true;
			if (search.available && (cache.Archived || !cache.Available)) exclude = true;

			if (!CacheERROR && !exclude)
			{
				cacheList.add(cache);
				// insert Logs
				JSONArray logs = jCache.getJSONArray("GeocacheLogs");
				for (int j = 0; j < logs.length(); j++)
				{
					JSONObject jLogs = (JSONObject) logs.get(j);
					JSONObject jFinder = (JSONObject) jLogs.get("Finder");
					JSONObject jLogType = (JSONObject) jLogs.get("LogType");
					LogEntry log = new LogEntry();
					log.CacheId = cache.Id;
					log.Comment = jLogs.getString("LogText");
					log.Finder = jFinder.getString("UserName");
					log.Id = jLogs.getInt("ID");
					log.Timestamp = new Date();
					try
					{
						String dateCreated = jLogs.getString("VisitDate");
						int date1 = dateCreated.indexOf("/Date(");
						int date2 = dateCreated.indexOf("-");
						String date = (String) dateCreated.subSequence(date1 + 6, date2);
						log.Timestamp = new Date(Long.valueOf(date));
					}
					catch (Exception exc)
					{
						Logger.Error("API", "SearchForGeocaches_ParseLogDate", exc);
					}
					log.Type = LogTypes.GC2CB_LogType(jLogType.getInt("WptLogTypeId"));
					logList.add(log);
				}

				// insert Images
				int imageListSizeOrg = imageList.size();
				JSONArray images = jCache.getJSONArray("Images");
				for (int j = 0; j < images.length(); j++)
				{
					JSONObject jImage = (JSONObject) images.get(j);

					ImageEntry image = new ImageEntry();
					image.CacheId = cache.Id;
					image.GcCode = cache.GcCode;
					image.Name = jImage.getString("Name");
					image.Description = jImage.getString("Description");
					image.ImageUrl = jImage.getString("Url").replace("img.geocaching.com/gc/cache", "img.geocaching.com/cache");
					// remove "/gc" to match the url used in the description

					image.IsCacheImage = true;

					imageList.add(image);
				}
				int imageListSizeGC = images.length();

				// insert images from Cache description
				LinkedList<String> allImages = DescriptionImageGrabber.GetAllImages(cache);
				int imageListSizeGrabbed = 0;

				if (allImages != null && allImages.size() > 0)
				{
					imageListSizeGrabbed = allImages.size();
				}

				while (allImages != null && allImages.size() > 0)
				{
					String url;
					url = allImages.poll();

					boolean found = false;
					for (ImageEntry im : imageList)
					{
						if (im.ImageUrl.equalsIgnoreCase(url))
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						ImageEntry image = new ImageEntry();

						image.CacheId = cache.Id;
						image.GcCode = cache.GcCode;
						image.Name = url.substring(url.lastIndexOf("/") + 1);
						image.Description = "";
						image.ImageUrl = url;
						image.IsCacheImage = true;

						imageList.add(image);
					}

				}
				Logger.DEBUG("Merged imageList has " + imageList.size() + " Entrys (" + imageListSizeOrg + "/" + imageListSizeGC + "/"
						+ imageListSizeGrabbed + ")");

				// insert Waypoints
				JSONArray waypoints = jCache.getJSONArray("AdditionalWaypoints");
				for (int j = 0; j < waypoints.length(); j++)
				{
					JSONObject jWaypoints = (JSONObject) waypoints.get(j);
					Waypoint waypoint = new Waypoint();
					waypoint.CacheId = cache.Id;

					try
					{
						waypoint.Pos = new Coordinate(jWaypoints.getDouble("Latitude"), jWaypoints.getDouble("Longitude"));
					}
					catch (Exception ex)
					{
						// no Coordinates -> Lat/Lon = 0/0
						waypoint.Pos = new Coordinate();
					}

					waypoint.Title = jWaypoints.getString("Description");
					waypoint.Description = jWaypoints.getString("Comment");
					waypoint.Type = GroundspeakAPI.getCacheType(jWaypoints.getInt("WptTypeID"));
					waypoint.GcCode = jWaypoints.getString("Code");
					cache.waypoints.add(waypoint);
				}
				// User Waypoints - Corrected Coordinates of the Geocaching.com Website
				JSONArray userWaypoints = jCache.getJSONArray("UserWaypoints");
				for (int j = 0; j < userWaypoints.length(); j++)
				{
					JSONObject jUserWaypoint = (JSONObject) userWaypoints.get(j);
					if (!jUserWaypoint.getString("Description").equals("Coordinate Override"))
					{
						continue; // only corrected Coordinate
					}
					Waypoint waypoint = new Waypoint();
					waypoint.CacheId = cache.Id;
					try
					{
						waypoint.Pos = new Coordinate(jUserWaypoint.getDouble("Latitude"), jUserWaypoint.getDouble("Longitude"));
					}
					catch (Exception ex)
					{
						// no Coordinates -> Lat/Lon = 0/0
						waypoint.Pos = new Coordinate();
					}
					waypoint.Title = jUserWaypoint.getString("Description");
					waypoint.Description = jUserWaypoint.getString("Description");
					waypoint.Type = CacheTypes.Final;
					waypoint.GcCode = "CO" + cache.GcCode.substring(2, cache.GcCode.length());
					cache.waypoints.add(waypoint);
				}
				// Spoiler aktualisieren
				actualizeSpoilerOfActualCache(cache);
			}

			// Notes
			Object note = jCache.get("GeocacheNote");
			if ((note != null) && (note instanceof String))
			{
				String s = (String) note;
				System.out.println(s);
				cache.tmpNote = s;
			}

		}
		GroundspeakAPI.checkCacheStatus(json, isLite);

		return result;
	}

	protected void writeExclusions(JSONObject request, SearchCoordinate searchC) throws JSONException
	{
		if (searchC.available)
		{
			JSONObject excl = new JSONObject();
			excl.put("Archived", false);
			excl.put("Available", true);
			request.put("GeocacheExclusions", excl);

		}
	}

	protected void actualizeSpoilerOfActualCache(Cache cache)
	{
		// hier im Core nichts machen da hier keine UI vorhanden ist
	}

	/**
	 * Search Definitions
	 */

	public static class Search
	{
		public int number;
		public boolean excludeHides = false;
		public boolean excludeFounds = false;
		public boolean available = true;
		public int geocacheLogCount = 10;
		public int trackableLogCount = 10;
		private boolean isLite;

		public Search(int number)
		{
			this.number = number;
		}

		protected void getRequest(JSONObject request, boolean isLite) throws JSONException
		{
			this.isLite = isLite;
			request.put("IsLite", isLite);
			request.put("StartIndex", 0);
			request.put("MaxPerPage", number);
			request.put("GeocacheLogCount", geocacheLogCount);
			request.put("TrackableLogCount", trackableLogCount);
			if (available)
			{
				JSONObject excl = new JSONObject();
				excl.put("Archived", false);
				excl.put("Available", true);
				request.put("GeocacheExclusions", excl);

			}
			if (excludeHides)
			{
				JSONObject excl = new JSONObject();
				JSONArray jarr = new JSONArray();
				jarr.put(CB_Core_Settings.GcLogin.getValue());
				excl.put("UserNames", jarr);
				request.put("NotHiddenByUsers", excl);
			}

			if (excludeFounds)
			{
				JSONObject excl = new JSONObject();
				JSONArray jarr = new JSONArray();
				jarr.put(CB_Core_Settings.GcLogin.getValue());
				excl.put("UserNames", jarr);
				request.put("NotFoundByUsers", excl);
			}
		}

		// isLite kann hier nochmal abgefragt werden da dieser Wert von einzelnen Search-Objecten ge�ndert werden k�nnte
		protected boolean getIsLite()
		{
			return isLite;
		}
	}

	public static class SearchCoordinate extends Search
	{
		public Coordinate pos;
		public float distanceInMeters;

		public SearchCoordinate(int number, Coordinate pos, float distanceInMeters)
		{
			super(number);
			this.pos = pos;
			this.distanceInMeters = distanceInMeters;
		}

		@Override
		protected void getRequest(JSONObject request, boolean isLite) throws JSONException
		{
			super.getRequest(request, isLite);
			JSONObject jpr = new JSONObject();
			jpr.put("DistanceInMeters", String.valueOf((int) distanceInMeters));
			JSONObject jpt = new JSONObject();
			jpt.put("Latitude", String.valueOf(pos.getLatitude()));
			jpt.put("Longitude", String.valueOf(pos.getLongitude()));
			jpr.put("Point", jpt);
			request.put("PointRadius", jpr);
		}
	}

	public static class SearchGC extends Search
	{
		private ArrayList<String> gcCodes;

		public SearchGC(String gcCode)
		{
			super(1);
			// einzelner Cache wird immer voll geladen
			this.gcCodes = new ArrayList<String>();
			this.gcCodes.add(gcCode);
		}

		public SearchGC(ArrayList<String> gcCodes)
		{
			super(gcCodes.size());
			this.gcCodes = gcCodes;
		}

		@Override
		protected void getRequest(JSONObject request, boolean isLite) throws JSONException
		{
			super.getRequest(request, isLite);
			JSONObject requestcc = new JSONObject();
			JSONArray requesta = new JSONArray();
			for (String gcCode : gcCodes)
			{
				requesta.put(gcCode);
			}
			requestcc.put("CacheCodes", requesta);
			request.put("CacheCode", requestcc);
		}
	}

	public static class SearchGCName extends SearchCoordinate
	{
		public String gcName;

		public SearchGCName(int number, Coordinate pos, float distanceInMeters, String gcName)
		{
			super(number, pos, distanceInMeters);
			this.gcName = gcName;
		}

		@Override
		protected void getRequest(JSONObject request, boolean isLite) throws JSONException
		{
			super.getRequest(request, isLite);
			JSONObject jgc = new JSONObject();
			jgc.put("GeocacheName", gcName);
			request.put("GeocacheName", jgc);
		}
	}

	public static class SearchGCOwner extends SearchCoordinate
	{
		public String OwnerName;

		public SearchGCOwner(int number, Coordinate pos, float distanceInMeters, String ownerName)
		{
			super(number, pos, distanceInMeters);
			this.OwnerName = ownerName;
		}

		@Override
		protected void getRequest(JSONObject request, boolean isLite) throws JSONException
		{
			super.getRequest(request, isLite);
			JSONObject jhidden = new JSONObject();
			JSONArray jusers = new JSONArray();
			jusers.put(OwnerName);
			jhidden.put("UserNames", jusers);
			request.put("HiddenByUsers", jhidden);
		}
	}

}
