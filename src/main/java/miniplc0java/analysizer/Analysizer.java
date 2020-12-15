package miniplc0java.analysizer;
import com.google.errorprone.annotations.Var;
import miniplc0java.tokenizer.*;
import org.checkerframework.checker.units.qual.A;

import javax.print.DocFlavor;
import java.awt.print.PrinterGraphics;
import java.io.*;
import java.util.ArrayList;

public class Analysizer {
    Tokenizer tokenizer;
    int pos;
    boolean FalseToJump = false;
    boolean isCreatingFunction = false;
    FileOutputStream stream ;
    Stack stack = new Stack();
    public FunctionList functionList = new FunctionList();
    public SymbolTable symbolTable = new SymbolTable();
    public ArrayList<Integer> brList = new ArrayList<>();
    static String[] stdio = {
            "getint","getdouble","getchar","putint","putdouble","putchar","putstr","putln"};
    public Token currentToken()
    {
        return tokenizer.TokenList.get(pos);
    }
    public Token nextToken()
    {
        return pos+1<=tokenizer.TokenList.size()-1?tokenizer.TokenList.get(pos+1):null;
    }
    public Token next_next_Token()
    {
        return pos+2<=tokenizer.TokenList.size()-1?tokenizer.TokenList.get(pos+2):null;
    }
    // 把变量的地址放在栈上 ，地址是4个字节
    public void push_variable_address(Variable variable) {
        if (variable.isGlobal) {
            functionList.add_instruction("globa",Instruction.get_byte_array_by_int(variable.offset));
            stack.push(SlotType.ADDR);
        }
        else if(variable.isParam)
        {
            functionList.add_instruction("arga",Instruction.get_byte_array_by_int(variable.offset));
            stack.push(SlotType.ADDR);

        }else {
            functionList.add_instruction("loca",Instruction.get_byte_array_by_int(variable.offset));
            stack.push(SlotType.ADDR);
        }
    }
    public Token expect(TokenType tt)
    {
        if(currentToken().tokenType==tt)
        {
            Token t= currentToken();
            GoNext();
            return t;
        }
        else throw new Error(tt.toString() + " is expected,but "+currentToken().tokenType.toString()+" is gotten");
    }
    public void GoNext()
    {
        pos+=1;
    }
    public Analysizer(String inputSrc,String out) throws IOException {
        tokenizer = new Tokenizer(inputSrc);
        tokenizer.TokenizerInit();
        tokenizer.readFile.PrintAll();
        pos = 0;
        symbolTable.addLine();
        functionList.addFunction("_start");
        stream = new FileOutputStream(out);
        analyseProgram();
        print_out();
        FileInputStream f = new FileInputStream(new File(out));
        byte[] a = f.readAllBytes();
        int time = 0;
        for(int i=0;i<a.length;i++)
        {
            time++;
            System.out.print(work(a[i])+" ");
            if(time==16) {
                System.out.println("");
                time = 0;
            }
        }
    }
    // expr 删除左递归的表达
    // expr -> expr_1 { = expr_1}
    // expr_1 -> expr_2 { sign_1 expr_2}   sign_1 -> > < >= <= == !=
    // expr_2 -> expr_3 { sign_2 expr_3}   sign_2 -> + -
    // expr_3 -> expr_4 { sign_3 expr_4}   sign_3 -> * /
    // expr_4 -> expr_5 { as ty}
    // expr_5 -> -expr_5 | Ident(expr,expr...) |(expr)| 15| 15.6E4 | "sadfasd"
    public void analyse_expr_1()
    {// 比较表达式的值只出现在if/while语句中
        analyse_expr_2();
        while(currentToken().tokenType==TokenType.LT|| //<
                currentToken().tokenType==TokenType.GT||// >
                currentToken().tokenType==TokenType.GE||// >=
                currentToken().tokenType==TokenType.LE ||// <=
                currentToken().tokenType==TokenType.EQ||// ==
                currentToken().tokenType==TokenType.NEQ// !=
        )
        {
            TokenType compareSign = currentToken().tokenType;
            GoNext();
            analyse_expr_2();
            if(!((stack.top()==SlotType.INT || stack.top()==SlotType.DOUBLE)&&stack.lower_top()==stack.top()))//
                throw new Error("the type on the stack can't be compared.");
                functionList.add_instruction(stack.top()==SlotType.INT? "cmp.i":"cmp.f");
                stack.pop();
                stack.pop();
                stack.push(SlotType.BOOL);
                if(compareSign==TokenType.LT) {
                    functionList.add_instruction("set.lt");
                    // 为0跳转 br.false
                    FalseToJump = true;
                }else if(compareSign==TokenType.LE) {
                    functionList.add_instruction("set.gt");
                    // 不为0跳转
                    FalseToJump = false;
                }else if(compareSign==TokenType.GT) {
                    functionList.add_instruction("set.gt");
                    // 为0跳转
                    FalseToJump = true;
                }else if(compareSign==TokenType.GE) {
                    functionList.add_instruction("set.lt");
                    // 不为0跳转
                    FalseToJump = false;
                }else if(compareSign==TokenType.EQ) {
                    // 不为0跳转
                    FalseToJump = false;
                }else if(compareSign==TokenType.NEQ) {
                    //为0 跳转
                    FalseToJump = true;
                }
        }

    }
    public void analyse_expr_2()
    {
        analyse_expr_3();
        boolean isAdding;
        while(currentToken().tokenType==TokenType.PLUS||
                currentToken().tokenType==TokenType.MINUS
        )
        {
            isAdding = currentToken().tokenType==TokenType.PLUS;
            GoNext();
            analyse_expr_3();
            // 分为整数相加减 和 浮点数相加减
            if(stack.top()==stack.lower_top()&&stack.top()==SlotType.INT)
            {
                String ins = isAdding? "add.i":"sub.i";
                functionList.add_instruction(ins);
                stack.pop(SlotType.INT);
                stack.pop(SlotType.INT);
                stack.push(SlotType.INT);
            }
            else if(stack.top()==stack.lower_top()&&stack.top()==SlotType.DOUBLE)
            {
                String ins = isAdding? "add.f":"sub.f";
                stack.pop(SlotType.DOUBLE);
                stack.pop(SlotType.DOUBLE);
                stack.push(SlotType.DOUBLE);
            }
        }

    }
    public void analyse_expr_3()
    {
        analyse_expr_4();
        boolean isMulting;
        while(currentToken().tokenType==TokenType.MUL||
                currentToken().tokenType==TokenType.DIV
        )
        {
            isMulting = currentToken().tokenType==TokenType.MUL;
            GoNext();
            analyse_expr_3();
            // 分为整数相乘除 和 浮点数

            if(stack.top()==stack.lower_top()&&stack.top()==SlotType.INT)
            {
                String ins = isMulting? "mul.i":"div.i";
                functionList.add_instruction(ins);
                stack.pop(SlotType.INT);
                stack.pop(SlotType.INT);
                stack.push(SlotType.INT);
            }
            else if(stack.top()==stack.lower_top()&&stack.top()==SlotType.DOUBLE)
            {
                String ins = isMulting? "mul.f":"div.f";
                stack.pop(SlotType.DOUBLE);
                stack.pop(SlotType.DOUBLE);
                stack.push(SlotType.DOUBLE);
            }
        }

    }
    public void analyse_expr_4()
    {
        analyse_expr_5();
        while(currentToken().tokenType==TokenType.AS_KW
        )// 类型转换 itof,ftoi
        {
            GoNext();
            VariableType type = analyse_type();
            // 栈顶的值不能参与类型转换
            if(stack.top()!=SlotType.DOUBLE&&stack.top()!=SlotType.INT)
                throw new Error("the type on the top of the stack can't be transfered.");
            else {
                stack.pop();
                stack.push(type);
                 }
        }

    }
    public VariableType analyse_type() {
        if (currentToken().tokenType != TokenType.IDENT)
            throw new Error("A type is needed(int,double,or void),but " + currentToken().tokenType.toString() + "is gotten");
        else if (currentToken().value.equals("double") )
        {
            GoNext();
            return VariableType.DOUBLE;
        }
        else if (currentToken().value.equals("int") )
        {
            GoNext();
            return VariableType.INT;
        }
        else if (currentToken().value.equals("void") )
        {
            GoNext();
            return VariableType.VOID;

        }
        else {
            throw new Error("A type is needed(int,double,or void),but " + currentToken().value.toString() + " is gotten");
        }
    }
    // expr_5 -> -expr_5 | Ident(expr,expr...) |(expr)| 15| 15.6E4 | "sadfasd"
    public void analyse_expr_5()
    {
        // 先处理负号
        boolean isNegative = false;
        while(currentToken().tokenType==TokenType.MINUS)
        {
            GoNext();
            isNegative = !isNegative;
        }

        if (currentToken().tokenType==TokenType.IDENT)
        {
            // 函数调用
            if(nextToken().tokenType==TokenType.L_PAREN)
            {
                String function_name = currentToken().value.toString();
                boolean isStdioFunc = false;
                for(int i=0;i<8;i++)
                {
                    if(function_name.equals(stdio[i]))
                    {
                        isStdioFunc = true;
                        break;
                    }
                }
                if(isStdioFunc)
                    stdio_func();
                else
                {
                    GoNext();
                    expect(TokenType.L_PAREN);
                    if(currentToken().tokenType!=TokenType.R_PAREN)
                    {
                        analyseExpr();
                    }
                    while(currentToken().tokenType!=TokenType.R_PAREN)
                    {

                        expect(TokenType.COMMA);
                        analyseExpr();
                    }
                    expect(TokenType.R_PAREN);
                }
            }
            else  // 引用符号表的参数，查询是否存在，并把他的地址放在栈上,load
            {
                Variable variable = symbolTable.getVariableByName(currentToken().value.toString());
                push_variable_address(variable);
                if(nextToken().tokenType!=TokenType.ASSIGN)// 不是赋值语句
                {
                    functionList.add_instruction("load.64");
                    stack.pop(SlotType.ADDR);
                    stack.push(variable.variableType);
                }else if(variable.isConst)throw new Error("const variable cannot be assigned.");
                GoNext();
            }

        }
        // 括号
        else if(currentToken().tokenType==TokenType.L_PAREN)
        {
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
        }
        // 字面量
        else if(currentToken().tokenType==TokenType.UINT_LITERAL)
        {
            //把数据push到栈上
            functionList.add_instruction("push",Instruction.get_byte_array_by_long((long)currentToken().value));
            stack.push(SlotType.INT);
           GoNext();
        }
        else if(currentToken().tokenType==TokenType.DOUBLE_LITERAL)
        {
            //把数据push到栈上
            functionList.add_instruction("push",Instruction.get_byte_array_by_long(Double.doubleToLongBits(
                    (double) currentToken().value
            )));
            stack.push(SlotType.DOUBLE);
            GoNext();
        }
        else if(currentToken().tokenType==TokenType.STRING_LITERAL)
        {
            //把数据存到全局变量，把全局变量的标号（long）push到栈上
            String s = currentToken().value.toString();
            Variable variable = new Variable(s,true,true,false,VariableType.STRING);
            symbolTable.symbol_table.get(0).add(variable);
            functionList.addVariable(variable);
            functionList.add_instruction("push",Instruction.get_byte_array_by_long(
                    variable.offset
            ));
            stack.push(SlotType.INT);
            GoNext();
        }
        else throw new Error("error occured pos = "+currentToken().startPos.toString());
        if(isNegative)
        {
            if(stack.top()==SlotType.INT)
                functionList.add_instruction("neg.i");
            else if(stack.top()==SlotType.DOUBLE)
                functionList.add_instruction("neg.f");
            else throw new Error("Address or void cannot be negated.");
        }
    }
    public void stdio_func()
    {
        String function_name = currentToken().value.toString();
        GoNext();
        if(function_name.equals("getint"))
        {
            expect(TokenType.L_PAREN);
            expect(TokenType.R_PAREN);
            functionList.add_instruction("scan.i");
            stack.push(SlotType.INT);
        }
        else if(function_name.equals("getdouble"))
        {
            expect(TokenType.L_PAREN);
            expect(TokenType.R_PAREN);
            functionList.add_instruction("scan.f");
            stack.push(SlotType.DOUBLE);
        }
        else if(function_name.equals("getchar"))
        {
            expect(TokenType.L_PAREN);
            expect(TokenType.R_PAREN);
            functionList.add_instruction("scan.c");
            stack.push(SlotType.INT);
        }
        else if(function_name.equals("putint"))
        {
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
            functionList.add_instruction("print.i");
            stack.pop(SlotType.INT);
        }
        else if(function_name.equals("putdouble"))
        {
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
            functionList.add_instruction("print.f");
            stack.pop(SlotType.DOUBLE);
        }
        else if(function_name.equals("putchar"))
        {
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
            functionList.add_instruction("print.c");
            stack.pop(SlotType.INT);
        }
        else if(function_name.equals("putstr"))
        {
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
            functionList.add_instruction("print.s");
            stack.pop(SlotType.INT);
        }
        else if(function_name.equals("putln"))
        {
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
            functionList.add_instruction("println");
        }

    }
    public void analyseExpr() // 表达式
    {// 赋值表达式的值是void 不能被使用
        analyse_expr_1();
       if(currentToken().tokenType==TokenType.ASSIGN)
        {
            GoNext();
            analyse_expr_1();
            if((stack.top()==SlotType.INT||stack.top()==SlotType.DOUBLE)&&stack.lower_top()==SlotType.ADDR)
            {
                functionList.add_instruction("store.64");
                stack.pop();
                stack.pop(SlotType.ADDR);
            }else throw new Error("");
        }
    }

// 可以在开始定义全局变量，也可以在函数内定义局部变量
    public void analyse_const_decl_stmt() // 常量赋值语句
    {
        expect(TokenType.CONST_KW);
        Token token=expect(TokenType.IDENT);
        expect(TokenType.COLON);
       VariableType type = analyse_type();
       // 常量不能是 void
       if(type==VariableType.VOID)
           throw new Error("const item can't be defined as void,pos: "+currentToken().startPos.toString());
       boolean global = symbolTable.symbol_table.size()==1;
       Variable v = new Variable(token.value.toString(),true,global,false,type);
       symbolTable.addOneVariable(v);
       // 加入到functionlist 会定义变量的offset
       functionList.addVariable(v);
       if(!global)
           functionList.top().local_slot++;
       // 现在应该把这个const的地址放在栈顶上，push，store.64
        push_variable_address(v);
        expect(TokenType.ASSIGN);
        analyseExpr();
        // 汇编和栈操作应该是同步的
        functionList.add_instruction("store.64");
        stack.pop(v.variableType);
        stack.pop(SlotType.ADDR);
        expect(TokenType.SEMICOLON);
    }
    public void analyse_let_decl_stmt() // 变量赋值语句
    {
        expect(TokenType.LET_KW);
        Token token = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        VariableType type = analyse_type();
        if(type==VariableType.VOID)
            throw new Error("const item can't be defined as void,pos: "+currentToken().startPos.toString());
        boolean global = symbolTable.symbol_table.size()==1;
        Variable v = new Variable(token.value.toString(),false,global,false,type);
        symbolTable.addOneVariable(v);
        functionList.addVariable(v);
        if(!global)
            functionList.top().local_slot++;
        if(currentToken().tokenType==TokenType.ASSIGN)
        {
            push_variable_address(v);
            GoNext();
            analyseExpr();
            // 汇编和栈操作应该是同步的
            functionList.add_instruction("store.64");
            stack.pop(v.variableType);
            stack.pop(SlotType.ADDR);
        }
        expect(TokenType.SEMICOLON);
    }
    // program -> decl_stmt* function*
    public void analyseProgram() // 整个程序
    {
        while(currentToken().tokenType==TokenType.CONST_KW || currentToken().tokenType==TokenType.LET_KW)
        {
            if(currentToken().tokenType==TokenType.CONST_KW)
            {
                analyse_const_decl_stmt();
            }
            else analyse_let_decl_stmt();
        }
        while(currentToken().tokenType==TokenType.FN_KW)
        {
            analyseFunction();
        }
        if(currentToken().tokenType==TokenType.EOF)
        {
            System.out.println("Syntax analyse passed.");
        }
        else throw new Error("error pos :"+currentToken().startPos.toString());
        //_start call main
        functionList.function_list.get(0).instructions.add(new Instruction("call",
                Instruction.get_byte_array_by_int(functionList.function_list.size()-1)));
        symbolTable.symbol_table.get(0).add(new Variable("_start",
                true,true,false,VariableType.STRING));
        functionList.function_list.get(0).id = symbolTable.GlobalVariables().size()-1;


    }
    public void analyse_function_param()
    {
        boolean isConst = false;
        if(currentToken().tokenType==TokenType.CONST_KW)
        {
            isConst = true;
            GoNext();
        }
        Token token = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        VariableType type = analyse_type();
        if(type == VariableType.VOID)
            throw new Error("function params can't be void");
        Variable v =new Variable(token.value.toString(),isConst,false,true,type);
        symbolTable.addOneVariable(v);
        functionList.addVariable(v);
        functionList.top().param_slot+=1;
    }
    public void analyseFunction()// 函数
    {
        expect(TokenType.FN_KW);
        Token token = expect(TokenType.IDENT);

        // 要判断函数名是否和全局变量同名，以及是否有重名函数
        ArrayList<Variable> global_variables = symbolTable.GlobalVariables();
        for(int i=0;i<global_variables.size();i++)
        {
            if(token.value.toString().equals(global_variables.get(i).name))
                throw new Error("Function name cannot be the same as global variable.");

        }
        // 在函数列表里添加函数
        functionList.addFunction(token.value.toString());
        // 在全局变量里添加函数名 String
        Variable variable = new Variable(token.value.toString(),true,true,false,
                VariableType.STRING);
        symbolTable.symbol_table.get(0).add(variable);
        functionList.addVariable(variable);
        // 函数的全局变量序号
        functionList.top().id = symbolTable.GlobalVariables().size()-1;
        expect(TokenType.L_PAREN);
        symbolTable.addLine();
       if(currentToken().tokenType!=TokenType.R_PAREN)
        {
            analyse_function_param();
        }
        while(currentToken().tokenType!=TokenType.R_PAREN)
        {
            expect(TokenType.COMMA);
            analyse_function_param();
        }
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        VariableType type = analyse_type();
        functionList.set_return_type(type);
        if(type == VariableType.DOUBLE || type ==VariableType.INT)
        {
            functionList.set_return_slot();
        }

        isCreatingFunction = true;
        analyse_block_stmt();
        // void 函数并且没有返回点
        if(functionList.top().type==VariableType.VOID&&functionList.top().return_point==0)
        {
            functionList.add_instruction("ret");
        }


    }
//    stmt ->
//    expr_stmt IDENT  (  int double  string -
//    | decl_stmt  const or let
//    | if_stmt    if
//    | while_stmt while
//    | return_stmt return
//    | block_stmt {
//    | empty_stmt ;

    public void analyse_stmt()//语句
    {
        if (currentToken().tokenType == TokenType.IDENT ||
                currentToken().tokenType == TokenType.L_PAREN ||
                currentToken().tokenType == TokenType.UINT_LITERAL ||
                currentToken().tokenType == TokenType.DOUBLE_LITERAL ||
                currentToken().tokenType == TokenType.STRING_LITERAL ||
                currentToken().tokenType == TokenType.MINUS)
            {// a;     a = 123;
                analyseExpr();
                expect(TokenType.SEMICOLON);
            }
        else if(currentToken().tokenType==TokenType.IF_KW)
        {
            analyse_if_stmt();
        }
        else if(currentToken().tokenType==TokenType.CONST_KW)
        {
            analyse_const_decl_stmt();
        }
        else if(currentToken().tokenType==TokenType.LET_KW)
        {
            analyse_let_decl_stmt();
        }
        else if(currentToken().tokenType==TokenType.WHILE_KW)
        {
            analyse_while_stmt();
        }
        else if(currentToken().tokenType==TokenType.RETURN_KW)
        {
            analyse_return_stmt();
        }
        else if(currentToken().tokenType==TokenType.L_BRACE)
        {
            analyse_block_stmt();
        }
        else if(currentToken().tokenType==TokenType.SEMICOLON)
        {
            GoNext();
        }
        else throw new Error("analyse_stmt failed ,pos: "+currentToken().startPos.toString());
    }
    public void analyse_if_stmt()
    {
        expect(TokenType.IF_KW);
        analyseExpr();
        //jump  下一个表达式的命令数
        functionList.add_instruction(FalseToJump?"br.false":"br.true");
        brList.add(functionList.top().instructions.size());
        analyse_block_stmt();
        functionList.add_instruction("br");
        brList.add(functionList.top().instructions.size());
        //jump 到末尾的命令数
        if(currentToken().tokenType!=TokenType.ELSE_KW) {
        }
        else {
            GoNext();
            if(currentToken().tokenType==TokenType.IF_KW)
            {
                analyse_if_stmt();
            }
            else if (currentToken().tokenType==TokenType.L_BRACE)
            {
                analyse_block_stmt();
                brList.add(functionList.top().instructions.size());
                for(int i=0;i<brList.size()-1;i++)
                {
                    Instruction instruction = functionList.top().instructions.get(brList.get(i)-1);
                    if(i%2==0)
                    {
                        instruction.with_operands = true;
                        instruction.instruction_num = Instruction.get_byte_array_by_int(brList.get(i+1)-brList.get(i));
                    }
                    else {
                        instruction.with_operands = true;
                        instruction.instruction_num = Instruction.get_byte_array_by_int(-brList.get(i)+brList.get(brList.size()-1));
                    }
                }
                brList.clear();
            }
            else throw new Error("the token follows else must be { or if,token "+currentToken().toString());
        }

    }
    public void analyse_while_stmt()
    {
        expect(TokenType.WHILE_KW);
        analyseExpr();
        analyse_block_stmt();
    }
    public void analyse_return_stmt()
    {
        expect(TokenType.RETURN_KW);
        if(currentToken().tokenType!=TokenType.SEMICOLON)
        {
            if(functionList.top().return_slot==0)
                throw new Error("this function should not have return value");
            functionList.add_instruction("arga",Instruction.get_byte_array_by_int(0));
            stack.push(SlotType.ADDR);

            analyseExpr();

            functionList.add_instruction("store.64");
            stack.pop(functionList.top().type);
            stack.pop(SlotType.ADDR);
        }else if(functionList.top().return_slot!=0)
            throw new Error("this function should have return value");

        functionList.add_instruction("ret");
        if(functionList.top().name.equals("main")&&functionList.top().type!=VariableType.VOID)
            functionList.add_instruction("stackalloc",Instruction.get_byte_array_by_int(1));
        expect(TokenType.SEMICOLON);
        functionList.top().return_point+=1;
    }
    public void analyse_block_stmt()
    {
        expect(TokenType.L_BRACE);
        if(!isCreatingFunction)
        symbolTable.addLine();
        isCreatingFunction = false;
        while (currentToken().tokenType!=TokenType.R_BRACE)
        {
            analyse_stmt();
        }
        expect(TokenType.R_BRACE);
        symbolTable.popLine();

    }
    public void print_out() throws IOException {
        stream.write(new byte[]{0x72,0x30,0x3B,0x3E,0x00,0x00,0x00,0x01});
        //全局变量数
        int global_num=symbolTable.GlobalVariables().size();
        System.out.println("GlobalVariableNum: "+global_num);
        ArrayList<Byte> bytes = Instruction.get_byte_array_by_int(global_num);
        stream.write(toByteArray(bytes));

        // 全局变量
        for(int i=0;i<symbolTable.GlobalVariables().size();i++)
        {
            Variable variable = symbolTable.GlobalVariables().get(i);
            if(variable.variableType==VariableType.STRING)
            {
                System.out.println("isConst: "+symbolTable.GlobalVariables().get(i).isConst);
                int isConst = symbolTable.GlobalVariables().get(i).isConst? 1:0;
                stream.write(isConst);
                System.out.println("length: "+variable.name.length());
                System.out.println("value: "+variable.name);
                stream.write(toByteArray(Instruction.get_byte_array_by_int(variable.name.length())));
                for(int p=0;p<variable.name.length();p++)
                    stream.write((int)variable.name.charAt(p));
                System.out.println(" ");
            }
            else
            {
                System.out.println("isConst: "+symbolTable.GlobalVariables().get(i).isConst);
                int isConst = symbolTable.GlobalVariables().get(i).isConst? 1:0;
                stream.write(isConst);
                System.out.println("length: 8");
                System.out.println("value: 0");
                stream.write(toByteArray(Instruction.get_byte_array_by_int(8)));
                stream.write(toByteArray(Instruction.get_byte_array_by_long(0)));
                System.out.println(" ");
            }
        }

        // func_num
        System.out.println("function_num: "+functionList.function_list.size());
        stream.write(toByteArray(Instruction.get_byte_array_by_int(functionList.function_list.size())));
        System.out.println("");

        //functions
        for(int i=0;i<functionList.function_list.size();i++)
        {
            Function f= functionList.function_list.get(i);
            System.out.println("function_id: "+f.id);
            stream.write(toByteArray(Instruction.get_byte_array_by_int(f.id)));
            System.out.println("return_slot: "+f.return_slot);
            stream.write(toByteArray(Instruction.get_byte_array_by_int(f.return_slot)));
            System.out.println("param_slot: "+f.param_slot);
            stream.write(toByteArray(Instruction.get_byte_array_by_int(f.param_slot)));
            System.out.println("local_slot: "+f.local_slot);
            stream.write(toByteArray(Instruction.get_byte_array_by_int(f.local_slot)));
            System.out.println("Instruction_num: "+f.instructions.size());
            stream.write(toByteArray(Instruction.get_byte_array_by_int(f.instructions.size())));
            for(int j=0;j<f.instructions.size();j++)
            {
                Instruction instruction = f.instructions.get(j);
                System.out.print(instruction.instruction_name);
                stream.write(instruction.instruction_byte);
                System.out.print(" ");
                if(instruction.with_operands)
                {
                    System.out.println(Instruction.get_num_by_byte_array(instruction.instruction_num));
                        stream.write(toByteArray(instruction.instruction_num));
                }else System.out.println(" ");
            }
            System.out.println(" ");

        }
    }
    public static String work(byte a)
    {
        int num = ((int)a+256)%256;
        int first = num/16;
        int second = num-first*16;
        String s1="",s2="";
        if(first<=9)
        {
            s1 = new String(String.valueOf(first));
        }
        else switch (first)
        {
            case 10:s1="A";
                break;
            case 11:s1="B";
                break;
            case 12:s1="C";
                break;
            case 13:s1="D";
                break;
            case 14:s1="E";
                break;
            case 15:s1="F";
                break;
        }
        if(second<=9)
        {
            s2 = new String(String.valueOf(second));
        }
        else switch (second)
        {
            case 10:s2="A";
                break;
            case 11:s2="B";
                break;
            case 12:s2="C";
                break;
            case 13:s2="D";
                break;
            case 14:s2="E";
                break;
            case 15:s2="F";
                break;
        }
        return  s1+s2;
    }
    public static byte[] toByteArray(ArrayList<Byte> bytes)
    {
        int len = bytes.size();
        byte[] res = new byte[len];
        for(int i=0;i<len;i++)
        {
            res[i]=bytes.get(i);
        }
        return res;
    }
}
