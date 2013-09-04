package CB_UI.GL_UI.Main.Actions;

import CB_UI.Config;
import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_Settings extends CB_ActionCommand
{

	public CB_Action_Show_Settings()
	{
		super("settings", MenuID.AID_SHOW_SETTINGS);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.settings_26.ordinal());
	}

	SettingsActivity settingsDialog;
	boolean lastNightValue;

	@Override
	public void Execute()
	{
		if (settingsDialog == null)
		{
			settingsDialog = new SettingsActivity();
			lastNightValue = Config.settings.nightMode.getValue();
		}
		else
		{
			if (lastNightValue != Config.settings.nightMode.getValue())
			{
				settingsDialog = new SettingsActivity();
			}
		}
		settingsDialog.show();
	}

}