import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class MyParserErrorListener extends BaseErrorListener {
    private final StringBuilder errorBuffer = new StringBuilder();
    private boolean parserError = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        this.parserError = true;
        String errorInformation = "Error type B at Line " + line + ": " + msg + "\n";
        this.errorBuffer.append(errorInformation);
    }

    public boolean isParserError() {
        return this.parserError;
    }

    public void printParserErrorInformation() {
        System.out.println(this.errorBuffer);
    }
}