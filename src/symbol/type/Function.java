package symbol.type;


import java.util.List;

public class Function implements Type {
    public Type returnType;
    public List<Type> paramsType;

    public Function(Type returnType, List<Type> paramsType) {
        this.returnType = returnType;
        this.paramsType = paramsType;
    }
}
