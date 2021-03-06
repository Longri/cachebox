package CB_UI.GL_UI.Controls.Dialogs;

import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Linearlayout;
import CB_UI_Base.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;

public class ParkingDialog extends ButtonDialog {

    private Linearlayout layout;

    private float TextFieldHeight;
    private SizeF msgBoxContentSize;
    private ImageButton btSetGPS, btSelectWP, btDeleteP;
    private Label lblSetGPS, lblSelectWP, lblDeleteP;

    public ParkingDialog() {
        super((calcMsgBoxSize("Text", true, true, false, false)).getBounds().asFloat(), "Parking-Dialog", "", Translation.Get("My_Parking_Area_Title"), MessageBoxButtons.Cancel, null, null);

        msgBoxContentSize = getContentSize();
        // initial VariableField
        TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

        float innerWidth = msgBoxContentSize.width;

        layout = new Linearlayout(innerWidth, "Layout");
        layout.setX(0);
        // layout.setBackground(new ColorDrawable(Color.GREEN));

        CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UI_Size_Base.that.getButtonHeight() * 2);

        btSetGPS = new ImageButton(MTBRec, "btSetGPS");
        btSelectWP = new ImageButton(MTBRec, "btSelectWP");
        btDeleteP = new ImageButton(MTBRec, "btDeleteP");

        btSetGPS.setImage(Sprites.getSpriteDrawable("my-parking-set"));
        btSelectWP.setImage(Sprites.getSpriteDrawable("my-parking-wp"));
        btDeleteP.setImage(Sprites.getSpriteDrawable("my-parking-delete"));

        btSetGPS.setX(0);
        btSelectWP.setX(btSetGPS.getMaxX());
        btDeleteP.setX(btSelectWP.getMaxX());

        Box box = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 2), "");

        box.addChild(btSetGPS);
        box.addChild(btSelectWP);
        box.addChild(btDeleteP);

        layout.addChild(box);

        Box box2 = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 2), "");

        lblSetGPS = new Label(this.name + " lblSetGPS", btSetGPS.ScaleCenter(0.8f));
        lblSelectWP = new Label(this.name + " lblSelectWP", btSelectWP.ScaleCenter(0.8f));
        lblDeleteP = new Label(this.name + " lblDeleteP", btDeleteP.ScaleCenter(0.8f));

        lblSetGPS.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        lblSelectWP.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        lblDeleteP.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

        lblSelectWP.setWrappedText(Translation.Get("My_Parking_Area_select"));
        lblSetGPS.setWrappedText(Translation.Get("My_Parking_Area_Add"));
        lblDeleteP.setWrappedText(Translation.Get("My_Parking_Area_Del"));

        box2.addChild(lblSetGPS);
        box2.addChild(lblSelectWP);
        box2.addChild(lblDeleteP);

        layout.addChild(box2);

        this.addChild(layout);

        // chk disable select and delete Button
        synchronized (Database.Data.Query) {
            Cache cache = Database.Data.Query.GetCacheByGcCode("CBPark");
            if (cache == null) {
                btSelectWP.disable();
                btDeleteP.disable();
            }
        }

        Size msgBoxSize = GL_MsgBox.calcMsgBoxSize("teste", true, true, false);
        msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight() - (TextFieldHeight / 2));
        this.setSize(msgBoxSize.asFloat());

        btSetGPS.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

                Config.ParkingLatitude.setValue(Locator.getLatitude());
                Config.ParkingLongitude.setValue(Locator.getLongitude());
                Config.AcceptChanges();
                CacheListChangedEventList.Call();

                close();
                return true;
            }
        });

        btSelectWP.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                synchronized (Database.Data.Query) {
                    Cache cache = Database.Data.Query.GetCacheByGcCode("CBPark");
                    if (cache != null)
                        GlobalCore.setSelectedCache(cache);
                }
                close();
                return true;
            }
        });

        btDeleteP.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                Config.ParkingLatitude.setValue(0.0);
                Config.ParkingLongitude.setValue(0.0);
                Config.AcceptChanges();
                CacheListChangedEventList.Call();
                close();
                return true;
            }
        });

    }

    @Override
    public void dispose() {
        msgBoxContentSize = null;
        if (btSetGPS != null)
            btSetGPS.dispose();
        if (btSelectWP != null)
            btSelectWP.dispose();
        if (btDeleteP != null)
            btDeleteP.dispose();
        if (lblSetGPS != null)
            lblSetGPS.dispose();
        if (lblSelectWP != null)
            lblSelectWP.dispose();
        if (lblDeleteP != null)
            lblDeleteP.dispose();
        super.dispose();
        btSetGPS = null;
        btSelectWP = null;
        btDeleteP = null;
        lblSetGPS = null;
        lblSelectWP = null;
        lblDeleteP = null;
    }

}
