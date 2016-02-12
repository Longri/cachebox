package API;

import java.util.ArrayList;

import org.junit.Test;

import CB_Core.Database;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.LiveMapQue;
import CB_Core.Api.SearchForGeocaches_Core;
import CB_Core.Api.SearchLiveMap;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Locator.Coordinate;
import CB_Locator.Map.Descriptor;
import CB_UI.Config;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Lists.CB_List;
import __Static.InitTestDBs;
import junit.framework.TestCase;

public class searchLiveMapTests extends TestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		LoadConfig();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

	}

	/**
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine g�ltige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht werden kann. (zum Schutz des Pers�nlichen API
	 * Keys)
	 */
	private void LoadConfig() {
		InitTestDBs.InitalConfig();
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testSearchLive() {

		// Descriptor Zoom Level 14 = search radius 2km
		// Center of Descriptor x=8801 y=5368 z=14 => 52� 34,982N / 13� 23,540E (Pankow)
		Descriptor descPankow = new Descriptor(8801, 5368, 14, false);
		Coordinate corPankow = new Coordinate("52� 34,9815N / 13� 23,540E");

		// List of Coordinates are into x=8801 y=5368 z=14
		Coordinate[] coordList = new Coordinate[] { new Coordinate("52� 34,973N / 13� 23,531E"), new Coordinate("52� 35,364N / 13� 24,170E"), new Coordinate("52� 35,367N / 13� 22,908E"), new Coordinate("52� 34,601N / 13� 22,923E"),
				new Coordinate("52� 34,598N / 13� 24,170E"), new Coordinate("52� 34,773N / 13� 23,346E"), new Coordinate("52� 34,933N / 13� 23,938E") };

		// check
		for (Coordinate cor : coordList) {
			Descriptor desc = new Descriptor(cor, 14);
			assertEquals("mustEquals", desc, descPankow);

			// Check center coordinate of Descriptor
			Coordinate cord = desc.getCenterCoordinate();
			assertEquals("mustEquals", cord, corPankow);
		}
	}

	@Test
	public void testLiveQue() {
		// get API limits and Check the limits after request

		GroundspeakAPI.GetCacheLimits(null);
		int CachesLeft = GroundspeakAPI.CachesLeft;

		Coordinate coord = new Coordinate("52� 34,9815N / 13� 23,540E");
		Descriptor desc = new Descriptor(coord, 14);

		// TODO
		// assertTrue("muss ausgef�hrt werden", LiveMapQue.quePosition(new Coordinate("52� 34,9815N / 13� 23,540E")));
		// assertFalse("darf nicht ausgef�hrt werden", LiveMapQue.quePosition(new Coordinate("52� 34,9815N / 13� 23,540E")));

		// Chk all Caches are in to the Descriptor of new Coordinate("52� 34,973N / 13� 23,531E")

		for (int i = 0; i < LiveMapQue.LiveCaches.getSize(); i++) {
			Cache ca = LiveMapQue.LiveCaches.get(i);

			Descriptor targetDesc = new Descriptor(ca.Pos, 14);

			if (!targetDesc.equals(desc)) {
				// Check max Distance from Center
				float distance = coord.Distance(ca.Pos, CalculationType.ACCURATE);
				assertTrue("Distance from center must be closer then request distance", distance <= LiveMapQue.Used_max_request_radius);
			}

			CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
			cleanLogs = Database.Logs(ca);// cache.Logs();

		}

		// Check if count are not same like requested (increase Max Count)
		assertTrue("count mast be lower then requested", LiveMapQue.LiveCaches.getSize() < LiveMapQue.MAX_REQUEST_CACHE_COUNT);

		GroundspeakAPI.GetCacheLimits(null);

		assertTrue("CacheLimits must not changed", CachesLeft == GroundspeakAPI.CachesLeft);

	}

	public static final int MAX_REQUEST_CACHE_COUNT = 200;
	private static final ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
	private static final ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
	private static final float Used_max_request_radius = 2120;

	public void test_request() {
		Descriptor desc = new Descriptor(new Coordinate("52� 34,9815N / 13� 23,540E"), 14);
		SearchLiveMap requestSearch = new SearchLiveMap(MAX_REQUEST_CACHE_COUNT, desc, Used_max_request_radius);

		CB_List<Cache> apiCaches = new CB_List<Cache>();

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		String result = t.SearchForGeocachesJSON(requestSearch, apiCaches, apiLogs, apiImages, 0, null);

		assertTrue("ApiImage-Size must be 0", apiImages.size() == 0);
		assertTrue("ApiLog-Size must be 0", apiLogs.size() == 0);
		assertTrue("CacheList-Size must be 79", apiCaches.size() == 79);

		//check LiveMapQue_Cache
		CB_List<Cache> CacheList = LiveMapQue.loadDescLiveFromCache(requestSearch);
		assertTrue("CacheList-Size must be 50", CacheList.size() == 50);

	}
}
