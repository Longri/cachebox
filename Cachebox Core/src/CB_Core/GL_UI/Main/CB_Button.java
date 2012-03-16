package CB_Core.GL_UI.Main;

import java.util.ArrayList;

import CB_Core.GL_UI.ButtonSprites;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.Math.CB_RectF;

public class CB_Button extends Button
{

	ArrayList<CB_ActionButton> mButtonActions;
	ButtonSprites mBtnSprites;

	public CB_Button(CB_RectF rec, String Name, ArrayList<CB_ActionButton> ButtonActions)
	{
		super(rec, Name);
		mButtonActions = ButtonActions;
	}

	public CB_Button(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
	}

	public CB_Button(CB_RectF rec, String Name, ButtonSprites sprites)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		mBtnSprites = sprites;
	}

	public ButtonSprites getSprites()
	{
		return mBtnSprites;
	}

	public void addAction(CB_ActionButton Action)
	{
		mButtonActions.add(Action);
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}