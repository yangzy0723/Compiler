import org.antlr.v4.runtime.tree.TerminalNode;

public class MyVisitor extends SysYParserBaseVisitor<Void>{

    public static int[] bracketColor = {SGR_Name.LightRed, SGR_Name.LightGreen, SGR_Name.LightYellow, SGR_Name.LightBlue, SGR_Name.LightMagenta, SGR_Name.LightCyan};
    public static int step = 0;
    public static int nowBracketOrder = 0;

    public static String[] keywords = {"CONST", "INT", "VOID", "IF", "ELSE", "WHILE", "BREAK", "CONTINUE", "RETURN"};
    public static String[] operators = {"PLUS", "MINUS", "MUL", "DIV", "MOD", "ASSIGN", "EQ", "NEQ", "LT", "GT", "LE", "GE", "NOT", "AND", "OR", "COMMA", "SEMICOLON"};
    public static String[] integerConst = {"INTEGER_CONST"};


    @Override
    public Void visitTerminal(TerminalNode node) {
//        if(node.getSymbol().getType() != -1){
//            if(SysYLexer.ruleNames[node.getSymbol().getType()] ==)
//        }
        String n = (node.getParent()).toString();
        //name correct
        String name = node.getText();
        if(node.getSymbol().getType() != -1)
            System.out.println(SysYLexer.VOCABULARY.getSymbolicName(node.getSymbol().getType()) + " " + name);
        return null;
    }


}
