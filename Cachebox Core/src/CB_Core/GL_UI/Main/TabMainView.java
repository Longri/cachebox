package CB_Core.GL_UI.Main;

import java.io.File;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Controls.Slider;
import CB_Core.GL_UI.Main.CB_ActionButton.GestureDirection;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowAbout;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowActivity;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowCacheList;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowCompassView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowCreditsView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowDescriptionView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowFieldNotesView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowHint;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowJokerView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowLogView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowNotesView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSolverView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSolverView2;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSpoilerView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowTestView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowTrackListView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowTrackableListView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowWaypointView;
import CB_Core.GL_UI.Views.AboutView;
import CB_Core.GL_UI.Views.CacheListView;
import CB_Core.GL_UI.Views.CompassView;
import CB_Core.GL_UI.Views.CreditsView;
import CB_Core.GL_UI.Views.DescriptionView;
import CB_Core.GL_UI.Views.FieldNotesView;
import CB_Core.GL_UI.Views.JokerView;
import CB_Core.GL_UI.Views.LogView;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.GL_UI.Views.NotesView;
import CB_Core.GL_UI.Views.SolverView;
import CB_Core.GL_UI.Views.SolverView2;
import CB_Core.GL_UI.Views.SpoilerView;
import CB_Core.GL_UI.Views.TrackListView;
import CB_Core.GL_UI.Views.TrackableListView;
import CB_Core.GL_UI.Views.WaypointView;
import CB_Core.GL_UI.Views.TestViews.TestView;
import CB_Core.Map.RouteOverlay;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.Color;

public class TabMainView extends MainViewBase
{
	// private CB_Action actionTest;
	private CB_Action_ShowTestView actionTestView;
	private CB_Action_ShowHint actionShowHint;
	private CB_Action_ShowMap actionShowMap;
	private CB_Action_ShowCacheList actionShowCacheList;

	private CB_Action_ShowAbout actionShowAboutView;
	private CB_Action_ShowCompassView actionShowCompassView;
	private CB_Action_ShowCreditsView actionShowCreditsView;
	private CB_Action_ShowDescriptionView actionShowDescriptionView;
	private CB_Action_ShowFieldNotesView actionShowFieldNotesView;
	private CB_Action_ShowJokerView actionShowJokerView;
	private CB_Action_ShowLogView actionShowLogView;
	private CB_Action_ShowNotesView actionShowNotesView;
	private CB_Action_ShowSolverView actionShowSolverView;
	private CB_Action_ShowSolverView2 actionShowSolverView2;
	private CB_Action_ShowSpoilerView actionShowSpoilerView;
	private CB_Action_ShowTrackableListView actionShowTrackableListView;
	private CB_Action_ShowTrackListView actionShowTrackListView;
	private CB_Action_ShowWaypointView actionShowWaypointView;
	private CB_Action_ShowActivity actionShowSettings;
	private CB_Action_ShowActivity actionNavigateTo1;
	private CB_Action_ShowActivity actionNavigateTo2;

	private CB_Action_ShowActivity actionRecTrack;
	private CB_Action_ShowActivity actionRecVoice;
	private CB_Action_ShowActivity actionRecPicture;
	private CB_Action_ShowActivity actionRecVideo;
	private CB_Action_ShowActivity actionDelCaches;
	private CB_Action_ShowActivity actionParking;

	private CB_Action_ShowActivity actionDayNight;
	private CB_Action_ShowActivity actionScreenLock;
	private CB_Action_ShowActivity actionClose;

	public static MapView mapView = null;
	public static CacheListView cacheListView = null;
	public static AboutView aboutView = null;
	public static CompassView compassView = null;
	public static CreditsView creditsView = null;
	public static DescriptionView descriptionView = null;
	public static FieldNotesView fieldNotesView = null;
	public static JokerView jokerView = null;
	public static LogView logView = null;
	public static NotesView notesView = null;
	public static SolverView solverView = null;
	public static SpoilerView spoilerView = null;
	public static TrackableListView trackableListView = null;
	public static TrackListView trackListView = null;
	public static WaypointView waypointView = null;
	public static TestView testView = null;
	public static SolverView2 solverView2 = null;

	public TabMainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);

		mainView = this;

	}

	@Override
	protected void Initial()
	{
		// Wird inerhalb des ersten Render Vorgangs aufgerufen.

		// eine Initialisierung der actions kommt hier zu sp�t, daher als Aufruf aus dem Constructor verschoben!

		ini();

	}

	private void ini()
	{
		// actionTest = new CB_ActionCommand("Test", CB_Action.AID_TEST1);
		// actionTest2 = new CB_ActionCommand("Test2", CB_Action.AID_TEST1);

		actionShowMap = new CB_Action_ShowMap();
		actionShowHint = new CB_Action_ShowHint();
		actionShowCacheList = new CB_Action_ShowCacheList();

		actionShowAboutView = new CB_Action_ShowAbout();
		actionShowCompassView = new CB_Action_ShowCompassView();
		actionShowCreditsView = new CB_Action_ShowCreditsView();
		actionShowDescriptionView = new CB_Action_ShowDescriptionView();
		actionShowFieldNotesView = new CB_Action_ShowFieldNotesView();
		actionShowJokerView = new CB_Action_ShowJokerView();
		actionShowLogView = new CB_Action_ShowLogView();
		actionShowNotesView = new CB_Action_ShowNotesView();
		actionShowSolverView = new CB_Action_ShowSolverView();
		actionShowSolverView2 = new CB_Action_ShowSolverView2();
		actionShowSpoilerView = new CB_Action_ShowSpoilerView();
		actionShowTrackableListView = new CB_Action_ShowTrackableListView();
		actionShowTrackListView = new CB_Action_ShowTrackListView();
		actionShowWaypointView = new CB_Action_ShowWaypointView();
		actionTestView = new CB_Action_ShowTestView();

		actionShowSettings = new CB_Action_ShowActivity("settings", CB_Action.AID_SHOW_SETTINGS, ViewConst.SETTINGS,
				SpriteCache.Icons.get(26));

		actionNavigateTo1 = actionNavigateTo2 = new CB_Action_ShowActivity("NavigateTo", CB_Action.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO,
				SpriteCache.Icons.get(46));

		actionRecTrack = new CB_Action_ShowActivity("TrackRec", CB_Action.AID_TRACK_REC, ViewConst.TRACK_REC, null);
		actionRecVoice = new CB_Action_ShowActivity("VoiceRec", CB_Action.AID_VOICE_REC, ViewConst.VOICE_REC, SpriteCache.Icons.get(11));
		actionRecPicture = new CB_Action_ShowActivity("TakePhoto", CB_Action.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO,
				SpriteCache.Icons.get(47));
		actionRecVideo = new CB_Action_ShowActivity("RecVideo", CB_Action.AID_VIDEO_REC, ViewConst.VIDEO_REC, SpriteCache.Icons.get(10));
		actionDelCaches = new CB_Action_ShowActivity("DeleteCaches", CB_Action.AID_DELETE_CACHES, ViewConst.DELETE_CACHES,
				SpriteCache.Icons.get(28));
		actionParking = new CB_Action_ShowActivity("Parking", CB_Action.AID_PARKING, ViewConst.PARKING, SpriteCache.BigIcons.get(20));

		actionDayNight = new CB_Action_ShowActivity("DayNight", CB_Action.AID_DAY_NIGHT, ViewConst.DAY_NIGHT, SpriteCache.Icons.get(48));
		actionScreenLock = new CB_Action_ShowActivity("screenlock", CB_Action.AID_LOCK, ViewConst.LOCK, SpriteCache.Icons.get(14));
		actionClose = new CB_Action_ShowActivity("quit", CB_Action.AID_QUIT, ViewConst.QUIT, SpriteCache.Icons.get(31));

		if (GlobalCore.isTab) addTabletTabs();
		else
			addPhoneTab();

		// add Slider as last
		Slider slider = new Slider(this, "Slider");
		this.addChild(slider);

		autoLoadTrack();
	}

	private void addPhoneTab()
	{
		// nur ein Tab

		// mit f�nf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.getInfoSliderHeight());
		rec.setPos(0, 0);

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CB_Button btn1 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, Tab);
		actionShowCacheList.setTab(this, Tab);

		actionShowAboutView.setTab(this, Tab);
		actionShowCompassView.setTab(this, Tab);
		actionShowCreditsView.setTab(this, Tab);
		actionShowDescriptionView.setTab(this, Tab);
		actionShowFieldNotesView.setTab(this, Tab);
		actionShowJokerView.setTab(this, Tab);
		actionShowLogView.setTab(this, Tab);
		actionShowNotesView.setTab(this, Tab);
		actionShowSolverView.setTab(this, Tab);
		actionShowSolverView2.setTab(this, Tab);
		actionShowSpoilerView.setTab(this, Tab);
		actionShowTrackableListView.setTab(this, Tab);
		actionShowTrackListView.setTab(this, Tab);
		actionShowWaypointView.setTab(this, Tab);
		actionNavigateTo1.setTab(this, Tab);

		actionRecTrack.setTab(this, Tab);
		actionRecVoice.setTab(this, Tab);
		actionRecPicture.setTab(this, Tab);
		actionRecVideo.setTab(this, Tab);
		actionDelCaches.setTab(this, Tab);
		actionParking.setTab(this, Tab);
		actionTestView.setTab(this, Tab);

		actionDayNight.setTab(this, Tab);
		actionScreenLock.setTab(this, Tab);
		actionClose.setTab(this, Tab);

		// Actions den Buttons zuweisen

		btn1.addAction(new CB_ActionButton(actionShowCacheList, true, GestureDirection.Up));
		btn1.addAction(new CB_ActionButton(actionShowTrackableListView, false, GestureDirection.Right));
		btn1.addAction(new CB_ActionButton(actionShowTrackListView, false, GestureDirection.Down));

		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true, GestureDirection.Up));
		btn2.addAction(new CB_ActionButton(actionShowWaypointView, false, GestureDirection.Right));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));
		btn2.addAction(new CB_ActionButton(actionShowSpoilerView, false));
		btn2.addAction(new CB_ActionButton(actionShowFieldNotesView, false));
		btn2.addAction(new CB_ActionButton(actionShowJokerView, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));
		btn2.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
		btn2.addAction(new CB_ActionButton(actionShowSolverView2, false));

		btn4.addAction(new CB_ActionButton(actionRecTrack, false));
		btn4.addAction(new CB_ActionButton(actionRecVoice, false));
		btn4.addAction(new CB_ActionButton(actionRecPicture, false));
		btn4.addAction(new CB_ActionButton(actionRecVideo, false));
		btn4.addAction(new CB_ActionButton(actionDelCaches, false));
		btn4.addAction(new CB_ActionButton(actionParking, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		btn3.addAction(new CB_ActionButton(actionShowCompassView, false, GestureDirection.Right));
		btn3.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
		btn3.addAction(new CB_ActionButton(actionTestView, false));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		btn5.addAction(new CB_ActionButton(actionDayNight, false));
		btn5.addAction(new CB_ActionButton(actionScreenLock, false));
		btn5.addAction(new CB_ActionButton(actionClose, false, GestureDirection.Down));

		btn5.performClick();// actionShowAboutView.Execute();
	}

	private void addTabletTabs()
	{
		addLeftForTabletsTab();
		addRightForTabletsTab();
	}

	private void addLeftForTabletsTab()
	{
		// mit f�nf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.getInfoSliderHeight());
		rec.setPos(0, 0);

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CB_Button btn1 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);
		// Tab.ShowView(new AboutView(this, "AboutView"));

		// Tab den entsprechneden Actions zuweisen
		actionShowCacheList.setTab(this, Tab);
		actionShowWaypointView.setTab(this, Tab);
		actionShowAboutView.setTab(this, Tab);
		actionShowCreditsView.setTab(this, Tab);
		actionShowTrackableListView.setTab(this, Tab);
		actionShowTrackListView.setTab(this, Tab);
		actionShowSettings.setTab(this, Tab);
		actionShowCompassView.setTab(this, Tab);
		actionShowLogView.setTab(this, Tab);
		actionShowFieldNotesView.setTab(this, Tab);
		actionShowJokerView.setTab(this, Tab);
		actionShowNotesView.setTab(this, Tab);
		actionNavigateTo1.setTab(this, Tab);

		actionRecTrack.setTab(this, Tab);
		actionRecVoice.setTab(this, Tab);
		actionRecPicture.setTab(this, Tab);
		actionRecVideo.setTab(this, Tab);
		actionDelCaches.setTab(this, Tab);
		actionParking.setTab(this, Tab);

		actionDayNight.setTab(this, Tab);
		actionScreenLock.setTab(this, Tab);
		actionClose.setTab(this, Tab);

		// Actions den Buttons zuweisen
		btn1.addAction(new CB_ActionButton(actionShowCacheList, true));
		btn1.addAction(new CB_ActionButton(actionShowTrackableListView, false));
		btn1.addAction(new CB_ActionButton(actionShowTrackListView, false));

		btn2.addAction(new CB_ActionButton(actionShowWaypointView, true, GestureDirection.Right));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));
		btn2.addAction(new CB_ActionButton(actionShowFieldNotesView, false));
		btn2.addAction(new CB_ActionButton(actionShowJokerView, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));

		btn3.addAction(new CB_ActionButton(actionShowCompassView, true, GestureDirection.Right));
		btn3.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));

		btn4.addAction(new CB_ActionButton(actionRecTrack, false));
		btn4.addAction(new CB_ActionButton(actionRecVoice, false));
		btn4.addAction(new CB_ActionButton(actionRecPicture, false));
		btn4.addAction(new CB_ActionButton(actionRecVideo, false));
		btn4.addAction(new CB_ActionButton(actionDelCaches, false));
		btn4.addAction(new CB_ActionButton(actionParking, false));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		btn5.addAction(new CB_ActionButton(actionDayNight, false));
		btn5.addAction(new CB_ActionButton(actionScreenLock, false));
		btn5.addAction(new CB_ActionButton(actionClose, false));

		btn5.performClick();// actionShowAboutView.Execute();
	}

	private void addRightForTabletsTab()
	{

		// mit f�nf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Right.getWidth());
		rec.setX(GL_UISizes.UI_Left.getWidth());
		rec.setY(0);

		rec.setHeight(this.getHeight() - UiSizes.getInfoSliderHeight());

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		// CB_Button btn1 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		// CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		// CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		// btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		// btnList.addButton(btn4);
		// btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, Tab);
		actionShowSolverView.setTab(this, Tab);
		actionShowSolverView2.setTab(this, Tab);
		actionShowDescriptionView.setTab(this, Tab);
		actionNavigateTo2.setTab(this, Tab);
		actionTestView.setTab(this, Tab);

		// Actions den Buttons zuweisen
		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true));
		btn2.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
		btn2.addAction(new CB_ActionButton(actionShowSolverView2, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		btn3.addAction(new CB_ActionButton(actionNavigateTo2, false, GestureDirection.Down));
		btn3.addAction(new CB_ActionButton(actionTestView, false));

		btn3.performClick();// actionShowMap.Execute();
	}

	private void autoLoadTrack()
	{
		RouteOverlay.Routes.clear();

		String trackPath = Config.settings.TrackFolder.getValue() + "/Autoload";
		if (FileIO.DirectoryExists(trackPath))
		{
			File dir = new File(trackPath);
			String[] files = dir.list();
			if (!(files == null))
			{
				if (files.length > 0)
				{
					for (String file : files)
					{
						LoadTrack(trackPath, file);

					}
				}
			}
		}
		else
		{
			File sddir = new File(trackPath);
			sddir.mkdirs();
		}
	}

	public void LoadTrack(String trackPath)
	{
		LoadTrack(trackPath, "");
	}

	public void LoadTrack(String trackPath, String file)
	{
		Color[] ColorField = new Color[8];
		ColorField[0] = Color.RED;
		ColorField[1] = Color.YELLOW;
		ColorField[2] = Color.BLACK;
		ColorField[3] = Color.LIGHT_GRAY;
		ColorField[4] = Color.GREEN;
		ColorField[5] = Color.BLUE;
		ColorField[6] = Color.CYAN;
		ColorField[7] = Color.GRAY;
		Color TrackColor;
		TrackColor = ColorField[(RouteOverlay.Routes.size()) % 8];

		String absolutPath = "";
		if (file.equals(""))
		{
			absolutPath = trackPath;
		}
		else
		{
			absolutPath = trackPath + "/" + file;
		}
		RouteOverlay.Routes.add(RouteOverlay.LoadRoute(absolutPath, TrackColor, Config.settings.TrackDistance.getValue()));
	}
}
