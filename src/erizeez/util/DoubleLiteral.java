package erizeez.util;

public class DoubleLiteral {
    public String integerPart;
    public String floatPart;
    public String ePart = "0";

    public DoubleLiteral(String integerPart, String floatPart, String ePart) {
        this.integerPart = integerPart;
        this.floatPart = floatPart;
        this.ePart = ePart;
    }

    public DoubleLiteral(String integerPart, String floatPart) {
        this.integerPart = integerPart;
        this.floatPart = floatPart;
    }

    @Override
    public String toString(){
        return integerPart + "." + floatPart + "E" + ePart;
    }
}
