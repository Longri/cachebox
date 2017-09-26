package de.droidcachebox;

import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingUsage;
import CB_Utils.Settings.SettingsList;

public class AndroidSettings {
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingBool RunOverLockScreen = (SettingBool) SettingsList.addSetting(new SettingBool("RunOverLockScreen", SettingCategory.Misc, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));

}