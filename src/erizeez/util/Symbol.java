package erizeez.util;

public class Symbol {
    public String name;
    public SymbolKind kind;
    public SymbolType type;
    public boolean isInit = false;
    public int pos;

    public Symbol(String name, SymbolKind kind, SymbolType type, int pos) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.pos = pos;
    }

    public void initSymbol(){
        this.isInit = true;
    }
}
