package CB_UI.GL_UI.Menu;

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.SearchGC;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.DeleteSelectedCache;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Controls.Dialogs.HintDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Lists.CB_List;

public class CB_AllContextMenuHandler {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(QuickButtonItem.class);

    public static void showBtnCacheContextMenu() {

	boolean selectedCacheIsNull = (GlobalCore.getSelectedCache() == null);

	boolean selectedCacheIsNoGC = false;

	if (!selectedCacheIsNull) {
	    selectedCacheIsNoGC = !GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
	}

	Menu icm = new Menu("BtnCacheContextMenu");
	icm.addOnClickListener(onItemClickListener);
	MenuItem mi;

	mi = icm.addItem(MenuID.MI_RELOAD_CACHE_INFO, "ReloadCacheAPI", SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal()));
	if (selectedCacheIsNull)
	    mi.setEnabled(false);
	if (selectedCacheIsNoGC)
	    mi.setEnabled(false);

	mi = icm.addItem(MenuID.MI_WAYPOINTS, "Waypoints", SpriteCacheBase.getThemedSprite("big" + CacheTypes.Trailhead.name())); //16
	if (selectedCacheIsNull)
	    mi.setEnabled(false);

	mi = icm.addItem(MenuID.MI_SHOW_LOGS, "ShowLogs", SpriteCacheBase.Icons.get(IconName.list_21.ordinal()));
	if (selectedCacheIsNull)
	    mi.setEnabled(false);

	mi = icm.addItem(MenuID.MI_HINT, "hint");
	boolean enabled = false;
	if (!selectedCacheIsNull && (GlobalCore.getSelectedCache().hasHint()))
	    enabled = true;
	mi.setEnabled(enabled);
	mi.setIcon(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.hint_19.ordinal())));

	mi = icm.addItem(MenuID.MI_SPOILER, "spoiler", SpriteCacheBase.Icons.get(IconName.images_22.ordinal()));
	mi.setEnabled(GlobalCore.selectedCachehasSpoiler());

	mi = icm.addItem(MenuID.MI_SOLVER, "Solver", SpriteCacheBase.Icons.get(IconName.solver_24.ordinal()));
	if (selectedCacheIsNull)
	    mi.setEnabled(false);

	if (GlobalCore.JokerisOnline()) {
	    mi = icm.addItem(MenuID.MI_JOKER, "joker", SpriteCacheBase.Icons.get(IconName.jokerPhone_25.ordinal()));
	    // Menu Item Telefonjoker enabled / disabled abhänging von gcJoker MD5

	    if (mi != null) {
		enabled = false;
		if (GlobalCore.JokerisOnline())
		    enabled = true;
		mi.setEnabled(enabled);
	    }

	}

	mi = icm.addItem(MenuID.MI_EDIT_CACHE, "MI_EDIT_CACHE");
	if (selectedCacheIsNull)
	    mi.setEnabled(false);

	mi = icm.addItem(MenuID.MI_FAVORIT, "Favorite", SpriteCacheBase.Icons.get(IconName.favorit_42.ordinal()));
	mi.setCheckable(true);
	if (selectedCacheIsNull)
	    mi.setEnabled(false);
	else
	    mi.setChecked(GlobalCore.getSelectedCache().isFavorite());

	mi = icm.addItem(MenuID.MI_DELETE_CACHE, "MI_DELETE_CACHE");
	if (selectedCacheIsNull)
	    mi.setEnabled(false);

	icm.Show();

    }

    static CancelWaitDialog wd;

    private static OnClickListener onItemClickListener = new OnClickListener() {

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
	    EditCache editCache = null;
	    CacheDAO dao = null;
	    switch (((MenuItem) v).getMenuItemId()) {
	    case MenuID.MI_HINT:
		HintDialog.show();
		return true;

	    case MenuID.MI_RELOAD_CACHE_INFO:
		wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

		    @Override
		    public void isCanceld() {

		    }
		}, new cancelRunnable() {

		    @Override
		    public void run() {
			SearchGC searchC = new SearchGC(GlobalCore.getSelectedCache().getGcCode());

			searchC.number = 1;

			CB_List<Cache> apiCaches = new CB_List<Cache>();
			ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
			ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

			CB_UI.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, GlobalCore.getSelectedCache().getGPXFilename_ID(), this);

			try {
			    GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}

			// Reload result from DB
			synchronized (Database.Data.Query) {
			    String sqlWhere = FilterInstances.LastFilter.getSqlWhere(Config.GcLogin.getValue());
			    CacheListDAO cacheListDAO = new CacheListDAO();
			    cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
			}

			CacheListChangedEventList.Call();

			wd.close();
		    }

		    @Override
		    public boolean cancel() {
			// TODO Handle cancel
			return false;
		    }
		});

		return true;

	    case MenuID.MI_WAYPOINTS:
		if (TabMainView.actionShowWaypointView != null)
		    TabMainView.actionShowWaypointView.Execute();
		return true;

	    case MenuID.MI_SHOW_LOGS:
		if (TabMainView.actionShowLogView != null)
		    TabMainView.actionShowLogView.Execute();
		return true;

	    case MenuID.MI_SPOILER:
		if (TabMainView.actionShowSpoilerView != null)
		    TabMainView.actionShowSpoilerView.Execute();
		return true;

	    case MenuID.MI_SOLVER:
		if (TabMainView.actionShowSolverView != null)
		    TabMainView.actionShowSolverView.Execute();
		return true;

	    case MenuID.MI_JOKER:
		if (TabMainView.actionShowJokerView != null)
		    TabMainView.actionShowJokerView.Execute();
		return true;

	    case MenuID.MI_EDIT_CACHE:
		if (editCache == null)
		    editCache = new EditCache(ActivityBase.ActivityRec(), "editCache");
		editCache.update(GlobalCore.getSelectedCache());
		return true;

	    case MenuID.MI_FAVORIT:
		if (GlobalCore.isSetSelectedCache()) {
		    GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
		    if (dao == null)
			dao = new CacheDAO();
		    dao.UpdateDatabase(GlobalCore.getSelectedCache());
		    CacheListChangedEventList.Call();
		}
		return true;

	    case MenuID.MI_DELETE_CACHE:
		DeleteSelectedCache.Execute();
		return true;

	    default:
		return false;

	    }

	}
    };

}
