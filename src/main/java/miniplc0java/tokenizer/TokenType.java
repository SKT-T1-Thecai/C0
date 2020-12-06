package miniplc0java.tokenizer;

public enum TokenType {
    FN_KW,           //fn
    LET_KW,          //let
    CONST_KW,        //const
    AS_KW,           //as
    WHILE_KW,        //while
    IF_KW,           //if
    ELSE_KW,         //else
    RETURN_KW,       //return
    BREAK_KW,        //break
    CONTINUE_KW,     //continue

    UINT_LITERAL,                     //0  00 10256
    STRING_LITERAL,
    DOUBLE_LITERAL,                   //1.34E4
    CHAR_LITERAL,

    IDENT,                            // [_a-zA-Z] [_a-zA-Z0-9]*


    PLUS,               //+
    MINUS,              //-
    MUL,                //*
    DIV,                // \
    ASSIGN,             //=
    EQ,                 // ==
    NEQ,                // !=
    LT,                 // <
    GT,                 // >
    LE,                 // <=
    GE,                 // >=
    L_PAREN,            // (
    R_PAREN,            // )
    L_BRACE,            // {
    R_BRACE,            // }
    ARROW,              // ->
    COMMA ,             //,
    COLON ,             //:
    //;
    SEMICOLON,
    COMMENT,            //   '//'+*+'\n'
    EOF,
}

