package CB_Core.GL_UI.Main.Actions;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.MenuItemConst;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.FieldNotesView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowFieldNotesView extends CB_Action_ShowView
{

	public CB_Action_ShowFieldNotesView()
	{
		super("FieldNotes", AID_SHOW_FIELDNOTES);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.fieldNotesView == null) && (tabMainView != null) && (tab != null)) TabMainView.fieldNotesView = new FieldNotesView(
				tab.getContentRec(), "FieldNotesView");

		if ((TabMainView.fieldNotesView != null) && (tab != null)) tab.ShowView(TabMainView.fieldNotesView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(0);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.fieldNotesView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public boolean ShowContextMenu()
	{
		platformConector.menuItemClicked(MenuItemConst.SHOW_FIELD_NOTE_CONTEXT_MENU);
		return true;
	}
}