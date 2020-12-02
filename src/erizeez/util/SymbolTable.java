package erizeez.util;

import java.util.Stack;

public class SymbolTable {
    public Stack<Symbol> symbolStack;
    public Stack<Integer> index;

    public void pushSymbol(String name, SymbolKind kind, SymbolType type){
        if(symbolStack.peek().kind == SymbolKind.VAR
                || symbolStack.peek().kind == SymbolKind.CONST){
            symbolStack.push(new Symbol(name, kind, type, symbolStack.peek().pos + 1));
        }else{
            symbolStack.push(new Symbol(name, kind, type, 0));
        }
    }

    public void pushParam(String name, SymbolType type){
        if(symbolStack.peek().kind == SymbolKind.PARAM){
            symbolStack.push(new Symbol(name, SymbolKind.PARAM, type, symbolStack.peek().pos + 1));
        }else{
            symbolStack.push(new Symbol(name, SymbolKind.PARAM, type, 0));
        }
    }

    public void pushFn(String name, SymbolType type){
        symbolStack.push(new Symbol(name, SymbolKind.FN, type, 0));
    }

    public boolean isNowExist(String name){
        for(int i = symbolStack.size() - 1; i >= index.peek(); i--){
            if(name.equals(symbolStack.get(i).name)){
                return true;
            }
        }
        return false;
    }

    public void clearNow(){
        while(symbolStack.size() >= index.peek()){
            symbolStack.pop();
        }
    }

    public boolean isExist(String name){
        for(int i = symbolStack.size() - 1; i >= 0; i--){
            if(name.equals(symbolStack.get(i).name)){
                return true;
            }
        }
        return false;
    }

    public Symbol getNowExist(String name){
        for(int i = symbolStack.size() - 1; i >= index.peek(); i--){
            if(name.equals(symbolStack.get(i).name)){
                return symbolStack.get(i);
            }
        }
        return null;
    }

    public Symbol getExist(String name){
        for(int i = symbolStack.size() - 1; i >= 0; i--){
            if(name.equals(symbolStack.get(i).name)){
                return symbolStack.get(i);
            }
        }
        return null;
    }
}
