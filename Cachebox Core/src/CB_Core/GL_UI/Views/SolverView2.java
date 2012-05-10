package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog.SloverBackStringListner;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.Menu.CB_AllContextMenuHandler;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

public class SolverView2 extends V_ListView implements SelectedCacheEvent
{
	private CustomAdapter lvAdapter;
	private Solver solver;
	private Cache cache;

	public SolverView2(CB_RectF rec, String Name)
	{
		super(rec, Name);

		Logger.LogCat("Create SolverView2 => " + rec.toString());
		/*
		 * Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel"); lblDummy.setFont(Fonts.getNormal());
		 * lblDummy.setText("Dummy SolverView"); setBackground(SpriteCache.ListBack);
		 * 
		 * if (GlobalCore.platform == Plattform.Desktop) this.addChild(lblDummy);
		 */

		cache = GlobalCore.SelectedCache();
	}

	@Override
	public void onShow()
	{
		// platformConector.showView(ViewConst.SOLVER_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		// CachListChangedEventList.Add(this);
		SelectedCacheEventList.Add(this);

		setBackground(SpriteCache.ListBack);

		cache = GlobalCore.SelectedCache();

		intiList();
	}

	private void intiList()
	{
		if (cache == null) solver = new Solver("");
		else
		{
			this.cache = GlobalCore.SelectedCache();
			String s = Database.GetSolver(this.cache);
			solver = new Solver(s);
			solver.Solve();
			// wenn der Solver noch leer ist oder die letzte Zeile nicht leer ist dann am Ende eine leere Zeile einf�gen
			if ((solver.size() == 0) || (solver.get(solver.size() - 1).getOrgText().length() > 0))
			{
				solver.add(solver.size(), new SolverZeile(solver, ""));
			}
		}

		lvAdapter = new CustomAdapter(solver);
		this.setBaseAdapter(lvAdapter);

		int itemCount = solver.size();
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount)
		{
			this.setUndragable();
		}
		else
		{
			this.setDragable();
		}

		this.setSelection(0);
		this.setListPos(0, false);

		this.invalidate();
		GL_Listener.glListener.renderOnce(this.getName() + " onShow()");
	}

	private void reloadList()
	{
		this.setBaseAdapter(lvAdapter);
		int itemCount = solver.size();
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount)
		{
			this.setUndragable();
		}
		else
		{
			this.setDragable();
		}

		this.invalidate();
		GL_Listener.glListener.renderOnce(this.getName() + " onShow()");
	}

	@Override
	public void onHide()
	{
		// platformConector.hideView(ViewConst.SOLVER_VIEW);
		// CachListChangedEventList.Add(this);
		SelectedCacheEventList.Remove(this);
		Database.SetSolver(GlobalCore.SelectedCache(), solver.getSolverString());
	}

	@Override
	protected void Initial()
	{
		Logger.LogCat("SolverView2 => Initial()");
		this.setListPos(0, false);
		chkSlideBack();
		GL_Listener.glListener.renderOnce(this.getName() + " Initial()");
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			// GlobalCore.SelectedCache(Database.Data.Query.get(selectionIndex));

			setSelection(selectionIndex);
			return true;
		}
	};

	private OnLongClickListener onItemLongClickListner = new OnLongClickListener()
	{

		@Override
		public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			invalidate();
			CB_AllContextMenuHandler.showBtnCacheContextMenu();
			return true;
		}
	};

	public class CustomAdapter implements Adapter
	{
		private Solver solver;

		public CustomAdapter(Solver solver)
		{
			this.solver = solver;
		}

		public int getCount()
		{
			if (solver == null) return 0;
			return solver.size();
		}

		public Object getItem(int position)
		{
			if (solver == null) return null;
			return solver.get(position);
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			if (solver == null) return null;
			SolverZeile solverZeile = solver.get(position);
			SolverViewItem v = new SolverViewItem(UiSizes.getCacheListItemRec().asFloat(), position, solverZeile);
			v.setClickable(true);
			v.setOnClickListener(onItemClickListner);
			v.setOnLongClickListener(onItemLongClickListner);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			if (solver == null) return 0;

			// SolverZeile solverZeile = solver.get(position);
			// if (solverZeile.Solution.length() == 0) return UiSizes.getCacheListItemRec().getHeight();
			// else
			return UiSizes.getCacheListItemRec().getHeight();
		}

	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		// Solver speichern
		Database.SetSolver(this.cache, solver.getSolverString());
		this.cache = cache;
		intiList();
	}

	public void ChangeLine()
	{
		// Show Dialog
		CB_RectF rec = GL_UISizes.UI_Left.copy();
		if (GlobalCore.isTab)
		{
			// da der Linke Tab bei einem Tablett nicht so Breit ist wie auf einem Phone,
			// verdoppeln wir hier die Breite (sieht besser aus)
			rec.setWidth(rec.getWidth() * 2);
		}

		String SolverString = solver.get(mSelectedIndex).getOrgText();

		SolverDialog solverDialog = new SolverDialog(rec, "SolverDialog", SolverString);

		neu = false;
		solverDialog.show(backListner);
	}

	boolean neu = false;

	final SloverBackStringListner backListner = new SloverBackStringListner()
	{

		@Override
		public void BackString(String backString)
		{
			SolverZeile zeile;
			if (neu)
			{
				zeile = new SolverZeile(solver, backString);
				solver.add(mSelectedIndex, zeile);
			}
			else
			{
				zeile = solver.get(mSelectedIndex);
				zeile.setText(backString);
			}

			for (int i = mSelectedIndex; i < solver.size(); i++)
			{
				SolverZeile zeile2 = solver.get(i);
				zeile2.Parse();
			}

			if (!neu)
			{
				// wenn der letzte Eintrag ge�ndert wurde dann soll hinter dem letzten Eintrag eine weitere neue Zeile eingef�gt werden
				if (mSelectedIndex == solver.size() - 1)
				{
					solver.add(solver.size(), new SolverZeile(solver, ""));
					neu = true; // damit die Liste neu geladen wird
				}
			}

			if (neu) reloadList();
		}
	};

	public void InsertLine()
	{
		CB_RectF rec = GL_UISizes.UI_Left.copy();
		if (GlobalCore.isTab)
		{
			// da der Linke Tab bei einem Tablett nicht so Breit ist wie auf einem Phone,
			// verdoppeln wir hier die Breite (sieht besser aus)
			rec.setWidth(rec.getWidth() * 2);
		}

		String SolverString = "";

		SolverDialog solverDialog = new SolverDialog(rec, "SolverDialog", SolverString);

		neu = true;
		solverDialog.show(backListner);
	}

	final OnMsgBoxClickListener deleteListener = new OnMsgBoxClickListener()
	{
		@Override
		public boolean onClick(int which)
		{
			if (which == 1)
			{
				solver.remove(mSelectedIndex);
				reloadList();
				return true;
			}
			else
				return false;
		}
	};

	public void DeleteLine()
	{
		GL_MsgBox.Show("Zeile l�schen?", "Solver", MessageBoxButtons.YesNo, MessageBoxIcon.Question, deleteListener);
	}
}