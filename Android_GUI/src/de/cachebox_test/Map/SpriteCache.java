package de.cachebox_test.Map;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Enth�lt die geladenen Sprites und das Handling f�r Laden und Entladen.
 * 
 * @author Longri
 */
public class SpriteCache
{
	public static ArrayList<Sprite> MapIconsSmall = null;
	public static ArrayList<Sprite> MapOverlay = null;
	public static ArrayList<Sprite> MapIcons = null;
	public static ArrayList<Sprite> MapArrows = null;
	public static ArrayList<Sprite> MapStars = null;
	public static ArrayList<Sprite> Bubble = null;

	/**
	 * Load the Sprites from recorce
	 */
	public static void LoadSprites()
	{
		TextureAtlas atlas;
		atlas = new TextureAtlas(Gdx.files.internal("data/pack"));

		MapIconsSmall = new ArrayList<Sprite>();
		MapIconsSmall.add(atlas.createSprite("small1yes"));
		MapIconsSmall.add(atlas.createSprite("small2yesyes"));
		MapIconsSmall.add(atlas.createSprite("small3yes"));
		MapIconsSmall.add(atlas.createSprite("small4yes"));
		MapIconsSmall.add(atlas.createSprite("small5yes"));
		MapIconsSmall.add(atlas.createSprite("small5solved"));
		MapIconsSmall.add(atlas.createSprite("small6yes"));
		MapIconsSmall.add(atlas.createSprite("small7yes"));
		MapIconsSmall.add(atlas.createSprite("small1no"));
		MapIconsSmall.add(atlas.createSprite("small2no"));
		MapIconsSmall.add(atlas.createSprite("small3no"));
		MapIconsSmall.add(atlas.createSprite("small4no"));
		MapIconsSmall.add(atlas.createSprite("small5no"));
		MapIconsSmall.add(atlas.createSprite("small5solved_no"));
		MapIconsSmall.add(atlas.createSprite("small6no"));
		MapIconsSmall.add(atlas.createSprite("small7no"));

		MapOverlay = new ArrayList<Sprite>();
		MapOverlay.add(atlas.createSprite("shaddowrect"));
		MapOverlay.add(atlas.createSprite("shaddowrect_selected"));
		MapOverlay.add(atlas.createSprite("deact"));
		MapOverlay.add(atlas.createSprite("cross"));

		MapIcons = new ArrayList<Sprite>();
		MapIcons.add(atlas.createSprite("0"));
		MapIcons.add(atlas.createSprite("1"));
		MapIcons.add(atlas.createSprite("2"));
		MapIcons.add(atlas.createSprite("3"));
		MapIcons.add(atlas.createSprite("4"));
		MapIcons.add(atlas.createSprite("5"));
		MapIcons.add(atlas.createSprite("6"));
		MapIcons.add(atlas.createSprite("7"));
		MapIcons.add(atlas.createSprite("8"));
		MapIcons.add(atlas.createSprite("9"));
		MapIcons.add(atlas.createSprite("10"));
		MapIcons.add(atlas.createSprite("11"));
		MapIcons.add(atlas.createSprite("12"));
		MapIcons.add(atlas.createSprite("13"));
		MapIcons.add(atlas.createSprite("14"));
		MapIcons.add(atlas.createSprite("15"));
		MapIcons.add(atlas.createSprite("16"));
		MapIcons.add(atlas.createSprite("17"));
		MapIcons.add(atlas.createSprite("18"));
		MapIcons.add(atlas.createSprite("19"));
		MapIcons.add(atlas.createSprite("20"));
		MapIcons.add(atlas.createSprite("21"));

		MapArrows = new ArrayList<Sprite>();
		MapArrows.add(atlas.createSprite("arrow_Compass"));
		MapArrows.add(atlas.createSprite("arrow_Compass_Trans"));
		MapArrows.add(atlas.createSprite("arrow_GPS"));
		MapArrows.add(atlas.createSprite("arrow_GPS_Trans"));

		MapStars = new ArrayList<Sprite>();
		MapStars.add(atlas.createSprite("stars0small"));
		MapStars.add(atlas.createSprite("stars0_5small"));
		MapStars.add(atlas.createSprite("stars1small"));
		MapStars.add(atlas.createSprite("stars1_5small"));
		MapStars.add(atlas.createSprite("stars2small"));
		MapStars.add(atlas.createSprite("stars2_5small"));
		MapStars.add(atlas.createSprite("stars3small"));
		MapStars.add(atlas.createSprite("stars3_5small"));
		MapStars.add(atlas.createSprite("stars4small"));
		MapStars.add(atlas.createSprite("stars4_5small"));
		MapStars.add(atlas.createSprite("stars5small"));

		Bubble = new ArrayList<Sprite>();
		Bubble.add(atlas.createSprite("Bubble"));
		Bubble.add(atlas.createSprite("Bubble_selected"));
		Bubble.add(atlas.createSprite("BubbleOverlay"));

	}

	/**
	 * Destroy cached sprites
	 */
	public static void destroyCache()
	{
		MapIconsSmall = null;
		MapOverlay = null;
		MapIcons = null;
		MapArrows = null;
		MapStars = null;
		Bubble = null;
	}

}