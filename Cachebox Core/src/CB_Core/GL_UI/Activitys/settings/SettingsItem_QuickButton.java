package CB_Core.GL_UI.Activitys.settings;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.QuickButtonList;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.SpinnerAdapter;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Main.Actions.QuickButton.QuickActions;
import CB_Core.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.utils.ColorDrawable;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.MoveableList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class SettingsItem_QuickButton extends CB_View_Base
{

	chkBox chkOnOff;
	Label lblChkOnOff;
	Spinner invisibleSelectSpinner;
	SpinnerAdapter selectAdapter;

	ImageButton up, down, del, add;
	V_ListView listView;
	Box boxForListView;

	public static MoveableList<QuickButtonItem> tmpQuickList = null;

	public SettingsItem_QuickButton(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showSelect();
				return true;
			}
		});

		initialButtons();
		initialListView();

		layout();
		reloadListViewItems();

		tmpQuickList = QuickButtonList.quickButtonList.clone();
		reloadListViewItems();
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	private void showSelect()
	{
		// erstelle Menu mit allen Actions, die noch nicht in der QuickButton List enthalten sind.

		final ArrayList<QuickActions> AllActionList = new ArrayList<QuickActions>();
		QuickActions[] tmp = QuickActions.values();

		for (QuickActions item : tmp)
		{
			boolean exist = false;
			for (Iterator<QuickButtonItem> it = tmpQuickList.iterator(); it.hasNext();)
			{
				QuickButtonItem listItem = it.next();
				if (listItem.getAction() == item) exist = true;
			}
			if (!exist) AllActionList.add(item);
		}

		Menu icm = new Menu("Select QuickButtonItem");
		icm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				int selected = (((MenuItem) v).getMenuItemId());

				QuickActions type = AllActionList.get(selected);

				float itemHeight = UiSizes.getQuickButtonListHeight() * 0.93f;

				QuickButtonItem tmp = new QuickButtonItem(new CB_RectF(0, 0, itemHeight, itemHeight), tmpQuickList.size(), QuickActions
						.getActionEnumById(type.ordinal()), QuickActions.getName(type.ordinal()), type);

				tmpQuickList.add(tmp);

				reloadListViewItems();

				return true;
			}
		});

		int menuIndex = 0;
		for (QuickActions item : AllActionList)
		{
			if (item == QuickActions.empty) continue;

			icm.addItem(menuIndex++, QuickActions.getName(item.ordinal()), new SpriteDrawable(QuickActions
					.getActionEnumById(item.ordinal()).getIcon()), true);
		}

		icm.setPrompt(GlobalCore.Translations.Get("selectQuickButtemItem"));

		icm.show();

	}

	private void initialButtons()
	{

		up = new ImageButton("up");
		down = new ImageButton("down");
		del = new ImageButton("del");
		add = new ImageButton("add");

		up.setWidth(up.getHeight());
		down.setWidth(up.getHeight());
		del.setWidth(up.getHeight());
		add.setWidth(up.getHeight());

		up.setImage(new SpriteDrawable(SpriteCache.Arrows.get(11)));
		down.setImage(new SpriteDrawable(SpriteCache.Arrows.get(11)));
		del.setImage(new SpriteDrawable(SpriteCache.Icons.get(28)));
		add.setImage(new SpriteDrawable(SpriteCache.Icons.get(52)));

		up.setImageScale(0.7f);
		down.setImageScale(0.7f);
		del.setImageScale(0.7f);
		add.setImageScale(0.7f);

		up.setImageRotation(90f);
		down.setImageRotation(-90f);

		this.addChild(up);
		this.addChild(down);
		this.addChild(del);
		this.addChild(add);

		add.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showSelect();
				return true;
			}
		});

		del.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				int index = listView.getSelectedIndex();

				if (index >= 0 && index < tmpQuickList.size())
				{
					tmpQuickList.remove(index);

					reloadListViewItems();
				}

				return true;
			}
		});

		down.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				int index = listView.getSelectedIndex();

				if (index >= 0 && index < tmpQuickList.size())
				{
					tmpQuickList.MoveItem(index, 1);

					reloadListViewItems();
					int newIndex = index + 1;
					if (newIndex >= tmpQuickList.size()) newIndex = 0;

					listView.setSelection(newIndex);
				}

				return true;
			}
		});

		up.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				int index = listView.getSelectedIndex();

				if (index >= 0 && index < tmpQuickList.size())
				{
					tmpQuickList.MoveItem(index, -1);

					reloadListViewItems();

					int newIndex = index - 1;
					if (newIndex < 0) newIndex = tmpQuickList.size() - 1;

					listView.setSelection(newIndex);
				}

				return true;
			}
		});

	}

	private void initialListView()
	{
		CB_RectF rec = new CB_RectF(0, 0, this.width, this.height);
		boxForListView = new Box(rec, "");
		boxForListView.setBackground(SpriteCache.activityBackground);

		listView = new V_ListView(rec.copy(), "");
		boxForListView.addChildDirekt(listView);

		{
			// TODO die Listview wird hier nur angezeigt, wenn ein
			// Hintergrund gesetzt ist! Warum weis ich nicht ?!?!
			// Ich habe ihn erstmal auf Tranzparent gesetzt!
			Color c = new Color(1, 1, 1, 0);
			listView.setBackground(new ColorDrawable(c));
		}

		this.addChild(boxForListView);

	}

	private void reloadListViewItems()
	{
		listView.setBaseAdapter(null);
		listView.setBaseAdapter(new CustomAdapter());
		listView.notifyDataSetChanged();
	}

	private void layout()
	{
		float btnLeft = this.width - Right - up.getWidth();
		float margin = up.getHalfHeight() / 2;

		add.setX(btnLeft);
		add.setY(Bottom + margin);

		del.setX(btnLeft);
		del.setY(add.getMaxY() + margin);

		down.setX(btnLeft);
		down.setY(del.getMaxY() + margin);

		up.setX(btnLeft);
		up.setY(down.getMaxY() + margin);

		this.setHeight(up.getMaxY() + margin);

		boxForListView.setX(margin);
		boxForListView.setY(margin);
		boxForListView.setWidth(add.getX() - margin - margin);
		boxForListView.setHeight(this.height - (margin * 2));

		listView.setX(margin);
		listView.setY(margin / 2);
		listView.setWidth(boxForListView.getWidth() - (margin * 2));
		listView.setHeight(boxForListView.getHeight() - margin);
	}

	public class CustomAdapter implements Adapter
	{

		@Override
		public ListViewItemBase getView(int position)
		{
			if (tmpQuickList == null) return null;

			Menu icm = new Menu("virtuell");

			QuickButtonItem item = tmpQuickList.get(position);

			QuickActions action = item.getAction();

			MenuItem mi = icm.addItem(position, QuickActions.getName(action.ordinal()),
					new SpriteDrawable(QuickActions.getActionEnumById(action.ordinal()).getIcon()), true);

			mi.setWidth(listView.getWidth() - (listView.getBackground().getLeftWidth() * 2));
			mi.setClickable(true);
			mi.setOnClickListener(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					listView.setSelection(((ListViewItemBase) v).getIndex());
					return false;
				}
			});

			return mi;
		}

		@Override
		public float getItemSize(int position)
		{
			return UiSizes.getButtonHeight();
		}

		@Override
		public int getCount()
		{
			if (tmpQuickList == null) return 0;
			return tmpQuickList.size();
		}
	}

	@Override
	protected void Initial()
	{

	};
}