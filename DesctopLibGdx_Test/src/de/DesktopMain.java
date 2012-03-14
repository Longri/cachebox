package de;

import java.util.Timer;
import java.util.TimerTask;
import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.Math.Size;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;
import CB_Core.GL_UI.ViewConst;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class DesktopMain {

	
	static GL_Listener CB_UI;
	
	private static Size myInitialSize = null;


	static devicesSizes ui = null;

	public static void main(String[] args) {
		DesktopLogger iLogger = new DesktopLogger();

//		iniPhone();
		iniTab();

		InitalConfig();
		Config.settings.MapViewDPIFaktor.setValue(1);

		CB_UI = new Desktop_GL_Listner(myInitialSize.width,
				myInitialSize.height);

		GL_View_Base.debug = true;

		int sw = myInitialSize.height > myInitialSize.width ? myInitialSize.width
				: myInitialSize.height;
		sw /= ui.Density;

		// chek if tablet

		GlobalCore.isTab = sw > 400 ? true : false;

		UiSizes.initial(ui);
		new LwjglApplication(CB_UI, "Game", myInitialSize.width,
				myInitialSize.height, false);

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Run();
			}
		};
		timer.schedule(task, 1000);

	}
	
	
	private static void iniPhone() {
		myInitialSize = new Size(480, 800);
//		myInitialSize = new Size(240, 400);

		ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.5f;
		ui.ButtonSize = new Size(65, 65);
		ui.RefSize = 64;
		ui.TextSize_Normal = 52;
		ui.ButtonTextSize = 50;
		ui.IconSize = 13;
		ui.Margin = 4;
		ui.ArrowSizeList = 11;
		ui.ArrowSizeMap = 18;
		ui.TB_IconSize = 8;
		ui.isLandscape = false;

	}

	private static void iniTab() {

		myInitialSize = new Size(1280, 750);
		ui = new devicesSizes();

		ui.Window = myInitialSize;
		ui.Density = 1.0f;
		ui.ButtonSize = new Size(65, 65);
		ui.RefSize = 64;
		ui.TextSize_Normal = 52;
		ui.ButtonTextSize = 50;
		ui.IconSize = 13;
		ui.Margin = 4;
		ui.ArrowSizeList = 11;
		ui.ArrowSizeMap = 18;
		ui.TB_IconSize = 8;
		ui.isLandscape = false;

	}

	

	private static void Run() {
		CB_UI.onStart();
//		CB_UI.setGLViewID(ViewConst.MAP_CONTROL_TEST_VIEW);
		CB_UI.setGLViewID(ViewConst.TEST_VIEW);
		// CB_UI.setGLViewID(ViewConst.CREDITS_VIEW);
//		 CB_UI.setGLViewID(ViewConst.GL_MAP_VIEW);
//		CB_UI.setGLViewID(ViewConst.ViewConst.ABOUT_VIEW);
		CB_UI.setGLViewID(ViewConst.TEST_LIST_VIEW);

		Gdx.input.setInputProcessor((InputProcessor) CB_UI);

	}

	/**
	 * Initialisiert die Config f�r die Tests! initialisiert wird die Config mit
	 * der unter Testdata abgelegten config.db3
	 */
	public static void InitalConfig() {

		if (Config.settings != null && Config.settings.isLoaded())
			return;

		// Read Config
		String workPath = "./testdata";

		Config.Initialize(workPath, workPath + "/cachebox.config");

		// hier muss die Config Db initialisiert werden
		try {
			Database.Settings = new TestDB(DatabaseType.Settings);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (!FileIO.DirectoryExists(Config.WorkPath))
			return;
		Database.Settings.StartUp(Config.WorkPath + "/Config.db3");
		Config.settings.ReadFromDB();
		Config.AcceptChanges();
	}

}
