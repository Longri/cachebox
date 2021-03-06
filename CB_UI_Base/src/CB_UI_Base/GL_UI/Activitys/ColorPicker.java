/*
 * Copyright (C) 2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.GL_UI.Activitys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.ColorPickerRec;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.GL_UI.utils.GradiantFill;
import CB_UI_Base.GL_UI.utils.GradiantFilledRectangle;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.HSV_Color;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class ColorPicker extends ActivityBase {
    private HSV_Color InitialColor;
    private HSV_Color actColor;
    private Button bOK;
    private Button bCancel;

    private IReturnListener mReturnListener;

    private ColorDrawable actColorDrawable;

    private Box lastColorBox;
    private Box actColorBox;
    private Image arrow;
    private ColorPickerRec viewSatVal;

    private Image viewCursor;
    private Image viewTarget;

    private Image viewHue;
    private GradiantFill gradiantWhite;
    private GradiantFill gradiantBlack;

    public ColorPicker(CB_RectF rec, Color color, IReturnListener listener) {
        super(rec, "ColorPicker");
        actColor = new HSV_Color(color);
        InitialColor = new HSV_Color(color);

        mReturnListener = listener;
        this.setClickable(true);
        createOkCancelBtn();
        createColorPreviewLine();
        createViewHue();
        createTest();

        hueChanged();

        moveCursor();
        moveTarget();

    }

    private void createOkCancelBtn() {
        bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
        bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        // Translations
        bOK.setText(Translation.Get("ok"));
        bCancel.setText(Translation.Get("cancel"));

        this.addChild(bOK);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                GL.that.RunOnGL(new IRunOnGL() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
                if (mReturnListener != null)
                    mReturnListener.returnColor(actColor);
                return true;
            }
        });

        this.addChild(bCancel);
        bCancel.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mReturnListener != null)
                    mReturnListener.returnColor(null);
                finish();
                return true;
            }
        });

    }

    @Override
    public void dispose() {
        gradiantWhite.dispose();
        gradiantBlack.dispose();
    }

    private void createColorPreviewLine() {
        CB_RectF rec = new CB_RectF(0, bOK.getMaxY() + margin, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight());
        lastColorBox = new Box(rec, "LastColor");
        actColorBox = new Box(rec, "aktColor");

        rec.setWidth(rec.getHeight());
        arrow = new Image(rec, "arrowImage", false);
        arrow.setDrawable(new SpriteDrawable(Sprites.Arrows.get(11)));

        float lineWidth = lastColorBox.getWidth() + margin + arrow.getWidth() + margin + actColorBox.getWidth();
        float left = this.getHalfWidth() - (lineWidth / 2);
        lastColorBox.setX(left);
        arrow.setX(lastColorBox.getMaxX() + margin);
        actColorBox.setX(arrow.getMaxX() + margin);

        lastColorBox.setBackground(new ColorDrawable(InitialColor));
        actColorBox.setBackground(new ColorDrawable(InitialColor));

        this.addChild(lastColorBox);
        this.addChild(arrow);
        this.addChild(actColorBox);
    }

    private void createViewHue() {
        float vWidth = bOK.getHeight();

        viewHue = new Image(this.getWidth() - rightBorder - margin - vWidth, actColorBox.getMaxY() + margin, vWidth, this.getHeight() - this.getTopHeight() - actColorBox.getMaxY() - margin * 2, "viewHue", false);
        viewHue.setDrawable(new SpriteDrawable(Sprites.ambilwarna_hue));
        this.addChild(viewHue);

        float cursorSize = Fonts.Measure("T").height;

        viewCursor = new Image(0, 0, cursorSize, cursorSize, "", false);
        viewCursor.setDrawable(new SpriteDrawable(Sprites.ambilwarna_cursor));
        this.addChild(viewCursor);

    }

    private void createTest() {
        CB_RectF rec = new CB_RectF(leftBorder + margin, viewHue.getY(), viewHue.getX() - margin * 3 - leftBorder, viewHue.getHeight());

        viewSatVal = new ColorPickerRec(rec, "");
        this.addChild(viewSatVal);

        {
            // Gradiant Test

            // Color blackTransparent = new Color(1f, 1f, 0f, 0.2f);
            // gradiantBlack = new GradiantFill(Color.BLACK, blackTransparent, -30);
            // rectangle FillRecBlack = new rectangle(rec, gradiantBlack);
            // this.addChild(FillRecBlack);
        }

        Color whiteTransparent = new Color(1f, 1f, 1f, 0f);
        gradiantWhite = new GradiantFill(Color.WHITE, whiteTransparent, 0);
        GradiantFilledRectangle FillRecWhite = new GradiantFilledRectangle(rec, gradiantWhite);
        this.addChild(FillRecWhite);

        Color blackTransparent = new Color(0f, 0f, 0f, 0f);
        gradiantBlack = new GradiantFill(Color.BLACK, blackTransparent, 90);
        GradiantFilledRectangle FillRecBlack = new GradiantFilledRectangle(rec, gradiantBlack);
        this.addChild(FillRecBlack);

        float cursorSize = Fonts.Measure("T").height;

        viewTarget = new Image(0, 0, cursorSize, cursorSize, "", false);
        viewTarget.setDrawable(new SpriteDrawable(Sprites.ambilwarna_target));
        this.addChild(viewTarget);

    }

    private void hueChanged() {
        if (viewSatVal != null)
            viewSatVal.setHue(actColor.getHue());
    }

    protected void moveCursor() {
        float y = viewHue.getHeight() - (getHue() * viewHue.getHeight() / 360.f);
        if (y == viewHue.getHeight())
            y = 0.f;

        viewCursor.setX((float) (viewHue.getX() - Math.floor(viewCursor.getWidth() / 2)));

        viewCursor.setY((float) (viewHue.getMaxY() - y - Math.floor(viewCursor.getHeight() / 2)));

    }

    protected void moveTarget() {
        float x = getSat() * viewSatVal.getWidth();
        float y = getVal() * viewSatVal.getHeight();

        viewTarget.setX((float) (viewSatVal.getX() + x - Math.floor(viewTarget.getWidth() / 2)));
        viewTarget.setY((float) (viewSatVal.getY() + y - Math.floor(viewTarget.getHeight() / 2)));

    }

    private float getHue() {
        return actColor.getHue();
    }

    private void setHue(float hue) {
        actColor.setHue(hue);
    }

    private float getSat() {
        return actColor.getSat();
    }

    private void setSat(float sat) {
        actColor.setSat(sat);
    }

    private float getVal() {
        return actColor.getVal();
    }

    private void setVal(float val) {
        actColor.setVal(val);
    }

    private void onClick_DracgHueView(float y) {
        if (y < 0.f)
            y = 0.f;
        if (y > viewHue.getHeight())
            y = viewHue.getHeight() - 0.001f; // to avoid looping from end to start.
        float hue = 360.f / viewHue.getHeight() * y;
        if (hue == 360.f)
            hue = 0.f;
        setHue(hue);

        // update view
        viewSatVal.setHue(getHue());
        moveCursor();
        regenarateActColorBox();
    }

    private void onClickDragg_Sat(float x, float y) {
        if (x < 0.f)
            x = 0.f;
        if (x > viewSatVal.getWidth())
            x = viewSatVal.getWidth();
        if (y < 0.f)
            y = 0.f;
        if (y > viewSatVal.getHeight())
            y = viewSatVal.getHeight();

        setSat(1.f / viewSatVal.getWidth() * x);
        setVal(1.f / viewSatVal.getHeight() * y);

        // update view
        moveTarget();
        regenarateActColorBox();
    }

    private void regenarateActColorBox() {

        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                if (actColorDrawable == null) {
                    actColorDrawable = new ColorDrawable(actColor);
                } else {
                    actColorDrawable.setColor(actColor);
                }

                actColorBox.setBackground(actColorDrawable);
            }
        });

    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {

        if (viewHue.contains(x, y)) {
            onClick_DracgHueView(y - viewHue.getY());
            return true;
        }

        if (viewSatVal.contains(x, y)) {
            onClickDragg_Sat(x - viewSatVal.getX(), y - viewSatVal.getY());
            return true;
        }

        return super.click(x, y, pointer, button);
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

        if (!KineticPan && viewHue.contains(x, y)) {
            onClick_DracgHueView(y - viewHue.getY());
            return true;
        }

        if (!KineticPan && viewSatVal.contains(x, y)) {
            onClickDragg_Sat(x - viewSatVal.getX(), y - viewSatVal.getY());
            return true;
        }

        return false;
    }

    public void setReturnListener(IReturnListener iReturnListener) {
        mReturnListener = iReturnListener;
    }

    public void setColor(Color color) {
        actColor = InitialColor = new HSV_Color(color);
        hueChanged();

        moveCursor();
        moveTarget();
        lastColorBox.setBackground(new ColorDrawable(InitialColor));
    }

    public interface IReturnListener {
        public void returnColor(Color color);
    }

}
