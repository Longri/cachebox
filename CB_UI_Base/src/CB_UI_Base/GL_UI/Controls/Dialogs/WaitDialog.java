package CB_UI_Base.GL_UI.Controls.Dialogs;

import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Animation.WorkAnimation;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Log.Log;
import CB_Utils.Log.Trace;
import com.badlogic.gdx.graphics.g2d.Batch;

public class WaitDialog extends ButtonDialog {
    private static final String log = "WaitDialog";
    AnimationBase animation;
    boolean canceld = false;

    public WaitDialog(Size size, String name) {
        super(size.getBounds().asFloat(), name, "", "", null, null, null);

    }

    public static WaitDialog ShowWait() {
        WaitDialog wd = createDialog("");
        wd.setCallerName(Trace.getCallerName());
        wd.Show();
        return wd;
    }

    public static WaitDialog ShowWait(String Msg) {
        WaitDialog wd = createDialog(Msg);
        wd.setCallerName(Trace.getCallerName());
        wd.Show();
        return wd;
    }

    protected static WaitDialog createDialog(String msg) {

        Size size = calcMsgBoxSize(msg, false, false, true, false);

        WaitDialog waitDialog = new WaitDialog(size, "WaitDialog");
        waitDialog.setTitle("");

        SizeF contentSize = waitDialog.getContentSize();

        CB_RectF imageRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());
        waitDialog.animation = WorkAnimation.GetINSTANCE(imageRec);
        waitDialog.addChild(waitDialog.animation);

        waitDialog.label = new Label("WaitDialog" + " label", contentSize.getBounds());
        waitDialog.label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UI_Size_Base.that.getButtonHeight());
        waitDialog.label.setX(imageRec.getMaxX() + margin);
        waitDialog.label.setWrappedText(msg);

        int lineCount = waitDialog.label.getLineCount();
        waitDialog.label.setY(0);

        if (lineCount == 1) {
            waitDialog.label.setText(msg);
            waitDialog.label.setVAlignment(VAlignment.CENTER);
        } else {
            waitDialog.label.setVAlignment(VAlignment.TOP);
        }

        float imageYPos = (contentSize.height < (waitDialog.animation.getHeight() * 1.7)) ? contentSize.halfHeight - waitDialog.animation.getHalfHeight() : contentSize.height - waitDialog.animation.getHeight() - margin;
        waitDialog.animation.setY(imageYPos);

        waitDialog.addChild(waitDialog.label);
        waitDialog.setButtonCaptions(MessageBoxButtons.NOTHING);

        return waitDialog;

    }

    public void setAnimation(final AnimationBase Animation) {
        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                WaitDialog.this.removeChild(WaitDialog.this.animation);
                CB_RectF imageRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());
                WaitDialog.this.animation = Animation.INSTANCE(imageRec);
                WaitDialog.this.addChild(WaitDialog.this.animation);
            }
        });

    }

    public void dismis() {
        Log.debug(log, "WaitDialog.Dismis");
        GL.that.RunOnGL(new IRunOnGL() {
            @Override
            public void run() {
                GL.that.closeDialog(WaitDialog.this);
                GL.that.renderOnce();
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        String caller = Trace.getCallerName(1);
        Log.debug(log, "WaitDialog.disposed ID:[" + this.DialogID + "] called:" + caller);
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public String toString() {
        return getName() + "DialogID[" + DialogID + "] \"" + this.label.getText() + "\" Created by: " + CallerName;
    }

}
