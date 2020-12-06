package miniplc0java.analysizer;

public class Variable {
    public String name;
    public boolean isConst;
    public boolean isGlobal;
    // 是不是参数
    public boolean isParam;
    public VariableType variableType;
    public int offset;
    public Variable(String name,boolean isConst,boolean isGlobal,boolean isParam,VariableType VT)
    {
        this.name = name;
        this.isConst = isConst;
        this.isGlobal = isGlobal;
        this.variableType = VT;
        this.isParam = isParam;
    }
}
