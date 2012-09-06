package CB_Core.GL_UI.Controls.PopUps;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;

/**
 * Eine View Base zur anzeige eines PopUp�s
 * 
 * @author Longri
 */
public abstract class PopUp_Base extends CB_View_Base
{
	public static final int SHOW_TIME_NEVER_CLOSE = -1;
	public static final int SHOW_TIME_NORMAL = 4000;

	public PopUp_Base(CB_RectF rec, String Name)
	{
		super(rec, Name);

	}

	@Override
	protected void Initial()
	{

	}

	public void show(float x, float y)
	{
		show(x, y, SHOW_TIME_NORMAL);
	}

	public void showNotCloseAutomaticly(float x, float y)
	{
		show(x, y, SHOW_TIME_NEVER_CLOSE);
	}

	public void showNotCloseAutomaticly()
	{
		show(this.getX(), this.getY(), SHOW_TIME_NEVER_CLOSE);
	}

	public void show(float x, float y, int msec)
	{
		GL.that.showPopUp(this, x, y);
		if (msec != SHOW_TIME_NEVER_CLOSE) startCloseTimer(msec);
	}

	public void close()
	{
		GL.that.closePopUp(this);
	}

	private void startCloseTimer(int msec)
	{
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				close();
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, msec);

	}

}
