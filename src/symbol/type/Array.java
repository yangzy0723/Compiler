package symbol.type;

public class Array implements Type {
    public Type contained;

    public Array(Type contained) {
        this.contained = contained;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Array array = (Array) o;
        return contained.equals(array.contained);
    }
}
