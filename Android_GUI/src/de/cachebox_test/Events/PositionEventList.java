package de.cachebox_test.Events;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Types.Coordinate;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import de.cachebox_test.Energy;

public class PositionEventList
{
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
		if (!Config.settings.HtcCompass.getValue())
		{
			// GPS richtung senden
			Call(location.getBearing(), true);
		}

		GlobalCore.LastPosition = new Coordinate(location.getLatitude(), location.getLongitude());
		GlobalCore.LastPosition.Valid = true; // Valid ob GPS oder Phone,
												// hauptsache eine Coordinate
		if (location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER))
		{
			GlobalCore.LastValidPosition = new Coordinate(location.getLatitude(), location.getLongitude());
			GlobalCore.LastValidPosition.Valid = true;
		}
		else
		{
			GlobalCore.LastValidPosition.Valid = false;
		}
		GlobalCore.Marker.Valid = false;
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
		Call(heading, false);
	}

	public static void Call(float heading, boolean force)
	{
		/**
		 * if display is switched off, so we need no heading changes
		 */
		if (Energy.DisplayOff()) return;

		if (!Config.settings.HtcCompass.getValue() && !force) return;

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

		int callCounter = 0;
		for (PositionEvent event : list)
		{
			event.OrientationChanged(heading);
			callCounter++;
		}

	}

}
