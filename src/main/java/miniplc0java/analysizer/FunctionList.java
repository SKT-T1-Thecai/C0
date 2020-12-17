package miniplc0java.analysizer;

import java.util.ArrayList;

public class FunctionList {
    public ArrayList<Function> function_list = new ArrayList<>();

    public void addFunction(String name) {
        // 不能有重名的函数
        for (int i = 0; i < function_list.size(); i++) {
            if (function_list.get(i).name.equals(name))
                throw new Error("Function redefined.");
        }

        Function function = new Function(name);
        function_list.add(function);
    }

    public Function top() {
        return function_list.get(function_list.size() - 1);
    }

    // 全局变量，函数的参数，函数的局部变量
    public void addVariable(Variable variable) {
        if (variable.isGlobal) {
            variable.offset = function_list.get(0).localVariables.size();
            function_list.get(0).localVariables.add(variable);
        } else if (variable.isParam) {
            variable.offset = top().paramVariables.size();
            top().paramVariables.add(variable);
        } else {
            variable.offset = top().localVariables.size();
            top().localVariables.add(variable);
        }
        // printAll();
    }

    public void printAll() {
        System.out.println("            ");
        for (int i = 0; i < function_list.size(); i++) {
            Function function = function_list.get(i);
            System.out.println("functionid: " + i);
            System.out.println("functionname: " + function.name);
            System.out.println("function_param_num: " + function.paramVariables.size());
            System.out.println("function_local_num: " + function.localVariables.size());
        }
        System.out.println("            ");
    }

    public void add_instruction(String name) {
        top().instructions.add(new Instruction(name));
    }

    public void add_instruction(String name, ArrayList<Byte> bytes) {
        top().instructions.add(new Instruction(name, bytes));
    }

    public void set_return_type(VariableType vt) {
        top().type = vt;
    }

    public void set_return_slot() {
        top().return_slot = 1;
    }

    public void addParamsIndex() {
        for (int i = 0; i < top().paramVariables.size(); i++) {
            top().paramVariables.get(i).offset += 1;
        }
    }

    public Function getFunctionByName(String name) {
        for (Function function : function_list) {
            if (function.name.equals(name))
                return function;
        }
        return null;
    }

    public int getFunctionIndex(String name) {
        for (int i = 0; i < function_list.size(); i++) {
            if (function_list.get(i).name.equals(name))
                return i;
        }
        throw new Error("dsfa");
    }
}
