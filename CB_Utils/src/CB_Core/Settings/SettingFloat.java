package CB_Core.Settings;

public class SettingFloat extends SettingBase<Float>
{

	public SettingFloat(String name, SettingCategory category, SettingModus modus, float defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	@Override
	public String toDBString()
	{
		return String.valueOf(value);
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		try
		{
			value = Float.valueOf(dbString);
			return true;
		}
		catch (Exception ex)
		{
			value = defaultValue;
			return false;
		}
	}

}
