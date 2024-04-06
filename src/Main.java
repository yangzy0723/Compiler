import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;


import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            System.err.println("input path is required");
        // String source = "tests/test1.sysy";
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        sysYLexer.removeErrorListeners();
        MyLexerErrorListener myLexerErrorListener = new MyLexerErrorListener();
        sysYLexer.addErrorListener(myLexerErrorListener);
//        List<? extends Token> myTokens = sysYLexer.getAllTokens();
//        if (myLexerErrorListener.isLexerError())
//            myLexerErrorListener.printLexerErrorInformation();
//        else
//            for (Token t : myTokens)
//                printSysYTokenInformation(t);
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);
        sysYParser.removeErrorListeners();
        MyParserErrorListener myParserErrorListener = new MyParserErrorListener();
        sysYParser.addErrorListener(myParserErrorListener);

        ParseTree tree = sysYParser.compUnit();
        MyVisitor myVisitor = new MyVisitor();
        myVisitor.visit(tree);

        if(myParserErrorListener.isParserError())
            myParserErrorListener.printParserErrorInformation();
        else
            myVisitor.printStringBuffer();
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
            System.err.println(SysYLexer.VOCABULARY.getSymbolicName(t.getType()) + " " + decimalString + " at Line " + t.getLine() + ".");
        }
        else
            System.err.println(SysYLexer.VOCABULARY.getSymbolicName(t.getType()) + " " + t.getText() + " at Line " + t.getLine() + ".");
    }
}