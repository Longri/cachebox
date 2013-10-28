package CB_UI.GL_UI.Views;

import java.text.SimpleDateFormat;

import CB_Core.Types.LogEntry;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.interfaces.ICopyPaste;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Clipboard;

public class LogViewItem extends ListViewItemBackground implements ICopyPaste
{
	private static NinePatch backheader;
	private LogEntry logEntry;
	private Image ivTyp;
	private Label lblFoundByName;
	private Label lblDate;
	private Label lblComment;

	private float secondTab = 0;
	private static float MeasuredLabelHeight = 0;

	private Clipboard clipboard = GlobalCore.getDefaultClipboard();

	public LogViewItem(CB_RectF rec, int Index, LogEntry logEntry)
	{
		super(rec, Index, "");
		this.setLongClickable(true);
		this.logEntry = logEntry;
		mBackIsInitial = false;
		MeasuredLabelHeight = Fonts.Measure("T").height * 1.5f;
		headHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + (UI_Size_Base.that.getMargin());

		iniImage();
		iniFoundLabel();
		iniDateLabel();
		iniCommentLabel();
	}

	private void iniImage()
	{
		ivTyp = new Image(getLeftWidth(), this.height - (headHeight / 2) - (UI_Size_Base.that.getButtonHeight() / 1.5f / 2),
				UI_Size_Base.that.getButtonHeight() / 1.5f, UI_Size_Base.that.getButtonHeight() / 1.5f, "");
		this.addChild(ivTyp);
		ivTyp.setDrawable(new SpriteDrawable(SpriteCacheBase.LogIcons.get(logEntry.Type.getIconID())));
		secondTab = ivTyp.getMaxX() + (UI_Size_Base.that.getMargin() * 2);
	}

	private void iniFoundLabel()
	{
		lblFoundByName = new Label(secondTab, this.height - (headHeight / 2) - (MeasuredLabelHeight / 2), width - secondTab
				- getRightWidth() - UI_Size_Base.that.getMargin(), MeasuredLabelHeight, logEntry.Finder);
		this.addChild(lblFoundByName);
	}

	private void iniDateLabel()
	{
		// SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
		SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy");
		String dateString = postFormater.format(logEntry.Timestamp);
		float DateLength = Fonts.Measure(dateString).width;

		lblDate = new Label(this.width - getRightWidth() - DateLength, this.height - (headHeight / 2) - (MeasuredLabelHeight / 2),
				DateLength, MeasuredLabelHeight, dateString);
		this.addChild(lblDate);
	}

	private void iniCommentLabel()
	{
		lblComment = new Label(getLeftWidth(), 0, this.width - getLeftWidthStatic() - getRightWidthStatic()
				- (UI_Size_Base.that.getMargin() * 2), this.height - headHeight - UI_Size_Base.that.getMargin(), "");
		lblComment.setWrappedText(logEntry.Comment);
		this.addChild(lblComment);
	}

	@Override
	protected void Initial()
	{
		backheader = new NinePatch(SpriteCacheBase.getThemedSprite("listrec-header"), 8, 8, 8, 8);
		super.Initial();
	}

	// static Member

	private static float headHeight;
	public static BitmapFontCache cacheNamePaint;

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	public void render(SpriteBatch batch)
	{
		super.render(batch);
		if (backheader != null)
		{
			backheader.draw(batch, 0, this.height - headHeight, this.width, headHeight);
		}
		else
		{
			resetInitial();
		}

	}

	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		isPressed = true;

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isPressed = false;

		return false;
	}

	@Override
	public String pasteFromClipboard()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String copyToClipboard()
	{
		clipboard.setContents(this.logEntry.Comment);
		return this.logEntry.Comment;
	}

	@Override
	public String cutToClipboard()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
