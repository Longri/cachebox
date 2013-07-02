package CB_Core.Settings;

public class SettingDouble extends SettingBase
{
	protected double value;
	protected double defaultValue;
	protected double lastValue;

	public SettingDouble(String name, SettingCategory category, SettingModus modus, double defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public double getValue()
	{
		return value;
	}

	public double getDefaultValue()
	{
		return defaultValue;
	}

	public void setValue(double value)
	{
		if (this.value == value) return;
		this.value = value;
		setDirty();
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
			value = Double.valueOf(dbString);
			return true;
		}
		catch (Exception ex)
		{
			value = defaultValue;
			return false;
		}
	}

	@Override
	public void loadDefault()
	{
		value = defaultValue;
	}

	@Override
	public void saveToLastValue()
	{
		lastValue = value;
	}

	@Override
	public void loadFromLastValue()
	{
		value = lastValue;
	}

}