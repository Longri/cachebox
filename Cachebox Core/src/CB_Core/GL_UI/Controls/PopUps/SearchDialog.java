package CB_Core.GL_UI.Controls.PopUps;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Activitys.SearchOverPosition;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.Slider;
import CB_Core.GL_UI.Controls.Slider.YPositionChanged;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Views.CacheListView;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class SearchDialog extends PopUp_Base
{
	public static SearchDialog that;

	/**
	 * True, wenn der Searchdialog sichtbar ist.
	 */
	private boolean mIsVisible = true;

	/**
	 * True, wenn eine Suche l�uft und der Iterator mit Next weiter durchlaufen werden kann.
	 */
	private boolean mSearchAktive = false;

	/**
	 * True, wenn die QuickButtonList beim �ffnen dieses Dialogs aufgeklappt war.
	 */
	private boolean mQuickButtonListWasShow;

	/*
	 * Buttons
	 */

	/**
	 * Option Title, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnTitle;

	/**
	 * Option GC-Code, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnGc;

	/**
	 * Option Owner, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnOwner;

	/**
	 * Option Online Suche On/Off
	 */
	private MultiToggleButton mTglBtnOnline;

	/**
	 * Button, welcher eine Suchanfrage als Filter verwendet
	 */
	private ImageButton mBtnFilter;

	/**
	 * Button, der eine Suche Startet
	 */
	private Button mBtnSearch;

	/**
	 * Button, der den n�chsten Treffer einer gestarteten Suche findet
	 */
	private Button mBtnNext;

	/**
	 * Button, der den Search Dialog schliesst
	 */
	private Button mBtnCancel;

	/**
	 * Such Eingabe Feld
	 */
	private EditWrapedTextField mEingabe;

	/**
	 * enth�lt den Index des element der CacheListe, an der die Suche steht
	 */
	private int mSearchIndex = -1;

	/**
	 * Enth�lt einen Iterator der aktuell durschten CacheList
	 */
	private Iterator<Cache> CacheListIterator = null;

	/**
	 * Enth�llt den Aktuellen Such Modus <br/>
	 * 0 = Title <br/>
	 * 1 = Gc-Code <br/>
	 * 2 = Owner <br/>
	 */
	private int mSearchState = 0;

	public SearchDialog()
	{
		super(new CB_RectF(), "SearchDialog");

		that = this;

		this.setSize(UiSizes.getCacheListItemSize().asFloat());

		if (GlobalCore.isTab)
		{
			this.setBackground(SpriteCache.activityBackground);
			this.setWidth(this.width * 1.4f);
			this.setX((UiSizes.getWindowWidth() / 2) - this.halfWidth);
			this.setY((UiSizes.getWindowHeight() / 2) - this.halfHeight);
		}
		else
		{
			this.setBackground(SpriteCache.ListBack);
		}
		// initial Buttons

		float margin = UiSizes.getMargin();
		if (GlobalCore.isTab) margin *= 2;
		float btnWidth = (this.width - (margin * 7)) / 4;

		CB_RectF rec = new CB_RectF(0, 0, btnWidth, UiSizes.getButtonHeight());

		mTglBtnTitle = new MultiToggleButton(rec, "mTglBtnTitle");
		mTglBtnGc = new MultiToggleButton(rec, "mTglBtnGc");
		mTglBtnOwner = new MultiToggleButton(rec, "mTglBtnOwner");
		mTglBtnOnline = new MultiToggleButton(rec, "mTglBtnOnline");

		rec.setWidth(btnWidth = (this.width - (margin * 5)) / 4);

		mBtnFilter = new ImageButton(rec, "mBtnFilter");
		mBtnSearch = new Button(rec, "mBtnSearch");
		mBtnNext = new Button(rec, "mBtnNext");
		mBtnCancel = new Button(rec, "mBtnCancel");

		rec.setWidth(this.width - (margin * 2));

		mEingabe = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "");

		mEingabe.setTextFieldListener(new TextFieldListener()
		{

			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{

			}

			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{
				textBox_TextChanged();
			}
		});

		mEingabe.setText("");

		// Layout! da es sich nicht �ndert, brauchen wir es nicht in eine Methode packen
		float y = margin;

		mBtnFilter.setPos(margin, y);
		mBtnSearch.setPos(mBtnFilter.getMaxX() + margin, y);
		mBtnNext.setPos(mBtnSearch.getMaxX() + margin, y);
		mBtnCancel.setPos(mBtnNext.getMaxX() + margin, y);

		mEingabe.setPos(margin, mBtnCancel.getMaxY() + margin);

		y = mEingabe.getMaxY() + margin;

		mTglBtnOnline.setPos(margin, y);
		mTglBtnTitle.setPos(mTglBtnOnline.getMaxX() + margin + margin, y);
		mTglBtnGc.setPos(mTglBtnTitle.getMaxX() + margin, y);
		mTglBtnOwner.setPos(mTglBtnGc.getMaxX() + margin, y);

		// die H�he nach dem Verbrauchten Platz einstellen

		this.setHeight(mTglBtnOwner.getMaxY() + margin);

		// Controls zum Dialog hinzuf�gen
		this.addChild(mTglBtnTitle);
		this.addChild(mTglBtnGc);
		this.addChild(mTglBtnOwner);
		this.addChild(mTglBtnOnline);
		this.addChild(mBtnFilter);
		this.addChild(mBtnSearch);
		this.addChild(mBtnNext);
		this.addChild(mBtnCancel);
		this.addChild(mEingabe);

		setLang();
		switchSearcheMode(0);

		mBtnCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// eventuell eingesetzten Search Filter zur�ck setzen
				clearSearchFilter();

				close();
				return true;
			}

		});

		mTglBtnTitle.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(0);
				return true;
			}
		});

		mTglBtnGc.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(1);
				return true;
			}
		});

		mTglBtnOwner.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(2);
				return true;
			}
		});

		mBtnSearch.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				mSearchAktive = false;
				mSearchIndex = -1;
				searchNow(false);
				return true;
			}
		});

		mBtnNext.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				searchNow(true);
				return true;

			}
		});

		mBtnFilter.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				if (mTglBtnOnline.getState() == 1)
				{
					close();
					askPremium();
				}
				else
				{
					setFilter();
				}
				return true;
			}
		});

		mTglBtnOnline.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				setFilterBtnState();
				textBox_TextChanged();
				return true;
			}
		});

	}

	private void setFilterBtnState()
	{
		if (mTglBtnOnline.getState() == 0)
		{
			mBtnFilter.setImage(null);
			mBtnFilter.setText(Translation.Get("Filter"));
		}
		else
		{

			mBtnFilter.setImage(new SpriteDrawable(SpriteCache.Icons.get(53)));
			mBtnFilter.setText("");
		}
	}

	/**
	 * Setzt die �bersetzten Texte auf die Buttons
	 */
	private void setLang()
	{
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnTitle, Translation.Get("Title"), Translation.Get("Title"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnGc, Translation.Get("GCCode"), Translation.Get("GCCode"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOwner, Translation.Get("Owner"), Translation.Get("Owner"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOnline, "Online", "Online");

		// der State muss erstmal gesetzt werden, damit die Anzeige
		// Aktuallisiert wird
		mTglBtnOnline.setState(0);

		mBtnFilter.setText(Translation.Get("Filter"));
		mBtnSearch.setText(Translation.Get("Search"));
		mBtnNext.setText(Translation.Get("Next"));
		mBtnCancel.setText(Translation.Get("abort"));

	}

	/**
	 * Schaltet den Such Modus um.
	 * 
	 * @param state
	 * <br/>
	 *            0 = Title <br/>
	 *            1 = Gc-Code <br/>
	 *            2 = Owner <br/>
	 */
	private void switchSearcheMode(int state)
	{
		mSearchState = state;

		if (state == 0)
		{
			mTglBtnTitle.setState(1);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(0);
		}
		if (state == 1)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(1);
			mTglBtnOwner.setState(0);
		}
		if (state == 2)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(1);
		}

	}

	private void textBox_TextChanged()
	{
		// reset SearchIndex, because of text changed.
		mSearchIndex = -1;

		boolean isText = mEingabe.getText().length() != 0;
		mBtnSearch.setEnable(isText);
		mBtnNext.disable();

		if (mTglBtnOnline.getState() == 0)
		{
			mBtnFilter.setEnable(isText);
		}
		else
		{
			mBtnFilter.enable();
		}

		// TODO Sofort Filter hat eine schlechte Performance, deshalb habe ich ihn ersteinmal abgeschalten.
		// Es w�re aber ein sch�nes Feature!
		// filterSearchByTextChnge();
	}

	/**
	 * schliesst die virtuelle Tastertur
	 */
	private void closeSoftKeyPad()
	{
		// close the virtual keyboard
		// InputMethodManager mgr = (InputMethodManager) mPtrMain.getSystemService(Context.INPUT_METHOD_SERVICE);
		// mgr.hideSoftInputFromWindow(mEingabe.getWindowToken(), 0);
	}

	/**
	 * Die aktive CahcheList wird durchsucht gefilterte Caches werden dabei nicht ber�cksichtigt.
	 * 
	 * @param ignoreOnlineSearch
	 *            (True, wenn Lokal gesucht werden soll, obwohl der MultiToggleButton "Online" aktiviert ist.
	 */
	private void searchNow(boolean ignoreOnlineSearch)
	{

		if (ignoreOnlineSearch || mTglBtnOnline.getState() == 0)
		{

			mSearchIndex++;

			// Replase LineBreaks

			String searchPattern = mEingabe.getText().toLowerCase();

			// Replase LineBreaks
			searchPattern = searchPattern.replace("\n", "");
			searchPattern = searchPattern.replace("\r", "");

			boolean criterionMatches = false;

			synchronized (Database.Data.Query)
			{

				if (!mSearchAktive)
				{
					CacheListIterator = Database.Data.Query.iterator();
					mSearchAktive = true;
				}

				Cache tmp = null;
				while (CacheListIterator.hasNext() && !criterionMatches)
				{

					tmp = CacheListIterator.next();

					switch (mSearchState)
					{
					case 0:
						criterionMatches = tmp.Name.toLowerCase().contains(searchPattern);
						break;
					case 1:
						criterionMatches = tmp.GcCode.toLowerCase().contains(searchPattern);
						break;
					case 2:
						criterionMatches = tmp.Owner.toLowerCase().contains(searchPattern)
								|| tmp.PlacedBy.toLowerCase().contains(searchPattern);
						break;
					}

					if (!criterionMatches) mSearchIndex++;
				}

				if (!criterionMatches)
				{
					mBtnNext.disable();
					mSearchAktive = false;
					GL_MsgBox.Show(Translation.Get("NoCacheFound"), Translation.Get("search"), MessageBoxButtons.OK,
							MessageBoxIcon.Asterisk, null);
				}
				else
				{

					Waypoint finalWp = null;
					if (tmp != null)
					{
						if (tmp.HasFinalWaypoint()) finalWp = tmp.GetFinalWaypoint();
						else if (tmp.HasStartWaypoint()) finalWp = tmp.GetStartWaypoint();
						GlobalCore.setSelectedWaypoint(tmp, finalWp);
					}
					// deactivate autoResort when Cache is selected by hand
					GlobalCore.autoResort = false;

					mBtnNext.enable();

				}
			}
		}
		else
		{
			searchAPI();
		}

	}

	/**
	 * setzt bei Eingabe eines Zeichens die CacheListItems auf Sichtbar oder unsichtbar
	 */
	private void filterSearchByTextChnge()
	{
		if (!Config.settings.dynamicFilterAtSearch.getValue()) return;
		if (CacheListView.that == null) return;
		if (mTglBtnOnline.getState() == 1)
		{
			// nicht bei Online Suche
			clearSearchFilter();
			return;
		}

		String searchPattern = mEingabe.getText().toLowerCase();

		// Replase LineBreaks
		searchPattern = searchPattern.replace("\n", "");
		searchPattern = searchPattern.replace("\r", "");

		synchronized (Database.Data.Query)
		{
			for (Cache cache : Database.Data.Query)
			{
				boolean set = true;
				switch (mSearchState)
				{
				case 0:
					set = cache.Name.toLowerCase().contains(searchPattern);
					break;
				case 1:
					set = cache.GcCode.toLowerCase().contains(searchPattern);
					break;
				case 2:
					set = cache.Owner.toLowerCase().contains(searchPattern) || cache.PlacedBy.toLowerCase().contains(searchPattern);
					break;
				}

				cache.setSearchVisible(set);
			}
		}
		CacheListView.that.getListView().setHasInvisibleItems(true);
		CacheListView.that.CacheListChangedEvent();
	}

	private void clearSearchFilter()
	{
		if (!Config.settings.dynamicFilterAtSearch.getValue()) return;
		synchronized (Database.Data.Query)
		{
			for (Cache cache : Database.Data.Query)
			{
				cache.setSearchVisible(true);
			}
		}
		if (CacheListView.that != null)
		{
			CacheListView.that.getListView().setHasInvisibleItems(false);
			CacheListView.that.CacheListChangedEvent();
		}
	}

	/**
	 * Sucht mit den Vorgaben nach Caches �ber die API. Die Gefundenen Caches werden in die DB eingetragen und im Anschluss wird der lokale
	 * Suchvorgang gestartet.
	 */
	private void searchAPI()
	{
		if (!GroundspeakAPI.isValidAPI_Key(true))
		{
			GL_MsgBox
					.Show(Translation.Get("apiKeyNeeded"), Translation.Get("Clue"), MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
		}
		else
		{

			wd = CancelWaitDialog.ShowWait(Translation.Get("chkApiState"), new IcancelListner()
			{

				@Override
				public void isCanceld()
				{
					closeWaitDialog();
				}
			}, new Runnable()
			{

				@Override
				public void run()
				{
					int ret = GroundspeakAPI.GetMembershipType(Config.GetAccessToken());
					if (ret == 3)
					{
						closeWaitDialog();
						searchOnlineNow();
					}
					else
					{
						GL_MsgBox.Show(Translation.Get("GC_basic"), Translation.Get("GC_title"), MessageBoxButtons.OKCancel,
								MessageBoxIcon.Powerd_by_GC_Live, new OnMsgBoxClickListener()
								{

									@Override
									public boolean onClick(int which, Object data)
									{
										if (which == GL_MsgBox.BUTTON_POSITIVE) searchOnlineNow();
										else
											closeWaitDialog();
										return true;
									}
								});
					}

				}
			});

		}
	}

	CancelWaitDialog wd = null;

	private void closeWaitDialog()
	{
		if (wd != null) wd.close();
	}

	private void searchOnlineNow()
	{
		wd = CancelWaitDialog.ShowWait(Translation.Get("searchOverAPI"), new IcancelListner()
		{

			@Override
			public void isCanceld()
			{
				closeWaitDialog();
			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				String accessToken = Config.GetAccessToken();

				Coordinate searchCoord = null;

				if (MapView.that != null && MapView.that.isVisible())
				{
					searchCoord = MapView.that.center;
				}
				else
				{
					searchCoord = Locator.getCoordinate();
				}

				if (searchCoord == null)
				{
					return;
				}

				// alle per API importierten Caches landen in der Category und
				// GpxFilename
				// API-Import
				// Category suchen, die dazu geh�rt
				CategoryDAO categoryDAO = new CategoryDAO();
				Category category = categoryDAO.GetCategory(GlobalCore.Categories, "API-Import");
				if (category == null) return; // should not happen!!!

				GpxFilename gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
				if (gpxFilename == null) return;

				ArrayList<Cache> apiCaches = new ArrayList<Cache>();
				ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
				ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

				CB_Core.Api.SearchForGeocaches.Search searchC = null;

				String searchPattern = mEingabe.getText().toLowerCase();

				// * 0 = Title <br/>
				// * 1 = Gc-Code <br/>
				// * 2 = Owner <br/>

				switch (mSearchState)
				{
				case 0:
					CB_Core.Api.SearchForGeocaches.SearchGCName searchCName = new CB_Core.Api.SearchForGeocaches.SearchGCName();
					searchCName.pos = searchCoord;
					searchCName.distanceInMeters = 5000000;
					searchCName.number = 50;
					searchCName.gcName = searchPattern;
					searchC = searchCName;
					break;

				case 1:
					CB_Core.Api.SearchForGeocaches.SearchGC searchCGC = new CB_Core.Api.SearchForGeocaches.SearchGC();
					searchCGC.gcCode = searchPattern;
					searchCGC.number = 1;
					searchC = searchCGC;
					break;

				case 2:
					CB_Core.Api.SearchForGeocaches.SearchGCOwner searchCOwner = new CB_Core.Api.SearchForGeocaches.SearchGCOwner();
					searchCOwner.OwnerName = searchPattern;
					searchCOwner.number = 50;
					searchCOwner.pos = searchCoord;
					searchCOwner.distanceInMeters = 5000000;

					searchC = searchCOwner;
					break;
				}

				if (searchC == null)
				{

					return;
				}

				CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, apiImages, gpxFilename.Id);

				if (apiCaches.size() > 0)
				{
					Database.Data.beginTransaction();

					CacheDAO cacheDAO = new CacheDAO();
					LogDAO logDAO = new LogDAO();
					ImageDAO imageDAO = new ImageDAO();
					WaypointDAO waypointDAO = new WaypointDAO();

					int counter = 0;

					synchronized (Database.Data.Query)
					{

						for (Cache cache : apiCaches)
						{
							counter++;
							cache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, cache.Longitude());
							cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, cache.Latitude());
							if (Database.Data.Query.GetCacheById(cache.Id) == null)
							{
								Database.Data.Query.add(cache);

								cacheDAO.WriteToDatabase(cache);

								for (LogEntry log : apiLogs)
								{
									if (log.CacheId != cache.Id) continue;
									// Write Log to database
									logDAO.WriteToDatabase(log);
								}

								for (ImageEntry image : apiImages)
								{
									if (image.CacheId != cache.Id) continue;
									// Write Image to database
									imageDAO.WriteToDatabase(image, false);
								}

								for (Waypoint waypoint : cache.waypoints)
								{
									waypointDAO.WriteToDatabase(waypoint);
								}
							}
						}
					}
					Database.Data.setTransactionSuccessful();
					Database.Data.endTransaction();

					Database.Data.GPXFilenameUpdateCacheCount();

					CachListChangedEventList.Call();

					if (counter == 1)
					{
						// select this Cache
						Cache cache = Database.Data.Query.GetCacheById(apiCaches.get(0).Id);
						GlobalCore.setSelectedCache(cache);
					}

				}
				closeWaitDialog();
			}
		});
	}

	/**
	 * setzt den Filter auf die Such Anfrage
	 */
	private void setFilter()
	{
		String searchPattern = mEingabe.getText().toLowerCase();

		String where = "";
		if (GlobalCore.LastFilter.toString().length() > 0) where = " AND (";

		GlobalCore.LastFilter.filterName = "";
		GlobalCore.LastFilter.filterGcCode = "";
		GlobalCore.LastFilter.filterOwner = "";

		if (mSearchState == 0) GlobalCore.LastFilter.filterName = searchPattern;
		else if (mSearchState == 1) GlobalCore.LastFilter.filterGcCode = searchPattern;
		if (mSearchState == 2) GlobalCore.LastFilter.filterOwner = searchPattern;

		ApplyFilter();
	}

	private FilterProperties props;

	public void ApplyFilter()
	{
		ApplyFilter(GlobalCore.LastFilter);
	}

	public void ApplyFilter(FilterProperties filter)
	{
		EditFilterSettings.ApplyFilter(filter);
	}

	public enum searchMode
	{
		Titel, GcCode, Owner
	}

	public void addSearch(String searchPattern, searchMode Mode)
	{

		Logger.DEBUG("addSearch " + searchPattern);

		mEingabe.setText(searchPattern);
		switchSearcheMode(Mode.ordinal());

		// auf Online schalten
		mTglBtnOnline.setState(1);
		setFilterBtnState();

		// Suche ausl�sen
		mBtnSearch.performClick();
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void onShow()
	{
		if (GlobalCore.isTab)
		{
			// TODO searchDialog plaziere rechts neben der Cache List
		}
		else
		{
			if (CacheListView.that != null)
			{
				setY(CacheListView.that.getMaxY() - this.height);
				CacheListView.that.setTopPlaceHolder(this.height);
			}
		}
		if (!GL.that.PopUpIsShown()) that.showNotCloseAutomaticly();

		Slider.that.registerPosChangedEvent(listner);
	}

	@Override
	public void onHide()
	{
		Slider.that.removePosChangedEvent(listner);

		if (!GlobalCore.isTab)
		{
			if (CacheListView.that != null)
			{
				CacheListView.that.resetPlaceHolder();
			}
		}
	}

	private YPositionChanged listner = new YPositionChanged()
	{

		@Override
		public void Position(float SliderTop, float SliderBottom)
		{
			if (GlobalCore.isTab)
			{
				// TODO plaziere rechts neben der Cache List
			}
			else
			{
				setY(CacheListView.that.getMaxY() - that.height);
			}

		}
	};

	CancelWaitDialog WD;
	GL_MsgBox MSB;

	private void askPremium()
	{

		if (!GroundspeakAPI.isValidAPI_Key(true))
		{
			GL_MsgBox
					.Show(Translation.Get("apiKeyNeeded"), Translation.Get("Clue"), MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
		}
		else
		{

			WD = CancelWaitDialog.ShowWait(Translation.Get("chkApiState"), new IcancelListner()
			{

				@Override
				public void isCanceld()
				{
					// TODO Handle Cancel

				}
			}, new Runnable()
			{

				@Override
				public void run()
				{
					int ret = GroundspeakAPI.GetMembershipType(Config.GetAccessToken());
					if (ret == 3)
					{
						// searchOnlineNow();
						showTargetApiDialog();
					}
					else
					{
						closeWD();

						GL.that.RunOnGL(new runOnGL()
						{

							@Override
							public void run()
							{
								MSB = GL_MsgBox.Show(Translation.Get("GC_basic"), Translation.Get("GC_title"), MessageBoxButtons.OKCancel,
										MessageBoxIcon.Powerd_by_GC_Live, new OnMsgBoxClickListener()
										{

											@Override
											public boolean onClick(int which, Object data)
											{
												closeMsgBox();
												if (which == GL_MsgBox.BUTTON_POSITIVE)
												{
													showTargetApiDialog();
												}

												return true;
											}
										});
							}
						});

					}
				}
			});

		}

	}

	private void closeMsgBox()
	{
		MSB.close();
	}

	private void closeWD()
	{
		if (WD != null) WD.close();
	}

	private void showTargetApiDialog()
	{
		GL.that.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				new SearchOverPosition().show();
			}
		});

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		lastPoint = new Vector2(this.Pos.x, this.Pos.y);
		return true;
	}

	private Vector2 lastPoint;

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		// TODO SearchDialog verschiebbar machen auf dem Tablett

		// if (KineticPan) return true;
		//
		// int dx = (int) (lastPoint.x - x);
		// int dy = (int) (y - lastPoint.y);
		//
		// this.setX(this.getX() + dx);
		// this.setY(this.getY() + dy);
		//
		// lastPoint.x = this.getX();
		// lastPoint.y = this.getY();
		return true;
	}

	@Override
	public void dispose()
	{
		super.dispose();
		that = null;
	}

}
