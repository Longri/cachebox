package cb_vtm;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_Locator.Events.PositionChangedEvent;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.Math.CB_RectF;

public class VtmMapView extends CB_View_Base implements PositionChangedEvent {

    public static VtmMapView that;

    public VtmMapView(CB_RectF contentRec) {
        super("VTM-View");
        that=this;

        this.set(contentRec.getX(), contentRec.getY(), contentRec.getWidth(), contentRec.getHeight());
        Label descTextView = new Label(this.name + " descTextView", 0, 0, this.getWidth(), this.getHeight());
        BitmapFont font = Fonts.getBig();

        descTextView.setFont(font).setHAlignment(Label.HAlignment.CENTER);

        descTextView.setWrappedText("VTM-View");
        this.addChild(descTextView);
    }


    @Override
    public void PositionChanged() {

    }

    @Override
    public void OrientationChanged() {

    }

    @Override
    public void SpeedChanged() {

    }

    @Override
    public String getReceiverName() {
        return null;
    }

    @Override
    public Priority getPriority() {
        return null;
    }
}
