package CB_Core.GL_UI.Controls.Dialogs;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Events.ProgressChangedEvent;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.UiSizes;

public class ProgressDialog extends GL_MsgBox implements ProgressChangedEvent
{
	public float mesuredLabelHeight = 0;

	public ProgressDialog(Size size, String name)
	{
		super(size, name);
		that = this;

		setButtonCaptions(this, MessageBoxButtons.Cancel);
		button3.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// ProgressThread.stop();
				if (CancelRunable != null)
				{

					Timer runTimer = new Timer();
					TimerTask task = new TimerTask()
					{

						@Override
						public void run()
						{
							CancelRunable.run();
						}
					};

					runTimer.schedule(task, 250);
				}
				close();
				return true;
			}
		});

		mesuredLabelHeight = Fonts.Mesure("T").height * 1.5f;

		progressMessageTextView = new Label(Left, margin, this.width - Left - Right, mesuredLabelHeight, "");
		this.addChild(progressMessageTextView);

		CB_RectF rec = new CB_RectF(0, progressMessageTextView.getMaxY() + margin, this.getContentSize().width,
				UiSizes.getButtonHeight() * 0.75f);

		progressBar = new ProgressBar(rec, "");
		progressBar.setProgress(0);
		this.addChild(progressBar);

		messageTextView = new Label(Left, progressBar.getMaxY() + margin, this.width - Left - Right, mesuredLabelHeight, "");
		this.addChild(messageTextView);

	}

	private Label messageTextView;
	private Label progressMessageTextView;
	private ProgressBar progressBar;
	private static Thread ProgressThread;
	private static String titleText;
	private static Runnable CancelRunable;
	private static ProgressDialog that;

	public static ProgressDialog Show(String title, Thread RunThread, Runnable cancel)
	{

		if (ProgressThread != null)
		{
			ProgressThread = null;

		}

		ProgressThread = RunThread;
		titleText = title;
		CancelRunable = cancel;

		ProgressDialog PD = new ProgressDialog(calcMsgBoxSize(title, true, true, true), "");

		PD.setHeight(PD.getHeight() + (PD.mesuredLabelHeight * 2f));

		PD.setTitle(titleText);
		GL_Listener.glListener.showDialog(PD);

		return PD;
	}

	public static void Ready()
	{
		that.close();
	}

	@Override
	public void ProgressChangedEventCalled(String Message, String ProgressMessage, int Progress)
	{
		setProgress(Message, ProgressMessage, Progress);
	}

	@Override
	public void onShow()
	{
		// Registriere Progress Changed Event
		ProgresssChangedEventList.Add(this);
		if (ProgressThread != null)
		{
			Timer runTimer = new Timer();
			TimerTask task = new TimerTask()
			{

				@Override
				public void run()
				{
					ProgressThread.run();
				}
			};

			runTimer.schedule(task, 100);

		}

	}

	@Override
	public void onHide()
	{
		// l�sche Registrierung Progress Changed Event
		ProgresssChangedEventList.Remove(this);
	}

	public void setProgress(final String Msg, final String ProgressMessage, final int value)
	{
		this.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				progressBar.setProgress(value);
				progressMessageTextView.setText(ProgressMessage);
				if (!Msg.equals("")) messageTextView.setText(Msg);
			}
		});

	}

}