package CB_Core.Solver.Functions;

import CB_Core.TranslationEngine.Translation;

public class FunctionHandySum extends Function
{
	private static final long serialVersionUID = -9107479222557989258L;

	public FunctionHandySum()
	{
		Names.add(new LacalNames("PhoneSum", "en"));
		Names.add(new LacalNames("HandySum", "de"));
		Names.add(new LacalNames("PS", "en"));
		Names.add(new LacalNames("HS", "de"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncPhoneSum");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescPhoneSum");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if (parameter.length != 1)
		{
			return Translation.Get("solverErrParamCount", "1", "$solverFuncPhoneSum");
		}
		int result = 0;
		String wert = parameter[0].toLowerCase();
		for (char c : wert.toCharArray())
		{
			int i = (int) c - (int) ('a') + 1;
			if ((i < 1) || (i > 26)) continue; // nur Buchstaben!!!
			i -= 3;
			if (i <= 0)
			{
				result += 2;
				continue;
			}
			i -= 3;
			if (i <= 0)
			{
				result += 3;
				continue;
			}
			i -= 3;
			if (i <= 0)
			{
				result += 4;
				continue;
			}
			i -= 3;
			if (i <= 0)
			{
				result += 5;
				continue;
			}
			i -= 3;
			if (i <= 0)
			{
				result += 6;
				continue;
			}
			i -= 4;
			if (i <= 0)
			{
				result += 7;
				continue;
			}
			i -= 3;
			if (i <= 0)
			{
				result += 8;
				continue;
			}
			i -= 4;
			if (i <= 0)
			{
				result += 9;
				continue;
			}
		}
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