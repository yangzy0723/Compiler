package symbol;

public class Error implements Type {
    public static int errorCount;

    public Error() {
        errorCount++;
    }
}
