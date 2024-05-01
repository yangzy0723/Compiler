package symbol.type;

public class Void implements Type {
    @Override
    public boolean equals(Object o) {
        return o instanceof Void;
    }
}
