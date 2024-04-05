import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Objects;

public class MyVisitor extends SysYParserBaseVisitor<Void>{
    private final StringBuilder stringBuffer = new StringBuilder();

    public static int[] bracketColor = {SGR_Name.LightRed, SGR_Name.LightGreen, SGR_Name.LightYellow, SGR_Name.LightBlue, SGR_Name.LightMagenta, SGR_Name.LightCyan};
    public static String[] lefts = {"L_PAREN", "L_BRACKT", "L_BRACE"};
    public static String[] rights = {"R_PAREN", "R_BRACKT", "R_BRACE"};
    public static int step = 0;
    public static int nowBracketOrder = -1;

    public static String[] keywords = {"CONST", "INT", "VOID", "IF", "ELSE", "WHILE", "BREAK", "CONTINUE", "RETURN"};
    public static String[] operators = {"PLUS", "MINUS", "MUL", "DIV", "MOD", "ASSIGN", "EQ", "NEQ", "LT", "GT", "LE", "GE", "NOT", "AND", "OR", "COMMA", "SEMICOLON"};
    public static String[] integerConst = {"INTEGER_CONST"};

    private boolean isFuncName = false;
    private boolean isStatement = false;
    private boolean isDeclare = false;

    @Override
    public Void visitTerminal(TerminalNode node) {
        if(node.getSymbol().getType() != -1){
            String s = "";
            String nodeSymbolicName =SysYLexer.VOCABULARY.getSymbolicName(node.getSymbol().getType());
            String nodeLiteralName = node.getText();
            stringBuffer.append("\u001B[0m");
            if(check(nodeSymbolicName, keywords)) {
                if(!isDeclare)
                    s = "\u001B[" + SGR_Name.LightCyan + "m" + nodeLiteralName;
                else
                    s = "\u001B[" + SGR_Name.LightCyan + ";" + SGR_Name.Underlined + "m" + nodeLiteralName;
            }
            else if(check(nodeSymbolicName, operators)) {
                if(!isDeclare)
                    s = "\u001B[" + SGR_Name.LightRed + "m" + nodeLiteralName;
                else
                    s = "\u001B[" + SGR_Name.LightRed + ";" + SGR_Name.Underlined + "m" + nodeLiteralName;
            }
            else if(check(nodeSymbolicName, integerConst)) {
                if(!isDeclare)
                    s = "\u001B[" + SGR_Name.Magenta + "m" + nodeLiteralName;
                else
                    s = "\u001B[" + SGR_Name.Magenta + ";" + SGR_Name.Underlined + "m" + nodeLiteralName ;
            }
            else if(check(nodeSymbolicName, lefts)){
                nowBracketOrder++;
                if(nowBracketOrder >= 6){
                    nowBracketOrder = nowBracketOrder % 6;
                    step++;
                }
                if(!isDeclare)
                    s = "\u001B[" + bracketColor[nowBracketOrder] + "m" + nodeLiteralName;
                else
                    s = "\u001B[" + bracketColor[nowBracketOrder] + ";" + SGR_Name.Underlined + "m" + nodeLiteralName;
            }
            else if(check(nodeSymbolicName, rights)){
                if(!isDeclare)
                    s = "\u001B[" + bracketColor[nowBracketOrder] + "m" + nodeLiteralName;
                else
                    s = "\u001B[" + bracketColor[nowBracketOrder] + ";" + SGR_Name.Underlined + "m" + nodeLiteralName;
                nowBracketOrder--;
                if(nowBracketOrder < 0){
                    nowBracketOrder = 5;
                    step--;
                }
            }
            else{
                if(isFuncName && Objects.equals(nodeSymbolicName, "IDENT")){
                    if(!isDeclare)
                        s = "\u001B[" + SGR_Name.LightYellow + "m" + nodeLiteralName;
                    else
                        s = "\u001B[" + SGR_Name.LightYellow + ";" + SGR_Name.Underlined + "m" + nodeLiteralName;
                }
                else if(isDeclare) {
                    s = "\u001B[" + SGR_Name.LightMagenta + ";" + SGR_Name.Underlined + "m" + nodeLiteralName;
                }
                else if(isStatement)
                    s = "\u001B[" + SGR_Name.White + "m" + nodeLiteralName;
                else
                    s = nodeLiteralName;
            }
            stringBuffer.append(s);
        }
        return this.defaultResult();
    }

//    @Override
//    public Void visitFuncName(SysYParser.FuncNameContext ctx) {
//        isFuncName = true;
//        Void ret = super.visitFuncName(ctx);
//        isFuncName = false;
//        return ret;
//    }
//
//    @Override
//    public Void visitStatement(SysYParser.StatementContext ctx) {
//        isStatement = true;
//        Void ret = super.visitStatement(ctx);
//        isStatement = false;
//        return ret;
//    }

    @Override
    public Void visitDecl(SysYParser.DeclContext ctx) {
        isDeclare = true;
        Void ret =  super.visitDecl(ctx);
        isDeclare = false;
        return ret;
    }


    boolean check(String goal, String[] sets){
        for(String s : sets)
            if(s.equals(goal))
                return true;
        return false;
    }

    public void printStringBuffer(){
        System.err.println(this.stringBuffer);
    }
}
