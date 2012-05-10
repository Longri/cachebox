package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

public class TextField extends LibGdx_Host_Control
{

	private com.badlogic.gdx.scenes.scene2d.ui.TextField mTextField;

	public TextField(CB_RectF rec, String Name)
	{

		super(rec, new com.badlogic.gdx.scenes.scene2d.ui.TextField(Style.getTextFieldStyle()), Name);

		mTextField = (com.badlogic.gdx.scenes.scene2d.ui.TextField) getActor();

	}

	@Override
	public void onShow()
	{
		GL_Listener.glListener.addRenderView(this, GL_Listener.FRAME_RATE_IDLE);
	}

	@Override
	public void onStop()
	{
		GL_Listener.glListener.removeRenderView(this);
	}

	public void setText(String text)
	{
		mTextField.setText(text);
	}

	public void setSelection(int selectionStart, int selectionEnd)
	{
		mTextField.setSelection(selectionStart, selectionEnd);
	}

	public void setCursorPosition(int cursorPosition)
	{
		mTextField.setCursorPosition(cursorPosition);
	}

	public String getText()
	{
		return mTextField.getText();
	}

	public void setMsg(String msg)
	{
		mTextField.setMessageText(msg);
	}

}