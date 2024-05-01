package symbol;

import java.util.LinkedHashMap;
import java.util.Map;

public class Scope {
    public Map<String, Symbol> symbolTable;
    public Scope parent;

    public Scope(Scope scope) {
        this.parent = scope;
        this.symbolTable = new LinkedHashMap<>();
        define(new BaseSymbol("int", new Int()));
        define(new BaseSymbol("void", new Void()));
    }

    public void define(Symbol symbol) {
        String symbolName = symbol.getName();
        symbolTable.put(symbolName, symbol);
    }

    public Symbol getSymbolFromName(String name) {
        Symbol symbol = symbolTable.get(name);
        if (symbol != null)
            return symbol;
        if (parent != null)
            return parent.getSymbolFromName(name);
        return null;
    }

    public Type getTypeFromName(String name){
        return getSymbolFromName(name).getType();
    }
}
