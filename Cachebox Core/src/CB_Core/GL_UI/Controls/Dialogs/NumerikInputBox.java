package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.NumPad;
import CB_Core.GL_UI.Controls.NumPad.keyEventListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;

public class NumerikInputBox extends CB_View_Base
{

	private static NumerikInputBox that;

	public NumerikInputBox(String name)
	{
		super(name);
		that = this;
	}

	private enum type
	{
		intType, doubleType, timeType
	}

	private static type mType;

	public static EditWrapedTextField editText;
	public static returnValueListner mReturnListner;
	public static returnValueListnerDouble mReturnListnerDouble;
	public static returnValueListnerTime mReturnListnerTime;

	public static GL_MsgBox Show(String msg, String title, int initialValue, returnValueListner Listner)
	{
		mReturnListner = Listner;
		mType = type.intType;

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = GL_MsgBox.margin;
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, (msgBoxSize.width - (margin * 2)) / 5 * 4);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new EditWrapedTextField(msgBox, textFieldRec, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setY(margin * 3);
		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());

		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		editText.setHeight(topBottom + SingleLineHeight);

		editText.setOnscreenKeyboard(new OnscreenKeyboard()
		{

			@Override
			public void show(boolean arg0)
			{
				// do nothing, don�t show Keybord
			}
		});
		editText.setFocus();

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - textFieldRec.getHeight());

		Label label = new Label(LabelRec, "MsgBoxLabel");
		label.setZeroPos();
		label.setY(editText.getMaxY() + margin);
		label.setWrappedText(msg);
		msgBox.addChild(label);

		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.addChild(editText);

		// ######### NumPad ################

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withoutDotOkCancel, listner);
		numPad.setY(margin);
		msgBox.addFooterChild(numPad);
		msgBox.setFooterHeight(numPad.getHeight() + (margin * 2));

		GL.that.showDialog(msgBox);

		return msgBox;
	}

	public static GL_MsgBox Show(String msg, String title, double initialValue, returnValueListnerDouble Listner)
	{
		mReturnListnerDouble = Listner;
		mType = type.doubleType;
		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = GL_MsgBox.margin;
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, (msgBoxSize.width - (margin * 2)) / 5 * 4);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new EditWrapedTextField(msgBox, textFieldRec, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setY(margin * 3);
		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());

		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		editText.setHeight(topBottom + SingleLineHeight);

		editText.setOnscreenKeyboard(new OnscreenKeyboard()
		{

			@Override
			public void show(boolean arg0)
			{
				// do nothing, don�t show Keybord
			}
		});
		editText.setFocus();

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - textFieldRec.getHeight());

		Label label = new Label(LabelRec, "MsgBoxLabel");
		label.setZeroPos();
		label.setY(editText.getMaxY() + margin);
		label.setWrappedText(msg);
		msgBox.addChild(label);

		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.addChild(editText);

		// ######### NumPad ################

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withOkCancel, listner);
		numPad.setY(margin);
		msgBox.addFooterChild(numPad);
		msgBox.setFooterHeight(numPad.getHeight() + (margin * 2));

		GL.that.showDialog(msgBox);

		return msgBox;
	}

	public static GL_MsgBox Show(String msg, String title, int initialMin, int initialSec, returnValueListnerTime Listner)
	{
		mReturnListnerTime = Listner;
		mType = type.timeType;

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = GL_MsgBox.margin;
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, (msgBoxSize.width - (margin * 2)) / 5 * 4);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new EditWrapedTextField(msgBox, textFieldRec, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setY(margin * 3);

		String initialValue = String.valueOf(initialMin) + ":" + String.valueOf(initialSec);

		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());

		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		editText.setHeight(topBottom + SingleLineHeight);

		editText.setOnscreenKeyboard(new OnscreenKeyboard()
		{

			@Override
			public void show(boolean arg0)
			{
				// do nothing, don�t show Keybord
			}
		});
		editText.setFocus();

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - textFieldRec.getHeight());

		Label label = new Label(LabelRec, "MsgBoxLabel");
		label.setZeroPos();
		label.setY(editText.getMaxY() + margin);
		label.setWrappedText(msg);
		msgBox.addChild(label);

		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.addChild(editText);

		// ######### NumPad ################

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withDoubleDotOkCancel, listner);
		numPad.setY(margin);
		msgBox.addFooterChild(numPad);
		msgBox.setFooterHeight(numPad.getHeight() + (margin * 2));

		GL.that.showDialog(msgBox);

		return msgBox;
	}

	public interface returnValueListner
	{
		public void returnValue(int value);

		public void cancelClicked();
	}

	public interface returnValueListnerDouble
	{
		public void returnValue(double value);

		public void cancelClicked();
	}

	public interface returnValueListnerTime
	{
		public void returnValue(int min, int sec);

		public void cancelClicked();
	}

	static keyEventListner listner = new keyEventListner()
	{

		@Override
		public void KeyPressed(String value)
		{
			int cursorPos = editText.getCursorPosition();

			if (value.equals("O"))
			{

				String StringValue = editText.getText();

				// Replase Linebraek
				StringValue = StringValue.replace("\n", "");
				StringValue = StringValue.replace("\r", "");

				boolean ParseError = false;

				if (mType == type.doubleType)
				{
					if (mReturnListnerDouble != null)
					{
						try
						{
							double dblValue = Double.parseDouble(StringValue);
							mReturnListnerDouble.returnValue(dblValue);
						}
						catch (NumberFormatException e)
						{
							ParseError = true;
						}
					}
				}
				else if (mType == type.doubleType)
				{
					if (mReturnListner != null)
					{
						try
						{

							int intValue = Integer.parseInt(StringValue);
							mReturnListner.returnValue(intValue);
						}
						catch (NumberFormatException e)
						{
							ParseError = true;
						}
					}
				}

				else if (mType == type.timeType)
				{
					if (mReturnListnerTime != null)
					{
						try
						{
							String[] s = StringValue.split(":");

							int intValueMin = Integer.parseInt(s[0]);
							int intValueSec = Integer.parseInt(s[1]);
							mReturnListnerTime.returnValue(intValueMin, intValueSec);
						}
						catch (NumberFormatException e)
						{
							ParseError = true;
						}
					}
				}

				if (ParseError)
				{
					GL.that.Toast(GlobalCore.Translations.Get("wrongValueEnterd"));
				}
				else
				{
					GL.that.closeDialog(that);
				}

			}
			else if (value.equals("C"))
			{
				if (mType == type.doubleType)

				{
					if (mReturnListnerDouble != null)
					{
						mReturnListnerDouble.cancelClicked();
					}
				}
				else if (mType == type.intType)
				{
					if (mReturnListner != null)
					{
						mReturnListner.cancelClicked();
					}
				}
				else if (mType == type.timeType)
				{
					if (mReturnListnerTime != null)
					{
						mReturnListnerTime.cancelClicked();
					}
				}

				GL.that.closeDialog(that);
			}
			else if (value.equals("<"))
			{
				if (cursorPos == 0) cursorPos = 1; // cursorPos darf nicht 0 sein
				editText.setCursorPosition(cursorPos - 1);
			}
			else if (value.equals(">"))
			{
				editText.setCursorPosition(cursorPos + 1);
			}
			else if (value.equals("D"))
			{
				if (cursorPos > 0)
				{
					String text2 = editText.getText().substring(cursorPos);
					String text1 = editText.getText().substring(0, cursorPos - 1);

					editText.setText(text1 + text2);
					editText.setCursorPosition(cursorPos + -1);
				}
			}
			else
			{
				String text2 = editText.getText().substring(cursorPos);
				String text1 = editText.getText().substring(0, cursorPos);

				editText.setText(text1 + value + text2);
				editText.setCursorPosition(cursorPos + value.length());
			}

		}
	};

	@Override
	public void onShow()
	{
		super.onShow();
		editText.setFocus();
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{
	}

}
