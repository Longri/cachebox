package CB_Core.GL_UI.Activitys.settings;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;

public class SettingsItemBase extends ListViewItemBackground
{
	protected Label lblName, lblDefault;

	private static float MesuredLabelHeight = -1;

	public SettingsItemBase(CB_RectF rec, int Index, String Name)
	{
		super(rec, Index, Name);
		Initial();
		Left = Right = Top = Bottom = LeftWidth / 2;

		if (MesuredLabelHeight == -1) MesuredLabelHeight = Fonts.MesureSmall("Tg").height;

		CB_RectF LblRec = new CB_RectF(Left, 0, this.width - Left - Right, this.halfHeight);

		lblDefault = new Label(LblRec, "");
		lblDefault.setFont(Fonts.getSmall());
		this.addChild(lblDefault);

		LblRec.setY(MesuredLabelHeight);
		LblRec.setHeight(this.height - MesuredLabelHeight);

		lblName = new Label(LblRec, "");
		lblName.setFont(Fonts.getNormal());
		this.addChild(lblName);

	}

	@Override
	protected void SkinIsChanged()
	{
	}

	public void setName(String name)
	{
		lblName.setWrappedText(name);
		lblName.setMesuredHeight();
		layout();
	}

	public void setDefault(String def)
	{
		lblDefault.setWrappedText(def);
		lblDefault.setMesuredHeight();
		layout();
	}

	protected void layout()
	{
		float asc = lblDefault.getFont().getDescent() * 2;

		lblDefault.setY(Bottom + asc);

		asc = lblName.getFont().getDescent();

		float a = 0;

		if (lblName.getLineCount() == 1) a = asc *= 2;

		lblName.setY(lblDefault.getMaxY() - (asc * 2) + a);

		this.setHeight(Bottom + lblDefault.getHeight() + lblName.getHeight() - asc);

	}

}