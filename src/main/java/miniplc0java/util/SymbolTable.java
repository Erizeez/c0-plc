package miniplc0java.util;

import java.util.Stack;

public class SymbolTable {
    public Stack<Symbol> symbolStack = new Stack();
    public Stack<Integer> index = new Stack();
    public int fnNum = 0;
    public int globalNum = 0;
    public int num = 0;

    public void pushSymbol(String name, SymbolKind kind, SymbolType type){
        symbolStack.push(new Symbol(name, kind, type, num++));
    }

    public void pushParam(String name, SymbolType type){
        if(symbolStack.peek().kind == SymbolKind.PARAM){
            symbolStack.push(new Symbol(name, SymbolKind.PARAM, type, symbolStack.peek().pos + 1));
        }else{
            symbolStack.push(new Symbol(name, SymbolKind.PARAM, type, 0));
        }
    }

    public void pushFn(String name){
        symbolStack.push(new Symbol(name, SymbolKind.FN, SymbolType.NONE, fnNum++));
    }

    public void pushGlobal(String name, SymbolKind kind, SymbolType type){
        symbolStack.insertElementAt(new Symbol(name, kind, type, globalNum), globalNum);
        globalNum++;
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

    public Symbol getFn(String fnName){
        for(Symbol s : symbolStack){
            System.out.println(s.name);
            if(fnName.equals(s.name)
                    && s.kind == SymbolKind.FN){
                return s;
            }
        }
        return null;
    }

}
