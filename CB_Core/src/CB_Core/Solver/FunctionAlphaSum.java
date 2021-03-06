package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionAlphaSum extends Function {
    private static final long serialVersionUID = -6962880870313633795L;

    public FunctionAlphaSum(Solver solver) {
        super(solver);
        Names.add(new LocalNames("AlphaSum", "en"));
        Names.add(new LocalNames("AS", "en"));
    }

    @Override
    public String getName() {
        return Translation.Get("solverFuncAlphaSum".hashCode());
    }

    @Override
    public String getDescription() {
        return Translation.Get("solverDescAlphaSum".hashCode());
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.Get("solverErrParamCount".hashCode(), "1", "$solverFuncAlphaSum");
        }
        int result = 0;
        if (parameter[0].length() == 0)
            return "0";
        parameter[0] = parameter[0].toLowerCase();
        for (char c : parameter[0].toCharArray()) {
            if ((c >= 'a') && (c <= 'z'))
                result += c - ('a') + 1;
        }
        return String.valueOf(result);
    }

    @Override
    public int getAnzParam() {
        return 1;
    }

    @Override
    public boolean needsTextArgument() {
        return true;
    }

    @Override
    public DataType getParamType(int i) {
        switch (i) {
            case 0:
                return DataType.String;
            default:
                return DataType.None;
        }
    }

    @Override
    public DataType getReturnType() {
        return DataType.Integer;
    }

    @Override
    public String getParamName(int i) {
        return "solverParamText";
    }
}
