package CB_Utils.Settings;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Utils.DB.Database_Core;
import CB_Utils.Log.Logger;

public abstract class SettingsList extends ArrayList<SettingBase<?>>
{
	private static SettingsList that;

	private static final long serialVersionUID = -969846843815877942L;

	private boolean isLoaded = false;

	public SettingsList()
	{
		that = this;

		// add Member to list
		Member[] mbrs = this.getClass().getFields();

		for (Member mbr : mbrs)
		{
			if (mbr instanceof Field)
			{
				try
				{
					Object obj = ((Field) mbr).get(this);
					if (obj instanceof SettingBase<?>)
					{
						add((SettingBase<?>) obj);
					}
				}
				catch (IllegalArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		mbrs = null;
	}

	public boolean isLoaded()
	{
		return isLoaded;
	}

	public static SettingBase<?> addSetting(SettingBase<?> setting)
	{

		if (that == null) try
		{
			throw new InstantiationException("Settings List not initial");
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
		that.add(setting);
		if (!that.contains(setting))
		{

		}
		else
		{
			String stop = "";
			Logger.LogCat(stop);
		}
		return setting;
	}

	@Override
	public boolean add(SettingBase<?> setting)
	{
		if (!that.contains(setting))
		{
			return super.add(setting);
		}
		return false;
	}

	protected abstract Database_Core getSettingsDB();

	protected abstract Database_Core getDataDB();

	protected SettingsDAO createSettingsDAO()
	{
		return new SettingsDAO();
	}

	public void WriteToDB()
	{
		// Write into DB
		SettingsDAO dao = createSettingsDAO();
		getSettingsDB().beginTransaction();
		Database_Core Data = getDataDB();

		try
		{
			if (Data != null) Data.beginTransaction();
		}
		catch (Exception ex)
		{
			// do not change Data now!
			Data = null;
		}

		try
		{
			for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
			{
				SettingBase<?> setting = it.next();
				if (!setting.isDirty()) continue; // is not changed -> do not

				if (SettingStoreType.Local == setting.getStoreType())
				{
					if (Data != null) dao.WriteToDatabase(Data, setting);
				}
				else if (SettingStoreType.Global == setting.getStoreType())
				{
					dao.WriteToDatabase(getSettingsDB(), setting);
				}
				else if (SettingStoreType.Platform == setting.getStoreType())
				{
					dao.WriteToPlatformSettings(setting);
				}
				setting.clearDirty();

			}
			if (Data != null) Data.setTransactionSuccessful();
			getSettingsDB().setTransactionSuccessful();
		}
		finally
		{
			getSettingsDB().endTransaction();
			if (Data != null) Data.endTransaction();
		}

	}

	public void ReadFromDB()
	{
		// Read from DB
		try
		{
			Logger.DEBUG("Reading global settings: " + getSettingsDB().getDatabasePath());
			Logger.DEBUG("and local settings: " + getSettingsDB().getDatabasePath());
		}
		catch (Exception e)
		{
			// gibt beim splash - Start: NPE in Translation.readMissingStringsFile
			// Nachfolgende Starts sollten aber protokolliert werden
		}
		SettingsDAO dao = new SettingsDAO();
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			if (SettingStoreType.Local == setting.getStoreType())
			{
				dao.ReadFromDatabase(getDataDB(), setting);
			}
			else if (SettingStoreType.Global == setting.getStoreType())
			{
				dao.ReadFromDatabase(getSettingsDB(), setting);
			}
			else if (SettingStoreType.Platform == setting.getStoreType())
			{
				dao.ReadFromPlatformSetting(setting);
			}
		}
		isLoaded = true;
	}

	public void LoadFromLastValue()
	{
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			setting.loadFromLastValue();
		}
	}

	public void SaveToLastValue()
	{
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			setting.saveToLastValue();
		}
	}

	public void LoadAllDefaultValues()
	{
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			setting.loadDefault();
		}
	}
}