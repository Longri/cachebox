package de.cachebox_test.Events;

import android.location.Location;

public interface PositionEvent 
{
	public void PositionChanged(Location location);
	public void OrientationChanged(float heading);
}