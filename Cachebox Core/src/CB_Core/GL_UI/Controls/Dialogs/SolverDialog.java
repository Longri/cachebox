package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.Events.KeyboardFocusChangedEvent;
import CB_Core.Events.KeyboardFocusChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.SelectSolverFunction;
import CB_Core.GL_UI.Activitys.SelectSolverFunction.IFunctionResult;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.Linearlayout;
import CB_Core.GL_UI.Controls.Linearlayout.LayoutChanged;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Solver.Functions.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SolverDialog extends ButtonDialog implements OnStateChangeListener, KeyboardFocusChangedEvent
{
	private enum pages
	{
		Nothing, Text, Function, Variable, Operator, Waypoint
	}

	private float initialYpos;

	private ScrollBox scrollBox;
	private Linearlayout mLinearLayout;
	private boolean ignoreStateChange = false;
	private MultiToggleButton btnTxt;
	private MultiToggleButton btnFx;
	private MultiToggleButton btnVar;
	private MultiToggleButton btnOp;
	private MultiToggleButton btnWp;
	private SizeF msgBoxContentSize;
	private float TextFieldHeight;
	private pages page;
	private String sVar;
	private String sForm;
	private float startY;
	// Controls for TextView
	private EditWrapedTextField mFormulaField;

	// Controls for FormulaView
	// Controls for VariableView
	private EditWrapedTextField tbFunction;
	private Button bFunction;
	private EditWrapedTextField[] tbFunctionParam = null;
	private Label[] lFunctionParam = null;

	// Controls for OperatorView
	// Controls for WaypointView

	public interface SloverBackStringListner
	{
		public void BackString(String backString);
	}

	private EditWrapedTextField mVariableField;
	private String mSolverString;

	private SloverBackStringListner mBackStringListner;

	public SolverDialog(CB_RectF rec, String name, String SolverString)
	{
		super(rec, name, "Solver", GlobalCore.Translations.Get("solver_formula"), MessageBoxButtons.OKCancel, MessageBoxIcon.None, null);
		mSolverString = SolverString;
		ignoreStateChange = false;
		page = pages.Nothing;
	}

	private void initialLayout()
	{
		// Split Solver String by =
		String[] solverStrings = mSolverString.split("=", 2);
		sVar = "";
		sForm = "";
		if (solverStrings.length == 1)
		{
			sForm = solverStrings[0];
		}
		else if (solverStrings.length > 1)
		{
			sVar = solverStrings[0];
			sForm = solverStrings[1];
		}

		msgBoxContentSize = getContentSize();
		// initial VariableField
		TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.5f;

		float y = msgBoxContentSize.height - TextFieldHeight;

		CB_RectF rec = new CB_RectF(0, y, msgBoxContentSize.width, TextFieldHeight);
		mVariableField = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "SolverDialogTextField");
		mVariableField.setText(sVar);
		// mVariableField.setMsg("Enter formula");
		addChild(mVariableField);
		y -= TextFieldHeight * 0.8;

		rec = new CB_RectF(0, y, msgBoxContentSize.width, TextFieldHeight);
		Label lbGleich = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "=");
		lbGleich.setFont(Fonts.getNormal());
		lbGleich.setText("=");
		setBackground(SpriteCache.activityBackground);
		addChild(lbGleich);
		y -= TextFieldHeight * 0.8;

		// Buttons zur Auswahl des Dialog-Typs
		float w = msgBoxContentSize.width / 5;
		float x = 0;
		btnTxt = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "TXT");
		addChild(btnTxt);
		x += w;
		btnFx = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "f(x)");
		addChild(btnFx);
		x += w;
		btnVar = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "@");
		addChild(btnVar);
		x += w;
		btnOp = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "+-");
		addChild(btnOp);
		x += w;
		btnWp = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "$GC");
		addChild(btnWp);

		// startposition for further controls
		this.startY = y;

		String caption = GlobalCore.Translations.Get("TXT");
		btnTxt.setText(caption);
		btnTxt.addState(caption, Color.GRAY);
		btnTxt.addState(caption, Color.GREEN);
		btnTxt.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("f(x)");
		btnFx.setText(caption);
		btnFx.addState(caption, Color.GRAY);
		btnFx.addState(caption, Color.GREEN);
		btnFx.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("@");
		btnVar.setText(caption);
		btnVar.addState(caption, Color.GRAY);
		btnVar.addState(caption, Color.GREEN);
		btnVar.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("+-*/");
		btnOp.setText(caption);
		btnOp.addState(caption, Color.GRAY);
		btnOp.addState(caption, Color.GREEN);
		btnOp.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("$GC");
		btnWp.setText(caption);
		btnWp.addState(caption, Color.GRAY);
		btnWp.addState(caption, Color.GREEN);
		btnWp.setOnStateChangedListner(this);

		button3.setText(GlobalCore.Translations.Get("close"));
		button1.setText(GlobalCore.Translations.Get("ok"));

		button1.setOnClickListener(OnOkClickListner);

		// y -= UiSizes.getButtonHeight();
		float restPlatz = this.height - y;
		// Dieses LinearLayout wird dann in eine ScrollBox verpackt, damit dies Scrollbar ist, wenn die L�nge den Anzeige Bereich
		// �berschreitet!

		rec = new CB_RectF(0, y - restPlatz, msgBoxContentSize.width, restPlatz);
		// initial ScrollBox mit einer Inneren H�he des halben rec�s.
		// Die Innere H�he muss angepasst werden, wenn sich die H�he des LinearLayouts ver�ndert hat.
		// Entweder wenn ein Control hinzugef�gt wurde oder wenn eine CollabseBox ge�ffnrt oder geschlossen wird!
		scrollBox = new ScrollBox(rec, rec.getHalfHeight(), "ScrollBox");

		// damit die Scrollbox auch Events erh�llt
		scrollBox.setClickable(true);

		// die ScrollBox erh�lt den Selben Hintergrund wie die Activity und wird damit ein wenig abgegrenzt von den Restlichen Controls
		scrollBox.setBackground(this.getBackground());

		// Initial LinearLayout
		// Dieses wird nur mit der Breite Initialisiert, die H�he ergibt sich aus dem Inhalt
		mLinearLayout = new Linearlayout(rec.getWidth(), "SelectSolverFunction-LinearLayout");

		// damit das LinearLayout auch Events erh�llt
		mLinearLayout.setClickable(true);

		mLinearLayout.setZeroPos();

		// hier setzen wir ein LayoutChanged Listner, um die innere H�he der ScrollBox bei einer ver�nderung der H�he zu setzen!
		mLinearLayout.setLayoutChangedListner(new LayoutChanged()
		{
			@Override
			public void LayoutIsChanged(Linearlayout linearLayout, float newHeight)
			{
				mLinearLayout.setZeroPos();
				scrollBox.setInerHeight(newHeight);
			}
		});

		// add LinearLayout zu ScrollBox und diese zu der Activity
		scrollBox.addChild(mLinearLayout);
		this.addChild(scrollBox);

		showPage(pages.Text);
	}

	private OnClickListener OnOkClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			// damit die �nderungen in sForm gespeichert werden
			showPage(pages.Nothing);
			String result = mVariableField.getText();
			if (result.length() > 0) result += "=";
			result += sForm;
			if (mBackStringListner != null) mBackStringListner.BackString(result);
			GL.that.closeDialog(SolverDialog.this);
			return true;
		}
	};

	public void show(SloverBackStringListner listner)
	{
		mBackStringListner = listner;
		initialLayout();
		GL.that.showDialog(this);

	}

	@Override
	public void onStateChange(GL_View_Base v, int State)
	{
		// Status�nderung eines MultiToggleButtons
		if (State == 1)
		{
			if (v == btnTxt)
			{
				showPage(pages.Text);
			}
			if (v == btnFx)
			{
				showPage(pages.Function);
			}
			if (v == btnVar)
			{
				showPage(pages.Variable);
			}
			if (v == btnOp)
			{
				showPage(pages.Operator);
			}
			if (v == btnWp)
			{
				showPage(pages.Waypoint);
			}
		}
	}

	private void showPage(pages page)
	{
		if (page == this.page) return;
		// set State of buttons
		btnTxt.setState(page == pages.Text ? 1 : 0);
		btnFx.setState(page == pages.Function ? 1 : 0);
		btnVar.setState(page == pages.Variable ? 1 : 0);
		btnOp.setState(page == pages.Operator ? 1 : 0);
		btnWp.setState(page == pages.Waypoint ? 1 : 0);

		// remove old controls
		switch (this.page)
		{
		case Text:
			hidePageText();
			break;
		case Function:
			hidePageFunction();
			break;
		case Variable:
			hidePageVariable();
			break;
		case Operator:
			hidePageOperator();
			break;
		case Waypoint:
			hidePageWaypoint();
			break;
		}
		switch (page)
		{
		case Text:
			showPageText();
			break;
		case Function:
			showPageFunction();
			break;
		case Variable:
			showPageVariable();
			break;
		case Operator:
			showPageOperator();
			break;
		case Waypoint:
			showPageWaypoint();
			break;
		}
		this.page = page;
	}

	private void hidePageWaypoint()
	{
		// TODO Auto-generated method stub

	}

	private void hidePageOperator()
	{
		// TODO Auto-generated method stub

	}

	private void hidePageVariable()
	{

	}

	private void hidePageFunction()
	{
		// ge�nderte Formel merken
		sForm = tbFunction.getText();
		sForm += "(";
		for (int i = 0; i < tbFunctionParam.length; i++)
		{
			if (i > 0) sForm += "; ";
			sForm += tbFunctionParam[i].getText();
		}
		sForm += ")";
		// Parameter entfernen
		removeFunctionParam();
		mLinearLayout.removeChild(tbFunction);
		mLinearLayout.removeChild(bFunction);
		tbFunction = null;
		bFunction = null;
	}

	private void removeFunctionParam()
	{
		if (tbFunctionParam != null)
		{
			for (int i = 0; i < tbFunctionParam.length; i++)
			{
				mLinearLayout.removeChild(tbFunctionParam[i]);
			}
			tbFunctionParam = null;
		}
		if (lFunctionParam != null)
		{
			for (int i = 0; i < lFunctionParam.length; i++)
			{
				mLinearLayout.removeChild(lFunctionParam[i]);
			}
			lFunctionParam = null;
		}
	}

	private void hidePageText()
	{
		// ge�nderten Text merken
		sForm = mFormulaField.getText();
		mLinearLayout.removeChild(mFormulaField);
		mFormulaField = null;
	}

	private void showPageWaypoint()
	{
		// TODO Auto-generated method stub

	}

	private void showPageOperator()
	{
		// TODO Auto-generated method stub

	}

	private void showPageVariable()
	{
	}

	private void showPageFunction()
	{
		// initial VariableField
		float y = startY;
		final CB_RectF rec = new CB_RectF(0, y, msgBoxContentSize.width - TextFieldHeight * 2, TextFieldHeight);
		tbFunction = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "SolverDialogTextField");
		tbFunction.setText(sForm);
		tbFunction.setZeroPos();
		mLinearLayout.addChild(tbFunction);
		float btnWidth = TextFieldHeight * 2;
		bFunction = new Button(scrollBox.getWidth() - scrollBox.getLeftWidth() - scrollBox.getRightWidth() - btnWidth, y, btnWidth,
				TextFieldHeight, "SolverDialogBtnVariable");
		bFunction.setText("F(x)");
		// Funktion aufsplitten nach Funktionsname und Parameter (falls m�glich!)
		String formula = sForm.trim();
		int posKlammerAuf = formula.indexOf("(");
		int posKlammerZu = formula.lastIndexOf(")");
		if ((posKlammerAuf >= 0) && (posKlammerZu > posKlammerAuf))
		{
			// g�ltige Formel erkannt
			String function = formula.substring(0, posKlammerAuf);
			tbFunction.setText(function);
			String parameter = formula.substring(posKlammerAuf + 1, posKlammerZu);
			// Parameter nach ";" trennen
			String[] parameters = parameter.split(";");
			CB_RectF rec2 = rec.copy();
			// Parameter einr�cken
			rec2.setX(rec2.getX() + TextFieldHeight / 2);

			tbFunctionParam = new EditWrapedTextField[parameters.length];
			lFunctionParam = new Label[parameters.length];
			for (int i = 0; i < parameters.length; i++)
			{
				// Eingabefelder f�r die Parameter einf�gen
				rec2.setY(rec2.getY() - TextFieldHeight * 3 / 4);
				lFunctionParam[i] = new Label(rec2.ScaleCenter(0.6f), "LabelFunctionParam");
				lFunctionParam[i].setVAlignment(VAlignment.BOTTOM);
				lFunctionParam[i].setText(GlobalCore.Translations.Get("Parameter") + " " + i);
				lFunctionParam[i].setZeroPos();
				mLinearLayout.addChild(lFunctionParam[i]);

				rec2.setY(rec2.getY() - lFunctionParam[i].getHeight() * 3 / 4);
				tbFunctionParam[i] = new EditWrapedTextField(SolverDialog.this, rec2, EditWrapedTextField.TextFieldType.SingleLine,
						"SolverDialogTextFieldParam");
				tbFunctionParam[i].setText(parameters[i].trim());
				tbFunctionParam[i].setZeroPos();
				mLinearLayout.addChild(tbFunctionParam[i]);

			}
		}
		bFunction.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Funktionsauswahl zeigen
				SelectSolverFunction ssf = new SelectSolverFunction(new IFunctionResult()
				{
					@Override
					public void selectedFunction(Function function)
					{
						if (function == null) return;
						tbFunction.setText(function.getLongLocalName());
						// evtl. vorhandene Parameter-Eingaben entfernen
						removeFunctionParam();
						tbFunctionParam = new EditWrapedTextField[function.getAnzParam()];
						lFunctionParam = new Label[function.getAnzParam()];
						rec.setX(rec.getX() + TextFieldHeight / 2);
						for (int i = 0; i < function.getAnzParam(); i++)
						{
							rec.setY(rec.getY() - TextFieldHeight * 3 / 4);
							lFunctionParam[i] = new Label(rec, "LabelFunctionParam");
							lFunctionParam[i].setText("Parameter " + i);
							lFunctionParam[i].setZeroPos();
							mLinearLayout.addChild(lFunctionParam[i]);
							rec.setY(rec.getY() - lFunctionParam[i].getHeight() * 3 / 4);
							tbFunctionParam[i] = new EditWrapedTextField(SolverDialog.this, rec,
									EditWrapedTextField.TextFieldType.SingleLine, "SolverDialogTextFieldParam");
							tbFunctionParam[i].setZeroPos();
							mLinearLayout.addChild(tbFunctionParam[i]);
						}
					}
				});
				GL.that.showDialog(ssf);
				return true;
			}
		});
		mLinearLayout.addChild(bFunction);
		y -= TextFieldHeight;
	}

	private void showPageText()
	{
		// ButtonDialog bd = new ButtonDialog("Name", "Title", "Message", MessageBoxButtons.OK, MessageBoxIcon.Error, null);
		// bd.Show();
		// if (true) return;
		// initial FormulaField
		float y = startY;
		CB_RectF rec = new CB_RectF(0, y, scrollBox.getWidth() - scrollBox.getLeftWidth() - scrollBox.getRightWidth(), TextFieldHeight);
		mFormulaField = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "SolverDialogTextField");
		mFormulaField.setText(sForm);
		mFormulaField.setZeroPos();
		mLinearLayout.addChild(mFormulaField);
		y -= TextFieldHeight;
	}

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{
		// clear dialog BackGrounds
		if (mHeader9patch != null) mHeader9patch = null;
		if (mFooter9patch != null) mFooter9patch = null;
		if (mCenter9patch != null) mCenter9patch = null;
		if (mTitle9patch != null) mTitle9patch = null;

		super.renderChilds(batch, parentInfo);
	}

	@Override
	public void onShow()
	{
		initialYpos = this.getY();
		KeyboardFocusChangedEventList.Add(this);
	}

	@Override
	public void onHide()
	{
		KeyboardFocusChangedEventList.Remove(this);
	}

	@Override
	public void KeyboardFocusChanged(EditTextFieldBase focus)
	{
		Logger.LogCat("SolverDialog FocusChangedEvent");

		if (focus == null)
		{
			this.setY(initialYpos);
			Logger.LogCat("SolverDialog set InitialPos - noFocus");
		}
		else
		{
			float WorldY = focus.getWorldRec().getY();
			if (UiSizes.getWindowHeight() / 2 > WorldY)
			{
				this.setY(UiSizes.getWindowHeight() - WorldY);
				Logger.LogCat("SolverDialog set Pos - " + (UiSizes.getWindowHeight() - WorldY));
			}
			else
			{
				Logger.LogCat("SolverDialog dont set Pos - " + WorldY);
			}
		}

		GL.that.renderOnce("SolverDialog Y-Pos Changed");
	}
}
