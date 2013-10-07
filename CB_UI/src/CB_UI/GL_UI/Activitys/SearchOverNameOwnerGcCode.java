package CB_UI.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.CoreSettingsForward;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Views.MapView;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class SearchOverNameOwnerGcCode extends ActivityBase
{
	private Button bImport, bCancel;
	private Label lblTitle, lblExcludeFounds, lblOnlyAvible, lblExcludeHides;
	private Image gsLogo;
	private chkBox checkBoxExcludeFounds, checkBoxOnlyAvible, checkBoxExcludeHides;

	/**
	 * Such Eingabe Feld
	 */
	private EditTextField mEingabe;
	private float lineHeight;

	private volatile Thread thread;
	private ImportAnimation dis;
	private Box box;
	private boolean importRuns = false;
	private SearchType actSearchType = null;

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

	private enum SearchType
	{
		Name, Owner, GC_Code
	}

	private static SearchOverNameOwnerGcCode that;

	public static SearchOverNameOwnerGcCode ShowInstanz()
	{
		if (that == null)
		{
			new SearchOverNameOwnerGcCode();
		}
		that.show();
		return that;
	}

	public SearchOverNameOwnerGcCode()
	{
		super(ActivityRec(), "searchOverPosActivity");
		that = this;
		lineHeight = UI_Size_Base.that.getButtonHeight();

		createOkCancelBtn();
		createBox();
		createTitleLine();
		createChkBoxLines();
		createtoggleButtonLine();
		initialContent();
	}

	@Override
	public void onShow()
	{
		textBox_TextChanged();
	}

	private void createOkCancelBtn()
	{
		bImport = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
		bCancel = new Button(bImport.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

		// Translations
		bImport.setText(Translation.Get("import"));
		bCancel.setText(Translation.Get("cancel"));

		this.addChild(bImport);
		bImport.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				ImportNow();
				return true;
			}

		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (importRuns)
				{

					cancelImport();

				}
				else
				{
					finish();
				}
				return true;
			}

		});

	}

	private void cancelImport()
	{

		// breche den Import Thread ab
		if (thread != null) thread.interrupt();

		importRuns = false;
		this.removeChildsDirekt(dis);
		dis.dispose();
		dis = null;

	}

	private void createBox()
	{
		box = new Box(ActivityRec(), "ScrollBox");
		this.addChild(box);
		box.setHeight(this.height - lineHeight - bImport.getMaxY() - margin - margin);
		box.setY(bImport.getMaxY() + margin);
		box.setBackground(this.getBackground());
	}

	private void createTitleLine()
	{

		float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

		gsLogo = new Image(innerWidth - margin - lineHeight, this.height - this.getTopHeight() - lineHeight - margin, lineHeight,
				lineHeight, "");
		gsLogo.setDrawable(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal())));
		this.addChild(gsLogo);

		lblTitle = new Label(leftBorder + margin, this.height - this.getTopHeight() - lineHeight - margin, innerWidth - (margin * 4)
				- gsLogo.getWidth(), lineHeight, "TitleLabel");
		lblTitle.setWrapType(WrapType.WRAPPED);
		lblTitle.setFont(Fonts.getBig());
		lblTitle.setWrappedText(Translation.Get("API_IMPORT_NAME_OWNER_CODE"));
		this.addChild(lblTitle);

	}

	private void createChkBoxLines()
	{
		checkBoxOnlyAvible = new chkBox("");
		checkBoxOnlyAvible.setPos(margin, box.getHeight() - margin - checkBoxOnlyAvible.getHeight());
		box.addChild(checkBoxOnlyAvible);

		checkBoxExcludeHides = new chkBox("");
		checkBoxExcludeHides.setPos(margin, checkBoxOnlyAvible.getY() - margin - checkBoxExcludeHides.getHeight());
		box.addChild(checkBoxExcludeHides);

		checkBoxExcludeFounds = new chkBox("");
		checkBoxExcludeFounds.setPos(margin, checkBoxExcludeHides.getY() - margin - checkBoxExcludeFounds.getHeight());
		box.addChild(checkBoxExcludeFounds);

		lblOnlyAvible = new Label(checkBoxOnlyAvible, Translation.Get("SearchOnlyAvible"));
		lblOnlyAvible.setX(checkBoxOnlyAvible.getMaxX() + margin);
		lblOnlyAvible.setWidth(this.width - margin - checkBoxOnlyAvible.getMaxX() - margin);
		box.addChild(lblOnlyAvible);

		lblExcludeHides = new Label(checkBoxExcludeHides, Translation.Get("SearchWithoutOwns"));
		lblExcludeHides.setX(checkBoxOnlyAvible.getMaxX() + margin);
		lblExcludeHides.setWidth(this.width - margin - checkBoxExcludeHides.getMaxX() - margin);
		box.addChild(lblExcludeHides);

		lblExcludeFounds = new Label(checkBoxExcludeFounds, Translation.Get("SearchWithoutFounds"));
		lblExcludeFounds.setX(checkBoxOnlyAvible.getMaxX() + margin);
		lblExcludeFounds.setWidth(this.width - margin - checkBoxExcludeFounds.getMaxX() - margin);
		box.addChild(lblExcludeFounds);

	}

	private void createtoggleButtonLine()
	{
		CB_RectF rec = new CB_RectF(0, 0, box.getWidth() - (margin * 2), UI_Size_Base.that.getButtonHeight());

		Box line = new Box(rec, "ToggLeButtonLine");

		line.setHeight(UI_Size_Base.that.getButtonHeight() * 2 + margin);

		mTglBtnTitle = new MultiToggleButton(rec, "mTglBtnTitle");
		mTglBtnGc = new MultiToggleButton(rec, "mTglBtnGc");
		mTglBtnOwner = new MultiToggleButton(rec, "mTglBtnOwner");

		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnTitle, Translation.Get("Title"), Translation.Get("Title"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnGc, Translation.Get("GCCode"), Translation.Get("GCCode"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOwner, Translation.Get("Owner"), Translation.Get("Owner"));

		line.initRow(true);
		line.addNext(mTglBtnTitle);
		line.addNext(mTglBtnGc);
		line.addLast(mTglBtnOwner);

		line.setY(checkBoxExcludeFounds.getY() - margin - line.getHeight());
		line.setX(margin);

		mEingabe = new EditTextField(this, rec, WrapType.SINGLELINE, "");

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

		line.addLast(mEingabe);

		box.addChild(line);
	}

	private void initialContent()
	{
		textBox_TextChanged();
		switchSearcheMode(0);
		checkBoxExcludeFounds.setChecked(Config.SearchWithoutFounds.getValue());
		checkBoxOnlyAvible.setChecked(Config.SearchOnlyAvible.getValue());
		checkBoxExcludeHides.setChecked(Config.SearchWithoutOwns.getValue());

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
	}

	private void ImportNow()
	{

		Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
		Config.SearchOnlyAvible.setValue(checkBoxOnlyAvible.isChecked());
		Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

		Config.AcceptChanges();

		bImport.disable();

		// disable UI
		dis = new ImportAnimation(box);
		dis.setBackground(getBackground());

		this.addChild(dis, false);

		importRuns = true;
		thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				boolean threadCanceld = false;

				try
				{
					if (actSearchType != null)
					{

						// alle per API importierten Caches landen in der Category und
						// GpxFilename
						// API-Import
						// Category suchen, die dazu geh�rt
						CategoryDAO categoryDAO = new CategoryDAO();
						Category category = categoryDAO.GetCategory(CoreSettingsForward.Categories, "API-Import");
						if (category != null) // should not happen!!!
						{
							GpxFilename gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
							if (gpxFilename != null)
							{
								ArrayList<Cache> apiCaches = new ArrayList<Cache>();
								ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
								ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
								CB_UI.Api.SearchForGeocaches.Search searchC = null;

								String searchPattern = mEingabe.getText().toLowerCase();

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

								// * 0 = Title <br/>
								// * 1 = Gc-Code <br/>
								// * 2 = Owner <br/>

								switch (actSearchType)
								{
								case Name:
									CB_UI.Api.SearchForGeocaches.SearchGCName searchCName = new CB_UI.Api.SearchForGeocaches.SearchGCName();
									searchCName.distanceInMeters = 5000000;
									searchCName.pos = searchCoord;
									searchCName.number = 50;
									searchCName.gcName = searchPattern;
									searchC = searchCName;
									break;

								case GC_Code:
									CB_UI.Api.SearchForGeocaches.SearchGC searchCGC = new CB_UI.Api.SearchForGeocaches.SearchGC();
									searchCGC.gcCode = searchPattern;
									searchCGC.number = 1;
									searchC = searchCGC;
									break;

								case Owner:
									CB_UI.Api.SearchForGeocaches.SearchGCOwner searchCOwner = new CB_UI.Api.SearchForGeocaches.SearchGCOwner();
									searchCOwner.OwnerName = searchPattern;
									searchCOwner.number = 50;
									searchCOwner.pos = searchCoord;
									searchCOwner.distanceInMeters = 5000000;

									searchC = searchCOwner;
									break;
								}

								if (searchC == null) return;
								searchC.excludeFounds = Config.SearchWithoutFounds.getValue();
								searchC.excludeHides = Config.SearchWithoutOwns.getValue();
								searchC.available = Config.SearchOnlyAvible.getValue();

								dis.setAnimationType(AnimationType.Download);
								CB_UI.Api.SearchForGeocaches.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, gpxFilename.Id);
								dis.setAnimationType(AnimationType.Work);
								if (apiCaches.size() > 0)
								{
									GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
								}

							}
						}
					}
				}
				catch (InterruptedException e)
				{
					// Thread abgebrochen!
					threadCanceld = true;
				}

				// Delete all LongDescription from Query! LongDescription is Loading by showing DescriptionView direct from DB
				for (Cache cache : Database.Data.Query)
				{
					cache.longDescription = "";
				}

				if (!threadCanceld)
				{
					CachListChangedEventList.Call();
					cancelImport();
					finish();
				}
				else
				{

					// Notify Map
					if (MapView.that != null) MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);

					bImport.enable();
				}
				importRuns = false;
			}

		});

		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();

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

		if (state == 0)
		{
			mTglBtnTitle.setState(1);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(0);
			actSearchType = SearchType.Name;
		}
		if (state == 1)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(1);
			mTglBtnOwner.setState(0);
			actSearchType = SearchType.GC_Code;
		}
		if (state == 2)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(1);
			actSearchType = SearchType.Owner;
		}

	}

	private void textBox_TextChanged()
	{

		boolean isText = mEingabe.getText().length() != 0;
		bImport.setEnable(isText);

	}

}
