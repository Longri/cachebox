package CB_Core.Solver.Functions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Solver.EntityList;
import CB_Core.Solver.FunctionEntity;
import CB_Core.Solver.TempEntity;
import CB_Core.TranslationEngine.Translation;

public abstract class Function implements Serializable
{

	private static final long serialVersionUID = 3322289615650829139L;

	public String Name()
	{
		return getName();
	}

	public ArrayList<LacalNames> Names = new ArrayList<LacalNames>();

	public abstract int getAnzParam();

	public abstract boolean needsTextArgument();

	public String Description()
	{
		return getDescription();
	}

	public class LacalNames implements Serializable
	{

		private static final long serialVersionUID = 2806640831319814402L;

		public LacalNames(String name, String local)
		{
			Name = name;
			Local = local;
		}

		public String Name;
		public String Local;
	}

	public Function()
	{
	}

	public abstract String getName();

	public abstract String getDescription();

	public abstract String Calculate(String[] parameter);

	public String getShortcut()
	{
		String ret = "abcdefghijklmnopqrstuvwxyz";
		String retEN = "abcdefghijklmnopqrstuvwxyz";
		String local = Translation.getLangId();
		Iterator<LacalNames> iterator = Names.iterator();
		do
		{
			LacalNames tmp = iterator.next();
			if (tmp.Local.equalsIgnoreCase(local))
			{
				if (tmp.Name.length() < ret.length()) ret = tmp.Name;
			}
			else if (tmp.Local.equalsIgnoreCase("en"))
			{
				if (tmp.Name.length() < retEN.length()) retEN = tmp.Name;
			}

		}
		while (iterator.hasNext());

		if (ret.equalsIgnoreCase("abcdefghijklmnopqrstuvwxyz"))
		{
			return retEN;
		}

		return ret;
	}

	public String getLongLocalName()
	{
		String ret = "";
		String retEN = "";
		String local = Translation.getLangId();
		Iterator<LacalNames> iterator = Names.iterator();
		do
		{
			LacalNames tmp = iterator.next();
			if (tmp.Local.equalsIgnoreCase(local))
			{
				if (tmp.Name.length() > ret.length()) ret = tmp.Name;
			}
			else if (tmp.Local.equalsIgnoreCase("en"))
			{
				if (tmp.Name.length() > retEN.length()) retEN = tmp.Name;
			}

		}
		while (iterator.hasNext());

		if (ret.equalsIgnoreCase(""))
		{
			return retEN;
		}

		return ret;
	}

	private boolean checkIsFunction(String function, TempEntity tEntity, EntityList entities)
	{
		try
		{
			function = function.toLowerCase();
			int pos = tEntity.Text.toLowerCase().indexOf(function.toLowerCase());
			if (pos < 0) return false;
			int pos1 = pos + function.length(); // 1. #
			if (!(tEntity.Text.charAt(pos1) == '#')) return false;
			if (pos1 + 1 >= tEntity.Text.length()) return false;
			int pos2 = tEntity.Text.toLowerCase().indexOf("#", pos1 + 1);
			if (pos2 < pos1) return false;
			if (pos2 != tEntity.Text.length() - 1) return false;
			if (pos == 0)
			{
				// Insert new Entity
				TempEntity rechts = new TempEntity(-1, tEntity.Text.substring(pos1, pos2 + 1));
				entities.Insert(rechts);
				FunctionEntity fEntity = new FunctionEntity(-1, this, rechts);
				String var = entities.Insert(fEntity);
				tEntity.Text = var;
				return true;
			}
			else
				return false;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public boolean InsertEntities(TempEntity tEntity, EntityList entities)
	{
		if (checkIsFunction(Name(), tEntity, entities)) return true;
		for (LacalNames name2 : Names)
		{
			if (checkIsFunction(name2.Name, tEntity, entities)) return true;
		}
		return false;
	}

	public boolean isFunction(String s)
	{
		if (Name().toLowerCase().equals(s.toLowerCase())) return true;
		for (LacalNames name2 : Names)
		{
			if (name2.Name.toLowerCase().equals(s.toLowerCase())) return true;
		}
		return false;
	}

}