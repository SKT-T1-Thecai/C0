package miniplc0java.util;

public class CheckCharType {
    public static boolean isInt(char a) {
        return a >= '0' && a <= '9';
    }

    public static boolean isAlpha(char a) {
        return (a >= 'a' && a <= 'z') || (a >= 'A' && a <= 'Z');
    }

    public static boolean isStringLiteralChar(char a)
    {
        return a <= 127 && a != '\n' && a != '\t' && a != '\r' && a != '\"' && a != '\'';
    }
    public static boolean isCharLiteralChar(char a)
    {
        return a <= 127 && a != '\n' && a != '\t' && a != '\r'  && a != '\''&& a != '\"';
    }
}
