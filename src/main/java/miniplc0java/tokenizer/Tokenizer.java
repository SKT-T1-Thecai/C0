package miniplc0java.tokenizer;
import java.util.ArrayList;

import miniplc0java.util.Caculate;
import miniplc0java.util.CheckCharType;
import miniplc0java.util.Pos;

public class Tokenizer {
    public ArrayList<Token> TokenList= new ArrayList<>();
    public ReadFile readFile;
    public String InputSrc;
    public Tokenizer(String inputSrc)
    {
        this.InputSrc = inputSrc;
        this.readFile = new ReadFile(this.InputSrc);
        this.readFile.ReadInit();
    }
    public Token NextToken() throws Error
    {
        if (readFile.isEOF()) {
            return new Token(TokenType.EOF, "", readFile.NowPos,readFile.NowPos);
        }
        skipSpaceCharacters();
        if(CheckCharType.isAlpha(readFile.GetNowChar())||readFile.GetNowChar()=='_')
        {
            return LexIdentOrKeyword();
        }
        else if (CheckCharType.isInt(readFile.GetNowChar()))
        {
            return LexIntOrDouble();
        }
        else return LexOtherSign();
    }
    public Token LexIdentOrKeyword() throws Error
    {
        //
        try{
            Pos startPos = readFile.NowPos;
            StringBuilder sb = new StringBuilder();
            while(CheckCharType.isAlpha(readFile.GetNowChar())||CheckCharType.isInt(readFile.GetNowChar())||readFile.GetNowChar()=='_')
            {
                sb.append(readFile.GetNowChar());
                readFile.GoNext();
            }
            Pos endPos = readFile.NowPos;
            String strIn = sb.toString();
            switch(strIn)
            {
                case "fn":
                    return new Token(TokenType.FN_KW,"fn",startPos,endPos);
                case "let":
                    return new Token(TokenType.LET_KW,"let",startPos,endPos);
                case "const":
                    return new Token(TokenType.CONST_KW,"const",startPos,endPos);
                case "as":
                    return new Token(TokenType.AS_KW,"as",startPos,endPos);
                case "while":
                    return new Token(TokenType.WHILE_KW,"while",startPos,endPos);
                case "if":
                    return new Token(TokenType.IF_KW,"if",startPos,endPos);
                case "else":
                    return new Token(TokenType.ELSE_KW,"else",startPos,endPos);
                case "return":
                    return new Token(TokenType.RETURN_KW,"return",startPos,endPos);
                case "break":
                    return new Token(TokenType.BREAK_KW,"break",startPos,endPos);
                case "continue":
                    return new Token(TokenType.CONTINUE_KW,"continue",startPos,endPos);
                default:return new Token(TokenType.IDENT,strIn,startPos,endPos);
            }
        }catch (Exception E)
        {
            throw new Error("failed while tokenizers:location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);
        }
    }
    public Token LexIntOrDouble() throws Error //1.56e+6
    {
        try{
            Pos startPos = readFile.NowPos;
            Pos endPos;
            StringBuilder IntegralPart = new StringBuilder();
            while(CheckCharType.isInt(readFile.GetNowChar()))
            {
                IntegralPart.append(readFile.GetNowChar());
                readFile.GoNext();
            }
            if(readFile.GetNowChar()!='.')
                {
                    endPos = readFile.NowPos;
                    return new Token(TokenType.UINT_LITERAL,Long.valueOf(IntegralPart.toString()),startPos,endPos);
                }
            else {
                if(CheckCharType.isInt(readFile.GetNextChar()))
                {
                    //15.63 15.63E12 15.63E+16
                    readFile.GoNext();
                    StringBuilder FractionalPart = new StringBuilder();
                    while(CheckCharType.isInt(readFile.GetNowChar()))
                    {
                        FractionalPart.append(readFile.GetNowChar());
                        readFile.GoNext();
                    }
                    if(readFile.GetNowChar()=='e'||readFile.GetNowChar()=='E')
                    {
                        if(CheckCharType.isInt(readFile.GetNextChar()))// 1.56e45
                        {
                            readFile.GoNext();
                            StringBuilder ExponentPart = new StringBuilder();
                            while(CheckCharType.isInt(readFile.GetNowChar()))
                            {
                                ExponentPart.append(readFile.GetNowChar());
                                readFile.GoNext();
                            }
                            endPos = readFile.NowPos;
                            return new Token(TokenType.DOUBLE_LITERAL, Caculate.ScienceCoutToDouble(IntegralPart,FractionalPart,ExponentPart
                            ),startPos,endPos);

                        }
                        else if (readFile.GetNextChar()=='+'||readFile.GetNextChar()=='-')
                        {
                            if(CheckCharType.isInt(readFile.GetNextNextChar()))
                            {
                                readFile.GoNext();
                                StringBuilder ExponentPart = new StringBuilder();
                                ExponentPart.append(readFile.GetNowChar());
                                readFile.GoNext();
                                while(CheckCharType.isInt(readFile.GetNowChar()))
                                    {
                                        ExponentPart.append(readFile.GetNowChar());
                                        readFile.GoNext();
                                    }
                                endPos = readFile.NowPos;
                                return new Token(TokenType.DOUBLE_LITERAL, Caculate.ScienceCoutToDouble(IntegralPart,FractionalPart,ExponentPart
                                ),startPos,endPos);
                            }
                            else
                                {
                                    endPos = readFile.NowPos;
                                    return new Token(TokenType.DOUBLE_LITERAL, Caculate.ScienceCoutToDouble(IntegralPart,FractionalPart,new StringBuilder("0")
                                    ),startPos,endPos);

                                }

                        }
                        else {
                            //1.56Epp 指针指向E
                            endPos = readFile.NowPos;
                            return new Token(TokenType.DOUBLE_LITERAL, Caculate.ScienceCoutToDouble(IntegralPart,FractionalPart,new StringBuilder("0")
                            ),startPos,endPos);
                        }
                    }
                    else {
                        //15.66A... 指针指向A
                        endPos = readFile.NowPos;
                        return new Token(TokenType.DOUBLE_LITERAL, Caculate.ScienceCoutToDouble(IntegralPart,FractionalPart,new StringBuilder("0")
                        ),startPos,endPos);

                    }
                }else {
                    throw new Error("failed while tokenizers:location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);
                }

            }
        }catch (Exception E)
        {
            throw new Error("failed while tokenizers:location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);
            //throw E;
        }
    }
    public Token LexOtherSign() throws Error
    {
        try{
            Pos startPos = readFile.NowPos;
            Pos endPos;
            char beginSign = readFile.GetNowChar();
            switch(beginSign)
            {
                // 仅用一个字符就能确定的token
                case '+':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.PLUS,beginSign,startPos,endPos);
                case '*':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.MUL,beginSign,startPos,endPos);
                case '(':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.L_PAREN,beginSign,startPos,endPos);
                case ')':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.R_PAREN,beginSign,startPos,endPos);
                case '{':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.L_BRACE,beginSign,startPos,endPos);
                case '}':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.R_BRACE,beginSign,startPos,endPos);
                case ',':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.COMMA,beginSign,startPos,endPos);
                case ':':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.COLON,beginSign,startPos,endPos);
                case ';':
                    readFile.GoNext();
                    endPos = readFile.NowPos;
                    return new Token(TokenType.SEMICOLON,beginSign,startPos,endPos);
                case '-': // - or ->
                    readFile.GoNext();
                    if(readFile.GetNowChar()=='>')
                    {
                        readFile.GoNext();
                        endPos = readFile.NowPos;
                        return new Token(TokenType.ARROW, "->",startPos,endPos);
                    }
                    else
                    {
                        endPos = readFile.NowPos;
                        return new Token(TokenType.MINUS,beginSign,startPos,endPos);
                    }
                case '<':// < or <=
                    readFile.GoNext();
                    if(readFile.GetNowChar()=='=')
                    {
                        readFile.GoNext();
                        endPos = readFile.NowPos;
                        return new Token(TokenType.LE, "<=",startPos,endPos);
                    }
                    else
                    {
                        endPos = readFile.NowPos;
                        return new Token(TokenType.LT,beginSign,startPos,endPos);
                    }
                case '>':// > or >=
                    readFile.GoNext();
                    if(readFile.GetNowChar()=='=')
                    {
                        readFile.GoNext();
                        endPos = readFile.NowPos;
                        return new Token(TokenType.GE, ">=",startPos,endPos);
                    }
                    else
                    {
                        endPos = readFile.NowPos;
                        return new Token(TokenType.GT,beginSign,startPos,endPos);
                    }
                case '!':// !=
                    readFile.GoNext();
                    if(readFile.GetNowChar()=='=')
                    {
                        readFile.GoNext();
                        endPos = readFile.NowPos;
                        return new Token(TokenType.NEQ, "!=",startPos,endPos);
                    }
                    else    throw new Error("failed while tokenizers:location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);
                case '=':// = or ==
                    readFile.GoNext();
                    if(readFile.GetNowChar()=='=')
                    {
                        readFile.GoNext();
                        endPos = readFile.NowPos;
                        return new Token(TokenType.EQ, "==",startPos,endPos);
                    }
                    else
                    {
                        endPos = readFile.NowPos;
                        return new Token(TokenType.ASSIGN,beginSign,startPos,endPos);
                    }
                case '"':// 字符串字面量
                    readFile.GoNext();
                    char getChar = readFile.GetNowChar();
                    StringBuilder sb = new StringBuilder();
                    while (CheckCharType.isStringLiteralChar(getChar))
                    {
                        if(getChar=='\\'&&(readFile.GetNextChar()=='n'||readFile.GetNextChar()=='r'||readFile.GetNextChar()=='t'
                                ||readFile.GetNextChar()=='\''||readFile.GetNextChar()=='\"'||readFile.GetNextChar()=='\\'))
                        {
//                            sb.append(getChar);
//                            readFile.GoNext();
//                            sb.append(readFile.GetNowChar());
//                            readFile.GoNext();
                            readFile.GoNext();
                            char cur = readFile.GetNowChar();
                            switch (cur)
                            {
                                case 'n':sb.append('\n');
                                break;
                                case 'r':sb.append('\r');
                                    break;
                                case 't':sb.append('\t');
                                    break;
                                case '\'':sb.append('\'');
                                    break;
                                case '\"':sb.append('\"');
                                    break;
                                case '\\':sb.append('\\');
                                    break;
                                default:throw new Error("impossible error");
                            }
                            readFile.GoNext();
                            getChar = readFile.GetNowChar();
                        }
                        else
                        {
                            sb.append(getChar);
                            readFile.GoNext();
                            getChar = readFile.GetNowChar();
                        }
                    }
                    if(getChar=='"')
                    {
                        readFile.GoNext();
                        endPos = readFile.NowPos;
                        return  new Token(TokenType.STRING_LITERAL,sb.toString(),startPos,endPos);
                    }else throw new Error("failed while tokenizers:location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);
                case '\'':// 字符串字面量
                    readFile.GoNext();
                    char getChar0 = readFile.GetNowChar();
                    long value = 0;
                    if (CheckCharType.isCharLiteralChar(getChar0))
                    {
                        if(getChar0=='\\'&&(readFile.GetNextChar()=='n'||readFile.GetNextChar()=='r'||readFile.GetNextChar()=='t'
                                ||readFile.GetNextChar()=='\''||readFile.GetNextChar()=='\"'||readFile.GetNextChar()=='\\'))
                        {
                            readFile.GoNext();
                            char nowChar = readFile.GetNowChar();
                            if(nowChar=='n')value='\n';else if(nowChar=='r')value='\r';else if(nowChar=='t')value='\t';
                            else value=nowChar;
                            readFile.GoNext();
                            getChar0 = readFile.GetNowChar();
                        }
                        else
                        {
                            value = getChar0;
                            readFile.GoNext();
                            getChar0 = readFile.GetNowChar();
                        }
                    }
                    if(getChar0=='\'')
                    {
                        readFile.GoNext();
                        endPos = readFile.NowPos;
                        return  new Token(TokenType.UINT_LITERAL,value,startPos,endPos);
                    }else throw new Error("failed while tokenizers:location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);
                case '/':if(readFile.GetNextChar()=='/')
                            {
                                readFile.GoNext();
                                readFile.GoNext();
                                StringBuilder comment = new StringBuilder();
                                while(readFile.GetNowChar()!='\n')
                                {
                                    comment.append(readFile.GetNowChar());
                                    readFile.GoNext();
                                }
                                endPos = readFile.NowPos;
                                return  new Token(TokenType.COMMENT,comment.toString(),startPos,endPos);
                            }
                            else {
                                readFile.GoNext();
                                endPos = readFile.NowPos;
                                return  new Token(TokenType.DIV,'/',startPos,endPos);
                                 }
                default:throw new Error("failed while tokenizers(illegal start character):location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);

         }
        }catch (Exception E)
        {
            throw new Error("failed while tokenizers:location--> row:"+readFile.NowPos.row+"col:"+readFile.NowPos.col);
        }
    }
    private void skipSpaceCharacters() {
        while (!readFile.isEOF() && Character.isWhitespace(readFile.GetNowChar())) {
            readFile.GoNext();
        }
    }

    public void TokenizerInit()
    {
        while(true)
        {
            skipSpaceCharacters();
            Token tk = NextToken();
            if(tk==null)return;
            else TokenList.add(tk);
            if(tk.tokenType==TokenType.EOF)return;
        }

    }
    public void print_tokens(){
        for(int i=0;i<TokenList.size();i++)
        {
            System.out.println(TokenList.get(i));
        }

    }

    public static void main(String[] args) throws Error {
        Tokenizer t= new Tokenizer("D:\\C0_new\\src\\in.txt");
        t.TokenizerInit();
//        for(int i=0;i<t.TokenList.size();i++)
//        {
//            System.out.println(t.TokenList.get(i));
//        }
        t.print_tokens();
    }
}
