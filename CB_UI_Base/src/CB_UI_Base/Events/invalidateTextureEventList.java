package CB_UI_Base.Events;

import java.util.ArrayList;

import CB_Utils.Log.Logger;

public class invalidateTextureEventList
{
	public static ArrayList<invalidateTextureEvent> list = new ArrayList<invalidateTextureEvent>();

	public static void Add(invalidateTextureEvent event)
	{
		synchronized (list)
		{
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(invalidateTextureEvent event)
	{
		synchronized (list)
		{
			list.remove(event);
		}
	}

	public static void Call()
	{

		try
		{
			synchronized (list)
			{
				for (invalidateTextureEvent event : list)
				{
					if (event != null) event.invalidateTexture();
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("invalidateTextureEventList", "Call()", e);
		}
	}
}