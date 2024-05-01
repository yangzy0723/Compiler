package symbol;

public class Int implements Type{
    @Override
    public boolean equals(Object o) {
        return o instanceof Int;
    }
}
