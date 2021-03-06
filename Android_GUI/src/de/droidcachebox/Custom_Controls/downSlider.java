/*
 * Copyright (C) 2011 team-cachebox.de
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

package de.droidcachebox.Custom_Controls;

import CB_Core.Attributes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.CoordinateGPS;
import CB_Locator.Events.GpsStateChangeEvent;
import CB_Locator.Events.GpsStateChangeEventList;
import CB_Locator.GPS;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Controls.QuickButtonList;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.Energy;
import CB_UI_Base.Math.CB_Rect;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Log;
import CB_Utils.Util.UnitFormatter;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.main;

import java.util.Iterator;

public final class downSlider extends View implements SelectedCacheEvent, GpsStateChangeEvent {

    private static final String log = "downSlider";
    public static boolean isInitial = false;
    private static int QuickButtonHeight;
    private static downSlider Me;
    private final int AnimationTime = 50;
    private final double AnimationMulti = 1.4;
    int attCompleadHeight = 0;
    int topCalc = -1;
    int attLineHeight = -1;
    int yPos = 0;
    Rect mBtnRec = new Rect();
    Rect mBackRec = new Rect();
    Paint backPaint;
    int tmpPos = 0;
    Handler handler = new Handler();
    Runnable task = new Runnable() {

        @Override
        public void run() {

        }
    };
    boolean mGpsStateChabgedListenerRegistred = false;
    /*
     * Private Member
     */
    private int height;
    private int width;
    private int imgSize;
    private StaticLayout WPLayoutName;
    private StaticLayout WPLayoutDesc;
    private StaticLayout WPLayoutCord;
    private StaticLayout WPLayoutClue;
    private TextPaint WPLayoutTextPaint;
    private TextPaint WPLayoutTextPaintBold;
    private int LineSep;
    private int WPInfoHeight = 0;
    private int GPSInfoHeight = 0;
    private int QuickButtonMaxHeight;
    private int attHeight = -1;
    private Cache mCache;
    private Waypoint mWaypoint;
    private int CacheInfoHeight = 0;
    private Paint paint;
    private boolean drag;
    private boolean ButtonDrag;
    // private int lastDragYPos = 0;
    private boolean swipeUp = false;
    private boolean swipeDown = false;
    private boolean initialNight = false;
    private Boolean isVisible = false;
    private String mLatitude = "";
    private String mLongitude = "";
    private String mSats;
    private String mAccuracy;
    private String mAlt;
    private TextPaint GPSLayoutTextPaint;
    private StaticLayout GPSLayout;
    private boolean AnimationIsRunning = false;
    private int AnimationDirection = -1;
    private int AnimationTarget = 0;
    Runnable AnimationTask = new Runnable() {

        @Override
        public void run() {

            if (!AnimationIsRunning)
                return; // Animation wurde abgebrochen

            int newValue = 0;
            if (AnimationDirection == -1) {
                int tmp = yPos - AnimationTarget;
                if (tmp <= 0)// Div 0 vehindern
                {
                    setPos_onUI(AnimationTarget);
                    AnimationIsRunning = false;
                }

                newValue = (int) (yPos - (tmp / AnimationMulti));
                if (newValue <= AnimationTarget) {
                    setPos_onUI(AnimationTarget);
                    AnimationIsRunning = false;
                } else {
                    setPos_onUI(newValue);
                    handler.postDelayed(AnimationTask, AnimationTime);
                }
            } else {
                int tmp = AnimationTarget - yPos;
                if (tmp <= 0)// Div 0 vehindern
                {
                    setPos_onUI(AnimationTarget);
                    AnimationIsRunning = false;
                } else {
                    newValue = (int) (yPos + (tmp / AnimationMulti));
                    if (newValue >= AnimationTarget) {
                        setPos_onUI(AnimationTarget);
                        AnimationIsRunning = false;
                    } else {
                        setPos_onUI(newValue);
                        handler.postDelayed(AnimationTask, AnimationTime);
                    }
                }

            }

        }
    };
    private GestureDetector mGestureDetector;
    private final OnTouchListener myTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            mGestureDetector.onTouchEvent(event);

            // events when touching the screen

            int eventaction = event.getAction();
            int X = (int) event.getX();
            int Y = (int) event.getY();
            if (contains(X, Y))
                drag = true;
            if (Y < QuickButtonHeight)
                ButtonDrag = true;

            switch (eventaction) {
                case MotionEvent.ACTION_DOWN: // touch down so check if the finger
                    // is on a ball
                    AnimationIsRunning = false;
                    break;

                case MotionEvent.ACTION_MOVE: // touch drag with the ball
                    // move the balls the same as the finger

                    if (drag) {
                        int value = Y - 25;// y - 25 minus halbe Button Höhe
                        int buttom = (int) (height - (10 * 2.2));
                        if (value > buttom)
                            value = buttom - 1;

                        setPos(value);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (drag) {
                        drag = false;
                        ButtonDrag = false;
                        ActionUp();
                    }

                    break;

            }

            if (drag) {
                return true;
            } else {
                return false;
            }
        }
    };

    public downSlider(Context context) {
        super(context);
    }

    public downSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        SelectedCacheEventList.Add(this);

        mGestureDetector = new GestureDetector(context, new LearnGestureListener());

        this.setOnTouchListener(myTouchListener);

        Me = this;

    }

    public downSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public static void ButtonShowStateChanged() {
        if (downSlider.Me != null) {
            if (Config.quickButtonShow.getValue()) {
                downSlider.Me.setPos_onUI(downSlider.Me.QuickButtonMaxHeight);
            } else {
                downSlider.Me.setPos_onUI(0);
            }

            if (GlobalCore.isSetSelectedCache()) {
                downSlider.Me.setCache_onUI(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
            }

        }
    }

    public static int getAktQuickButtonHeight() {
        return Config.quickButtonShow.getValue() ? QuickButtonHeight : 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.width = measure(widthMeasureSpec);
        this.height = measure(heightMeasureSpec);
        imgSize = (int) ((height / 5) * 0.6);
        setMeasuredDimension(this.width, this.height);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) {
        try {
            QuickButtonMaxHeight = UiSizes.that.getQuickButtonListHeight() + 20;
        } catch (Exception e) {
            e.printStackTrace();
        }
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        result = specSize;

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (QuickButtonList.that == null)
            return; // noch nicht initialisiert

        /**
         * Beim ersten Zeichnen, wird der Letzte Zustand abgefragt!
         */
        if (!isInitial) {
            if (Config.quickButtonShow.getValue() && Config.quickButtonLastShow.getValue()) {
                setPos(QuickButtonMaxHeight);
            } else {
                setPos(0);
            }

        }

        if (!drag && !AnimationIsRunning && !ButtonDrag) {
            yPos = QuickButtonHeight = Config.quickButtonShow.getValue() ? ((int) QuickButtonList.that.getHeight()) : 0;
        }

        float FSize = ((float) (UI_Size_Base.that.getScaledFontSize_big() * 1.3));

        if (paint == null || Config.nightMode.getValue() != initialNight) {
            paint = new Paint();
            paint.setColor(Global.getColor(R.attr.TextColor));
            paint.setTextSize((float) (UI_Size_Base.that.getScaledFontSize() * 1.3));
            paint.setAntiAlias(true);
            initialNight = Config.nightMode.getValue();
        }

        final Drawable Slide = Global.BtnIcons[0];

        mBtnRec.set(-10, yPos - UI_Size_Base.that.getMargin(), width + 10, (int) (yPos + UI_Size_Base.that.getMargin() + 10 * 3.3));
        Log.trace(log, "AndroidSlider bound: " + mBtnRec.toShortString());

        Slide.setBounds(mBtnRec);
        Slide.draw(canvas);

        // Draw Background
        if (backPaint == null) {
            backPaint = new Paint();
            backPaint.setColor(Global.getColor(R.attr.SlideDownBackColor));
            // backPaint.setColor(Color.RED); //DEBUG RED
        }
        mBackRec.set(-10, QuickButtonHeight + 2, width + 10, yPos + 1);
        canvas.drawRect(mBackRec, backPaint);

        if (!isInitial) {

            if (CB_UI.GL_UI.Controls.Slider.setAndroidSliderHeight(mBtnRec.height())) {
                isInitial = true;
            }
        }

        // Draw Slide Icons
        final Drawable SlideIcon = (QuickButtonHeight > 0) ? Global.Icons[41] : Global.Icons[40];
        Rect SlideIconRec = new Rect(mBtnRec);
        SlideIconRec.set(SlideIconRec.left + 10, SlideIconRec.top, SlideIconRec.height(), SlideIconRec.bottom);
        SlideIcon.setBounds(SlideIconRec);
        SlideIcon.draw(canvas);

        SlideIconRec.offset(width - SlideIconRec.width(), 0);
        SlideIcon.setBounds(SlideIconRec);
        SlideIcon.draw(canvas);

        // draw Cache Name
        if (mCache != null)
            canvas.drawText(mCache.getName(), 20 + SlideIconRec.width(), yPos + (FSize + (FSize / 3)), paint);

        // Draw only is visible
        if (Config.quickButtonShow.getValue()) {
            if (yPos <= QuickButtonMaxHeight) {
                if (Energy.SliderIsShown())
                    Energy.resetSliderIsShown();
                // Slider is visible, chk if GpsStateChangedListener
                if (mGpsStateChabgedListenerRegistred)
                    unregisterGpsStateChangedListener();
                return;
            }
        } else {
            if (yPos <= 1) {
                if (Energy.SliderIsShown())
                    Energy.resetSliderIsShown();
                // Slider is visible, chk if GpsStateChangedListener
                if (mGpsStateChabgedListenerRegistred)
                    unregisterGpsStateChangedListener();
                return;
            }
        }

        // Slider is visible, chk if GpsStateChangedListener
        if (!mGpsStateChabgedListenerRegistred)
            registerGpsStateChangedListener();

        if (!Energy.SliderIsShown())
            Energy.setSliderIsShown();

        if (Config.quickButtonShow.getValue()) {
            canvas.clipRect(mBackRec);
        }

        // draw GPS Info
        int versatz = -yPos + GPSInfoHeight;
        canvas.translate(0, -versatz);
        drawGPSInfo(canvas);
        canvas.translate(0, +versatz);
        // canvas.clipRect(mBackRec);

        // if(yPos>1)return;
        // draw WP Info
        versatz += WPInfoHeight;
        canvas.translate(0, -versatz);
        Boolean WPisDraw = drawWPInfo(canvas);
        canvas.translate(0, +versatz);
        // canvas.clipRect(mBackRec);

        // draw Cache Info

        if (mCache != null) {
            int lines = 0;
            if (mCache.getAttributes() != null) {
                Iterator<Attributes> iterator = mCache.getAttributes().iterator();

                if (iterator != null && iterator.hasNext()) {
                    if (attHeight == -1)
                        attHeight = (int) (UI_Size_Base.that.getIconSize() * 0.75);

                    lines = 1 + (mCache.getAttributes().size() / 8);
                    attCompleadHeight = (int) (lines * attHeight * 1.3);

                }
            }

            if (attLineHeight == -1)
                attLineHeight = attHeight + (UI_Size_Base.that.getScaledFontSize() / 3);

            if (CacheInfoHeight == 0) {
                CacheInfoHeight = (int) (FSize * 9) + attCompleadHeight;
                topCalc = CacheInfoHeight - (attLineHeight * lines) - attLineHeight + UI_Size_Base.that.getScaledFontSize();
            }

            versatz += CacheInfoHeight;
            canvas.translate(5, -versatz);
            CacheDraw.DrawInfo(mCache, canvas, width - 10, CacheInfoHeight, WPisDraw ? Global.getColor(R.attr.ListBackground) : Global.getColor(R.attr.ListBackground_select), DrawStyle.withOwnerAndName);

            // draw Attributes

            int left = 8;

            int top = topCalc;
            if (mCache.getAttributes() != null) {
                Iterator<Attributes> iterator = mCache.getAttributes().iterator();
                if (iterator != null && iterator.hasNext()) {
                    int i = 0;
                    do {
                        Attributes att = iterator.next();
                        String uri = "drawable/" + att.getImageName();

                        int imageResource = getResources().getIdentifier(uri, null, main.mainActivity.getPackageName());
                        Drawable image = null;
                        try {
                            image = getResources().getDrawable(imageResource);
                        } catch (NotFoundException e) {
                            image = Global.Icons[34];
                        }

                        if (image != null) {
                            left += ActivityUtils.PutImageTargetHeight(canvas, image, left, top, attHeight) + 3;
                            i++;
                            if (i % 8 == 0 && i > 7) {
                                left = 8;
                                top += attLineHeight;
                            }
                        }

                    } while (iterator.hasNext());
                }
            }
        }
        canvas.translate(0, +versatz);
    }

    private Boolean drawWPInfo(Canvas canvas) {
        if (mWaypoint == null)
            return false;

        int LineColor = Global.getColor(R.attr.ListSeparator);
        CB_Rect DrawingRec = new CB_Rect(5, 5, width - 5, WPInfoHeight - 5);
        ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.ListBackground_select));

        int left = 15;
        int top = LineSep * 2;

        int iconWidth = 0;
        try {
            // draw icon
            if ((mWaypoint.Type.ordinal()) < Global.CacheIconsBig.length)
                iconWidth = ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[mWaypoint.Type.ordinal()], UiSizes.that.getHalfCornerSize(), UiSizes.that.getCornerSize(), imgSize);
        } catch (Exception e1) {

        }

        try {
            // draw Text info
            left += iconWidth;
            top += ActivityUtils.drawStaticLayout(canvas, WPLayoutName, left, top);
            top += ActivityUtils.drawStaticLayout(canvas, WPLayoutDesc, left, top);
            top += ActivityUtils.drawStaticLayout(canvas, WPLayoutCord, left, top);
            if (mWaypoint.getClue() != null)
                ActivityUtils.drawStaticLayout(canvas, WPLayoutClue, left, top);
        } catch (Exception e) {
        }

        return true;
    }

    private Boolean drawGPSInfo(Canvas canvas) {
        if (GPSInfoHeight == 0)
            return false;

        int LineColor = Global.getColor(R.attr.ListSeparator);
        CB_Rect DrawingRec = new CB_Rect(5, 5, width - 5, GPSInfoHeight - 5);
        ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.ListBackground));

        Bitmap b = Bitmap.createBitmap(main.strengthLayout.getMeasuredWidth(), main.strengthLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        main.strengthLayout.draw(c);

        int left = width - b.getWidth() - 22;
        int top = GPSInfoHeight - b.getHeight() - 10;

        canvas.drawBitmap(b, left, top, null);

        left = 15;
        top = LineSep * 2;

        int iconWidth = 0;
        // draw icon
        iconWidth = ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[30], UiSizes.that.getHalfCornerSize(), UiSizes.that.getCornerSize(), imgSize);

        // draw Text info
        left += iconWidth;
        top += ActivityUtils.drawStaticLayout(canvas, GPSLayout, left, top);

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Log.d("Cachebox", "Size changed to " + w + "x" + h);
    }

    public void setPos_onUI(final int Pos) {
        tmpPos = Pos;
        ((main) main.mainActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // ((main) main.mainActivity).InfoDownSlider.setPos(Pos);
                // ((main) main.mainActivity).InfoDownSlider.invalidate();
            }
        });
    }

    private void startUpdateTimer() {
        handler.postDelayed(task, 400);
    }

    public int getPos() {
        return yPos;
    }

    public void setPos(int Pos) {
        if (Pos >= 0) {
            yPos = Pos;
            if (Config.quickButtonShow.getValue()) {
                if (Pos <= QuickButtonMaxHeight) {
                    QuickButtonHeight = Pos;
                } else {
                    QuickButtonHeight = QuickButtonMaxHeight;
                }
            } else {
                QuickButtonHeight = 0;
            }
            ((main) main.mainActivity).setQuickButtonHeight(QuickButtonHeight);

        } else {
            yPos = 0;

        }

        // chk if info Visible then update info
        int InfoBeginnAt = Config.quickButtonShow.getValue() ? QuickButtonMaxHeight : 0;
        if (yPos > InfoBeginnAt) {
            if (!isVisible)
                startUpdateTimer();
            isVisible = true;

        } else {
            isVisible = false;

        }

        this.invalidate();

        // CB_UI.GL_UI.Controls.Slider.setAndroidSliderPos(yPos);

    }

    public boolean contains(int x, int y) {
        return mBtnRec.contains(x, y);
    }

    public void ActionUp() // Slider zurück scrolllen lassen
    {

        if (main.mainActivity != null) {
            ((main) main.mainActivity).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    boolean QuickButtonShow = Config.quickButtonShow.getValue();

                    // check if QuickButtonList snap in
                    if (yPos >= (QuickButtonMaxHeight * 0.5) && QuickButtonShow) {
                        QuickButtonHeight = QuickButtonMaxHeight;
                        Config.quickButtonLastShow.setValue(true);
                        Config.AcceptChanges();
                    } else {
                        QuickButtonHeight = 0;
                        Config.quickButtonLastShow.setValue(false);
                        Config.AcceptChanges();
                    }

                    ((main) main.mainActivity).setQuickButtonHeight(QuickButtonHeight);

                    if (swipeUp || swipeDown) {
                        if (swipeUp) {
                            startAnimationTo(QuickButtonShow ? QuickButtonHeight : 0);
                        } else {
                            startAnimationTo((int) (height - (UI_Size_Base.that.getScaledFontSize() * 2.2)));
                        }
                        swipeUp = swipeDown = false;

                    } else {
                        if (yPos > height * 0.7) {
                            startAnimationTo((int) (height - (UI_Size_Base.that.getScaledFontSize() * 2.2)));
                        } else {
                            startAnimationTo(QuickButtonShow ? QuickButtonHeight : 0);
                            // isVisible=false;
                        }
                    }

                    invalidate();

                }
            });
        }

    }

    @Override
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
        setCache_onUI(cache, waypoint);
    }

    public void setCache_onUI(final Cache cache, final Waypoint waypoint) {
        if (mCache != null) {
            if (cache == null)
                return;

            if (cache == mCache) {
                if (mWaypoint == null && waypoint == null)
                    return;
                if (waypoint != null) {
                    if (waypoint == mWaypoint)
                        return;
                }
            }

        }

        ((main) main.mainActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCache = cache;
                if (waypoint == null) {
                    mWaypoint = null;
                } else {
                    mWaypoint = waypoint;
                }
                attCompleadHeight = 0;
                CacheInfoHeight = 0;
                WPInfoHeight = 0;
                topCalc = 0;

                int TextWidth = downSlider.Me.width - downSlider.Me.imgSize;

                Rect bounds = new Rect();
                WPLayoutTextPaint = new TextPaint();
                WPLayoutTextPaint.setTextSize((float) (UI_Size_Base.that.getScaledFontSize() * 1.3));
                WPLayoutTextPaint.getTextBounds("T", 0, 1, bounds);
                LineSep = bounds.height() / 3;

                String Clue = "";
                if (mWaypoint != null) {
                    if (mWaypoint.getClue() != null)
                        Clue = mWaypoint.getClue();
                    WPLayoutTextPaint.setAntiAlias(true);
                    WPLayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
                    WPLayoutCord = new StaticLayout(UnitFormatter.FormatLatitudeDM(waypoint.Pos.getLatitude()) + " / " + UnitFormatter.FormatLongitudeDM(waypoint.Pos.getLongitude()), WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                            false);
                    WPLayoutDesc = new StaticLayout(mWaypoint.getDescription(), WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    WPLayoutClue = new StaticLayout(Clue, WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    WPLayoutTextPaintBold = new TextPaint(WPLayoutTextPaint);
                    WPLayoutTextPaintBold.setFakeBoldText(true);
                    WPLayoutName = new StaticLayout(waypoint.getGcCode() + ": " + waypoint.getTitle(), WPLayoutTextPaintBold, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    WPInfoHeight = (LineSep * 5) + WPLayoutCord.getHeight() + WPLayoutDesc.getHeight() + WPLayoutClue.getHeight() + WPLayoutName.getHeight();
                }

                downSlider.Me.invalidate();

            }
        });
    }

    public void setNewLocation(CoordinateGPS location) {

        if (this.width == 0)
            return;

        // mSats = String.valueOf(location.getExtras().getInt("satellites"));
        mSats = GPS.getSatAndFix();

        mAccuracy = String.valueOf(location.getAccuracy());
        mAlt = Locator.getAltStringWithCorection();
        mLatitude = UnitFormatter.FormatLatitudeDM(location.getLatitude());
        mLongitude = UnitFormatter.FormatLongitudeDM(location.getLongitude());

        String br = "\n";
        String Text = Translation.Get("current") + " " + mLatitude + " " + mLongitude + br + Translation.Get("alt") + " " + mAlt + br + Translation.Get("accuracy") + "  +/- " + mAccuracy + "m" + br + Translation.Get("sats") + " " + mSats;

        if (GPSLayoutTextPaint == null) {
            GPSLayoutTextPaint = new TextPaint();
            GPSLayoutTextPaint.setTextSize((float) (UI_Size_Base.that.getScaledFontSize() * 1.3));
            GPSLayoutTextPaint.setAntiAlias(true);
            GPSLayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
        }

        int TextWidth = this.width - this.imgSize;
        GPSLayout = new StaticLayout(Text, GPSLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        GPSInfoHeight = (mLatitude.equals("")) ? 0 : (LineSep * 4) + GPSLayout.getHeight();

        this.invalidate();
    }

    private void startAnimationTo(int newYPos) {
        if (yPos == newYPos)
            return; // wir brauchen nichts Animieren

        AnimationIsRunning = true;
        AnimationTarget = newYPos;
        if (yPos > newYPos)
            AnimationDirection = -1;
        else
            AnimationDirection = 1;
        handler.postDelayed(AnimationTask, AnimationTime);
    }

    @Override
    public void GpsStateChanged() {
        this.invalidate();

    }

    private void registerGpsStateChangedListener() {
        GpsStateChangeEventList.Add(this);
        mGpsStateChabgedListenerRegistred = true;
    }

    private void unregisterGpsStateChangedListener() {
        GpsStateChangeEventList.Remove(this);
        mGpsStateChabgedListenerRegistred = false;
    }

    class LearnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            // Log.d("onSingleTapUp", ev.toString());
            return true;
        }

        @Override
        public void onShowPress(MotionEvent ev) {
            // Log.d("onShowPress", ev.toString());
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            // Log.d("onLongPress", ev.toString());
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Log.d("onScroll", e1.toString());
            return true;
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            // Log.d("onDownd", ev.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY > 500) {
                swipeDown = true;
            } else if (velocityY < -500) {
                swipeUp = true;
            }
            return true;
        }
    }

}
