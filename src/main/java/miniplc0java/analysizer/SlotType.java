package miniplc0java.analysizer;
public enum  SlotType {
    INT,
    DOUBLE,
    BOOL,
    ADDR;

    @Override
    public String toString() {
        if (this == INT)
            return "int";
        else if (this == DOUBLE)
            return "double";
        else if (this == BOOL)
            return "bool";
        else if (this == ADDR)
            return "addr";
        else throw new Error("this error will never occur.");
    }

}
