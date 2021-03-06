package CB_UI_Base;

import CB_UI_Base.GL_UI.DisplayType;
import CB_Utils.Plattform;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Clipboard;

public abstract class Global {
    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");
    public static boolean forcePhone = true;
    public static boolean useSmallSkin = false;
    public static DisplayType displayType = DisplayType.Normal;
    public static double displayDensity = 1;
    protected static Global Instance;
    private static Clipboard defaultClipBoard;

    protected Global() {
        Instance = this;
    }

    public static boolean isTestVersion() {
        return false;
    }

    public static FileHandle getInternalFileHandle(String path) {
        if (Plattform.used == Plattform.undef)
            throw new IllegalArgumentException("Platform not def");

        if (Plattform.used == Plattform.Android) {
            return Gdx.files.internal(path);
        } else {
            FileHandle ret=Gdx.files.classpath(path);

            if(ret!=null&!ret.exists()){
                //try internal
                ret=Gdx.files.internal(path);
            }

            return ret;
        }
    }

    public static Clipboard getDefaultClipboard() {
        if (defaultClipBoard == null) {
            return null;
        } else {
            return defaultClipBoard;
        }
    }

    public static void setDefaultClipboard(Clipboard clipBoard) {
        defaultClipBoard = clipBoard;
    }

    protected abstract String getVersionPrefix();

}
