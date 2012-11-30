package CB_Core.GL_UI;

import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.interfaces.ViewOptionsMenu;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Types.MoveableList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class CB_View_Base extends GL_View_Base implements ViewOptionsMenu
{

	// protected float LeftWidth; // linker Rand
	// protected float RightWidth; // rechter Rand
	// protected float TopHeight; // oberer Rand
	// protected float BottomHeight; // unterer Rand

	// # Constructors

	public CB_View_Base(String Name)
	{
		super(Name);
	}

	/**
	 * Constructor fuer ein neues GL_View_Base mit Angabe der linken unteren Ecke und der Hoehe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public CB_View_Base(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public CB_View_Base(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height, Parent, Name);
	}

	public CB_View_Base(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	public CB_View_Base(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec, Parent, Name);
	}

	public CB_View_Base(SizeF size, String Name)
	{
		super(size, Name);
	}

	@Override
	public void onFree()
	{

	}

	protected boolean isInitial = false;

	public void resetInitial()
	{
		isInitial = false;
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{

			@Override
			public void run()
			{
				GL.that.renderOnce("ResetInitial");
			}
		}, 50);
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (!isInitial)
		{
			isInitial = true;
			Initial();
		}
	}

	@Override
	protected void renderWithoutScissor(SpriteBatch batch)
	{

	}

	/**
	 * Da die meisten Sprite Initialisierungen von Sprites im Render Thread durchgef�hrt werden m�ssen, wird diese Methode, zu
	 * Initialisierung im ersten Render Durchgang ausgef�hrt.
	 */
	protected abstract void Initial();

	@Override
	public void onRezised(CB_RectF rec)
	{

	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{

		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		return false;
	}

	@Override
	public void dispose()
	{
		// Remove from RenderViews if registered
		GL.that.removeRenderView(this);

		if (childs == null)
		{
			// set this to null!
			setToNull(this);
		}
		else
		{

			try
			{
				synchronized (childs)
				{
					for (GL_View_Base v : childs)
					{
						removeChild(v);
						v.dispose();
					}

					childs.clear();
					// set this to null!
					setToNull(this);
				}
			}
			catch (ConcurrentModificationException e)
			{
				setToNull(this);
			}
			catch (NullPointerException e)
			{
				setToNull(this);
			}
		}

	}

	public static void setToNull(CB_View_Base view)
	{
		if (view.childs == null)
		{
			view = null;
		}
		else
		{
			synchronized (view.childs)
			{
				view.childs = null;
				view = null;
			}
		}
	}

	public int getCildCount()
	{
		if (childs == null) return -1;
		synchronized (childs)
		{
			return childs.size();
		}
	}

	public GL_View_Base addChildDirekt(final GL_View_Base view)
	{
		if (childs == null) return null;
		synchronized (childs)
		{
			if (!childs.contains(view)) childs.add(view);
		}

		return view;
	}

	public GL_View_Base addChildDirektLast(final GL_View_Base view)
	{
		if (childs == null) return null;
		synchronized (childs)
		{
			if (!childs.contains(view)) childs.add(0, view);
		}

		return view;
	}

	public void removeChildsDirekt()
	{
		if (childs == null) return;
		synchronized (childs)
		{
			childs.clear();
		}
	}

	public void removeChildsDirekt(GL_View_Base view)
	{
		if (childs == null) return;
		synchronized (childs)
		{
			if (childs.contains(view)) childs.remove(view);
		}
	}

	public void removeChildsDirekt(MoveableList<GL_View_Base> childs)
	{
		if (childs == null) return;
		synchronized (childs)
		{
			childs.remove(childs);
		}
	}

	public GL_View_Base getChild(int i)
	{
		if (childs == null) return null;
		synchronized (childs)
		{
			if (childs.size() < i || childs.size() == 0) return null;
			return childs.get(i);
		}
	}

	@Override
	public String toString()
	{
		return getName() + " X,Y/Width,Height = " + this.Pos.x + "," + this.Pos.y + "/" + this.width + "," + this.height;
	}

	public float getLeftWidth()
	{
		return drawableBackground != null ? drawableBackground.getLeftWidth() : 0;
	}

	public float getBottomHeight()
	{
		return drawableBackground != null ? drawableBackground.getBottomHeight() : 0;
	}

	public float getRightWidth()
	{
		return drawableBackground != null ? drawableBackground.getRightWidth() : 0;
	}

	public float getTopHeight()
	{
		return drawableBackground != null ? drawableBackground.getTopHeight() : 0;
	}

	// Designing this ( a page, a box, a panel, ...) by adding rows of objects<GL_View_Base>
	// the position and width (stretched equally on this) of the objects is calculated automatically
	private MoveableList<GL_View_Base> row;
	private boolean topdown = true; // false = bottomup
	private float rowYPos = 0;
	private float rowMaxHeight = 0;
	private float xMargin = 0;
	private float yMargin = 0;
	private float leftBorder;
	private float rightBorder;
	private float maxY;
	private float minY = -1;

	/**
	 ** setting the margins between the added objects
	 **/
	public void setMargins(float xMargin, float yMargin)
	{
		this.xMargin = xMargin;
		this.yMargin = yMargin;
	}

	/**
	 ** no borders to use on this (page), if you want
	 **/
	public void setNoBorders()
	{
		if (this.row == null) this.initRow();
		this.leftBorder = 0f;
		this.rightBorder = 0f;
	}

	/**
	 ** setting the borders to use on this (page), if you want
	 **/
	public void setBorders(float l, float r)
	{
		if (this.row == null) this.initRow();
		this.leftBorder = l;
		this.rightBorder = r;
	}

	/**
	 ** start objects at top
	 **/
	public void initRow()
	{
		initRow(true);
	}

	/**
	 ** start objects at top (direction true) or bottom (direction false)
	 **/
	public void initRow(boolean direction)
	{
		if (direction)
		{
			initRow(direction, this.height - this.getTopHeight());
		}
		else
		{
			// starting at 0
			initRow(direction, this.getBottomHeight()); // this.BottomHeight;
		}
	}

	/**
	 ** start objects at this y Position, direction true = topdown
	 **/
	// TODO ob beliebige Position richtig funktioniert bleibt noch zu pr�fen
	private void initRow(boolean direction, float y)
	{
		if (this.row == null)
		{
			this.row = new MoveableList<GL_View_Base>();
		}
		else
		{
			this.row.clear();
		}
		this.rowYPos = y;
		this.leftBorder = this.getLeftWidth();
		this.rightBorder = this.getRightWidth();
		if (minY < 0)
		{
			// nur beim ersten Mal, sonst m�ssen die Werte erhalten bleiben
			if (direction)
			{
				this.minY = this.getBottomHeight();
				this.maxY = y;
			}
			else
			{
				this.minY = y;
				this.maxY = this.height - this.getTopHeight();
			}
		}
		this.topdown = direction;
	}

	/**
	 ** get available height (not filled with objects)
	 **/
	public float getAvailableWidth()
	{
		if (this.row == null) this.initRow();
		return this.width - this.leftBorder - this.rightBorder;
	}

	/**
	 ** get available height (not filled with objects)
	 **/
	public float getAvailableHeight()
	{
		if (this.row == null) this.initRow();
		return this.maxY - this.minY;
	}

	public void adjustHeight()
	{
		this.setHeight(this.getHeight() - this.maxY + this.minY);
		// eigentlich egal, es sollte ja nichts mehr hinzukommen.
		if (this.topdown)
		{
			this.maxY = this.minY;
		}
		else
		{
			this.minY = this.maxY;
		}
	}

	// Note: Final Position and Size of objects is done on addLast
	// Note: Changing of objects (depending on final Position or Size) must be done after addLast
	// Examples: setting Text of a Button, ....
	/**
	 ** Add the object at the end of the current row. the current row will be ended after the object is added.
	 **/
	public void addLast(GL_View_Base c)
	{
		addMe(c, true);
	}

	/**
	 * Add the object at the end of the current row.
	 **/
	public void addNext(GL_View_Base c)
	{
		addMe(c, false);
	}

	/**
	 ** Add the object at the end of the current row. the current row will be ended after the object is added.
	 **/
	public void addLast(GL_View_Base c, float Weight)
	{
		c.setWeight(Weight);
		addMe(c, true);
	}

	/**
	 * Add the object at the end of the current row.
	 **/
	public void addNext(GL_View_Base c, float Weight)
	{
		c.setWeight(Weight);
		addMe(c, false);
	}

	// ===================================================================
	private void addMe(GL_View_Base c, boolean lastInRow)
	// ===================================================================
	{
		if (c == null) return;
		if (this.row == null) this.initRow();
		row.add(c);
		if (lastInRow)
		{
			// Determine this.rowMaxHeight
			this.rowMaxHeight = 0;
			for (GL_View_Base g : this.row)
			{
				if (g.getHeight() > this.rowMaxHeight) this.rowMaxHeight = g.getHeight();
			}
			if (this.topdown)
			{
				this.rowYPos = this.rowYPos - this.rowMaxHeight;
			}
			// Determine width of objects from number of objects in row
			float rowXPos = this.leftBorder;
			float weightedSize = 0;
			for (GL_View_Base g : this.row)
			{
				weightedSize += g.getWeight();
			}
			float objectWidth = (this.width - this.leftBorder - this.rightBorder) / weightedSize - this.xMargin;
			for (GL_View_Base g : this.row)
			{
				g.setWidth(objectWidth);
				g.setPos(rowXPos, this.rowYPos);
				rowXPos = rowXPos + objectWidth * g.getWeight() + this.xMargin;
				this.addChildDirekt(g);
			}
			//
			if (this.topdown)
			{
				this.rowYPos = this.rowYPos - this.yMargin;
				this.maxY = this.rowYPos;
			}
			else
			{
				this.rowYPos = this.rowYPos + this.rowMaxHeight + this.yMargin;
				this.minY = this.rowYPos;
			}
			this.row.clear();
		}
	}

}
