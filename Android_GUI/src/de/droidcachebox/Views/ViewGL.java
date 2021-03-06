package de.droidcachebox.Views;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_Listener.GL_Listener_Interface;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.R;

import java.util.concurrent.atomic.AtomicBoolean;

public class ViewGL extends RelativeLayout implements ViewOptionsMenu, GL_Listener_Interface {
    public final static int GLSURFACE_VIEW20 = 0;
    public final static int GLSURFACE_GLSURFACE = 3;
    private static final String log = "ViewGL";
    public static View ViewGl;
    private static int mAktSurfaceType = -1;
    public GL glListener;
    AtomicBoolean isContinousRenderMode = new AtomicBoolean(true);

    @SuppressWarnings("deprecation")
    public ViewGL(Context context, LayoutInflater inflater, View glView, GL glListener) {
        super(context);
        ViewGl = glView;
        GL.that.setGL_Listener_Interface(this);
        this.glListener = glListener;
        try {

            RelativeLayout mapviewLayout = (RelativeLayout) inflater.inflate(R.layout.mapviewgl, null, false);
            this.addView(mapviewLayout);

            mapviewLayout.addView(glView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        } catch (Exception ex) {
        }
    }

    public ViewGL(Context context) {
        super(context);
    }

    public static int getSurfaceType() {
        return mAktSurfaceType;
    }

    /**
     * Setzt den OpenGl Surface Type!
     *
     * @param Type </br> GLSURFACE_VIEW20=0 </br> GLSURFACE_CUPCAKE = 1 </br> GLSURFACE_DEFAULT = 2 </br> GLSURFACE_GLSURFACE = 3
     */
    public static void setSurfaceType(int Type) {
        mAktSurfaceType = Type;
    }

    @Override
    public boolean ItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                return true;

        }
        return false;
    }

    @Override
    public void BeforeShowMenu(Menu menu) {
    }

    @Override
    public int GetMenuId() {
        return 0;
    }

    @Override
    public void OnShow() {
        CB_Utils.Log.Log.debug(log, "OnShow");
        // GL_Listener.onStart();
    }

    // public void Initialize()
    // {
    // glListener.Initialize();
    // }

    @Override
    public void OnHide() {
        CB_Utils.Log.Log.debug(log, "OnHide");
        // GL_Listener.onStop();
    }

    @Override
    public void OnFree() {

    }

    @Override
    public void ActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public int GetContextMenuId() {
        return 0;
    }

    @Override
    public void BeforeShowContextMenu(Menu menu) {
    }

    @Override
    public boolean ContextMenuItemSelected(MenuItem item) {
        return false;
    }

    public void InitializeMap() {
        // glListener.InitializeMap();

    }

    @Override
    public void RequestRender() {

        // Log.debug(log, "RequestRender von : " + requestName);

        switch (mAktSurfaceType) {
            case GLSURFACE_VIEW20:
                ((GLSurfaceView20) ViewGl).requestRender();
                break;

            case GLSURFACE_GLSURFACE:
                ((GLSurfaceView) ViewGl).requestRender();
                break;
        }
    }

    @Override
    public void RenderDirty() {
        // Log.debug(log, "Set: RenderDirty");
        try {
            switch (mAktSurfaceType) {
                case GLSURFACE_VIEW20:
                    ((GLSurfaceView20) ViewGl).setRenderMode(GLSurfaceView20.RENDERMODE_WHEN_DIRTY);
                    break;

                case GLSURFACE_GLSURFACE:
                    ((GLSurfaceView) ViewGl).setRenderMode(GLSurfaceView20.RENDERMODE_WHEN_DIRTY);
                    break;
            }
            isContinousRenderMode.set(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void RenderContinous() {
        // Log.debug(log, "Set: RenderContinous");
        switch (mAktSurfaceType) {
            case GLSURFACE_VIEW20:
                ((GLSurfaceView20) ViewGl).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                break;

            case GLSURFACE_GLSURFACE:
                ((GLSurfaceView) ViewGl).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                break;
        }
        isContinousRenderMode.set(true);
    }

    @Override
    public boolean isContinous() {
        return isContinousRenderMode.get();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mesuredWidth = measure(widthMeasureSpec);
        int mesuredHeight = measure(heightMeasureSpec);

        Log.d("CACHEBOX", "With/Height " + mesuredWidth + " / " + mesuredHeight);

        setMeasuredDimension(mesuredWidth, mesuredHeight);

    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        result = specSize;

        return result;
    }

}
