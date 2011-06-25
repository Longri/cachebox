package de.droidcachebox.Events;

import java.util.ArrayList;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.Geocaching.Coordinate;

import android.location.Location;
import android.os.SystemClock;

public class PositionEventList {
	public static ArrayList<PositionEvent> list = new ArrayList<PositionEvent>();
	public static void Add(PositionEvent event)
	{
		list.add(event);	
	}
	
	public static void Remove(PositionEvent event)
	{
		list.remove(event);	
	}
	
	public static void Call(Location location)
	{
		Global.LastValidPosition = new Coordinate(location.getLatitude(), location.getLongitude());
		Global.Marker.Valid=false;
		for (PositionEvent event : list)
		{
			event.PositionChanged(location);
		}
	}

	private static int anzCompassValues = 0;
	private static float compassValue = 0;
	private static long lastCompassTick = -99999;
	
	public static void Call(float heading)
	{
		
		if (!Config.GetBool("HtcCompass"))
			return;		
	
		anzCompassValues++;
		compassValue += heading;

		long aktTick = SystemClock.uptimeMillis();
		if (aktTick < lastCompassTick + 300)
		{
			// do not update view now, only every 200 millisec
			return;
		}
		if (anzCompassValues == 0)
		{
			lastCompassTick = aktTick;
			return;
		}
		// Durchschnitts Richtung berechnen
		heading = compassValue / anzCompassValues;
		anzCompassValues = 0;
		compassValue = 0;
		lastCompassTick = aktTick;
		
		
		
		
		
		for (PositionEvent event : list)
		{
			event.OrientationChanged(heading);
		}
	}

}
