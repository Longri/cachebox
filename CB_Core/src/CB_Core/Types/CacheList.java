package CB_Core.Types;

import java.util.Collections;

import CB_Core.Enums.CacheTypes;
import CB_Core.Util.MoveableList;
import CB_Locator.Coordinate;
import CB_Locator.Locator;

public class CacheList extends MoveableList<Cache>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean ResortAtWork = false;

	public Cache GetCacheByGcCode(String GcCode)
	{
		for (Cache cache : this)
		{
			if (cache.GcCode.equalsIgnoreCase(GcCode)) return cache;
		}
		return null;
	}

	public Cache GetCacheById(long cacheId)
	{
		for (Cache cache : this)
		{
			if (cache.Id == cacheId) return cache;
		}
		return null;
	}

	/**
	 * @param selectedCoord
	 *            GlobalCore.getSelectedCoord()
	 * @param selected
	 *            new CacheWithWp(GlobalCore.getSelectedCache(),GlobalCore.getSelectedWP())
	 * @param userName
	 *            Config.settings.GcLogin.getValue()
	 * @param ParkingLatitude
	 *            Config.settings.ParkingLatitude.getValue()
	 * @param ParkingLongitude
	 *            Config.settings.ParkingLongitude.getValue()
	 * @param DisplayOff
	 *            Energy.DisplayOff()
	 * @return CacheWithWP [null posible] set To<br>
	 *         GlobalCore.setSelectedWaypoint(nextCache, waypoint, false);<br>
	 *         GlobalCore.NearestCache(nextCache);
	 */
	public CacheWithWP Resort(Coordinate selectedCoord, CacheWithWP selected)
	{

		CacheWithWP retValue = null;

		this.ResortAtWork = true;
		boolean LocatorValid = Locator.Valid();
		// Alle Distanzen aktualisieren
		if (LocatorValid)
		{
			for (Cache cache : this)
			{
				cache.Distance(true);
			}
		}
		else
		{
			// sort after Distance from selected Cache
			Coordinate fromPos = selectedCoord;
			// avoid "illegal waypoint"
			if (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)
			{
				fromPos = selected.getCache().Pos;
			}
			if (fromPos == null)
			{
				this.ResortAtWork = false;
				return retValue;
			}
			for (Cache cache : this)
			{
				cache.Distance(true, fromPos);
			}
		}

		Collections.sort(this);

		// N�chsten Cache ausw�hlen
		if (this.size() > 0)
		{
			Cache nextCache = this.get(0); // or null ...
			for (int i = 0; i < this.size(); i++)
			{
				nextCache = this.get(i);
				if (!nextCache.Archived)
				{
					if (nextCache.Available)
					{
						if (!nextCache.Found) // eigentlich wenn has_fieldnote(found,DNF,Maint,SBA, aber note vielleicht nicht) , aber found
												// kann nicht r�ckg�ngig gemacht werden.
						{
							if (!nextCache.ImTheOwner())
							{
								if (nextCache.Type != CacheTypes.Mystery)
								{
									break;
								}
								else
								{
									if (nextCache.CorrectedCoordiantesOrMysterySolved())
									{
										break;
									}
								}
							}
						}
					}
				}
			}
			// Wenn der nachste Cache ein Mystery mit Final Waypoint ist
			// -> gleich den Final Waypoint auswahlen!!!
			// When the next Cache is a mystery with final waypoint
			// -> activate the final waypoint!!!
			Waypoint waypoint = nextCache.GetFinalWaypoint();
			if (waypoint == null)
			{
				// wenn ein Cache keinen Final Waypoint hat dann wird �berpr�ft, ob dieser einen Startpunkt definiert hat
				// Wenn ein Cache einen Startpunkt definiert hat dann wird beim Selektieren dieses Caches gleich dieser Startpunkt
				// selektiert
				waypoint = nextCache.GetStartWaypoint();
			}

			retValue = new CacheWithWP(nextCache, waypoint);
		}

		CB_Core.Events.CachListChangedEventList.Call();

		// vorhandenen Parkplatz Cache nach oben schieben
		Cache park = this.GetCacheByGcCode("CBPark");
		if (park != null)
		{
			this.MoveItemFirst(this.indexOf(park));
		}

		// Cursor.Current = Cursors.Default;
		this.ResortAtWork = false;
		return retValue;
	}

}