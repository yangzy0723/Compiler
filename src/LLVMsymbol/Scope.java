package LLVMsymbol;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.LinkedHashMap;
import java.util.Map;

public class Scope {
    public Map<String, LLVMValueRef> symbolTable;
    public Scope parent;

    public Scope(Scope scope) {
        this.parent = scope;
        this.symbolTable = new LinkedHashMap<>();
    }

    public void define(String name, LLVMValueRef symbol) {
        symbolTable.put(name, symbol);
    }

    public LLVMValueRef getSymbolFromName(String name) {
        LLVMValueRef symbol = symbolTable.get(name);
        if (symbol != null)
            return symbol;
        if (parent != null)
            return parent.getSymbolFromName(name);
        return null;
    }
}
