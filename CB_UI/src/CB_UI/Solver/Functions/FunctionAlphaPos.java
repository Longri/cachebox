package CB_UI.Solver.Functions;

import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionAlphaPos extends Function
{
	private static final long serialVersionUID = -2993835599804293184L;

	public FunctionAlphaPos()
	{
		Names.add(new LacalNames("AlphaPos", "en"));
		Names.add(new LacalNames("AP", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncAlphaPos");
		// return "AlphaPos";
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescAlphaPos");
		// return "Position des ersten Zeichens im Alphabet";
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncAlphaPos");
			// return "Diese Funktion ben�tigt %s Parameter".replace("%s", "1");
		}
		String wert = parameter[0].trim().toLowerCase();
		if (wert.length() == 0) return "0";
		char c = wert.charAt(0);
		int result = (int) c - (int) ('a') + 1;
		return String.valueOf(result);
	}

	@Override
	public int getAnzParam()
	{
		return 1;
	}

	@Override
	public boolean needsTextArgument()
	{
		return true;
	}
}