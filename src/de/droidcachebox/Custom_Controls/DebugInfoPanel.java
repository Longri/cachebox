package de.droidcachebox.Custom_Controls;


import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.StringFunctions;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Views.CacheListView;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import android.os.Debug;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout.LayoutParams;



/*
 * Control Tamplate zum Copieren!
 * 
 * XML Layout einbindung �ber :
 * 
    <de.droidcachebox.Custom_Controls.Control_Tamplate
			android:id="@+id/myName" android:layout_height="wrap_content"
			android:layout_width="fill_parent" android:layout_marginLeft="2dip"
			android:layout_marginRight="2dip" android:layout_marginTop="1dip" />
 */





public final class DebugInfoPanel extends View 
{

	public DebugInfoPanel(Context context) 
	{
		super(context);
	}

	public DebugInfoPanel(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		
		
		
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				LayoutParams param = (LayoutParams) getLayoutParams();
				
				
				float distanceX=0;
				float distanceY=0;
				
				
				 switch (event.getAction() & MotionEvent.ACTION_MASK) {
			      case MotionEvent.ACTION_DOWN:
			    	 lastX= event.getX();
			    	 lastY= event.getY();
			         
			         break;



			      

			      case MotionEvent.ACTION_MOVE:
			    	  distanceX = lastX-event.getX();
			    	  distanceY = lastY-event.getY();
			         break;
			      }

				
				
				
				if(distanceX>0 || distanceY>0)
				{
					
					int x = param.rightMargin;
					int y = param.topMargin;
					x += (int) distanceX;
					y -= (int) distanceY;
					
					param.setMargins(0,y,x,0);
					lastX= event.getX();
			    	lastY= event.getY();
					setLayoutParams(param);
				}
				
				return true;
			}
		});
	}

	public DebugInfoPanel(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	
	/*
	 *  Private Member
	 */
	private int height;
	private int width;
	private TextPaint LayoutTextPaint;
	private int LineSep;
	private StaticLayout LayoutMemInfo;
	private int ContentWidth;
	private float lastX;
	private float lastY;
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);
		Rect bounds = new Rect();
		LayoutTextPaint = new TextPaint();
		LayoutTextPaint.setTextSize(Global.scaledFontSize_normal);
		LayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LayoutTextPaint.setColor(Color.WHITE);
		LineSep = bounds.height()/3;
		ContentWidth=width-(Global.CornerSize*2);
		
		LayoutMemInfo = new StaticLayout("1. Zeile " + StringFunctions.newLine() + "2.Zeile" + StringFunctions.newLine() + "3.Zeile", LayoutTextPaint, ContentWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		
		
		
		
		
		// Calc height
		this.height = 0;
		this.height += (LineSep*4)+ LayoutMemInfo.getHeight();
		
		
		
        setMeasuredDimension(this.width, this.height);
	}
	
    private int measure(int measureSpec) 
    {
        int result = 0;
        
        int specSize = MeasureSpec.getSize(measureSpec);

       
            result = specSize;
        
        
        return result;
    }


    private int left;
	private int top;
	@Override
	protected void onDraw(Canvas canvas) 
	{
		 int LineColor = Color.argb(200, 200, 255, 200);
		 int BackColor = Color.argb(100, 50, 70, 50);
	     Rect DrawingRec = new Rect(0,0, width, height);
	     ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, BackColor);
	     
	     
	     left=Global.CornerSize;
	     top=Global.CornerSize;
	     drawMemInfo(canvas);
	     
	     
	     invalidate();
	     
	}
	
	
	private void drawMemInfo(Canvas canvas)
	{
		ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
		android.app.ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);

		int pid [] = {android.os.Process.myPid()};

		
		android.os.Debug.MemoryInfo[] mi = activityManager.getProcessMemoryInfo(pid);

		// calculate total_bytes_used using mi...

		long available_bytes = activityManager.getMemoryClass();
		String line1 = "Memory Info:";
		String line2 = "Gesamt: " + available_bytes * 1024 + " kB";
		String line3 = "Free: " + (available_bytes * 1024 - Debug.getNativeHeapAllocatedSize() / 1024) + " kB";
		LayoutMemInfo = new StaticLayout(line1 + StringFunctions.newLine() +line2 + StringFunctions.newLine() + line3, LayoutTextPaint, ContentWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
	    
		
		ActivityUtils.drawStaticLayout(canvas, LayoutMemInfo, left, top);
		
		
	}
	

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		Log.d("Cachebox", "Size changed to " + w + "x" + h);
	}
	
	public void setHeight(int MyHeight)
	{
		this.height = MyHeight;
		this.invalidate();
	}
	
}
