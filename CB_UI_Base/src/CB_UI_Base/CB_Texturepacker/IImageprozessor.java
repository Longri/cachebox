package CB_UI_Base.CB_Texturepacker;

import CB_Utils.fileProvider.File;
import com.badlogic.gdx.utils.Array;

public interface IImageprozessor {

    public abstract void addImage(File file);

    public abstract Array<Rect_Base> getImages();

}