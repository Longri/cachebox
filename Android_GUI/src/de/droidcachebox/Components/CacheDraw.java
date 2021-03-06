package de.droidcachebox.Components;

import CB_Core.Database;
import CB_Core.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_UI.GlobalCore;
import CB_UI_Base.Math.CB_Rect;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Lists.CB_List;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.UnitFormatter;
import android.graphics.*;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CacheDraw {

    public static CB_Rect BearingRec;

    ;
    // Die Cached Bmp wird nur zur Darstellung als Bubble in der MapView benötigt.
    public static Bitmap CachedBitmap;
    private static StaticLayout layoutCacheName;
    private static StaticLayout layoutCacheOwner;
    private static int VoteWidth = 0;
    private static int rightBorder = 0;

    // Static Measured Member
    private static int nameLayoutWidthRightBorder = 0;
    private static int nameLayoutWidth = 0;
    private static Paint DTPaint;
    private static TextPaint namePaint;
    private static long CachedBitmapId = -1;
    private static Paint CachedBitmapPaitnt;
    public String shortDescription;
    public String longDescription;

    public static void DrawInfo(Cache cache, Canvas canvas, int width, int height, int BackgroundColor, DrawStyle drawStyle) {
        int x = 0;
        int y = 0;
        CB_Rect DrawChangedRect = new CB_Rect(x, y, width, height);
        DrawInfo(cache, canvas, DrawChangedRect, BackgroundColor, drawStyle, false);
    }

    public static void ReleaseCacheBMP() {
        if (CachedBitmap != null) {
            CachedBitmap.recycle();
            CachedBitmap = null;
            CachedBitmapId = -1;
        }

    }

    public static void DrawInfo(Cache cache, int Width, int Height, DrawStyle drawStyle) {
        if (CachedBitmap == null || !(CachedBitmapId == cache.Id)) {
            CachedBitmap = Bitmap.createBitmap(Width, Height, Bitmap.Config.ARGB_8888);
            CB_Rect newRec = new CB_Rect(0, 0, Width, Height);
            Canvas scaledCanvas = new Canvas(CachedBitmap);
            scaledCanvas.drawColor(Color.TRANSPARENT);
            DrawInfo(cache, scaledCanvas, newRec, Color.TRANSPARENT, Color.TRANSPARENT, drawStyle, false);
            CachedBitmapId = cache.Id;
        }

    }

    public static void DrawInfo(Cache cache, Canvas canvas, CB_Rect rec, int BackgroundColor, DrawStyle drawStyle, float scale) {
        if (CachedBitmap == null || !(CachedBitmapId == cache.Id)) {
            CachedBitmap = Bitmap.createBitmap(rec.getWidth(), rec.getHeight(), Bitmap.Config.ARGB_8888);
            CB_Rect newRec = new CB_Rect(0, 0, rec.getWidth(), rec.getHeight());
            Canvas scaledCanvas = new Canvas(CachedBitmap);
            scaledCanvas.drawColor(Color.TRANSPARENT);
            DrawInfo(cache, scaledCanvas, newRec, BackgroundColor, Color.RED, drawStyle, false);
            CachedBitmapId = cache.Id;
        }

        if (CachedBitmapPaitnt == null) {
            CachedBitmapPaitnt = new Paint();
            CachedBitmapPaitnt.setAntiAlias(true);
            CachedBitmapPaitnt.setFilterBitmap(true);
            CachedBitmapPaitnt.setDither(true);
        }

        canvas.save();
        canvas.scale(scale, scale);
        canvas.drawBitmap(CachedBitmap, rec.getPos().x / scale, rec.getCrossPos().y / scale, CachedBitmapPaitnt);
        canvas.restore();

    }

    public static void DrawInfo(Cache cache, Canvas canvas, CB_Rect rec, int BackgroundColor, DrawStyle drawStyle, Boolean withoutBearing) {
        DrawInfo(cache, canvas, rec, BackgroundColor, -1, drawStyle, withoutBearing);
    }

    public static void DrawInfo(Cache cache, Canvas canvas, CB_Rect rec, int BackgroundColor, int BorderColor, DrawStyle drawStyle, Boolean withoutBearing) {
        try {
            // init
            Boolean notAvailable = (!cache.isAvailable() || cache.isArchived());
            Boolean GlobalSelected = cache.Id == GlobalCore.getSelectedCache().Id;
            if (BackgroundColor == -1)
                BackgroundColor = GlobalSelected ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
            if (BorderColor == -1)
                BorderColor = Global.getColor(R.attr.ListSeparator);

            final int left = rec.getPos().x + UiSizes.that.getHalfCornerSize();
            final int top = rec.getPos().y + UiSizes.that.getHalfCornerSize();
            final int width = rec.getWidth() - UiSizes.that.getHalfCornerSize();
            final int height = rec.getHeight() - UiSizes.that.getHalfCornerSize();
            final int SDTImageTop = (int) (height - (UiSizes.that.getScaledFontSize() / 0.9)) + rec.getPos().y;
            final int SDTLineTop = SDTImageTop + UiSizes.that.getScaledFontSize();

            // Measure
            if (VoteWidth == 0) // Grössen noch nicht berechnet
            {

                VoteWidth = UiSizes.that.getScaledIconSize() / 2;

                rightBorder = (int) (width * 0.15);
                nameLayoutWidthRightBorder = width - VoteWidth - UI_Size_Base.that.getIconSize() - rightBorder - (UI_Size_Base.that.getScaledFontSize() / 2);
                nameLayoutWidth = width - VoteWidth - UI_Size_Base.that.getIconSize() - (UI_Size_Base.that.getScaledFontSize() / 2);
                DTPaint = new Paint();
                DTPaint.setTextSize((float) (UI_Size_Base.that.getScaledFontSize() * 1.3));
                DTPaint.setAntiAlias(true);
            }
            if (namePaint == null) {
                namePaint = new TextPaint();
                namePaint.setTextSize((float) (UI_Size_Base.that.getScaledFontSize() * 1.3));
            }

            // reset namePaint attr
            namePaint.setColor(Global.getColor(R.attr.TextColor));
            namePaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
            namePaint.setAntiAlias(true);

            DTPaint.setColor(Global.getColor(R.attr.TextColor));

            // Draw roundetChangedRect
            ActivityUtils.drawFillRoundRecWithBorder(canvas, rec, 2, BorderColor, BackgroundColor);

            // Draw Vote
            if (cache.Rating > 0)
                ActivityUtils.PutImageScale(canvas, Global.StarIcons[(int) (cache.Rating * 2)], -90, left, top, (double) UiSizes.that.getScaledIconSize() / 160);

            int correctPos = (int) (UI_Size_Base.that.getScaledFontSize() * 1.3);

            // Draw Icon
            if (cache.CorrectedCoordiantesOrMysterySolved()) {
                ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[19], left + VoteWidth - correctPos, top - UI_Size_Base.that.getScaledFontSize() / 2, UI_Size_Base.that.getIconSize());
            } else {
                ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[cache.Type.ordinal()], left + VoteWidth - correctPos, top - UI_Size_Base.that.getScaledFontSize() / 2, UI_Size_Base.that.getIconSize());
            }

            // Draw Cache Name

            if (notAvailable) {
                namePaint.setColor(Color.RED);
                namePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                namePaint.setAntiAlias(true);
                DTPaint.setAntiAlias(true);
            }

            String dateString = "";

            try {
                SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
                dateString = postFormater.format(cache.getDateHidden());
            } catch (Exception e) {

            }

            String CacheName = (String) TextUtils.ellipsize(cache.getName(), namePaint, nameLayoutWidthRightBorder, TextUtils.TruncateAt.END);

            String drawName = (drawStyle == DrawStyle.withOwner) ? "by " + cache.getOwner() + ", " + dateString : CacheName;

            if (drawStyle == DrawStyle.all) {
                drawName += "\n";
                drawName += cache.getGcCode();
            } else if (drawStyle == DrawStyle.withOwner) {
                drawName = drawName + "\n" + cache.Pos.FormatCoordinate() + "\n" + cache.getGcCode();
            }

            if (drawStyle == DrawStyle.withOwnerAndName || drawStyle == DrawStyle.withOwner) {
                layoutCacheName = new StaticLayout(drawName, namePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } else {
                layoutCacheName = new StaticLayout(drawName, namePaint, nameLayoutWidthRightBorder, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }

            int LayoutHeight = ActivityUtils.drawStaticLayout(canvas, layoutCacheName, left + VoteWidth + UI_Size_Base.that.getIconSize() + 5, top);

            // over draw 3. Cache name line
            int VislinesHeight = LayoutHeight * 2 / layoutCacheName.getLineCount();
            if (layoutCacheName.getLineCount() > 2) {
                Paint backPaint = new Paint();
                backPaint.setColor(BackgroundColor);
                // backPaint.setColor(Color.RED); //DEBUG

                canvas.drawRect(new Rect(left + VoteWidth + UI_Size_Base.that.getIconSize() + 5, SDTImageTop, nameLayoutWidthRightBorder + left + VoteWidth + UI_Size_Base.that.getIconSize() + 5, top + LayoutHeight + VislinesHeight - 7), backPaint);
            }

            // Draw owner and Last Found
            if (drawStyle == DrawStyle.withOwnerAndName && cache.getOwner() != null) {
                String DrawText = "by " + cache.getOwner() + ", " + dateString;

                // trim Owner Name length
                int counter = 0;
                do {
                    DrawText = "by " + cache.getOwner().substring(0, cache.getOwner().length() - counter) + ", " + dateString;
                    counter++;
                } while (((int) namePaint.measureText(DrawText)) >= nameLayoutWidth);

                DrawText = DrawText + "\n" + "\n" + cache.Pos.FormatCoordinate() + "\n" + cache.getGcCode() + "\n";

                String LastFound = getLastFoundLogDate(cache);
                if (!LastFound.equals("")) {
                    DrawText += "\n";
                    DrawText += "last found: " + LastFound;
                }
                layoutCacheOwner = new StaticLayout(DrawText, namePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // layoutCacheOwner= new StaticLayout(DrawText, 0, 30, namePaint,
                // nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false,
                // TextUtils.TruncateAt.START, nameLayoutWidth);
                ActivityUtils.drawStaticLayout(canvas, layoutCacheOwner, left + VoteWidth + UI_Size_Base.that.getIconSize() + 5, top + (VislinesHeight / 2));
            }

            // Draw S/D/T
            int SDTleft = left + 2;
            String CacheSize;
            switch (cache.Size) {
                case micro:
                    CacheSize = "M"; // micro;
                    break;
                case small:
                    CacheSize = "S"; // small;
                    break;
                case regular:
                    CacheSize = "R"; // regular;
                    break;
                case large:
                    CacheSize = "L"; // large;
                    break;
                default:
                    CacheSize = "O"; // other;
                    break;
            }
            canvas.drawText(CacheSize, SDTleft, SDTLineTop, DTPaint);
            SDTleft += UiSizes.that.getSpaceWidth();
            SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.SizeIcons[(cache.Size.ordinal())], SDTleft, SDTImageTop, UI_Size_Base.that.getScaledFontSize());
            SDTleft += UiSizes.that.getTabWidth();
            canvas.drawText("D", SDTleft, SDTLineTop, DTPaint);
            SDTleft += UiSizes.that.getSpaceWidth();
            SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.StarIcons[(int) (cache.getDifficulty() * 2)], SDTleft, SDTImageTop, UI_Size_Base.that.getScaledFontSize());
            SDTleft += UiSizes.that.getTabWidth();
            canvas.drawText("T", SDTleft, SDTLineTop, DTPaint);
            SDTleft += UiSizes.that.getSpaceWidth();
            SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.StarIcons[(int) (cache.getTerrain() * 2)], SDTleft, SDTImageTop, UI_Size_Base.that.getScaledFontSize());
            SDTleft += UiSizes.that.getSpaceWidth();

            // Draw TB
            int numTb = cache.NumTravelbugs;
            if (numTb > 0) {
                SDTleft += ActivityUtils.PutImageScale(canvas, Global.Icons[0], -90, SDTleft, (int) (SDTImageTop - (UI_Size_Base.that.getScaledFontSize() / (UiSizes.that.getTbIconSize() * 0.1))),
                        (double) UI_Size_Base.that.getScaledFontSize() / UiSizes.that.getTbIconSize());
                // SDTleft += space;
                if (numTb > 1)
                    canvas.drawText("x" + String.valueOf(numTb), SDTleft, SDTLineTop, DTPaint);
            }

            // Draw Bearing

            if (drawStyle != DrawStyle.withoutBearing && drawStyle != DrawStyle.withOwnerAndName && !withoutBearing) {

                int BearingHeight = (int) ((rec.getRight() - rightBorder < SDTleft) ? rec.getTop() - (UI_Size_Base.that.getScaledFontSize() * 2) : rec.getTop() - (UI_Size_Base.that.getScaledFontSize() * 0.8));

                if (BearingRec == null)
                    BearingRec = new CB_Rect(rec.getRight() - rightBorder, rec.getBottom(), rec.getRight(), BearingHeight);
                DrawBearing(cache, canvas, BearingRec);
            }

            if (cache.isFound()) {

                ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[2], left + VoteWidth - correctPos + UI_Size_Base.that.getIconSize() / 2, top - UI_Size_Base.that.getScaledFontSize() / 2 + UI_Size_Base.that.getIconSize() / 2,
                        UI_Size_Base.that.getIconSize() / 2);// Smile
            }

            if (cache.isFavorite()) {
                ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[19], left + VoteWidth - correctPos + 2, top, UI_Size_Base.that.getIconSize() / 2);
            }

            if (cache.isArchived()) {
                ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[24], left + VoteWidth - correctPos + 2, top, UI_Size_Base.that.getIconSize() / 2);
            } else if (!cache.isAvailable()) {
                ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[14], left + VoteWidth - correctPos + 2, top, UI_Size_Base.that.getIconSize() / 2);
            }

            if (cache.ImTheOwner()) {
                ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[17], left + VoteWidth - correctPos + UI_Size_Base.that.getIconSize() / 2, top - UI_Size_Base.that.getScaledFontSize() / 2 + UI_Size_Base.that.getIconSize() / 2,
                        UI_Size_Base.that.getIconSize() / 2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void DrawBearing(Cache cache, Canvas canvas, CB_Rect drawingRec) {

        if (Locator.Valid()) {
            Coordinate position = Locator.getCoordinate();
            double heading = Locator.getHeading();
            double bearing = CoordinateGPS.Bearing(CalculationType.FAST, position.getLatitude(), position.getLongitude(), cache.Pos.getLatitude(), cache.Pos.getLongitude());
            double cacheBearing = bearing - heading;
            String cacheDistance = UnitFormatter.DistanceString(cache.Distance(CalculationType.FAST, false));
            DrawBearing(cache, canvas, drawingRec, cacheDistance, cacheBearing);

        }
    }

    private static void DrawBearing(Cache cache, Canvas canvas, CB_Rect drawingRec, String Distance, double Bearing) {

        double scale = (double) UI_Size_Base.that.getScaledFontSize() / UiSizes.that.getArrowScaleList();

        ActivityUtils.PutImageScale(canvas, Global.Arrows[1], Bearing, drawingRec.getLeft(), drawingRec.getBottom(), scale);
        canvas.drawText(Distance, drawingRec.getLeft(), drawingRec.getTop(), DTPaint);

    }

    private static String getLastFoundLogDate(Cache cache) {
        String FoundDate = "";
        CB_List<LogEntry> logs = new CB_List<LogEntry>();
        logs = Database.Logs(cache);// cache.Logs();
        for (int i = 0, n = logs.size(); i < n; i++) {
            LogEntry l = logs.get(i);
            if (l.Type == LogTypes.found) {
                try {
                    SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
                    FoundDate = postFormater.format(l.Timestamp);
                } catch (Exception e) {

                }
                break;
            }
        }
        return FoundDate;
    }

    // Draw Metods
    public enum DrawStyle {
        all, // alle infos
        withoutBearing, // ohne Richtungs-Pfeil
        withoutSeparator, // ohne unterster trennLinie
        withOwner, // mit Owner statt Name
        withOwnerAndName; // mit Owner und Name
    }

}
