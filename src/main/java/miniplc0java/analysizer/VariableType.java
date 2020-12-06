package miniplc0java.analysizer;

public enum VariableType {
    INT,
    DOUBLE,
    VOID,
    STRING;

    @Override
    public String toString() {
        switch (this)
        {
            case INT:return "int";
            case VOID: return "void";
                case DOUBLE:return "double";
                    case STRING:return "string";
            default:return null;
        }
    }
}
