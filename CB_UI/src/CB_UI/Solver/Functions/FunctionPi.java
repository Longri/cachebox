package CB_UI.Solver.Functions;

import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionPi extends Function
{
	private static final long serialVersionUID = -5961548229978339692L;

	public FunctionPi()
	{
		Names.add(new LacalNames("Pi", "en"));
	}

	@Override
	public String getName()
	{
		return Translation.Get("solverFuncPi");
	}

	@Override
	public String getDescription()
	{
		return Translation.Get("solverDescPi");
	}

	@Override
	public String Calculate(String[] parameter)
	{
		if ((parameter.length != 1) || (parameter[0].trim() != "")) return Translation.Get("solverErrParamCount", "0", "$solverFuncPi");
		return String.valueOf(Math.PI);
	}

	@Override
	public int getAnzParam()
	{
		return 0;
	}

	@Override
	public boolean needsTextArgument()
	{
		return false;
	}

}