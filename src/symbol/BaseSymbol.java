package symbol;

import symbol.type.Type;

public class BaseSymbol implements Symbol {
    public Type type;
    public String name;

    public BaseSymbol(String name, Type type) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }
}
