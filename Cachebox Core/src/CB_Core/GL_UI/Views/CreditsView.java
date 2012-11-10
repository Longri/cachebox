package CB_Core.GL_UI.Views;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Linearlayout;
import CB_Core.GL_UI.Controls.Linearlayout.LayoutChanged;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CreditsView extends CB_View_Base
{
	private float ref, lineHeight, margin;
	private Image logo;
	private ScrollBox scrollBox;
	private Linearlayout layout;

	public CreditsView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		this.setBackground(SpriteCache.AboutBack);

		ref = UiSizes.getWindowHeight() / 13;
		CB_RectF CB_LogoRec = new CB_RectF(this.halfWidth - (ref * 2.5f), this.height - ((ref * 5) / 4.11f) - ref, ref * 5,
				(ref * 5) / 4.11f);

		logo = new Image(CB_LogoRec, "Logo");
		logo.setDrawable(SpriteCache.logo);
		this.addChild(logo);

		scrollBox = new ScrollBox(rec, 100, "ScrollBox");
		scrollBox.setHeight(logo.getY() - (ref / 2));
		scrollBox.setZeroPos();
		// scrollBox.setBackground(new ColorDrawable(Color.RED)); //Debug

		this.addChild(scrollBox);

		layout = new Linearlayout(rec.getWidth(), "LinearLayout");
		layout.setLayoutChangedListner(new LayoutChanged()
		{

			@Override
			public void LayoutIsChanged(Linearlayout linearLayout, float newHeight)
			{
				scrollBox.setInerHeight(newHeight);
			}
		});

		scrollBox.addChild(layout);

	}

	@Override
	protected void Initial()
	{
		margin = Menu.margin;

		lineHeight = Fonts.Measure("Tg").height * 1.6f;
		layout.removeChilds();

		captioned("conception");
		addPersonToLayout(Job.idea);
		divider();

		captioned("develop");
		addPersonToLayout(Job.developer);
		divider();

		captioned("graphic");
		addPersonToLayout(Job.designer);
		divider();

		captioned("tester");
		addPersonToLayout(Job.tester);
		divider();

		captioned("localization");
		addPersonToLayout(Job.localization);
		divider();

		captioned("library");
		addPersonToLayout(Job.library);
		divider();

		captioned("service");
		addPersonToLayout(Job.service);
		divider();

		captioned("sponsor");
		addPersonToLayout(Job.sponsor);
		divider();
	}

	private void divider()
	{
		Box box = new Box(new CB_RectF(0, 0, this.width, lineHeight * 2f), "");
		layout.addChild(box);
	}

	private void captioned(String title)
	{
		title = GlobalCore.Translations.Get(title);
		Box box = new Box(new CB_RectF(0, 0, this.width, lineHeight * 1.2f), "");
		Label label = new Label(box, "");
		box.addChild(label);
		label.setFont(Fonts.getBig());
		label.setText(title + ":", HAlignment.CENTER);
		layout.addChild(box);
	}

	private void addPersonToLayout(Job job)
	{
		for (Person item : getPersons())
		{
			if (item.job == job)
			{
				String entry = item.name;
				float itemHeight = (item.image != null) ? GL_UISizes.Info.getHeight() : lineHeight;
				if (item.desc != null) entry += "  (" + item.desc + ")";

				Box box = new Box(new CB_RectF(0, 0, this.width, itemHeight), "");

				if (entry != null)
				{
					Label label = new Label(box, "");
					box.addChild(label);
					label.setText(entry, HAlignment.CENTER);
				}

				layout.addChild(box);
				if (item.image != null)
				{

					float SeitenVewrhältnis = item.image.getHeight() / item.image.getWidth();
					float imageWidth = itemHeight / SeitenVewrhältnis;
					float xPos = (this.halfWidth - (Fonts.Measure(entry).width / 2)) - itemHeight - margin - margin;
					if (entry == null) xPos = this.halfWidth - (imageWidth / 2);
					Image img = new Image(xPos, 0, imageWidth, itemHeight, "");
					img.setDrawable(new SpriteDrawable(item.image));
					box.addChild(img);
				}
			}
		}
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void resize(float width, float height)
	{
		logo.setY(this.height - ((ref * 5) / 4.11f) - ref);
		scrollBox.setHeight(logo.getY() - (ref / 2));
	}

	public enum Job
	{
		idea, developer, designer, tester, sponsor, library, service, localization
	}

	public class Person
	{

		public String name;
		public String nick;
		public Job job;
		public String email;
		public String desc = null;
		public Sprite image = null;

		public Person(String Name, Job job)
		{
			this.job = job;
			this.name = Name;
		}

		public Person(String Name, Job job, String desc)
		{
			this.job = job;
			this.name = Name;
			this.desc = desc;
		}

		public Person(String Name, Job job, Sprite image)
		{
			this.job = job;
			this.name = Name;
			this.image = image;
		}

	}

	private ArrayList<Person> getPersons()
	{
		ArrayList<Person> list = new ArrayList<CreditsView.Person>();

		list.add(new Person("hannes!", Job.idea, "2009-2011"));
		list.add(new Person("Stonefinger", Job.designer));
		list.add(new Person("Groundspeak API", Job.service, SpriteCache.Icons.get(35)));
		list.add(new Person(null, Job.library, SpriteCache.getThemedSprite("libgdx")));// Name at Logo image
		list.add(new Person("Mapsforge", Job.library, SpriteCache.getThemedSprite("mapsforge_logo")));
		list.add(new Person("OpenRouteService.org", Job.service, SpriteCache.getThemedSprite("openrouteservice_logo")));
		list.add(new Person("OpenStreetMap", Job.service, SpriteCache.getThemedSprite("osm_logo")));
		list.add(new Person("Faktor zwei", Job.sponsor, SpriteCache.getThemedSprite("FXzwei")));
		list.add(new Person("Ging-Buh", Job.developer));
		list.add(new Person("Longri", Job.developer));
		list.add(new Person("ersthelfer", Job.developer));
		list.add(new Person("arbor95", Job.developer));
		list.add(new Person("droogi", Job.tester));
		list.add(new Person("droogi", Job.localization));
		list.add(new Person("Teleskopix", Job.tester));
		list.add(new Person("halkman", Job.localization));
		list.add(new Person("Lady-in-blue", Job.tester));
		list.add(new Person("Koblenzer", Job.tester));
		list.add(new Person("GeoSilverio", Job.tester));
		list.add(new Person("GeoPfaff", Job.tester));
		return list;
	}

}
