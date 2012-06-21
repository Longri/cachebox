package CB_Core.GL_UI.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.CacheListChangedEventListner;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.Menu.CB_AllContextMenuHandler;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Locator;
import CB_Core.Types.Waypoint;

public class CacheListView extends V_ListView implements CacheListChangedEventListner, SelectedCacheEvent, PositionChangedEvent
{
	private CustomAdapter lvAdapter;

	public CacheListView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		registerSkinChangedEvent();
		CachListChangedEventList.Add(this);
	}

	@Override
	protected void Initial()
	{
		// Logger.LogCat("CacheListView => Initial()");
		// this.setListPos(0, false);
		chkSlideBack();
		GL_Listener.glListener.renderOnce(this.getName() + " Initial()");
	}

	@Override
	public void onShow()
	{
		setBackground(SpriteCache.ListBack);

		CachListChangedEventList.Add(this);
		SelectedCacheEventList.Add(this);
		PositionChangedEventList.Add(this);

		synchronized (Database.Data.Query)
		{
			lvAdapter = new CustomAdapter(Database.Data.Query);
			this.setBaseAdapter(lvAdapter);

			int itemCount = Database.Data.Query.size();
			int itemSpace = this.getMaxItemCount();

			if (itemSpace >= itemCount)
			{
				this.setUndragable();
			}
			else
			{
				this.setDragable();
			}
		}
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				// aktuellen Cache in der List anzeigen
				if (GlobalCore.SelectedCache() != null)
				{
					setSelectedCacheVisible();

				}
				else
					setSelection(0);

				resetInitial();
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 150);

		GL_Listener.glListener.renderOnce(this.getName() + " onShow()");
	}

	/**
	 * setzt den Aktuell selectierten Cache an die 2. Pos in der Liste
	 */
	public void setSelectedCacheVisible()
	{
		int centerList = mMaxItemCount / 2;
		setSelectedCacheVisible(-centerList);
	}

	/**
	 * setzt den Aktuell selectierten Cache an pos
	 * 
	 * @param pos
	 */
	public void setSelectedCacheVisible(int pos)
	{
		if (!this.isDrageble()) return;
		int id = 0;
		int first = this.getFirstVisiblePosition();
		int last = this.getLastVisiblePosition();

		synchronized (Database.Data.Query)
		{
			for (Cache ca : Database.Data.Query)
			{
				if (ca == GlobalCore.SelectedCache())
				{
					this.setSelection(id);
					if (!(first <= id && last >= id)) this.scrollToItem(id - pos);
					break;
				}
				id++;
			}
		}

		chkSlideBack();
		GL_Listener.glListener.renderOnce(this.getName() + " setSelectedCachVisible");
	}

	@Override
	public void onHide()
	{
		SelectedCacheEventList.Remove(this);
		CachListChangedEventList.Remove(this);
		PositionChangedEventList.Remove(this);
		lvAdapter = null;
		this.setBaseAdapter(lvAdapter);
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			Cache cache;
			synchronized (Database.Data.Query)
			{
				cache = Database.Data.Query.get(selectionIndex);
			}
			if (cache != null)
			{
				// Wenn ein Cache einen Final waypoint hat dann soll gleich dieser aktiviert werden
				Waypoint waypoint = cache.GetFinalWaypoint();
				GlobalCore.SelectedWaypoint(cache, waypoint);
			}
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

			Cache cache;
			synchronized (Database.Data.Query)
			{
				cache = Database.Data.Query.get(selectionIndex);
			}
			Waypoint finalWp = null;
			if (cache.HasFinalWaypoint()) finalWp = cache.GetFinalWaypoint();
			// shutdown AutoResort when selecting a cache by hand
			GlobalCore.autoResort = false;
			GlobalCore.SelectedWaypoint(cache, finalWp);

			invalidate();
			CB_AllContextMenuHandler.showBtnCacheContextMenu();
			return true;
		}
	};

	public class CustomAdapter implements Adapter
	{
		private CacheList cacheList;

		public CustomAdapter(CacheList cacheList)
		{
			this.cacheList = cacheList;
		}

		public int getCount()
		{
			if (cacheList == null) return 0;
			return cacheList.size();
		}

		public Object getItem(int position)
		{
			if (cacheList == null) return null;
			return cacheList.get(position);
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			if (cacheList == null) return null;
			Cache cache = cacheList.get(position);
			CacheListViewItem v = new CacheListViewItem(UiSizes.getCacheListItemRec().asFloat(), position, cache);
			v.setClickable(true);
			v.setOnClickListener(onItemClickListner);
			v.setOnLongClickListener(onItemLongClickListner);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			if (cacheList == null) return 0;
			// alle Items haben die gleiche Gr��e (H�he)
			return UiSizes.getCacheListItemRec().getHeight();
		}

	}

	@Override
	public void CacheListChangedEvent()
	{
		this.setBaseAdapter(null);
		synchronized (Database.Data.Query)
		{
			lvAdapter = new CustomAdapter(Database.Data.Query);

			this.setBaseAdapter(lvAdapter);

			int itemCount = Database.Data.Query.size();
			int itemSpace = this.getMaxItemCount();

			if (itemSpace >= itemCount)
			{
				this.setUndragable();
			}
			else
			{
				this.setDragable();
			}
		}
		chkSlideBack();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		if (GlobalCore.SelectedCache() != null)
		{
			setSelectedCacheVisible();
		}
	}

	@Override
	protected void SkinIsChanged()
	{
		reloadItems();
		setBackground(SpriteCache.ListBack);
		CacheListViewItem.ResetBackground();
	}

	@Override
	public void PositionChanged(Locator locator)
	{
		GL_Listener.glListener.renderOnce("Core.CacheListView");
	}

	@Override
	public void OrientationChanged(float heading)
	{
		GL_Listener.glListener.renderOnce("Core.CacheListView");
	}

	@Override
	public String getReceiverName()
	{
		return "Core.CacheListView";
	}

}
