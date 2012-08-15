package CB_Core.GL_UI.Views.AdvancedSettingsView;

import CB_Core.Settings.SettingBase;
import CB_Core.Settings.SettingCategory;
import CB_Core.Settings.SettingModus;

/**
 * Der Button der sich hinter einer Category verbirgt und in der Settings List als Toggle Button dieser Category angezeigt wird.
 * 
 * @author Longri
 */
public class SettingsListButtonSkinSpinner extends SettingBase
{

	public SettingsListButtonSkinSpinner(String name, SettingCategory category, SettingModus modus, boolean global)
	{
		super(name, category, modus, global);

	}

}