package miniplc0java.analysizer;

import java.util.ArrayList;

public class Function {
    public String name;
    public int id;
    public int return_slot = 0;
    public int param_slot = 0;
    public int local_slot = 0;
    public int return_point = 0;
    // 局部变量
    public ArrayList<Variable> localVariables = new ArrayList<>();
    // 参数变量
    public ArrayList<Variable> paramVariables = new ArrayList<>();
    // 函数返回值类型
    public VariableType type;
    public Function(String name)
    {
        this.name = name;
    }
    public ArrayList<Instruction> instructions = new ArrayList<>();
    public Instruction topInstruction()
    {
        return instructions.get(instructions.size()-1);
    }
}
