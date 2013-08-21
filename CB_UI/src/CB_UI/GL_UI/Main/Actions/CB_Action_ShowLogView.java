package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Menu.MenuID;
import CB_UI.GL_UI.SpriteCacheBase.IconName;
import CB_UI.GL_UI.Views.LogView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowLogView extends CB_Action_ShowView
{

	public CB_Action_ShowLogView()
	{
		super("ShowLogs", MenuID.AID_SHOW_LOGS);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.logView == null) && (tabMainView != null) && (tab != null)) TabMainView.logView = new LogView(tab.getContentRec(),
				"LogView");

		if ((TabMainView.logView != null) && (tab != null)) tab.ShowView(TabMainView.logView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.list_4.ordinal());
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.logView;
	}
}
