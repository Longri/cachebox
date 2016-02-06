package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_Core.Database;
import CB_Core.Types.CacheWithWP;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_switch_Autoresort extends CB_Action {

    public CB_Action_switch_Autoresort() {
	super("AutoResort", MenuID.AID_AUTO_RESORT);
    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.Icons.get(IconName.autoSelectOff_16.ordinal());
    }

    @Override
    public void Execute() {
	GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
	if (GlobalCore.getAutoResort()) {
	    synchronized (Database.Data.Query) {
		if (GlobalCore.isSetSelectedCache()) {
		    CacheWithWP ret = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
		    GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
		    GlobalCore.setNearestCache(ret.getCache());
		    ret.dispose();
		}
	    }
	}
    }
}
