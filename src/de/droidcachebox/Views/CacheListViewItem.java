package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Cache;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class CacheListViewItem extends View {
    private Cache cache;
    private static int width;
    private static int height = 0;
    private static Rect drawRec;
    private boolean BackColorChanger = false;
       
    
    /// <summary>
    /// Spiegelung des Logins bei Gc, damit ich das nicht dauernd aus der
    /// Config lesen muss.
    /// </summary>
    static String gcLogin = "";
    
    
    
    
	public CacheListViewItem(Context context, Cache cache, Boolean BackColorId) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        if (gcLogin.equals(""))gcLogin = Config.GetString("GcLogin");
        BackColorChanger = BackColorId;
        
       }

	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       
        if (CacheListViewItem.height == 0) // H�he ist noch nicht berechnet 
        {
        	CacheListViewItem.width = measureWidth(widthMeasureSpec);
        	CacheListViewItem.height = (int) (Global.scaledFontSize_normal * 5);
        	CacheListViewItem.drawRec = new Rect(5,2,CacheListViewItem.width - 5,CacheListViewItem.height-2);
        }
        
        setMeasuredDimension(CacheListViewItem.width, CacheListViewItem.height);
            
	}
    
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) Global.Paints.Day.ListBackground.measureText(cache.Name) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        width = specSize;
        return result;
    }
   int BackgroundColor;
   static double fakeBearing =0;
   static final Rect myRec = new Rect(2,1, width-2, height-1); 
    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        
        Boolean GlobalSelected = cache == Global.SelectedCache();
       
        if (BackColorChanger)
        {
        	BackgroundColor = (GlobalSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground);
        }
        else
        {
        	BackgroundColor = (GlobalSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground_secend);
        }
        
        cache.DrawInfo(canvas, drawRec, BackgroundColor, Cache.DrawStyle.all);
        Canvas.freeGlCaches();
        
    }

}
