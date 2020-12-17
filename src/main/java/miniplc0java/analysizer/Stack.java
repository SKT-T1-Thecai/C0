package miniplc0java.analysizer;

import miniplc0java.tokenizer.Token;

import java.util.ArrayList;

// 并不会模拟真实的栈，仅仅储存栈上的值的类型： bool int(long) double addr 全都是8字节
public class Stack {
public ArrayList<SlotType> stack = new ArrayList<>();
public SlotType top()
{
    return stack.size()>0? stack.get(stack.size()-1):null;
}
public SlotType lower_top()
    {
        return stack.get(stack.size()-2);
    }
public void push(SlotType st)
{
    stack.add(st);
}
public void pop(SlotType st)
{
    if(top()==st)
    {

        stack.remove(stack.size()-1);
    }
    else throw new Error(st.toString()+" is not on the top of the stack.");
}
public void pop(VariableType vt)//int double
{
    if (vt==VariableType.INT)
    {
        if(top()==SlotType.INT)
        {
            stack.remove(stack.size()-1);
        }else throw new Error("INT slot is not on the stack;");
    }
    else if (vt==VariableType.DOUBLE)
    {
        if(top()==SlotType.DOUBLE)
        {
            stack.remove(stack.size()-1);
        }else throw new Error("DOUBLE slot is not on the stack;");
    }
    else throw new Error("undefined error;");
}
    public void push(VariableType vt)//int double
    {
        if (vt==VariableType.INT)
        {
            stack.add(SlotType.INT);
        }
        else if (vt==VariableType.DOUBLE)
        {
           stack.add(SlotType.DOUBLE);
        }
        else throw new Error("void or string cannot be saved in the stack;");
    }
public void pop()
{
    stack.remove(stack.size()-1);
}
public static  VariableType toVT(SlotType ST)
{
if(ST==SlotType.INT)
    return VariableType.INT;
if(ST==SlotType.DOUBLE)
    return VariableType.DOUBLE;
else return null;
}
}
