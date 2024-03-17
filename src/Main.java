import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;


import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            System.err.println("input path is required");
//        String source = "tests/test1.sysy";
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
        if(t.getType() == SysYLexer.INTEGER_CONST){
            int num = 0;
            String numString = t.getText();
            String decimalString = "";
            if (numString.startsWith("0x") || numString.startsWith("0X"))
                num = Integer.parseInt(numString.substring(2), 16); // 十六进制转换
            else if (numString.startsWith("0"))
                num = Integer.parseInt(numString, 8); // 八进制转换
            else
                num = Integer.parseInt(numString); // 十进制转换
            decimalString = String.valueOf(num);
            System.out.println(SysYLexer.VOCABULARY.getSymbolicName(t.getType()) + " " + decimalString + " at Line " + t.getLine() + ".");
        }
        else
            System.out.println(SysYLexer.VOCABULARY.getSymbolicName(t.getType()) + " " + t.getText() + " at Line " + t.getLine() + ".");
    }
}