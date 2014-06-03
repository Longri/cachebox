package API;

import junit.framework.TestCase;
import CB_Locator.Coordinate;
import CB_Locator.Map.Descriptor;
import CB_UI.Config;
import __Static.InitTestDBs;

public class searchLiveMapTests extends TestCase
{
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		LoadConfig();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();

	}

	/**
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine g�ltige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht werden kann. (zum Schutz des Pers�nlichen API
	 * Keys)
	 */
	private void LoadConfig()
	{
		InitTestDBs.InitalConfig();
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testSearchLive()
	{

		Descriptor.Init();

		// Descriptor Zoom Level 14 = search radius 2km
		// Center of Descriptor x=8801 y=5368 z=14 => 52� 34,973N / 13� 23,531E (Pankow)
		Descriptor descPankow = new Descriptor(8801, 5368, 14, false);

		// List of Coordinates are into x=8801 y=5368 z=14
		Coordinate[] coordList = new Coordinate[]
			{ new Coordinate("52� 34,973N / 13� 23,531E"), new Coordinate("52� 35,364N / 13� 24,170E"),
					new Coordinate("52� 35,367N / 13� 22,908E"), new Coordinate("52� 34,601N / 13� 22,923E"),
					new Coordinate("52� 34,598N / 13� 24,170E"), new Coordinate("52� 34,773N / 13� 23,346E"),
					new Coordinate("52� 34,933N / 13� 23,938E") };

		// check
		for (Coordinate cor : coordList)
		{
			Descriptor desc = new Descriptor(cor, 14);
			assertEquals("mustEquals", desc, descPankow);
		}

		// Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128);
		//
		// ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		// SearchLiveMap searchC = new SearchLiveMap(2, searchCoord, 5000);
		//
		// ApiGroundspeak_SearchForGeocaches apis = new ApiGroundspeak_SearchForGeocaches(searchC, apiCaches);
		// apis.execute();
		//
		// assertFalse("Keine Caches gefunden", apiCaches.size() < 1);

	}
}
