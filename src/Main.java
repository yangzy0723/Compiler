import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            System.err.println("input path is required");
        if (args.length < 2)
            System.err.println("output path is required");
        String source = "tests/test1.sysy";
        String target1 = "tests/out.ll";
        String target = "tests/out.asm";
//        String source = args[0];
//        String target = args[1];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);

//        sysYLexer.removeErrorListeners();
//        MyLexerErrorListener myLexerErrorListener = new MyLexerErrorListener();
//        sysYLexer.addErrorListener(myLexerErrorListener);

//        List<? extends Token> myTokens = sysYLexer.getAllTokens();
//        if (myLexerErrorListener.isLexerError())
//            myLexerErrorListener.printLexerErrorInformation();
//        else
//            for (Token t : myTokens)
//                printSysYTokenInformation(t);

        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);

//        sysYParser.removeErrorListeners();
//        MyParserErrorListener myParserErrorListener = new MyParserErrorListener();
//        sysYParser.addErrorListener(myParserErrorListener);

        ParseTree tree = sysYParser.compUnit();
//        VisitorColorAndFormat myColorAndFormatVisitor = new VisitorColorAndFormat();
//        myColorAndFormatVisitor.visit(tree);

//        if(myParserErrorListener.isParserError())
//            myParserErrorListener.printParserErrorInformation();
//        else
//            myColorAndFormatVisitor.printStringBuffer();

//        VisitorType myTypeVisitor = new VisitorType();
//        myTypeVisitor.visit(tree);
//        if (!VisitorType.error)
//            System.err.println("No semantic errors in the program!");

        VisitorLLVM myLLVMVisitor = new VisitorLLVM(null);
        myLLVMVisitor.visit(tree);

        String asm = AsmBuilder.buildAsmCode(myLLVMVisitor.getModule());

        BufferedWriter writer = new BufferedWriter(new FileWriter(target));
        writer.write(asm);
        writer.close();
    }

    private static void printSysYTokenInformation(Token t) {
        if (t.getType() == SysYLexer.INTEGER_CONST) {
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
        } else
            System.err.println(SysYLexer.VOCABULARY.getSymbolicName(t.getType()) + " " + t.getText() + " at Line " + t.getLine() + ".");
    }
}