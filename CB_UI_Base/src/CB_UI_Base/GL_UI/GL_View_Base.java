/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.GL_UI;

import java.util.ArrayList;
import java.util.Iterator;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.MoveableList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public abstract class GL_View_Base extends CB_RectF
{

	// # CONSTANTS

	/**
	 * Pointer ID for Mouse wheel scrolling up
	 */
	public static final int MOUSE_WHEEL_POINTER_UP = -280272;
	private final ParentInfo myInfoForChild = new ParentInfo();
	protected final Matrix4 rotateMatrix = new Matrix4();

	/**
	 * Pointer ID for Mouse wheel scrolling down
	 */
	public static final int MOUSE_WHEEL_POINTER_DOWN = -280273;

	public static boolean debug = false;
	public static boolean disableScissor = false;

	public boolean withoutScissor = false;

	// # private Member
	protected String name = "";

	protected Drawable drawableBackground;

	/**
	 * Enthaelt alle GL_Views innerhalb dieser Gl_View
	 */
	protected final MoveableList<GL_View_Base> childs = new MoveableList<GL_View_Base>();

	protected OnClickListener mOnClickListener;
	protected OnClickListener mOnLongClickListener;
	protected OnClickListener mOnDoubleClickListener;

	private Pixmap debugRegPixmap = null;
	private Texture debugRegTexture = null;
	private Sprite DebugSprite = null;

	/**
	 * Don't use this Flag direct, use the method isClickable() </br></br> Maby a child is clickable!!
	 */
	private boolean isClickable = false;
	private boolean isLongClickable = false;
	private boolean isDoubleClickable = false;

	private boolean ChildIsClickable = false;
	private boolean ChildIsLongClickable = false;
	private boolean ChildIsDoubleClickable = false;

	protected boolean onTouchUp = false;
	protected boolean onTouchDown = false;
	public Vector2 lastTouchPos;

	private boolean mVisible = true;

	protected GL_View_Base parent;
	protected static int nDepthCounter = 0;

	private boolean enabled = true;

	protected float Weight = 1f;

	// # Constructors

	public GL_View_Base(String Name)
	{
		super();
		name = Name;
	}

	/**
	 * Constructor fuer ein neues GL_View_Base mit Angabe der linken unteren Ecke und der Hoehe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public GL_View_Base(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height);
		name = Name;

	}

	public GL_View_Base(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height);
		parent = Parent;
		name = Name;

	}

	public GL_View_Base(CB_RectF rec, String Name)
	{
		super(rec);
		name = Name;

	}

	public GL_View_Base(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec);
		parent = Parent;
		name = Name;

	}

	public GL_View_Base(SizeF size, String Name)
	{
		super(0, 0, size.width, size.height);
		name = Name;

	}

	public void setVisible(boolean On)
	{
		if (On)
		{
			setVisible();
		}
		else
		{
			setInvisible();
		}
	}

	public void setVisible()
	{
		if (mVisible) return;
		mVisible = true;
		GL.that.renderOnce();
	}

	public void setInvisible()
	{
		if (!mVisible) return;
		mVisible = false;
		GL.that.renderOnce();
	}

	public MoveableList<GL_View_Base> getchilds()
	{
		return childs;
	}

	/**
	 * Gibt die Parent View zur�ck, wenn diese �ber den Constructor �bergeben wurde!
	 * 
	 * @return parent View oder null
	 */
	public GL_View_Base getParent()
	{
		return parent;
	}

	/**
	 * Returns TRUE if with and height >0, is not disposed and is not set to invisible
	 * 
	 * @return
	 */
	public boolean isVisible()
	{
		if (this.isDisposed) return false;
		if (this.getWidth() <= 0f || this.getHeight() <= 0f) return false;
		return mVisible;
	}

	public GL_View_Base addChild(final GL_View_Base view)
	{
		return addChild(view, false);
	}

	public GL_View_Base addChild(final GL_View_Base view, final boolean last)
	{
		if (childs.contains(view)) return view;
		GL.that.RunOnGLWithThreadCheck(new IRunOnGL()
		{
			@Override
			public void run()
			{
				if (last)
				{
					childs.add(0, view);
				}
				else
				{
					childs.add(view);
				}
				chkChildClickable();
			}
		});

		return view;
	}

	public void removeChild(final GL_View_Base view)
	{
		GL.that.RunOnGLWithThreadCheck(new IRunOnGL()
		{
			@Override
			public void run()
			{
				try
				{
					if (childs != null && childs.size() > 0) childs.remove(view);
				}
				catch (Exception e)
				{
				}
				chkChildClickable();
			}
		});
	}

	public void removeChilds()
	{
		GL.that.RunOnGLWithThreadCheck(new IRunOnGL()
		{
			@Override
			public void run()
			{
				try
				{
					if (childs != null && childs.size() > 0) childs.clear();
				}
				catch (Exception e)
				{
				}
				chkChildClickable();
			}
		});
	}

	public void removeChilds(final MoveableList<GL_View_Base> Childs)
	{
		GL.that.RunOnGLWithThreadCheck(new IRunOnGL()
		{
			@Override
			public void run()
			{
				try
				{
					if (childs != null && childs.size() > 0) childs.remove(Childs);
				}
				catch (Exception e)
				{
				}
				chkChildClickable();
			}
		});
	}

	/**
	 * Checks whether any child has the status Clickable. </br>If so, then this view must also Clickable!
	 */
	protected void chkChildClickable()
	{
		boolean tmpClickable = false;
		boolean tmpDblClickable = false;
		boolean tmpLongClickable = false;
		if (childs != null)
		{

			try
			{

				for (int i = 0, n = childs.size(); i < n; i++)
				{
					GL_View_Base tmp = childs.get(i);
					if (tmp != null)
					{
						if (tmp.isClickable()) tmpClickable = true;
						if (tmp.isLongClickable()) tmpLongClickable = true;
						if (tmp.isDblClickable()) tmpDblClickable = true;
					}

				}
			}
			catch (Exception e)
			{
			}
		}

		ChildIsClickable = tmpClickable;
		ChildIsDoubleClickable = tmpDblClickable;
		ChildIsLongClickable = tmpLongClickable;
	}

	protected float leftBorder = 0;
	protected float rightBorder = 0;
	protected float topBorder = 0;
	protected float bottomBorder = 0;
	protected float innerWidth = getWidth();
	protected float innerHeight = getHeight();

	/**
	 ** setting the drawableBackground and changes the Borders (do own Borders afterwards)
	 **/
	public void setBackground(Drawable background)
	{
		if (isDisposed) return;
		drawableBackground = background;
		if (background != null)
		{
			leftBorder = background.getLeftWidth();
			rightBorder = background.getRightWidth();
			topBorder = background.getTopHeight();
			bottomBorder = background.getBottomHeight(); // this.BottomHeight;
		}
		else
		{
			leftBorder = 0;
			rightBorder = 0;
			topBorder = 0;
			bottomBorder = 0; // this.BottomHeight;
		}
		innerWidth = getWidth() - leftBorder - rightBorder;
		innerHeight = getHeight() - topBorder - bottomBorder;
	}

	/**
	 ** no borders to use on this (page), if you want
	 **/
	public void setNoBorders()
	{
		leftBorder = 0f;
		rightBorder = 0f;
		innerWidth = getWidth();
	}

	/**
	 ** setting the borders to use on this (page), if you want
	 **/
	public void setBorders(float l, float r)
	{
		leftBorder = l;
		rightBorder = r;
		innerWidth = getWidth() - l - r;
	}

	public Drawable getBackground()
	{
		return drawableBackground;
	}

	public float getLeftWidth()
	{
		return leftBorder;
	}

	public float getRightWidth()
	{
		return rightBorder;
	}

	public float getTopHeight()
	{
		return topBorder;
	}

	public float getBottomHeight()
	{
		return bottomBorder;
	}

	/**
	 ** get available width (not filled with objects)
	 **/
	public float getInnerWidth()
	{
		return innerWidth;
	}

	/**
	 ** get available height (not filled with objects)
	 **/
	public float getInnerHeight()
	{
		return innerHeight;
	}

	/**
	 * Die renderChilds() Methode wird vom GL_Listner bei jedem Render-Vorgang aufgerufen. Hier wird dann zuerst die render() Methode dieser
	 * View aufgerufen. Danach werden alle Childs iteriert und dessen renderChilds() Methode aufgerufen, wenn die View sichtbar ist
	 * (Visibility).
	 * 
	 * @param batch
	 */
	public void renderChilds(final Batch batch, ParentInfo parentInfo)
	{
		if (myParentInfo == null) return;

		if (this.isDisposed) return;

		if (thisInvalidate)
		{
			myParentInfo.setParentInfo(parentInfo);
			CalcMyInfoForChild();
		}

		if (!withoutScissor)
		{
			if (intersectRec == null || intersectRec.getHeight() + 1 < 0 || intersectRec.getWidth() + 1 < 0) return; // hier gibt es nichts
																														// zu rendern
			if (!disableScissor) Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			Gdx.gl.glScissor((int) intersectRec.getX(), (int) intersectRec.getY(), (int) intersectRec.getWidth() + 1, (int) intersectRec.getHeight() + 1);
		}

		float A = 0, R = 0, G = 0, B = 0; // Farbwerte der batch um diese wieder einzustellen, wenn ein ColorFilter angewandt wurde!

		boolean ColorFilterSeted = false; // Wir benutzen hier dieses Boolean um am ende dieser Methode zu entscheiden, ob wir die alte
											// Farbe des Batches wieder herstellen m�ssen. Wir verlassen uns hier nicht darauf, das
											// mColorFilter!= null ist, da dies in der zwichenzeit passiert sein kann.

		// Set Colorfilter ?
		if (mColorFilter != null)
		{
			ColorFilterSeted = true;
			// zuerst alte Farbe abspeichern, um sie Wieder Herstellen zu k�nnen
			// hier muss jeder Wert einzeln abgespeichert werden, da bei getColor()
			// nur eine Referenz zur�ck gegeben wird
			Color c = batch.getColor();
			A = c.a;
			R = c.r;
			G = c.g;
			B = c.b;

			batch.setColor(mColorFilter);
		}

		// first Draw Background?

		if (drawableBackground != null)
		{
			drawableBackground.draw(batch, 0, 0, getWidth(), getHeight());
		}

		// set rotation
		boolean isRotated = false;

		if (mRotate != 0 || mScale != 1)
		{
			isRotated = true;

			rotateMatrix.idt();
			rotateMatrix.translate(mOriginX, mOriginY, 0);
			rotateMatrix.rotate(0, 0, 1, mRotate);
			rotateMatrix.scale(mScale, mScale, 1);
			rotateMatrix.translate(-mOriginX, -mOriginY, 0);

			batch.setTransformMatrix(rotateMatrix);
		}

		try
		{
			this.render(batch);
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
			// reset Colorfilter ?
			if (ColorFilterSeted)
			{
				// alte abgespeicherte Farbe des Batches wieder herstellen!
				batch.setColor(R, G, B, A);
			}
			return;
		}

		// reverse rotation
		if (isRotated)
		{
			rotateMatrix.idt();
			// rotateMatrix.rotate(0, 0, 1, 0);
			// rotateMatrix.scale(1, 1, 1);

			batch.setTransformMatrix(rotateMatrix);
		}

		if (childs != null && childs.size() > 0)
		{
			for (int i = 0, n = childs.size(); i < n; i++)
			{

				if (i >= childs.size())
				{
					break; // ConcurrentModificationException
				}

				// alle renderChilds() der in dieser GL_View_Base
				// enthaltenen Childs auf rufen.

				try
				{
					GL_View_Base view = childs.get(i);
					// hier nicht view.render(batch) aufrufen, da sonnst die in der
					// view enthaldenen Childs nicht aufgerufen werden.
					try
					{
						if (view != null && !view.isDisposed() && view.isVisible())
						{
							synchronized (view)
							{
								if (childsInvalidate) view.invalidate();

								getMyInfoForChild().setParentInfo(myParentInfo);
								getMyInfoForChild().setWorldDrawRec(intersectRec);

								getMyInfoForChild().add(view.getX(), view.getY());

								batch.setProjectionMatrix(getMyInfoForChild().Matrix());
								nDepthCounter++;

								view.renderChilds(batch, getMyInfoForChild());
								nDepthCounter--;
							}
						}
						else
						{
							if (view != null && view.isDisposed())
							{
								// Remove disposedView from child list
								this.removeChild(view);
							}
						}
					}
					catch (java.lang.IllegalStateException e)
					{
						if (view != null && view.isDisposed())
						{
							// Remove disposedView from child list
							this.removeChild(view);
						}
					}

				}
				catch (java.util.NoSuchElementException e)
				{
					break; // da die Liste nicht mehr g�ltig ist, brechen wir hier den Iterator ab
				}
				catch (java.util.ConcurrentModificationException e)
				{
					break; // da die Liste nicht mehr g�ltig ist, brechen wir hier den Iterator ab
				}
				catch (java.lang.IndexOutOfBoundsException e)
				{
					break; // da die Liste nicht mehr g�ltig ist, brechen wir hier den Iterator ab
				}
			}
			childsInvalidate = false;
		}

		// Draw Debug REC
		if (debug)
		{

			if (DebugSprite != null)
			{
				batch.flush();
				DebugSprite.draw(batch);

			}

		}

		// reset Colorfilter ?
		if (ColorFilterSeted)
		{
			// alte abgespeicherte Farbe des Batches wieder herstellen!
			batch.setColor(R, G, B, A);
		}

	}

	public boolean isDisposed()
	{
		return isDisposed;
	}

	private void writeDebug()
	{
		if (DebugSprite == null)
		{
			try
			{
				GL.that.RunOnGLWithThreadCheck(new IRunOnGL()
				{

					@Override
					public void run()
					{
						// int w = getNextHighestPO2((int) getWidth());
						// int h = getNextHighestPO2((int) getHeight());

						int w = (int) getWidth();
						int h = (int) getHeight();

						debugRegPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
						debugRegPixmap.setColor(1f, 0f, 0f, 1f);
						debugRegPixmap.drawRectangle(1, 1, (int) getWidth() - 1, (int) getHeight() - 1);

						debugRegTexture = new Texture(debugRegPixmap, Pixmap.Format.RGBA8888, false);

						DebugSprite = new Sprite(debugRegTexture, (int) getWidth(), (int) getHeight());
					}
				});

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	public CB_RectF ThisWorldRec = new CB_RectF();
	public CB_RectF intersectRec = new CB_RectF();
	public ParentInfo myParentInfo = new ParentInfo();
	private boolean mustSetScissor = false;
	protected boolean childsInvalidate = false;
	protected boolean thisInvalidate = true;

	public CB_RectF getWorldRec()
	{
		if (ThisWorldRec == null) return new CB_RectF();
		return ThisWorldRec.copy();
	}

	/**
	 * Berechnet das Scissor Rechteck und die Infos fuer die Childs immer dann wenn sich etwas an Position oder Groesse dieses GL_View_Base
	 * geaendert hat. Wenn sich etwas geaendert hat, wird auch ein Invalidate an die Childs uebergeben, da diese auch neu berechnet werden
	 * muessen. Die detection wann sich etwas geaendert hat, kommt von der ueberschriebenen CB_RectF Methode CalcCrossPos, da diese bei
	 * jeder Aenderung aufgerufen wird.
	 */
	protected void CalcMyInfoForChild()
	{
		childsInvalidate = true;
		ThisWorldRec.setRec(this);// .copy().offset(myParentInfo.Vector());
		// ThisWorldRec.offset(myParentInfo.Vector());
		ThisWorldRec.offset(-this.getX() + myParentInfo.Vector().x, -this.getY() + myParentInfo.Vector().y);
		mustSetScissor = !myParentInfo.drawRec().contains(ThisWorldRec);

		if (mustSetScissor)
		{
			intersectRec.setRec(myParentInfo.drawRec().createIntersection(ThisWorldRec));
		}
		else
		{
			intersectRec.setRec(ThisWorldRec);
		}

		thisInvalidate = false;

		if (debug) writeDebug();
	}

	public void invalidate()
	{
		thisInvalidate = true;
	}

	protected abstract void render(Batch batch);

	// ########################
	// Rotate Property
	// ########################
	protected float mRotate = 0;
	protected float mOriginX;
	protected float mOriginY;
	protected float mScale = 1f;
	private boolean isDisposed = false;

	public void setRotate(float Rotate)
	{
		mRotate = Rotate;
	}

	public void setOrigin(float originX, float originY)
	{
		mOriginX = originX;
		mOriginY = originY;
	}

	public void setOriginCenter()
	{
		mOriginX = this.getHalfWidth();
		mOriginY = this.getHalfHeight();
	}

	/**
	 * setzt den Scale Factor des dargestellten Images, wobei die Gr��e nicht ver�ndert wird. Ist das Image gr��er, wird es abgeschnitten
	 * 
	 * @param value
	 */
	public void setScale(float value)
	{
		mScale = value;
	}

	@Override
	public void resize(float width, float height)
	{
		try
		{
			innerWidth = width - leftBorder - rightBorder;
			innerHeight = height - topBorder - bottomBorder;
			onResized(this);
		}
		catch (Exception e1)
		{
			int i = 0;
			i = i + 1;
		}
		DebugSprite = null;

		// Eine Gr��en�nderung an die Childs Melden
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (int i = 0, n = childs.size(); i < n; i++)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = childs.get(i);
					if (view != null) view.onParentRezised(this);
				}
			}
			catch (Exception e)
			{
				// do nothing
			}
		}
	}

	public abstract void onResized(CB_RectF rec);

	public abstract void onParentRezised(CB_RectF rec);

	public void onShow()
	{
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (int i = 0, n = childs.size(); i < n; i++)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = childs.get(i);
					if (view != null) view.onShow();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	public void onHide()
	{
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (int i = 0, n = childs.size(); i < n; i++)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = childs.get(i);
					if (view != null) view.onHide();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	public void onStop()
	{
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (int i = 0, n = childs.size(); i < n; i++)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = childs.get(i);
					view.onStop();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean click(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht �berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown �bergeben!!!
		boolean handled = false;
		try
		{
			if (childs != null && childs.size() > 0)
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
					GL_View_Base view = iterator.next();

					if (view == null || !view.isClickable()) continue;
					// Invisible Views can not be clicked!
					if (!view.isVisible()) continue;

					if (view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						handled = view.click(x - (int) view.getX(), y - (int) view.getY(), pointer, button);

						// if handled, we can break and don't test the rest
						if (handled) break;
					}
				}
			}
			if (!handled)
			{
				// kein Klick in einem untergeordnetem View
				// -> hier behandeln
				if (mOnClickListener != null)
				{
					handled = mOnClickListener.onClick(this, x, y, pointer, button);
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return handled;
	}

	public boolean doubleClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht �berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown �bergeben!!!
		boolean behandelt = false;
		try
		{
			if (childs != null && childs.size() > 0)
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
					GL_View_Base view = iterator.next();

					if (view == null || !view.isClickable()) continue;
					// Invisible Views can not be clicked!
					if (!view.isVisible()) continue;

					if (view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						behandelt = view.doubleClick(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}
				}
			}
			if (!behandelt)
			{
				// kein Klick in einem untergeordnetem View
				// -> hier behandeln
				if (mOnDoubleClickListener != null)
				{
					behandelt = mOnDoubleClickListener.onClick(this, x, y, pointer, button);
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return behandelt;
	}

	public boolean longClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht �berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown �bergeben!!!
		boolean behandelt = false;

		try
		{
			if (childs != null && childs.size() > 0)
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
					GL_View_Base view = iterator.next();

					if (view == null || !view.isClickable()) continue;

					if (view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						behandelt = view.longClick(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}
				}
			}
			if (!behandelt)
			{
				// kein Klick in einem untergeordnetem View
				// -> hier behandeln
				if (mOnLongClickListener != null)
				{
					behandelt = mOnLongClickListener.onClick(this, x, y, pointer, button);
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return behandelt;
	}

	public final GL_View_Base touchDown(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht �berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown �bergeben!!!
		// touchDown liefert die View zur�ck, die dieses TochDown Ereignis angenommen hat
		GL_View_Base resultView = null;

		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
					GL_View_Base view = iterator.next();

					// Invisible Views can not be clicked!
					if (view == null || !view.isVisible()) continue;
					if (!view.getEnabled()) continue;
					if (view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						lastTouchPos = new Vector2(x - view.getX(), y - view.getY());
						resultView = view.touchDown(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}

					if (resultView != null) break;
				}
			}
			catch (Exception e)
			{
				return null;
			}
		}

		if (resultView == null)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			boolean behandelt = onTouchDown(x, y, pointer, button);
			if (behandelt) resultView = this;
		}

		GL.that.renderOnce();
		return resultView;
	}

	public final boolean touchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht �berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown �bergeben!!!
		boolean behandelt = false;

		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					GL_View_Base view = iterator.next();

					if (view != null && view.contains(x, y))
					{
						behandelt = view.touchDragged(x - (int) view.getX(), y - (int) view.getY(), pointer, KineticPan);
					}
					if (behandelt) break;
				}
			}
			catch (Exception e)
			{
				return false;
			}
		}

		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			behandelt = onTouchDragged(x, y, pointer, KineticPan);
		}
		return behandelt;
	}

	public final boolean touchUp(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht �berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown �bergeben!!!
		boolean behandelt = false;

		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					GL_View_Base view = iterator.next();
					if (view != null && view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						behandelt = view.touchUp(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}

					if (behandelt) break;
				}
			}
			catch (Exception e)
			{
				return false;
			}
		}

		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			behandelt = onTouchUp(x, y, pointer, button);
		}

		return behandelt;
	}

	// die untergeordneten Klassen m�ssen diese Event-Handler �berschreiben!!!
	// public abstract boolean onClick(int x, int y, int pointer, int button);

	public abstract boolean onLongClick(int x, int y, int pointer, int button);

	public abstract boolean onTouchDown(int x, int y, int pointer, int button);

	public abstract boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan);

	public abstract boolean onTouchUp(int x, int y, int pointer, int button);

	@Override
	public void dispose()
	{
		isDisposed = true;
		DebugSprite = null;

		try
		{
			if (debugRegTexture != null) debugRegTexture.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (debugRegPixmap != null) debugRegPixmap.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		debugRegPixmap = null;
		debugRegTexture = null;
		debugRegTexture = null;
		name = null;
		data = null;
		mOnClickListener = null;
		mOnLongClickListener = null;
		mOnDoubleClickListener = null;
		drawableBackground = null;
		parent = null;
		DebugSprite = null;
		lastTouchPos = null;

		if (debugRegPixmap != null)
		{
			debugRegPixmap.dispose();
		}
		debugRegPixmap = null;

		if (childs != null)
		{
			for (int i = 0; i < childs.size(); i++)
			{
				childs.get(i).dispose();
			}
			childs.clear();
		}

		super.dispose();
	}

	/**
	 * Interface definition for a callback to be invoked when a view is clicked.
	 */
	public interface OnClickListener
	{
		/**
		 * Called when a view has been clicked.
		 * 
		 * @param v
		 *            The view that was clicked.
		 */
		boolean onClick(GL_View_Base v, int x, int y, int pointer, int button);
	}

	/**
	 * Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
	 * 
	 * @param l
	 *            The callback that will run
	 * @see #setClickable(boolean)
	 */
	public void setOnClickListener(OnClickListener l)
	{
		isClickable = l != null;
		mOnClickListener = l;
	}

	public OnClickListener getOnClickListner()
	{
		return mOnClickListener;
	}

	public OnClickListener getOnLongClickListner()
	{
		return mOnLongClickListener;
	}

	public OnClickListener getOnDblClickListner()
	{
		return mOnDoubleClickListener;
	}

	/**
	 * Register a callback to be invoked when this view is long clicked. If this view is not clickable, it becomes clickable.
	 * 
	 * @param l
	 *            The callback that will run
	 * @see #setClickable(boolean)
	 */
	public void setOnLongClickListener(OnClickListener l)
	{
		isLongClickable = l != null;
		mOnLongClickListener = l;
	}

	/**
	 * Register a callback to be invoked when this view is double clicked. If this view is not clickable, it becomes clickable.
	 * 
	 * @param l
	 *            The callback that will run
	 * @see #setClickable(boolean)
	 */
	public void setOnDoubleClickListener(OnClickListener l)
	{
		isDoubleClickable = l != null;
		mOnDoubleClickListener = l;
	}

	public boolean isDblClickable()
	{
		if (!this.isVisible()) return false;
		return isDoubleClickable | ChildIsDoubleClickable;
	}

	public boolean isLongClickable()
	{
		if (!this.isVisible()) return false;
		return isLongClickable | ChildIsLongClickable;
	}

	public boolean isClickable()
	{
		if (!this.isVisible()) return false;
		return isClickable | ChildIsClickable;
	}

	/**
	 * Setzt dieses View Clicable mit der Uebergabe von True. Wenn dieses View nicht Clickable ist, werden auch keine Click-Abfragen an die
	 * Childs weitergegeben.
	 * 
	 * @param value
	 */
	public void setClickable(boolean value)
	{
		isClickable = value;
	}

	public void setLongClickable(boolean value)
	{
		isLongClickable = value;
	}

	public void setDoubleClickable(boolean value)
	{
		isDoubleClickable = value;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void setY(float i)
	{
		if (this.getY() == i) return;
		super.setY(i);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce();
	}

	@Override
	public void setX(float i)
	{
		if (this.getX() == i) return;
		super.setX(i);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce();
	}

	@Override
	public void setPos(Vector2 Pos)
	{
		if (this.getX() == Pos.x && this.getY() == Pos.y) return;
		super.setPos(Pos);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce();
	}

	public void setZeroPos()
	{
		super.setPos(0, 0);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce();
	}

	@Override
	public void setPos(float x, float y)
	{
		super.setPos(x, y);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce();
	}

	// Abfrage der clickToleranz, mit der Bestimmt wird ab welcher Bewegung ein onTouchDragged erzeugt wird und beim loslassen kein click
	// dies kann hier f�r einzelne Views unabh�ngig bestimmt werden
	public int getClickTolerance()
	{
		// wenn eine View clickable ist dann mu� f�r die Verschiebung (onTouchDragged) ein gewisser Toleranzbereich definiert werden,
		// innerhalb dem erstmal kein onTouchDragged aufgerufen wird
		if (isClickable()) return UI_Size_Base.that.getClickToleranz();
		else
			// Wenn aber eine View nicht clickable ist dann darf der onTouchDragged sofort aufgerufen werden
			return 1;
	}

	// ############# Skin changed ################

	private interface skinChangedEventListner
	{
		public void SkinChanged();
	}

	private static ArrayList<skinChangedEventListner> skinChangedEventList = new ArrayList<GL_View_Base.skinChangedEventListner>();

	public void registerSkinChangedEvent()
	{
		if (calling) return;
		// synchronized (skinChangedEventList)
		// {
		skinChangedEventList.add(listner);
		// }
	}

	private static boolean calling = false;

	public static void CallSkinChanged()
	{
		// synchronized (skinChangedEventList)
		// {
		calling = true;
		for (skinChangedEventListner listner : skinChangedEventList)
		{
			if (listner != null) listner.SkinChanged();
		}
		calling = false;
		// }
	}

	protected skinChangedEventListner listner = new skinChangedEventListner()
	{

		@Override
		public void SkinChanged()
		{
			SkinIsChanged();
		}
	};

	protected abstract void SkinIsChanged();

	// ############# End Skin changed ############

	protected Color mColorFilter = null;

	public void setColorFilter(Color color)
	{
		mColorFilter = color;
	}

	public void clearColorFilter()
	{
		mColorFilter = null;
	}

	public Color getColorFilter()
	{
		return mColorFilter;
	}

	public void setEnabled(boolean value)
	{
		enabled = value;
	}

	public boolean getEnabled()
	{
		return enabled;
	}

	// @Override
	// protected void calcCrossCorner()
	// {
	// super.calcCrossCorner();
	// thisInvalidate = true;
	// }

	private Object data = null;

	public void setData(Object data)
	{
		this.data = data;
	}

	public Object getData()
	{
		return data;
	}

	public ParentInfo getMyInfoForChild()
	{
		return myInfoForChild;
	}

}
