package CB_UI.GL_UI.Views;

import java.util.ArrayList;

import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class LogView extends V_ListView implements SelectedCacheEvent
{
	public static CB_RectF ItemRec;
	public static LogView that;

	public LogView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		ItemRec = (new CB_RectF(0, 0, this.width, UI_Size_Base.that.getButtonHeight() * 1.1f)).ScaleCenter(0.97f);
		setBackground(SpriteCacheBase.ListBack);

		this.setBaseAdapter(null);
		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
		this.setDisposeFlag(false);
	}

	@Override
	public void onShow()
	{
		// if Tab register for Cache Changed Event
		if (GlobalCore.isTab)
		{
			SelectedCacheEventList.Add(this);
		}

		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());

		resetInitial();
	}

	@Override
	public void onHide()
	{
		SelectedCacheEventList.Remove(this);
	}

	@Override
	public void Initial()
	{
		super.Initial();

		createItemList(aktCache);

		this.setBaseAdapter(null);
		lvAdapter = new CustomAdapter();
		this.setBaseAdapter(lvAdapter);

		this.setEmptyMsg(Translation.Get("EmptyLogList"));

		this.scrollTo(0);
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	Cache aktCache;
	CustomAdapter lvAdapter;

	ArrayList<LogViewItem> itemList;

	private void createItemList(Cache cache)
	{
		if (itemList == null) itemList = new ArrayList<LogViewItem>();
		itemList.clear();

		if (cache == null) return; // Kein Cache angew�hlt

		ArrayList<LogEntry> cleanLogs = new ArrayList<LogEntry>();
		cleanLogs = Database.Logs(cache);// cache.Logs();

		int index = 0;
		for (LogEntry logEntry : cleanLogs)
		{
			CB_RectF rec = ItemRec.copy();
			rec.setHeight(MeasureItemHeight(logEntry));
			LogViewItem v = new LogViewItem(rec, index++, logEntry);

			itemList.add(v);
		}

	}

	private float MeasureItemHeight(LogEntry logEntry)
	{
		// object ist nicht von Dialog abgeleitet, daher
		float margin = UI_Size_Base.that.getMargin();
		float headHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + margin;

		float mesurdWidth = ItemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic() - ListViewItemBackground.getRightWidthStatic()
				- (margin * 2);

		float commentHeight = (margin * 4) + Fonts.MeasureWrapped(logEntry.Comment, mesurdWidth).height;

		return headHeight + commentHeight;
	}

	public class CustomAdapter implements Adapter
	{
		public CustomAdapter()
		{

		}

		public int getCount()
		{
			if (itemList != null)
			{

				return itemList.size();

			}
			else
			{
				return 0;
			}
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			if (itemList != null)
			{
				return itemList.get(position);

			}
			else
				return null;
		}

		@Override
		public float getItemSize(int position)
		{
			if (itemList.size() == 0) return 0;
			return itemList.get(position).getHeight();
		}

	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			aktCache = cache;
		}

		resetInitial();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		SetSelectedCache(cache, waypoint);
	}

}