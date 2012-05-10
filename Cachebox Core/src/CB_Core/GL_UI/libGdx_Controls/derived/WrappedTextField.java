package CB_Core.GL_UI.libGdx_Controls.derived;

import java.util.ArrayList;
import java.util.List;

import CB_Core.Math.SimplePointF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.ui.utils.Clipboard;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * A single-line text input field.
 * <p>
 * The preferred height of a text field is the height of the {@link WrappedTextFieldStyle#font} and {@link WrappedTextFieldStyle#background}
 * . The preferred width of a text field is 150, a relatively arbitrary size.
 * <p>
 * The text field will copy the currently selected text when ctrl+c is pressed, and paste any text in the clipboard when ctrl+v is pressed.
 * Clipboard functionality is provided via the {@link Clipboard} interface. Currently there are two standard implementations, one for the
 * desktop and one for Android. The Android clipboard is a stub, as copy & pasting on Android is not supported yet.
 * <p>
 * The text field allows you to specify an {@link OnscreenKeyboard} for displaying a softkeyboard and piping all key events generated by the
 * keyboard to the text field. There are two standard implementations, one for the desktop and one for Android. The desktop keyboard is a
 * stub, as a softkeyboard is not needed on the desktop. The Android {@link OnscreenKeyboard} implementation will bring up the default IME.
 * 
 * @author mzechner
 */
public class WrappedTextField extends Widget
{
	static private final char BACKSPACE = 8;
	static private final char ENTER_DESKTOP = '\r';
	static private final char ENTER_ANDROID = '\n';
	static private final char TAB = '\t';
	static private final char DELETE = 127;
	static private final char BULLET = 149;

	private WrappedTextFieldStyle style;
	private String text, messageText;
	private CharSequence displayText;
	private int cursor;

	public int CursorLine = 0;
	public int CursorRow = 0;
	public int lineCount = 0;
	private Clipboard clipboard;
	private TextFieldListener listener;
	private TextFieldFilter filter;
	private OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();

	private boolean passwordMode;
	private StringBuilder passwordBuffer;

	private final Rectangle fieldBounds = new Rectangle();
	private final TextBounds textBounds = new TextBounds();
	private final Rectangle scissor = new Rectangle();
	private float renderOffset, textOffset;
	private int visibleTextStart, visibleTextEnd;
	private final FloatArray glyphAdvances = new FloatArray();
	private final FloatArray glyphPositions = new FloatArray();
	private ArrayList<SimplePointF> glyphPointF = new ArrayList<SimplePointF>();
	private int[] lineCharCount;
	private boolean[] lineEndsWidthLineBreak;

	private boolean cursorOn = true;
	private float blinkTime = 0.42f;
	private long lastBlink;

	private boolean hasSelection;
	private int selectionStart;
	private float selectionX, selectionWidth;

	private char passwordCharacter = BULLET;

	public WrappedTextField(Skin skin)
	{
		this("", null, skin.getStyle(WrappedTextFieldStyle.class), null);
	}

	public WrappedTextField(String text, Skin skin)
	{
		this(text, null, skin.getStyle(WrappedTextFieldStyle.class), null);
	}

	/**
	 * @param messageText
	 *            Text to show when empty. May be null.
	 */
	public WrappedTextField(String text, String messageText, Skin skin)
	{
		this(text, messageText, skin.getStyle(WrappedTextFieldStyle.class), null);
	}

	public WrappedTextField(WrappedTextFieldStyle style)
	{
		this("", null, style, null);
	}

	public WrappedTextField(String text, WrappedTextFieldStyle style)
	{
		this(text, null, style, null);
	}

	/**
	 * @param messageText
	 *            Text to show when empty. May be null.
	 */
	public WrappedTextField(String text, String messageText, WrappedTextFieldStyle style)
	{
		this(text, messageText, style, null);
	}

	/**
	 * @param messageText
	 *            Text to show when empty. May be null.
	 */
	public WrappedTextField(String text, String messageText, WrappedTextFieldStyle style, String name)
	{
		super(name);
		setStyle(style);
		this.clipboard = Clipboard.getDefaultClipboard();
		setText(text);
		this.messageText = messageText;
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public void setStyle(WrappedTextFieldStyle style)
	{
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	public void setPasswordCharacter(char passwordCharacter)
	{
		this.passwordCharacter = passwordCharacter;
	}

	/**
	 * Returns the text field's style. Modifying the returned style may not have an effect until {@link #setStyle(WrappedTextFieldStyle)} is
	 * called.
	 */
	public WrappedTextFieldStyle getStyle()
	{
		return style;
	}

	@Override
	public void layout()
	{
	}

	private void calculateOffsets()
	{
		if (true) return;

		float position = glyphPositions.get(cursor);
		float distance = position - Math.abs(renderOffset);
		float visibleWidth = width;
		if (style.background != null) visibleWidth -= style.background.getLeftWidth() + style.background.getRightWidth();

		// check whether the cursor left the left or right side of
		// the visible area and adjust renderoffset.
		if (distance <= 0)
		{
			if (cursor > 0) renderOffset = -glyphPositions.get(cursor - 1);
			else
				renderOffset = 0;
		}
		else
		{
			if (distance > visibleWidth)
			{
				renderOffset -= distance - visibleWidth;
			}
		}

		// calculate first visible char based on render offset
		visibleTextStart = 0;
		textOffset = 0;
		float start = Math.abs(renderOffset);
		int len = glyphPositions.size;
		float startPos = 0;
		for (int i = 0; i < len; i++)
		{
			if (glyphPositions.items[i] >= start)
			{
				visibleTextStart = i;
				startPos = glyphPositions.items[i];
				textOffset = glyphPositions.items[visibleTextStart] - start;
				break;
			}
		}

		// calculate last visible char based on visible width and render offset
		visibleTextEnd = Math.min(displayText.length(), cursor + 1);
		for (; visibleTextEnd <= displayText.length(); visibleTextEnd++)
		{
			if (glyphPositions.items[visibleTextEnd] - startPos > visibleWidth) break;
		}
		visibleTextEnd = Math.max(0, visibleTextEnd - 1);

		// calculate selection x position and width
		if (hasSelection)
		{
			int minIndex = Math.min(cursor, selectionStart);
			int maxIndex = Math.max(cursor, selectionStart);
			float minX = Math.max(glyphPositions.get(minIndex), glyphPositions.get(visibleTextStart));
			float maxX = Math.min(glyphPositions.get(maxIndex), glyphPositions.get(visibleTextEnd));
			selectionX = minX;
			selectionWidth = maxX - minX;
		}
	}

	public static int debugCursorPos = 0;
	public static int debugLineCount = 0;
	public static int debugCursorLine = 0;
	public static int debugCursorRow = 0;
	public static String debugCharBeforCursor = "";
	public static int debugRealCursorPos = 0;

	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;
		final TextureRegion selection = style.selection;
		final NinePatch cursorPatch = style.cursor;

		{
			// Debug
			debugCursorPos = cursor;
			debugLineCount = (int) (textBounds.height / font.getLineHeight());
			debugCursorRow = CursorRow;
			debugCursorLine = CursorLine;

			debugCharBeforCursor = debugVorCursor();

			debugRealCursorPos = getRealCusorPos();
		}

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		float bgLeftWidth = 0;
		float bgRightWidth = 0;
		float bgTopHeight = 0;
		if (style.background != null)
		{
			style.background.draw(batch, x, y, width, height);
			bgLeftWidth = style.background.getLeftWidth();
			bgRightWidth = style.background.getRightWidth();
			bgTopHeight = style.background.getTopHeight();
		}

		float textY = (int) (height + font.getDescent() - bgTopHeight);
		calculateOffsets();

		boolean focused = stage != null && stage.getKeyboardFocus() == this;
		if (focused && hasSelection && selection != null)
		{
			batch.draw(selection, x + selectionX + bgLeftWidth + renderOffset, y + textY - textBounds.height - font.getDescent() / 2,
					selectionWidth, textBounds.height);
		}

		if (displayText.length() == 0)
		{
			if (!focused && messageText != null)
			{
				if (style.messageFontColor != null)
				{
					font.setColor(style.messageFontColor.r, style.messageFontColor.g, style.messageFontColor.b, style.messageFontColor.a
							* parentAlpha);
				}
				else
					font.setColor(0.7f, 0.7f, 0.7f, parentAlpha);
				BitmapFont messageFont = style.messageFont != null ? style.messageFont : font;
				font.draw(batch, messageText, x + bgLeftWidth, y + textY);
			}
		}
		else
		{
			font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha);
			// font.draw(batch, displayText, x + bgLeftWidth + textOffset, y + textY, visibleTextStart, visibleTextEnd);
			font.drawWrapped(batch, displayText, x + bgLeftWidth + textOffset, y + textY, width - bgLeftWidth - bgRightWidth);
		}
		if (focused)
		{
			blink();
			if (cursorOn && cursorPatch != null)
			{
				float lineHeight = font.getLineHeight();
				// cursorPatch.draw(batch, x + bgLeftWidth + glyphPositions.get(cursor) + renderOffset - 1, y + textY - textBounds.height
				// - font.getDescent(), cursorPatch.getTotalWidth(), lineHeight + font.getDescent() / 2);

				cursorPatch.draw(batch, x + bgLeftWidth + glyphPointF.get(cursor).x + renderOffset - 1,
						y + textY - lineHeight - font.getDescent() - glyphPointF.get(cursor).y, cursorPatch.getTotalWidth(), lineHeight
								+ font.getDescent() / 2);

			}
		}
	}

	String debugVorCursor()
	{

		// String ret = "" + displayText.charAt(Math.max(getRealCusorPos() - 1, 0));

		String ret = "" + displayText.subSequence((Math.max(getRealCusorPos() - 1, 0)), displayText.length());

		ret = ret.replace("/n", "//N");

		return ret;
	}

	private int getRealCusorPos()
	{
		int ret = 0;

		for (int i = 0; i < CursorLine; i++)
		{
			if (lineEndsWidthLineBreak[i]) ret++;
		}

		ret += cursor;

		return ret;
	}

	private int replaseLineBreak(String str, int replace)
	{
		int pos = str.indexOf("\n");
		if (pos > 0)
		{
			replace++;
			str = str.replaceFirst("\n", "");
		}

		pos = str.indexOf("\r");
		if (pos > 0)
		{
			replace++;
			str = str.replaceFirst("\r", "");
		}

		if (str.contains("\n") || str.contains("\r"))
		{
			replace = replaseLineBreak(str, replace);
		}

		return replace;
	}

	private void updateDisplayText()
	{
		if (passwordMode && style.font.containsCharacter(passwordCharacter))
		{
			if (passwordBuffer == null) passwordBuffer = new StringBuilder(text.length());
			if (passwordBuffer.length() > text.length()) //
			passwordBuffer.setLength(text.length());
			else
			{
				for (int i = passwordBuffer.length(), n = text.length(); i < n; i++)
					passwordBuffer.append(passwordCharacter);
			}
			displayText = passwordBuffer;
		}
		else
			displayText = text;

		calcTextBounds();
	}

	private void blink()
	{
		long time = TimeUtils.nanoTime();
		if ((time - lastBlink) / 1000000000.0f > blinkTime)
		{
			cursorOn = !cursorOn;
			lastBlink = time;
		}
	}

	@Override
	public boolean touchDown(float x, float y, int pointer)
	{
		if (pointer != 0) return false;
		if (stage != null) stage.setKeyboardFocus(this);
		keyboard.show(true);
		clearSelection();
		lastBlink = 0;
		cursorOn = false;

		float lineHeight = style.font.getLineHeight();
		int clickedCursorLine = (int) ((this.height - y + (lineHeight / 2)) / lineHeight) - 1;

		int lineBeginn = 0;
		for (int l = 0; l < clickedCursorLine; l++)
		{
			lineBeginn += lineCharCount[l];
		}

		for (int i = lineBeginn; i < glyphPositions.size; i++)
		{
			float pos = glyphPositions.items[i];
			if (pos > x)
			{
				cursor = Math.max(0, i - 1);
				return true;
			}
		}
		cursor = Math.max(0, glyphPositions.size - 1);
		calcLineRow();
		return true;
	}

	public boolean keyDown(int keycode)
	{
		final BitmapFont font = style.font;

		if (stage != null && stage.getKeyboardFocus() == this)
		{
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
			{
				// paste
				if (keycode == Keys.V) paste();
				// copy
				if (keycode == Keys.C || keycode == Keys.INSERT) copy();
			}
			else if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
			{
				// paste
				if (keycode == Keys.INSERT) paste();
				// cut
				if (keycode == Keys.FORWARD_DEL)
				{
					if (hasSelection)
					{
						copy();
						delete();
					}
				}
				// selection
				if (keycode == Keys.LEFT)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor--;
				}
				if (keycode == Keys.RIGHT)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor++;
				}
				if (keycode == Keys.HOME)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor = 0;
				}
				if (keycode == Keys.END)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor = text.length();
				}

				cursor = Math.max(0, cursor);
				cursor = Math.min(text.length(), cursor);
			}
			else
			{
				// cursor movement or other keys (kill selection)
				if (keycode == Keys.LEFT)
				{
					cursor--;
					clearSelection();
				}
				if (keycode == Keys.RIGHT)
				{
					cursor++;
					clearSelection();
				}
				if (keycode == Keys.HOME)
				{
					cursor = 0;
					clearSelection();
				}
				if (keycode == Keys.END)
				{
					cursor = text.length();
					clearSelection();
				}

				cursor = Math.max(0, cursor);
				cursor = Math.min(text.length(), cursor);
			}

			return true;
		}
		return false;
	}

	private void copy()
	{
		if (hasSelection)
		{
			int minIndex = Math.min(cursor, selectionStart);
			int maxIndex = Math.max(cursor, selectionStart);
			clipboard.setContents(text.substring(minIndex, maxIndex));
		}
	}

	private void paste()
	{
		String content = clipboard.getContents();
		if (content != null)
		{
			// StringBuilder builder = new StringBuilder();
			// for (int i = 0; i < content.length(); i++)
			// {
			// char c = content.charAt(i);
			// if (style.font.containsCharacter(c)) builder.append(c);
			// }
			// content = builder.toString();
			if (!hasSelection)
			{
				text = text.substring(0, cursor) + content + text.substring(cursor, text.length());
				updateDisplayText();
				cursor += content.length();
			}
			else
			{
				int minIndex = Math.min(cursor, selectionStart);
				int maxIndex = Math.max(cursor, selectionStart);

				text = (minIndex > 0 ? text.substring(0, minIndex) : "")
						+ (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
				cursor = minIndex;
				text = text.substring(0, cursor) + content + text.substring(cursor, text.length());
				updateDisplayText();
				cursor = minIndex + content.length();
				clearSelection();
			}
		}
	}

	private void delete()
	{
		int minIndex = Math.min(cursor, selectionStart);
		int maxIndex = Math.max(cursor, selectionStart);
		text = (minIndex > 0 ? text.substring(0, minIndex) : "")
				+ (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
		updateDisplayText();
		cursor = minIndex;
		clearSelection();
	}

	public boolean keyTyped(char character)
	{
		final BitmapFont font = style.font;

		if (stage != null && stage.getKeyboardFocus() == this)
		{
			if (character == BACKSPACE && (cursor > 0 || hasSelection))
			{
				if (!hasSelection)
				{
					text = text.substring(0, cursor - 1) + text.substring(cursor);
					updateDisplayText();
					cursor--;
				}
				else
				{
					delete();
				}
			}
			if (character == DELETE)
			{
				if (cursor < text.length() || hasSelection)
				{
					if (!hasSelection)
					{
						text = text.substring(0, cursor) + text.substring(cursor + 1);
						updateDisplayText();
					}
					else
					{
						delete();
					}
				}
				calcLineRow();
				return true;
			}
			if (character != ENTER_DESKTOP && character != ENTER_ANDROID)
			{
				if (filter != null && !filter.acceptChar(this, character)) return true;
			}
			if (character == TAB || character == ENTER_ANDROID) next(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
					|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT));
			if (font.containsCharacter(character))
			{
				if (!hasSelection)
				{
					text = text.substring(0, cursor) + character + text.substring(cursor, text.length());
					updateDisplayText();
					cursor++;
				}
				else
				{
					int minIndex = Math.min(cursor, selectionStart);
					int maxIndex = Math.max(cursor, selectionStart);

					text = (minIndex > 0 ? text.substring(0, minIndex) : "")
							+ (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
					cursor = minIndex;
					text = text.substring(0, cursor) + character + text.substring(cursor, text.length());
					updateDisplayText();
					cursor++;
					clearSelection();
				}
			}
			if (listener != null) listener.keyTyped(this, character);
			calcLineRow();
			return true;
		}
		else
			return false;
	}

	/**
	 * Focuses the next TextField. If none is found, the keyboard is hidden. Does nothing if the text field is not in a stage.
	 * 
	 * @param up
	 *            If true, the TextField with the same or next smallest y coordinate is found, else the next highest.
	 */
	public void next(boolean up)
	{
		if (stage == null) return;
		WrappedTextField textField = findNextTextField(stage.getActors(), null, up);
		if (textField != null) stage.setKeyboardFocus(textField);
		else
			Gdx.input.setOnscreenKeyboardVisible(false);
	}

	private WrappedTextField findNextTextField(List<Actor> actors, WrappedTextField best, boolean up)
	{
		for (int i = 0, n = actors.size(); i < n; i++)
		{
			Actor actor = actors.get(i);
			if (actor instanceof WrappedTextField)
			{
				if (actor == this) continue;
				if (actor.y == y)
				{
					if (best == null && actor.x >= x ^ up) best = (WrappedTextField) actor;
				}
				else if (actor.y < y ^ up && (best == null || actor.y - y > best.y - y ^ up))
				{
					best = (WrappedTextField) actor;
				}
			}
			if (actor instanceof Group) best = findNextTextField(((Group) actor).getActors(), best, up);
		}
		return best;
	}

	/**
	 * @param listener
	 *            May be null.
	 */
	public void setTextFieldListener(TextFieldListener listener)
	{
		this.listener = listener;
	}

	/**
	 * @param filter
	 *            May be null.
	 */
	public void setTextFieldFilter(TextFieldFilter filter)
	{
		this.filter = filter;
	}

	/** @return May be null. */
	public String getMessageText()
	{
		return messageText;
	}

	/**
	 * Sets the text that will be drawn in the text field if no text has been entered.
	 * 
	 * @parma messageText May be null.
	 */
	public void setMessageText(String messageText)
	{
		this.messageText = messageText;
	}

	public void setText(String text)
	{
		if (text == null) throw new IllegalArgumentException("text cannot be null.");

		// StringBuffer buffer = new StringBuffer();
		// for (int i = 0; i < text.length(); i++)
		// {
		// char c = text.charAt(i);
		// if (font.containsCharacter(c)) buffer.append(c);
		// }
		//
		// this.text = buffer.toString();
		this.text = text;

		updateDisplayText();
		cursor = 0;
		clearSelection();

		// textBounds.set(font.getBounds(displayText));
		calcTextBounds();

	}

	private void calcTextBounds()
	{
		if (width == 0) return;
		BitmapFont font = style.font;
		float bgLeftWidth = 0;
		float bgRightWidth = 0;
		if (style.background != null)
		{
			bgLeftWidth = style.background.getLeftWidth();
			bgRightWidth = style.background.getRightWidth();

		}

		float wrapWidth = width - bgLeftWidth - bgRightWidth;
		int lines = (int) (font.getWrappedBounds(displayText, wrapWidth).height / font.getLineHeight()) + 2;
		lineCharCount = new int[lines];
		lineEndsWidthLineBreak = new boolean[lines];
		int start = 0;
		int numLines = 0;
		int length = displayText.length();
		float maxWidth = 0;

		glyphAdvances.clear();
		glyphPositions.clear();
		glyphPointF.clear();

		while (start < length)
		{
			int newLine = WrappedTextField.indexOf(displayText, '\n', start);
			// Eat whitespace at start of line.
			while (start < newLine)
			{
				if (!WrappedTextField.isWhitespace(displayText.charAt(start))) break;
				start++;
			}
			int lineEnd = start + font.computeVisibleGlyphs(displayText, start, newLine, wrapWidth);
			int nextStart = lineEnd + 1;
			if (lineEnd < newLine)
			{
				// Find char to break on.
				while (lineEnd > start)
				{
					if (WrappedTextField.isWhitespace(displayText.charAt(lineEnd))) break;
					lineEnd--;
				}
				if (lineEnd == start) lineEnd = nextStart - 1; // If no characters to break, show all.
				else
				{
					nextStart = lineEnd;
					// Eat whitespace at end of line.
					while (lineEnd > start)
					{
						if (!WrappedTextField.isWhitespace(displayText.charAt(lineEnd - 1))) break;
						lineEnd--;
					}
				}
			}
			if (lineEnd > start)
			{
				// one line

				float lineWidth = font.getBounds(displayText, start, lineEnd).width;
				maxWidth = Math.max(maxWidth, lineWidth);
				FloatArray lineGlyphAdvances = new FloatArray();
				FloatArray lineGlyphPositions = new FloatArray();

				CharSequence lineChars = displayText.subSequence(start, lineEnd);
				if ((((String) lineChars).endsWith("\n") || ((String) lineChars).endsWith("\r")))
				{

					lineEndsWidthLineBreak[numLines] = true;

				}
				else
				{
					lineEndsWidthLineBreak[numLines] = false;
				}

				// Hier ist die letzte glyph weite immr 0.0
				font.computeGlyphAdvancesAndPositions(lineChars, lineGlyphAdvances, lineGlyphPositions);

				for (int i = 0; i < lineGlyphPositions.size; i++)
				{

					glyphPointF.add(new SimplePointF(lineGlyphPositions.get(i), numLines * font.getLineHeight()));

				}

				glyphAdvances.addAll(lineGlyphAdvances);
				glyphPositions.addAll(lineGlyphPositions);
			}

			lineCharCount[numLines] = lineEnd - start;
			start = nextStart;
			numLines++;
		}

		glyphAdvances.add(0);
		glyphPositions.add(width);

		textBounds.width = maxWidth;
		textBounds.height = (font.getData().capHeight + (numLines - 1) * font.getData().lineHeight) - font.getDescent() * 2;

		Glyph g = font.getData().getGlyph('\r');

		font.computeGlyphAdvancesAndPositions(displayText, glyphAdvances, glyphPositions);

		calcLineRow();
	}

	private void calcLineRow()
	{
		if (lineCharCount != null && lineCharCount.length > 0)
		{
			int count = 0;
			for (int i = 0; i < lineCharCount.length; i++)
			{
				// falsche berechnung bei Wraped Line

				boolean endWithLineBreak = lineEndsWidthLineBreak[i];
				if (!endWithLineBreak) count++;
				count += lineCharCount[i];
				if (cursor < count)
				{
					if (!endWithLineBreak) count--;
					CursorLine = i;
					CursorRow = count - lineCharCount[i] - cursor;
					if (CursorRow < 0) CursorRow *= -1;
					break;
				}
			}
		}
	}

	static int indexOf(CharSequence text, char ch, int start)
	{
		final int n = text.length();
		for (; start < n; start++)
			if (text.charAt(start) == ch) return start;
		return n;
	}

	static boolean isWhitespace(char c)
	{
		switch (c)
		{
		case '\n':
		case '\r':
		case '\t':
		case ' ':
			return true;
		default:
			return false;
		}
	}

	static boolean isLineBraek(char c)
	{
		switch (c)
		{
		case '\n':
		case '\r':
		case '\t':
			return true;
		default:
			return false;
		}
	}

	/** @return Never null, might be an empty string. */
	public String getText()
	{
		return text;
	}

	/** Sets the selected text. */
	public void setSelection(int selectionStart, int selectionEnd)
	{
		if (selectionStart < 0) throw new IllegalArgumentException("selectionStart must be >= 0");
		if (selectionEnd < 0) throw new IllegalArgumentException("selectionEnd must be >= 0");
		selectionStart = Math.min(text.length(), selectionStart);
		selectionEnd = Math.min(text.length(), selectionEnd);
		if (selectionEnd == selectionStart)
		{
			clearSelection();
			return;
		}
		if (selectionEnd < selectionStart)
		{
			int temp = selectionEnd;
			selectionEnd = selectionStart;
			selectionStart = temp;
		}

		hasSelection = true;
		this.selectionStart = selectionStart;
		cursor = selectionEnd;
	}

	public void clearSelection()
	{
		hasSelection = false;
		calcLineRow();
	}

	/** Sets the cursor position and clears any selection. */
	public void setCursorPosition(int cursorPosition)
	{
		if (cursorPosition < 0) throw new IllegalArgumentException("cursorPosition must be >= 0");
		clearSelection();
		cursor = Math.min(cursorPosition, text.length());
	}

	public int getCursorPosition()
	{
		return cursor;
	}

	/** Default is an instance of {@link DefaultOnscreenKeyboard}. */
	public OnscreenKeyboard getOnscreenKeyboard()
	{
		return keyboard;
	}

	public void setOnscreenKeyboard(OnscreenKeyboard keyboard)
	{
		this.keyboard = keyboard;
	}

	public void setClipboard(Clipboard clipboard)
	{
		this.clipboard = clipboard;
	}

	public float getPrefWidth()
	{
		return 150;
	}

	public float getPrefHeight()
	{
		float prefHeight = textBounds.height;
		if (style.background != null) prefHeight += style.background.getBottomHeight() + style.background.getTopHeight();
		return prefHeight;
	}

	/**
	 * If true, the text in this text field will be shown as bullet characters. The font must have character 149 or this will have no
	 * affect.
	 */
	public void setPasswordMode(boolean passwordMode)
	{
		this.passwordMode = passwordMode;
	}

	/**
	 * Interface for listening to typed characters.
	 * 
	 * @author mzechner
	 */
	static public interface TextFieldListener
	{
		public void keyTyped(WrappedTextField textField, char key);
	}

	/**
	 * Interface for filtering characters entered into the text field.
	 * 
	 * @author mzechner
	 */
	static public interface TextFieldFilter
	{
		/**
		 * @param textField
		 * @param key
		 * @return whether to accept the character
		 */
		public boolean acceptChar(WrappedTextField textField, char key);

		static public class DigitsOnlyFilter implements TextFieldFilter
		{
			@Override
			public boolean acceptChar(WrappedTextField textField, char key)
			{
				return Character.isDigit(key);
			}

		}
	}

	/**
	 * An interface for onscreen keyboards. Can invoke the default keyboard or render your own keyboard!
	 * 
	 * @author mzechner
	 */
	static public interface OnscreenKeyboard
	{
		public void show(boolean visible);
	}

	/**
	 * The default {@link OnscreenKeyboard} used by all {@link TextField} instances. Just uses
	 * {@link Input#setOnscreenKeyboardVisible(boolean)} as appropriate. Might overlap your actual rendering, so use with care!
	 * 
	 * @author mzechner
	 */
	static public class DefaultOnscreenKeyboard implements OnscreenKeyboard
	{
		@Override
		public void show(boolean visible)
		{
			Gdx.input.setOnscreenKeyboardVisible(visible);
		}
	}

	/**
	 * The style for a text field, see {@link TextField}.
	 * 
	 * @author mzechner
	 */
	static public class WrappedTextFieldStyle
	{
		/** Optional. */
		public NinePatch background, cursor;
		public BitmapFont font;
		public Color fontColor;
		/** Optional. */
		public TextureRegion selection;
		/** Optional. */
		public BitmapFont messageFont;
		/** Optional. */
		public Color messageFontColor;

		public WrappedTextFieldStyle()
		{
		}

		public WrappedTextFieldStyle(BitmapFont font, Color fontColor, BitmapFont messageFont, Color messageFontColor, NinePatch cursor,
				TextureRegion selection, NinePatch background)
		{
			this.messageFont = messageFont;
			this.messageFontColor = messageFontColor;
			this.background = background;
			this.cursor = cursor;
			this.font = font;
			this.fontColor = fontColor;
			this.selection = selection;
		}

		public WrappedTextFieldStyle(WrappedTextFieldStyle style)
		{
			this.messageFont = style.messageFont;
			if (style.messageFontColor != null) this.messageFontColor = new Color(style.messageFontColor);
			this.background = style.background;
			this.cursor = style.cursor;
			this.font = style.font;
			if (style.fontColor != null) this.fontColor = new Color(style.fontColor);
			this.selection = style.selection;
		}
	}
}