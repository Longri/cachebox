package CB_Core.GL_UI.Views;

import java.text.SimpleDateFormat;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.LogEntry;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LogViewItem extends ListViewItemBackground
{
	private static NinePatch backheader;
	private LogEntry logEntry;
	private Image ivTyp;
	private Label lblFoundByName;
	private Label lblDate;
	private Label lblComment;

	private float secondTab = 0;
	private static float MesuredLabelHeight = 0;

	public LogViewItem(CB_RectF rec, int Index, LogEntry logEntry)
	{
		super(rec, Index, "");

		this.logEntry = logEntry;
		mBackIsInitial = false;
		MesuredLabelHeight = Fonts.Mesure("T").height * 1.5f;
		headHeight = (UiSizes.getButtonHeight() / 1.5f) + (Dialog.margin);

		iniImage();
		iniFoundLabel();
		iniDateLabel();
		iniCommentLabel();
	}

	private void iniImage()
	{
		ivTyp = new Image(LeftWidth, this.height - (headHeight / 2) - (UiSizes.getButtonHeight() / 1.5f / 2),
				UiSizes.getButtonHeight() / 1.5f, UiSizes.getButtonHeight() / 1.5f, "");
		this.addChild(ivTyp);
		ivTyp.setSprite(SpriteCache.LogIcons.get(logEntry.Type.getIconID()));
		secondTab = ivTyp.getMaxX() + (Dialog.margin * 2);
	}

	private void iniFoundLabel()
	{
		lblFoundByName = new Label(secondTab, this.height - (headHeight / 2) - (MesuredLabelHeight / 2), width - secondTab - RightWidth
				- Dialog.margin, MesuredLabelHeight, "");
		lblFoundByName.setFont(Fonts.getNormal());
		lblFoundByName.setText(logEntry.Finder);
		this.addChild(lblFoundByName);
	}

	private void iniDateLabel()
	{
		SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
		String dateString = postFormater.format(logEntry.Timestamp);
		float DateLength = Fonts.Mesure(dateString).width;

		lblDate = new Label(this.width - RightWidth - DateLength, this.height - (headHeight / 2) - (MesuredLabelHeight / 2), DateLength,
				MesuredLabelHeight, "");
		lblDate.setFont(Fonts.getNormal());
		lblDate.setText(dateString);
		this.addChild(lblDate);
	}

	private void iniCommentLabel()
	{
		lblComment = new Label(LeftWidth + Dialog.margin, 0, this.width - LeftWidth - RightWidth - (Dialog.margin * 2), this.height
				- headHeight, "");
		lblComment.setFont(Fonts.getNormal());
		lblComment.setWrappedText(logEntry.Comment);
		this.addChild(lblComment);

	}

	@Override
	protected void Initial()
	{
		backheader = new NinePatch(SpriteCache.getThemedSprite("listrec-header"), 8, 8, 8, 8);
		super.Initial();
	}

	// static Member

	private static float headHeight;
	public static BitmapFontCache cacheNamePaint;

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

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

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return false;
	}
}