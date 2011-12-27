package de.cachebox_test.Map;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import CB_Core.Map.Descriptor;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TileGL implements Destroyable
{
	public enum TileState
	{
		Scheduled, Present, LowResolution, Disposed
	};

	public Descriptor Descriptor = null;

	public TileState State;

	// / <summary>
	// / Textur der Kachel
	// / </summary>
	public Texture texture = null;
	private byte[] bytes;

	// / <summary>
	// / Frames seit dem letzten Zugriff auf die Textur
	// / </summary>
	public long Age = 0;

	public TileGL(Descriptor desc, byte[] bytes, TileState state)
	{
		Descriptor = desc;
		this.texture = null;
		// this.texture = texture;
		this.bytes = bytes;
		State = state;
	}

	public void createTexture()
	{
		if (texture != null) return;
		texture = new Texture(new Pixmap(bytes, 0, bytes.length));
	}

	public String ToString()
	{
		return State.toString() + ", " + Descriptor.ToString();
	}

	@Override
	public void destroy() throws DestroyFailedException
	{
		if (texture != null) texture.dispose();
	}

	@Override
	public boolean isDestroyed()
	{
		return false;
	}

}