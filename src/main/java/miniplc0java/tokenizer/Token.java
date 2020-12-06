package miniplc0java.tokenizer;
import miniplc0java.util.*;


public class Token {
    public TokenType tokenType;
    public Object value;
    public Pos startPos;
    public Pos endPos;
    public Token(TokenType tokenType, Object value, Pos startPos, Pos endPos) {
        this.tokenType = tokenType;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Line: ").append(this.startPos.row).append(' ');
        sb.append("Column: ").append(this.startPos.col).append(' ');
        sb.append("Type: ").append(this.tokenType).append(' ');
        sb.append("Value: ").append(this.value);
        return sb.toString();
    }
}
