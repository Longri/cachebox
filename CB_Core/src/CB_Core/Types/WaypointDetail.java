package CB_Core.Types;

import java.io.Serializable;
import java.util.Date;

public class WaypointDetail implements Serializable
{
	private static final long serialVersionUID = -3177862382324983452L;

	// / L�sung einer QTA
	private byte[] Clue;
	// / Kommentartext
	private byte[] Description;

	public int checkSum = 0; // for replication

	public Date time;

	public WaypointDetail()
	{

	}

	public String getDescription()
	{
		if (Description == null) return Waypoint.EMPTY_STRING;
		return new String(Description, Waypoint.UTF_8);
	}

	public void setDescription(String description)
	{
		if (description == null)
		{
			Description = null;
			return;
		}
		Description = description.getBytes(Waypoint.UTF_8);
	}

	public String getClue()
	{
		if (Clue == null) return Waypoint.EMPTY_STRING;
		return new String(Clue, Waypoint.UTF_8);
	}

	public void setClue(String clue)
	{
		if (clue == null)
		{
			Clue = null;
			return;
		}
		Clue = clue.getBytes(Waypoint.UTF_8);
	}

	public void setTime(Date time2)
	{
		time = time2;
	}

	public void setCheckSum(int i)
	{
		checkSum = i;
	}

	public void dispose()
	{
		// TODO Auto-generated method stub

	}

}