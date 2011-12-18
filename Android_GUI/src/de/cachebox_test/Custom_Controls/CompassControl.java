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

package de.cachebox_test.Custom_Controls;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import android.content.Context;
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
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;

public final class CompassControl extends View
{

	public CompassControl(Context context)
	{
		super(context);
		init();
	}

	public CompassControl(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();

		rimColorFilter = Global.getColor(R.attr.Compass_rimColorFilter);
		faceColorFilter = Global.getColor(R.attr.Compass_faceColorFilter);
		TextColor = Global.getColor(R.attr.Compass_TextColor);
		N_TextColor = Global.getColor(R.attr.Compass_N_TextColor);

	}

	public CompassControl(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	/*
	 * Private Member
	 */

	// drawing tools
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;

	private RectF faceRect;
	private Bitmap faceTexture;
	private Paint facePaint;
	private Paint rimShadowPaint;

	private Paint scalePaint;
	private RectF scaleRect;

	private Paint distancePaint;
	private Path distancePath;

	private Paint arrowPaint;
	private Bitmap arrow;
	private Matrix arrowMatrix;
	private float arrowScale;

	private Paint backgroundPaint;
	// end drawing tools

	private Bitmap background; // holds the cached static part
	private Bitmap scaleCache; // holds the cached static part

	// scale configuration
	private static final int totalNicks = 180;
	private static final float degreesPerNick = 2f;
	private static int centerDegree = 0; // the one in the top center (12
											// o'clock)
	private static int cacheDegree = 90; // Richtung zum Cache
	private static final int minDegrees = 0;
	private static final int maxDegrees = 360;

	// Stylable Colors
	private int rimColorFilter = Color.argb(255, 0, 50, 0);
	private int faceColorFilter = Color.argb(255, 30, 255, 30);
	private int TextColor = Color.argb(255, 0, 0, 0);
	private int N_TextColor = Color.argb(255, 200, 0, 0);

	private String distance = "distance";

	public void init()
	{

		rimColorFilter = Global.getColor(R.attr.Compass_rimColorFilter);
		faceColorFilter = Global.getColor(R.attr.Compass_faceColorFilter);
		TextColor = Global.getColor(R.attr.Compass_TextColor);
		N_TextColor = Global.getColor(R.attr.Compass_N_TextColor);

		initDrawingTools();
		regenerateBackground();
		regenarateScale();
		this.invalidate();
	}

	private void initDrawingTools()
	{
		rimRect = new RectF(0.05f, 0.05f, 0.95f, 0.95f);

		// the linear gradient is a bit skewed for realism
		rimPaint = new Paint();
		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f, main.N ? Color.rgb(0x40, 0x45, 0x40) : Color.rgb(0xf0, 0xf5, 0xf0),
				Color.rgb(0x30, 0x31, 0x30), Shader.TileMode.CLAMP));

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
		rimCirclePaint.setStrokeWidth(0.005f);

		float rimSize = 0.02f;
		faceRect = new RectF();
		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, rimRect.right - rimSize, rimRect.bottom - rimSize);

		faceTexture = BitmapFactory.decodeResource(getContext().getResources(), !Config.settings.nightMode.getValue() ? R.drawable.plastic
				: R.drawable.night_plastic);

		BitmapShader paperShader = new BitmapShader(faceTexture, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
		Matrix paperMatrix = new Matrix();
		facePaint = new Paint();
		facePaint.setFilterBitmap(true);
		paperMatrix.setScale(1.0f / faceTexture.getWidth(), 1.0f / faceTexture.getHeight());
		paperShader.setLocalMatrix(paperMatrix);
		facePaint.setStyle(Paint.Style.FILL);
		facePaint.setShader(paperShader);

		rimShadowPaint = new Paint();
		rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect.width() / 2.0f, new int[]
			{ 0x00000000, 0x00000500, 0x50000500 }, new float[]
			{ 0.96f, 0.96f, 0.99f }, Shader.TileMode.MIRROR));
		rimShadowPaint.setStyle(Paint.Style.FILL);

		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(0x9f004d0f);
		scalePaint.setStrokeWidth(0.005f);
		scalePaint.setAntiAlias(true);

		scalePaint.setTextSize(0.045f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextScaleX(0.8f);
		scalePaint.setTextAlign(Paint.Align.CENTER);

		float scalePosition = 0.10f;
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition, faceRect.right - scalePosition, faceRect.bottom
				- scalePosition);

		distancePaint = new Paint();
		distancePaint.setColor(N ? 0xafBA6109 : 0xaf946109);
		distancePaint.setAntiAlias(true);
		distancePaint.setTypeface(Typeface.DEFAULT_BOLD);
		distancePaint.setTextAlign(Paint.Align.CENTER);
		distancePaint.setTextSize(0.12f);
		distancePaint.setTextScaleX(0.9f);

		distancePath = new Path();
		distancePath.addArc(new RectF(0.18f, 0.18f, 0.82f, 0.82f), -180.0f, -180.0f);

		arrowPaint = new Paint();
		arrowPaint.setFilterBitmap(true);

		int arroeResource = 0;
		if (Config.settings.nightMode.getValue())
		{
			if (Config.settings.isChris.getValue())
			{
				arroeResource = R.drawable.chris_compass_arrow;
			}
			else
			{
				arroeResource = R.drawable.compass_arrow;
			}
		}
		else
		{
			if (Config.settings.isChris.getValue())
			{
				arroeResource = R.drawable.chris_compass_arrow;
			}
			else
			{
				arroeResource = R.drawable.compass_arrow;
			}
		}

		arrow = BitmapFactory.decodeResource(getContext().getResources(), arroeResource);
		arrowMatrix = new Matrix();
		arrowScale = (1.0f / arrow.getWidth()) * 0.4f;
		;
		arrowMatrix.setScale(arrowScale, arrowScale);

		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		this.invalidate();
	}

	private float centerDrawingPointX;
	private float centerDrawingPointY;

	private void drawRim(Canvas canvas)
	{
		// set the ColorFilter to Paints
		LightingColorFilter colorFilter = new LightingColorFilter(0xff888888, rimColorFilter);
		rimPaint.setColorFilter(colorFilter);
		rimCirclePaint.setColorFilter(colorFilter);
		rimShadowPaint.setColorFilter(colorFilter);

		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);
		// now the outer rim circle
		canvas.drawOval(rimRect, rimCirclePaint);
	}

	private void drawFace(Canvas canvas)
	{
		// set the ColorFilter to Paints
		LightingColorFilter colorFilter = new LightingColorFilter(0xffffffff, faceColorFilter);
		facePaint.setColorFilter(colorFilter);

		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);
		// draw the rim shadow inside the face
		canvas.drawOval(faceRect, rimShadowPaint);
	}

	private void drawScale(Canvas canvas)
	{
		scalePaint.setColor(TextColor);
		canvas.drawOval(scaleRect, scalePaint);

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		// canvas.rotate(-centerDegree, 0.5f, 0.5f);
		for (int i = 0; i < totalNicks; ++i)
		{
			float y1 = scaleRect.top;
			float y2 = y1 - 0.020f;

			// restore Color and Size
			scalePaint.setColor(TextColor);
			scalePaint.setTextSize(0.045f);

			canvas.drawLine(0.5f, y1, 0.5f, y2, scalePaint);

			if (i % 15 == 0)
			{
				int value = (int) (i * degreesPerNick);
				String valueString;
				if (value == 0)
				{
					valueString = "N";
					scalePaint.setTextSize(0.06f);
					scalePaint.setColor(N_TextColor);
				}
				else if (value == 90)
				{
					valueString = "E";
					scalePaint.setTextSize(0.06f);
				}
				else if (value == 180)
				{
					valueString = "S";
					scalePaint.setTextSize(0.06f);
				}
				else if (value == 270)
				{
					valueString = "W";
					scalePaint.setTextSize(0.06f);
				}
				else
				{
					valueString = Integer.toString(value);

				}

				canvas.drawText(valueString, 0.5f, y2 - 0.015f, scalePaint);
				if (value >= minDegrees && value <= maxDegrees)
				{

				}
			}

			canvas.rotate(degreesPerNick, 0.5f, 0.5f);
		}
		canvas.restore();
	}

	private void regenarateScale()
	{
		if (scaleCache != null)
		{
			scaleCache.recycle();
		}

		if (getWidth() == 0 || getHeight() == 0)
		{
			return;
		}

		scaleCache = Bitmap.createBitmap(getMyDrawingHeight(), getMyDrawingHeight(), Bitmap.Config.ARGB_8888);
		Canvas scaleCacheCanvas = new Canvas(scaleCache);
		float scale = (float) getMyDrawingHeight();// getHeight();
		scaleCacheCanvas.scale(scale, scale);

		drawScale(scaleCacheCanvas);

	}

	private void drawCachedScale(Canvas canvas)
	{
		if (scaleCache == null) regenarateScale();

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(-centerDegree, centerDrawingPointX + scaleCache.getWidth() / 2, centerDrawingPointY + scaleCache.getHeight() / 2);

		canvas.drawBitmap(scaleCache, centerDrawingPointX, centerDrawingPointY, scalePaint);

		canvas.restore();
	}

	private void drawDistance(Canvas canvas)
	{

		canvas.drawTextOnPath(distance, distancePath, 0.0f, 0.0f, distancePaint);
	}

	public Boolean N = false;

	private void drawArrow(Canvas canvas)
	{
		if (GlobalCore.LastValidPosition.Valid || GlobalCore.Marker.Valid)
		{
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate(cacheDegree, 0.5f, 0.5f);
			canvas.translate(0.5f - arrow.getWidth() * arrowScale / 2.0f, 0.5f - arrow.getHeight() * arrowScale / 2.0f);
			canvas.drawBitmap(arrow, arrowMatrix, arrowPaint);
			canvas.restore();
		}
	}

	private void drawBackground(Canvas canvas)
	{
		if (background == null)
		{
			Log.w("CacheBox", "Background not created");
		}
		else
		{
			canvas.drawBitmap(background, centerDrawingPointX, centerDrawingPointY, backgroundPaint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		drawBackground(canvas);
		drawCachedScale(canvas);

		float scale = (float) getMyDrawingHeight();
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(centerDrawingPointX, centerDrawingPointY);
		canvas.scale(scale, scale);

		drawDistance(canvas);
		drawArrow(canvas);

		canvas.restore();

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Logger.DEBUG("Compass Size Changed");

		regenerateBackground();
		regenarateScale();
	}

	private void regenerateBackground()
	{
		// free the old bitmap
		if (background != null)
		{
			background.recycle();
		}

		if (getWidth() == 0 || getHeight() == 0)
		{
			return;
		}

		// background = Bitmap.createBitmap(getWidth(), getHeight(),
		// Bitmap.Config.ARGB_8888);
		background = Bitmap.createBitmap(getMyDrawingHeight(), getMyDrawingHeight(), Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getMyDrawingHeight();// getHeight();
		backgroundCanvas.scale(scale, scale);
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);

	}

	private int getMyDrawingHeight()
	{
		int ret = Math.min(getHeight(), getWidth());
		centerDrawingPointY = (getHeight() - ret) / 1.3f;
		centerDrawingPointX = (getWidth() - ret) / 2;
		return ret;
	}

	public void setInfo(double CompassHeading, double CacheBearing, String CacheDistance)
	{
		cacheDegree = (int) CacheBearing;
		centerDegree = (int) CompassHeading;
		distance = CacheDistance;
		this.invalidate();
	}

	public void setHeight(int value)
	{
		//
		setMeasuredDimension(value, value);

	}

	public void dispose()
	{
		this.destroyDrawingCache();
		rimRect = null;
		rimPaint = null;
		rimCirclePaint = null;
		faceRect = null;
		faceTexture = null;
		facePaint = null;
		rimShadowPaint = null;
		scalePaint = null;
		scaleRect = null;
		distancePaint = null;
		distancePath = null;
		arrowPaint = null;
		arrow.recycle();
		arrow = null;
		arrowMatrix = null;
		backgroundPaint = null;
		background.recycle();
		background = null;
		distance = null;

	}
}