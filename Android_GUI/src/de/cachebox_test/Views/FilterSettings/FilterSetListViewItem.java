package de.cachebox_test.Views.FilterSettings;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.Sizes;
import de.cachebox_test.Views.FilterSettings.FilterSetListView.FilterSetEntry;

public class FilterSetListViewItem extends View
{
	private FilterSetEntry mFilterSetEntry;

	private static int width;
	private static int height = 0;
	private Context mContext;
	private boolean BackColorChanger = false;
	private StaticLayout layoutEntryName;
	private Resources mRes;
	private ArrayList<FilterSetListViewItem> mChildList = new ArrayList<FilterSetListViewItem>();

	private static TextPaint textPaint;

	public FilterSetListViewItem(Context context, FilterSetEntry fne, Boolean BackColorId)
	{
		super(context);
		mContext = context;
		mRes = mContext.getResources();
		this.mFilterSetEntry = fne;
		BackColorChanger = BackColorId;

		if (textPaint == null)
		{
			textPaint = new TextPaint();
			textPaint.setTextSize(Sizes.getScaledFontSize());
			textPaint.setColor(Global.getColor(R.attr.TextColor));
			textPaint.setAntiAlias(true);
		}

	}

	public FilterSetEntry getFilterSetEntry()
	{
		return mFilterSetEntry;
	}

	public FilterSetListViewItem addChild(FilterSetListViewItem item)
	{
		mChildList.add(item);
		return item;
	}

	public void toggleChildeViewState()
	{
		if (mChildList != null && mChildList.size() > 0)
		{
			int newState = (mChildList.get(0).getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;

			for (FilterSetListViewItem tmp : mChildList)
			{
				tmp.setVisibility(newState);
			}
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		width = PresetListView.windowW;

		height = Sizes.getIconSize() + Sizes.getCornerSize() * 2;
		setMeasuredDimension(width, height);

	}

	// Draw Methods

	// static Member
	private static Paint TextPaint;

	// private Member
	int left;
	int top;
	int BackgroundColor;

	@Override
	protected void onDraw(Canvas canvas)
	{

		// initial
		left = Sizes.getCornerSize();
		top = Sizes.getCornerSize();

		TextPaint = new Paint();
		TextPaint.setAntiAlias(true);
		TextPaint.setFakeBoldText(true);
		TextPaint.setTextSize((float) (Sizes.getScaledFontSize_big()));
		TextPaint.setColor(Global.getColor(R.attr.TextColor));

		int innerWidth = width - (Sizes.getCornerSize() * 4) - (Sizes.getIconSize() * 2);
		layoutEntryName = new StaticLayout(mFilterSetEntry.getName(), textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

		textPaint.setColor(Global.getColor(R.attr.TextColor));

		boolean selected = false;
		if (this.mFilterSetEntry == FilterSetListView.aktFilterSetEntry) selected = true;

		if (BackColorChanger)
		{
			BackgroundColor = (selected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
		}
		else
		{
			BackgroundColor = (selected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground_secend);
		}

		if (this.mFilterSetEntry.getItemType() != FilterSetListView.COLLABSE_BUTTON_ITEM)
		{
			ActivityUtils.drawFillRoundRecWithBorder(canvas, new Rect(5, 5, width - 5, height - 5), 2,
					Global.getColor(R.attr.ListSeparator), BackgroundColor, Sizes.getCornerSize());
		}

		switch (this.mFilterSetEntry.getItemType())
		{
		case FilterSetListView.COLLABSE_BUTTON_ITEM:
			drawCollabseButtonItem(canvas);
			break;
		case FilterSetListView.CHECK_ITEM:
			drawChkItem(canvas);
			break;
		case FilterSetListView.THREE_STATE_ITEM:
			drawThreeStateItem(canvas);
			break;
		case FilterSetListView.NUMERICK_ITEM:
			drawNumerickItem(canvas);
			break;
		}
		// draw Name
		if (this.mFilterSetEntry.getItemType() == FilterSetListView.THREE_STATE_ITEM)
		{
			left += ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left - 20, top);
		}
		else
		{
			left += ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left + 10, top + 10);
		}

		if (this.mFilterSetEntry.getItemType() == FilterSetListView.NUMERICK_ITEM)
		{
			canvas.drawText(String.valueOf(this.mFilterSetEntry.getNumState()), (float) (width / 1.5), (float) (height / 1.8), TextPaint);
		}

	}

	private static Drawable btnBack;
	// private static Drawable btnBack_pressed;
	private static Drawable minusBtn;
	private static Drawable plusBtn;

	private void drawCollabseButtonItem(Canvas canvas)
	{

		btnBack = mRes.getDrawable(main.N ? R.drawable.night_btn_default_normal : R.drawable.day_btn_default_normal);

		Rect bounds = new Rect(3, 7, width - 3, height);
		btnBack.setBounds(bounds);

		btnBack.draw(canvas);

	}

	private void drawChkItem(Canvas canvas)
	{
		drawIcon(canvas);
		drawRightChkBox(canvas);
		if (this.mFilterSetEntry.getState() == 1)
		{
			Rect oldBounds = Global.Icons[27].getBounds();
			Global.Icons[27].setBounds(rChkBounds);
			Global.Icons[27].draw(canvas);
			Global.Icons[27].setBounds(oldBounds);
		}

	}

	private void drawThreeStateItem(Canvas canvas)
	{
		drawIcon(canvas);
		drawRightChkBox(canvas);
		if (this.mFilterSetEntry.getState() == 1)
		{
			Rect oldBounds = Global.Icons[27].getBounds();
			Global.Icons[27].setBounds(rChkBounds);
			Global.Icons[27].draw(canvas);
			Global.Icons[27].setBounds(oldBounds);
		}
		else if (this.mFilterSetEntry.getState() == -1)
		{
			Rect oldBounds = Global.Icons[28].getBounds();
			Global.Icons[28].setBounds(rChkBounds);
			Global.Icons[28].draw(canvas);
			Global.Icons[28].setBounds(oldBounds);
		}
	}

	private static TextPaint mTextPaint;
	private static StaticLayout layoutPlus;
	private static StaticLayout layoutMinus;
	private static Rect lBounds;
	private static Rect rBounds;
	private static Rect rChkBounds;

	private void drawNumerickItem(Canvas canvas)
	{

		plusBtn = mRes.getDrawable(main.N ? R.drawable.night_btn_default_normal : R.drawable.day_btn_default_normal);
		minusBtn = mRes.getDrawable(main.N ? R.drawable.night_btn_default_normal : R.drawable.day_btn_default_normal);

		lBounds = new Rect(7, 7, height, height - 7);
		minusBtn.setBounds(lBounds);

		rBounds = new Rect(width - height - 7, 7, width - 7, height - 7);
		plusBtn.setBounds(rBounds);

		mTextPaint = new TextPaint();
		mTextPaint.setTextSize(Sizes.getScaledFontSize() * 3);
		mTextPaint.setColor(Global.getColor(R.attr.TextColor));
		mTextPaint.setAntiAlias(true);
		mTextPaint.setFakeBoldText(true);

		layoutMinus = new StaticLayout("-", mTextPaint, height - 7, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
		layoutPlus = new StaticLayout("+", mTextPaint, height - 7, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

		// draw [-] Button
		minusBtn.draw(canvas);

		// draw [+] Button
		plusBtn.draw(canvas);

		// draw +/- on Button
		ActivityUtils.drawStaticLayout(canvas, layoutMinus, 4, top - 3);
		ActivityUtils.drawStaticLayout(canvas, layoutPlus, width - 5 - height, top);

		left += minusBtn.getBounds().width() + minusBtn.getBounds().left;

		if (mFilterSetEntry.getIcon() != null)
		{
			ActivityUtils.PutImageTargetHeight(canvas, mFilterSetEntry.getIcon(), left, top, Sizes.getIconSize() / 2);
			top += Sizes.getIconSize() / 1.5;
		}

	}

	private void drawIcon(Canvas canvas)
	{
		if (mFilterSetEntry.getIcon() != null) left += ActivityUtils.PutImageTargetHeight(canvas, mFilterSetEntry.getIcon(), left, top,
				Sizes.getIconSize()) + Sizes.getIconSize() / 2;

	}

	private void drawRightChkBox(Canvas canvas)
	{
		if (rBounds == null || rChkBounds == null)
		{
			rBounds = new Rect(width - height - 7, 7, width - 7, height - 7);// =
																				// right
																				// Button
																				// bounds
			int halfSize = rBounds.width() / 4;
			int corrRecSize = (rBounds.width() - rBounds.height()) / 2;
			rChkBounds = new Rect(rBounds.left + halfSize, rBounds.top + halfSize - corrRecSize, rBounds.right - halfSize, rBounds.bottom
					- halfSize + corrRecSize);
		}
		ActivityUtils.drawFillRoundRecWithBorder(canvas, rChkBounds, 3, Global.getColor(R.attr.ListSeparator), BackgroundColor,
				Sizes.getCornerSize());
	}

	public void plusClick()
	{
		this.mFilterSetEntry.plusClick();
	}

	public void minusClick()
	{
		this.mFilterSetEntry.minusClick();
	}

	public void stateClick()
	{
		this.mFilterSetEntry.stateClick();
	}

	public void setValue(int value)
	{

		this.mFilterSetEntry.setState(value);

	}

	public void setValue(float value)
	{
		this.mFilterSetEntry.setState(value);

	}

	public int getChecked()
	{
		return mFilterSetEntry.getState();
	}

	public float getValue()
	{
		return (float) mFilterSetEntry.getNumState();
	}

	public FilterSetListViewItem getChild(int i)
	{
		return mChildList.get(i);
	}

	public void setValue(boolean b)
	{
		this.mFilterSetEntry.setState(b ? 1 : 0);
	}

	public int getChildLength()
	{
		return mChildList.size();
	}

	public boolean getBoolean()
	{
		if (mFilterSetEntry.getState() == 0) return false;

		return true;
	}

}