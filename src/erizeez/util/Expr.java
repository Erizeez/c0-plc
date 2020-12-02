package erizeez.util;

public class Expr {
    public ExprType type;
    public Object value;

    public Expr(ExprType type, Object value) {
        this.type = type;
        this.value = value;
    }
}
