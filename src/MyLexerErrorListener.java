import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class MyLexerErrorListener extends BaseErrorListener {
    private boolean lexerError = false;
    private final StringBuilder errorBuffer = new StringBuilder();
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (e != null) {
            this.lexerError = true;
            String errorInformation = "Error type A at Line " + line + ":" + msg + "\n";
            this.errorBuffer.append(errorInformation);
        }
    }

    public boolean isLexerError() {
        return this.lexerError;
    }

    public void printLexerErrorInformation() {
        System.err.println(this.errorBuffer);
    }
}
