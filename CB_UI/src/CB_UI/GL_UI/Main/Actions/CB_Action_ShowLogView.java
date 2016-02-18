package CB_UI.GL_UI.Main.Actions;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_Core.CB_Core_Settings;
import CB_Core.Api.GroundspeakAPI;
import CB_UI.GlobalCore;
import CB_UI.GlobalCore.IChkRedyHandler;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.LogView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;

public class CB_Action_ShowLogView extends CB_Action_ShowView {

	public CB_Action_ShowLogView() {
		super("ShowLogs", MenuID.AID_SHOW_LOGS);
	}

	@Override
	public void Execute() {
		GlobalCore.filterLogsOfFriends = false; // Reset Filter by Friends when opening LogView

		if ((TabMainView.logView == null) && (tabMainView != null) && (tab != null))
			TabMainView.logView = new LogView(tab.getContentRec(), "LogView");

		if ((TabMainView.logView != null) && (tab != null))
			tab.ShowView(TabMainView.logView);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.listIcon.name());
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.logView;
	}

	@Override
	public boolean hasContextMenu() {
		return true;
	}

	@Override
	public Menu getContextMenu() {
		Menu cm = new Menu("LogListContextMenu");

		cm.addOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				switch (((MenuItem) v).getMenuItemId()) {
				case MenuID.MI_LOAD_FRIENDS_LOGS:
					reloadLogs(false);
					return true;
				case MenuID.MI_RELOADLOGS:
					reloadLogs(true);
					return true;
				case MenuID.MI_FILTERLOGS:
					GlobalCore.filterLogsOfFriends = !GlobalCore.filterLogsOfFriends;
					if (LogView.that != null) {
						LogView.that.resetInitial();
					}
					break;
				}
				return false;
			}

		});

		MenuItem mi;
		cm.addItem(MenuID.MI_RELOADLOGS, "ReloadLogs", Sprites.getSprite(IconName.importIcon.name()));
		if (CB_Core_Settings.Friends.getValue().length() > 0) {
			cm.addItem(MenuID.MI_LOAD_FRIENDS_LOGS, "LoadLogsOfFriends", Sprites.getSprite(IconName.importIcon.name()));
			mi = cm.addItem(MenuID.MI_FILTERLOGS, "FilterLogsOfFriends", Sprites.getSprite(IconName.filter.name()));
			mi.setCheckable(true);
			mi.setChecked(GlobalCore.filterLogsOfFriends);
		}
		return cm;
	}

	private void reloadLogs(final boolean all) {
		if (GroundspeakAPI.ApiLimit()) {
			GlobalCore.MsgDownloadLimit();
			return;
		}

		// First check API-Key with visual Feedback
		GlobalCore.chkAPiLogInWithWaitDialog(new IChkRedyHandler() {

			@Override
			public void checkReady(int MemberType) {
				TimerTask tt = new TimerTask() {

					@Override
					public void run() {
						if (all) {
							new CB_Action_LoadLogs().Execute();
						} else {
							new CB_Action_LoadFriendLogs().Execute();
						}

					}
				};
				Timer t = new Timer();
				t.schedule(tt, 100);
			}
		});
	}

}
