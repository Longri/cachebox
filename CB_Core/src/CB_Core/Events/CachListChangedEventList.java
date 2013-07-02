package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.CoreSettingsForward;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;

public class CachListChangedEventList
{
	public static ArrayList<CacheListChangedEventListner> list = new ArrayList<CacheListChangedEventListner>();

	public static void Add(CacheListChangedEventListner event)
	{
		synchronized (list)
		{
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(CacheListChangedEventListner event)
	{
		synchronized (list)
		{
			list.remove(event);
		}
	}

	private static Thread threadCall;

	/**
	 * @param ParkingLatitude
	 *            Config.settings.ParkingLatitude.getValue()
	 * @param ParkingLongitude
	 *            Config.settings.ParkingLongitude.getValue()
	 * @param DisplayOff
	 *            Energy.DisplayOff()
	 */
	public static void Call()
	{
		if (CoreSettingsForward.DisplayOff) return;

		synchronized (Database.Data.Query)
		{
			Cache cache = Database.Data.Query.GetCacheByGcCode("CBPark");

			if (cache != null) Database.Data.Query.remove(cache);

			// add Parking Cache
			if (CoreSettingsForward.ParkingLatitude != 0)
			{
				cache = new Cache(CoreSettingsForward.ParkingLatitude, CoreSettingsForward.ParkingLongitude, "My Parking area",
						CacheTypes.MyParking, "CBPark");
				Database.Data.Query.add(0, cache);
			}

		}

		if (threadCall != null)
		{
			if (threadCall.getState() != Thread.State.TERMINATED) return;
			else
				threadCall = null;
		}

		if (threadCall == null) threadCall = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					for (CacheListChangedEventListner event : list)
					{
						if (event == null) continue;
						event.CacheListChangedEvent();
					}
				}

			}
		});

		threadCall.start();

	}

}