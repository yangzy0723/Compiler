package symbol;

public class FunctionSymbol extends Scope implements Symbol{
    public String name;
    public Type type;

    public FunctionSymbol(String name, Function type, Scope parent) {
        super(parent);
        this.name = name;
        this.type = type;
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
