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
    private boolean isIf = false;
    private boolean isWhile = false;
    private boolean isFunc = false;

    @Override
    public Void visitTerminal(TerminalNode node) {
        if(node.getSymbol().getType() != -1) {
            String nodeSymbolicName = SysYLexer.VOCABULARY.getSymbolicName(node.getSymbol().getType());
            String nodeLiteralName = node.getText();
            stringBuffer.append("\u001B[0m");
            if(check(nodeSymbolicName, keywords)) {
                if(!isDeclare)
                    stringBuffer.append("\u001B[").append(SGR_Name.LightCyan).append("m").append(nodeLiteralName);
                else
                    stringBuffer.append("\u001B[").append(SGR_Name.LightCyan).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName);
            }
            else if(check(nodeSymbolicName, operators)) {
                if(!isDeclare)
                    stringBuffer.append("\u001B[").append(SGR_Name.LightRed).append("m").append(nodeLiteralName);
                else
                   stringBuffer.append("\u001B[").append(SGR_Name.LightRed).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName);
            }
            else if(check(nodeSymbolicName, integerConst)) {
                if(!isDeclare)
                   stringBuffer.append("\u001B[").append(SGR_Name.Magenta).append("m").append(nodeLiteralName);
                else
                    stringBuffer.append("\u001B[").append(SGR_Name.Magenta).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName);
            }
            else if(check(nodeSymbolicName, lefts)){
                nowBracketOrder++;
                if(nowBracketOrder >= 6){
                    nowBracketOrder = nowBracketOrder % 6;
                    step++;
                }
                if(Objects.equals(nodeSymbolicName, "L_BRACE") && !isDeclare) {
                    if(isWhile)
                        stringBuffer.append(" ");
                    else if(isIf)
                        stringBuffer.append(" ");
                    else if(isFunc)
                        stringBuffer.append(" ");
                }
                if(!isDeclare)
                    stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append("m").append(nodeLiteralName);
                else
                    stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName);
                if(Objects.equals(nodeSymbolicName, "L_BRACE") && !isDeclare) {
                    if(isWhile) {
                        stringBuffer.append("\n");
                        isWhile = false;
                    }
                    else if(isIf){
                        stringBuffer.append("\n");
                        isIf = false;
                    }
                    else if(isFunc){
                        stringBuffer.append("\n");
                        isFunc = false;
                    }
                    else
                        stringBuffer.append("\n");
                }
            }
            else if(check(nodeSymbolicName, rights)){
                if(!isDeclare)
                    stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append("m").append(nodeLiteralName);
                else
                    stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName);
                nowBracketOrder--;
                if(nowBracketOrder < 0){
                    nowBracketOrder = 5;
                    step--;
                }
            }
            else{
                if(isFuncName && Objects.equals(nodeSymbolicName, "IDENT")){
                    if(!isDeclare)
                        stringBuffer.append("\u001B[").append(SGR_Name.LightYellow).append("m").append(nodeLiteralName);
                    else
                        stringBuffer.append("\u001B[").append(SGR_Name.LightYellow).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName);
                }
                else if(isDeclare)
                    stringBuffer.append("\u001B[").append(SGR_Name.LightMagenta).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName);
                else if(isStatement)
                    stringBuffer.append("\u001B[").append(SGR_Name.White).append("m").append(nodeLiteralName);
                else
                    stringBuffer.append(nodeLiteralName);
            }
        }
        return this.defaultResult();
    }

    @Override
    public Void visitFuncName(SysYParser.FuncNameContext ctx) {
        isFuncName = true;
        Void ret = super.visitFuncName(ctx);
        isFuncName = false;
        return ret;
    }

//    @Override
//    public Void visitStatement(SysYParser.StatementContext ctx) {
//        isStatement = true;
//        Void ret = super.visitStatement(ctx);
//        stringBuffer.append("\n");
//        isStatement = false;
//        return ret;
//    }

    @Override
    public Void visitStatementIVal(SysYParser.StatementIValContext ctx) {
        isStatement = true;
        Void ret = super.visitStatementIVal(ctx);
        stringBuffer.append("\n");
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitStatementExp(SysYParser.StatementExpContext ctx) {
        isStatement = true;
        Void ret = super.visitStatementExp(ctx);
        stringBuffer.append("\n");
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitStatementBlock(SysYParser.StatementBlockContext ctx) {
        Void ret = super.visitStatementBlock(ctx);
        stringBuffer.append("\n");
        return ret;
    }

    @Override
    public Void visitStatementIf(SysYParser.StatementIfContext ctx) {
        isStatement = true;
        isIf = true;
        Void ret = super.visitStatementIf(ctx);
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitStatementWhile(SysYParser.StatementWhileContext ctx) {
        isStatement = true;
        isWhile = true;
        Void ret = super.visitStatementWhile(ctx);
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitStatementBreak(SysYParser.StatementBreakContext ctx) {
        isStatement = true;
        Void ret = super.visitStatementBreak(ctx);
        stringBuffer.append("\n");
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitStatementContinue(SysYParser.StatementContinueContext ctx) {
        isStatement = true;
        Void ret = super.visitStatementContinue(ctx);
        stringBuffer.append("\n");
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitStatementReturnWithExp(SysYParser.StatementReturnWithExpContext ctx) {
        isStatement = true;
        Void ret = super.visitStatementReturnWithExp(ctx);
        stringBuffer.append("\n");
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitStatementReturnWithoutExp(SysYParser.StatementReturnWithoutExpContext ctx) {
        isStatement = true;
        Void ret = super.visitStatementReturnWithoutExp(ctx);
        stringBuffer.append("\n");
        isStatement = false;
        return ret;
    }

    @Override
    public Void visitDefFunc(SysYParser.DefFuncContext ctx) {
        isFunc = true;
        Void ret = super.visitDefFunc(ctx);
        stringBuffer.append("\n");
        return ret;
    }

    @Override
    public Void visitDecl(SysYParser.DeclContext ctx) {
        isDeclare = true;
        Void ret =  super.visitDecl(ctx);
        stringBuffer.append("\n");
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
        System.out.println(this.stringBuffer);
    }
}
