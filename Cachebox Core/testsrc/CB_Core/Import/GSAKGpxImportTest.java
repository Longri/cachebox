package CB_Core.Import;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import junit.framework.TestCase;

public class GSAKGpxImportTest extends TestCase {
	
	public static void testGpxImport() throws Exception
	{
		ImportHandler importHandler = new ImportHandler();
		GPXFileImporter importer = new GPXFileImporter( "./testdata/gpx/GSAK_1_1.gpx" );
		assertTrue( "Objekt muss konstruierbar sein", importer!=null );
		importer.doImport( importHandler );
		
		Iterator<Cache> cacheIterator = importHandler.getCacheIterator();
		Cache cache = cacheIterator.next();
		
		assertTrue( "Cache muss zur�ckgegeben werden", cache!=null );
		
		assertTrue( "Pos: Latitude falsch", cache.Pos.Latitude == 52.579333 );
		assertTrue( "Pos: Longitude falsch", cache.Pos.Longitude == 13.40545 );
		assertTrue( "Pos ist ung�ltig", cache.Pos.Valid );
		
		assertEquals( "GcCode falsch", "GC1XCEW", cache.GcCode );
		assertEquals( "DateHidden falsch", "Mon Aug 17 08:00:00 CEST 2009", cache.DateHidden.toString() );
		assertEquals( "url falsch", "http", cache.Url );// URL wird noch nicht ausgelesen
		assertTrue( "Found ist falsch", cache.Found );

		assertEquals( "Id ist falsch", cache.GcId, "1358542" );
		assertFalse( "ist available ist falsch", cache.Available );
		assertTrue( "ist archived ist falsch", cache.Archived );
		assertEquals( "Name falsch", "Schlossblick # 2/ View at the castle  #2", cache.Name );
		assertEquals( "Placed by falsch", "Risou", cache.PlacedBy );
		assertEquals( "Owner falsch", "Risou", cache.Owner );
		assertTrue( "Typ ist falsch", cache.Type == CacheTypes.Undefined );
		assertTrue( "Size ist falsch", cache.Size == CacheSizes.micro );
		assertTrue( "Difficulty ist falsch", cache.Difficulty == 2 );
		assertTrue( "Terrain ist falsch", cache.Terrain == 2 );
		
		
		
		// Attribute Tests
		
		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();
		
		PositvieList.add(Attributes.Bicycles);
		PositvieList.add(Attributes.Dogs);
		PositvieList.add(Attributes.Ticks);
		PositvieList.add(Attributes.Thorns);
		PositvieList.add(Attributes.TakesLess);
		
		NegativeList.add(Attributes.Anytime);
		NegativeList.add(Attributes.Night);
		
		
		Iterator positiveInterator = PositvieList.iterator();
		Iterator negativeInterator = NegativeList.iterator();
		
		while(positiveInterator.hasNext())
		{
			assertTrue( "Attribut falsch", cache.isAttributePositiveSet( (Attributes) positiveInterator.next() ) );
		}
		
		while(negativeInterator.hasNext())
		{
			assertTrue( "Attribut falsch", cache.isAttributeNegativeSet( (Attributes) negativeInterator.next() ) );
		}
		
		
		// f�lle eine Liste mit allen Attributen
		ArrayList<Attributes> attributes= new ArrayList<Attributes>();
		Attributes[] tmp = Attributes.values();
		for ( Attributes item : tmp)
		{
			attributes.add(item);
		}
		
		//L�sche die vergebenen Atribute aus der Kommplett Liste
		positiveInterator = PositvieList.iterator();
		negativeInterator = NegativeList.iterator();
		
		while(positiveInterator.hasNext())
		{
			attributes.remove(positiveInterator.next());
		}
		
		while(negativeInterator.hasNext())
		{
			attributes.remove(negativeInterator.next());
		}
		
		//Teste ob die �brig gebliebenen Atributte auch nicht vergeben wurden.
		Iterator RestInterator = attributes.iterator();
		
		while(RestInterator.hasNext())
		{
			Attributes attr = (Attributes) RestInterator.next();
			assertFalse( "Attribut falsch", cache.isAttributePositiveSet( attr ) );
			assertFalse( "Attribut falsch", cache.isAttributeNegativeSet( attr ) );
		}
//		
		
		
		// TODO Beschreibungstexte �berpr�fen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );
		
		assertEquals( "Hint falsch", "", cache.hint );
		
		Iterator<LogEntry> logIterator = importHandler.getLogIterator();
		LogEntry log = logIterator.next();
		
		assertEquals( "CacheId ist falsch", log.CacheId, 24564478518575943L );
		assertEquals( "Id ist falsch", log.Id, 140640156 );
		assertEquals( "Timestamp falsch", "Sat Jan 08 20:00:00 CET 2011", log.Timestamp.toString() );
		assertEquals( "Finder falsch", "Katipa", log.Finder );
		assertTrue( "LogTyp falsch", log.Type == LogTypes.found );

		assertEquals( "Log Entry falsch", "Jaja. Lange gesucht an den typischen Stellen, um dann letztendlich ganz woanders f�ndig zu werden...", log.Comment );

		
	}

}
