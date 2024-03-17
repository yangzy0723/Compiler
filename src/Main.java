import antlr.MyErrorListener;
import antlr.SysYLexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            System.err.println("input path is required");
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        sysYLexer.removeErrorListeners();
        MyErrorListener myErrorListener = new MyErrorListener();
        sysYLexer.addErrorListener(myErrorListener);
        List<? extends Token> myTokens = sysYLexer.getAllTokens();
        if (myErrorListener.isLexerError())
            myErrorListener.printLexerErrorInformation();
        else
            for (Token t : myTokens)
                printSysYTokenInformation(t);
    }

    private static void printSysYTokenInformation(Token t){
        if(t.getType() == SysYLexer.INT){

        }
        else
            System.out.println(t.getClass() + " " + t.getText() + " at Line " + t.getLine() + ".");
    }
}