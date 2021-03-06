package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.SolverView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSolverView extends CB_Action_ShowView {

    public CB_Action_ShowSolverView() {
        super("Solver", MenuID.AID_SHOW_SOLVER);
    }

    @Override
    public void Execute() {
        if ((TabMainView.solverView == null) && (tabMainView != null) && (tab != null))
            TabMainView.solverView = new SolverView(tab.getContentRec(), "SolverView");

        if ((TabMainView.solverView != null) && (tab != null))
            tab.ShowView(TabMainView.solverView);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.solverIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return TabMainView.solverView;
    }
}
