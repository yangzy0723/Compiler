import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Objects;
import java.util.Stack;
import java.util.Vector;

public class MyVisitor extends SysYParserBaseVisitor<Void>{
    private int indentLevel = 0;
    private int needRecover = 0;
    private boolean extraIndent = false;
    private StringBuilder stringBuffer = new StringBuilder();
    private final Stack<Integer> lastIfNeedRecovers = new Stack<>();
    private final Vector<StringBuilder> stringBuffers = new Vector<>();
    private final Vector<Integer> indentLevels = new Vector<>();

    public static int[] bracketColor = {SGR_Name.LightRed, SGR_Name.LightGreen, SGR_Name.LightYellow, SGR_Name.LightBlue, SGR_Name.LightMagenta, SGR_Name.LightCyan};
    public static String[] lefts = {"L_PAREN", "L_BRACKT", "L_BRACE"};
    public static String[] rights = {"R_PAREN", "R_BRACKT", "R_BRACE"};
    public static int step = 0;
    public static int nowBracketOrder = -1;

    public static String[] keywords = {"CONST", "INT", "VOID", "IF", "ELSE", "WHILE", "BREAK", "CONTINUE", "RETURN"};
    public static String[] operators = {"PLUS", "MINUS", "MUL", "DIV", "MOD", "ASSIGN", "EQ", "NEQ", "LT", "GT", "LE", "GE", "NOT", "AND", "OR", "COMMA", "SEMICOLON"};
    public static String[] keywordsSpace = {"CONST", "INT", "VOID", "IF", "ELSE", "WHILE", "RETURN"};
    public static String[] operatorsSpace = {"PLUS", "MINUS", "MUL", "DIV", "MOD", "ASSIGN", "EQ", "NEQ", "LT", "GT", "LE", "GE", "AND", "OR"};
    public static String[] integerConst = {"INTEGER_CONST"};

    private boolean isFuncName = false;
    private boolean isStatement = false;
    private boolean isDeclare = false;
    private boolean isLeftBraceSpace = false;
    private boolean isLeftBraceSpaceElse = false;   //用于判断else {这种情况，else右边一个空格，{左边一个空格
    private boolean isBreakWithoutExp = false;
    private boolean isUnaryOp = false;
    private boolean passIf = false;
    private boolean passElse = false;
    private boolean passElseIf = false;
    private boolean passWhile = false;

    @Override
    public Void visitTerminal(TerminalNode node) {
        if(node.getSymbol().getType() != -1) {
            String nodeSymbolicName = SysYLexer.VOCABULARY.getSymbolicName(node.getSymbol().getType());
            String nodeLiteralName = node.getText();

            if(check(nodeSymbolicName, keywords)) {
                if(Objects.equals(nodeSymbolicName, "IF")) {
                    if(passElse)
                        passElseIf = true;
                    else
                        passIf = true;
                    passElse = false;
                    isLeftBraceSpaceElse = false;
                }
                else if(Objects.equals(nodeSymbolicName, "ELSE")) {
                    isLeftBraceSpaceElse = true;
                    passElse = true;
                }
                else if(Objects.equals(nodeSymbolicName, "WHILE"))
                    passWhile = true;

                if(!isDeclare)
                    stringBuffer.append("\u001B[").append(SGR_Name.LightCyan).append("m").append(nodeLiteralName).append("\u001B[0m");
                else
                    stringBuffer.append("\u001B[").append(SGR_Name.LightCyan).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName).append("\u001B[0m");

                if(check(nodeSymbolicName, keywordsSpace)){
                    if(!(Objects.equals(nodeSymbolicName, "RETURN") && isBreakWithoutExp))
                        stringBuffer.append(" ");
                }
            }
            else if(check(nodeSymbolicName, operators)) {
                if(!isUnaryOp && check(nodeSymbolicName, operatorsSpace))
                    stringBuffer.append(" ");

                if(!isDeclare)
                    stringBuffer.append("\u001B[").append(SGR_Name.LightRed).append("m").append(nodeLiteralName).append("\u001B[0m");
                else
                    stringBuffer.append("\u001B[").append(SGR_Name.LightRed).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName).append("\u001B[0m");

                if(!isUnaryOp && check(nodeSymbolicName, operatorsSpace))
                        stringBuffer.append(" ");
                isUnaryOp = false;

                if(Objects.equals(nodeSymbolicName, "COMMA"))
                    stringBuffer.append(" ");
            }
            else if(check(nodeSymbolicName, integerConst)) {
                if(!isDeclare)
                   stringBuffer.append("\u001B[").append(SGR_Name.Magenta).append("m").append(nodeLiteralName).append("\u001B[0m");
                else
                    stringBuffer.append("\u001B[").append(SGR_Name.Magenta).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName).append("\u001B[0m");
            }
            else if(check(nodeSymbolicName, lefts)){
                nowBracketOrder++;
                if(nowBracketOrder >= 6){
                    nowBracketOrder = nowBracketOrder % 6;
                    step++;
                }

                if(isDeclare)
                    stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName).append("\u001B[0m");
                else{
                    if(Objects.equals(nodeSymbolicName, "L_BRACE")){
                        if(isLeftBraceSpaceElse){
                            stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append("m").append("{").append("\u001B[0m");
                            isLeftBraceSpaceElse = false;
                        }
                        else if(isLeftBraceSpace){
                            stringBuffer.append(" ");
                            stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append("m").append("{").append("\u001B[0m");
                            isLeftBraceSpace = false;
                        }
                        else{
                            newLine();
                            stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append("m").append("{").append("\u001B[0m");
                        }
                        extraIndent = true;
                        indentLevel++;
                    }
                    else
                        stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append("m").append(nodeLiteralName).append("\u001B[0m");
                }
            }
            else if(check(nodeSymbolicName, rights)){
                if(isDeclare)
                    stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName).append("\u001B[0m");
                else{
                    if(Objects.equals(nodeSymbolicName, "R_BRACE")) {
                        newLine();
                        indentLevel--;
                    }
                    stringBuffer.append("\u001B[").append(bracketColor[nowBracketOrder]).append("m").append(nodeLiteralName).append("\u001B[0m");
                }

                nowBracketOrder--;
                if(nowBracketOrder < 0){
                    nowBracketOrder = 5;
                    step--;
                }
            }
            else{
                if(isFuncName && Objects.equals(nodeSymbolicName, "IDENT")){
                    if(!isDeclare)
                        stringBuffer.append("\u001B[").append(SGR_Name.LightYellow).append("m").append(nodeLiteralName).append("\u001B[0m");
                    else
                        stringBuffer.append("\u001B[").append(SGR_Name.LightYellow).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName).append("\u001B[0m");
                }
                else if(isDeclare)
                    stringBuffer.append("\u001B[").append(SGR_Name.LightMagenta).append(";").append(SGR_Name.Underlined).append("m").append(nodeLiteralName).append("\u001B[0m");
                else if(isStatement)
                    stringBuffer.append("\u001B[").append(SGR_Name.White).append("m").append(nodeLiteralName).append("\u001B[0m");
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

    @Override
    public Void visitStatementIVal(SysYParser.StatementIValContext ctx) {
        newLine();

        isStatement = true;
        Void ret = super.visitStatementIVal(ctx);
        isStatement = false;

        return ret;
    }

    @Override
    public Void visitStatementExp(SysYParser.StatementExpContext ctx) {
        newLine();

        isStatement = true;
        Void ret = super.visitStatementExp(ctx);
        isStatement = false;

        return ret;
    }

    @Override
    public Void visitStatementIf(SysYParser.StatementIfContext ctx) {
        if(!passElse)
            newLine();
        else
            passElse = false;

        lastIfNeedRecovers.push(needRecover);
        isStatement = true;
        isLeftBraceSpace = true;
        Void ret = super.visitStatementIf(ctx);
        isLeftBraceSpace = false;
        isStatement = false;
        lastIfNeedRecovers.pop();

        isLeftBraceSpaceElse = false;

        return ret;
    }

    @Override
    public Void visitStatementElse(SysYParser.StatementElseContext ctx) {
        int lastIfNeedRecover = lastIfNeedRecovers.peek();
        needRecover = needRecover - lastIfNeedRecover;
        newLine();
        Void ret = super.visitStatementElse(ctx);
        needRecover += lastIfNeedRecover;
        return ret;
    }

    @Override
    public Void visitStatementWhile(SysYParser.StatementWhileContext ctx) {
        newLine();

        isStatement = true;
        isLeftBraceSpace = true;
        Void ret = super.visitStatementWhile(ctx);
        isLeftBraceSpace = false;
        isStatement = false;

        return ret;
    }

    @Override
    public Void visitStatementBreak(SysYParser.StatementBreakContext ctx) {
        newLine();

        return super.visitStatementBreak(ctx);
    }

    @Override
    public Void visitStatementContinue(SysYParser.StatementContinueContext ctx) {
        newLine();

        isStatement = true;
        Void ret = super.visitStatementContinue(ctx);
        isStatement = false;

        return ret;
    }

    @Override
    public Void visitStatementReturnWithExp(SysYParser.StatementReturnWithExpContext ctx) {
        newLine();

        isStatement = true;
        Void ret = super.visitStatementReturnWithExp(ctx);
        isStatement = false;

        return ret;
    }

    @Override
    public Void visitStatementReturnWithoutExp(SysYParser.StatementReturnWithoutExpContext ctx) {
        newLine();

        isStatement = true;
        isBreakWithoutExp = true;
        Void ret = super.visitStatementReturnWithoutExp(ctx);
        isBreakWithoutExp = false;
        isStatement = false;

        return ret;
    }

    @Override
    public Void visitDefFunc(SysYParser.DefFuncContext ctx) {
        newLine();
        newLine();

        isLeftBraceSpace = true;
        Void ret = super.visitDefFunc(ctx);
        isLeftBraceSpace = false;

        return ret;
    }

    @Override
    public Void visitDecl(SysYParser.DeclContext ctx) {
        newLine();

        isDeclare = true;
        Void ret =  super.visitDecl(ctx);
        isDeclare = false;

        return ret;
    }

    @Override
    public Void visitExpressionUnaryOp(SysYParser.ExpressionUnaryOpContext ctx) {
        isUnaryOp = true;
        Void ret = super.visitExpressionUnaryOp(ctx);
        isUnaryOp = false;

        return ret;
    }

    @Override
    public Void visitBlock(SysYParser.BlockContext ctx) {
        passIf = false;
        passElse = false;
        passElseIf = false;
        passWhile = false;

        int tmp = 0;
        if(isLeftBraceSpace) {
            tmp = needRecover;
            needRecover = 0;
        }
        Void ret = super.visitBlock(ctx);
        needRecover = tmp;

        return ret;
    }

    private boolean check(String goal, String[] sets){
        for(String s : sets)
            if(s.equals(goal))
                return true;
        return false;
    }

    private void newLine(){
        if(isLeftBraceSpaceElse)
            isLeftBraceSpaceElse = false;
        if(passIf || passElse || passElseIf || passWhile){
            stringBuffers.add(stringBuffer);
            indentLevels.add(indentLevel);
            stringBuffer = new StringBuilder();
            indentLevel++;
            needRecover++;
            passIf = false;
            passElse = false;
            passElseIf = false;
            passWhile = false;
        }
        else if(needRecover > 0){
            stringBuffers.add(stringBuffer);
            indentLevels.add(indentLevel);
            stringBuffer = new StringBuilder();
            indentLevel -= needRecover;
            needRecover = 0;
        }
        else {
            if (extraIndent)
                indentLevel--;
            stringBuffers.add(stringBuffer);
            indentLevels.add(indentLevel);
            if (extraIndent) {
                extraIndent = false;
                indentLevel++;
            }
            stringBuffer = new StringBuilder();
        }
    }

    public void printStringBuffer(){
        newLine();
        while(stringBuffers.get(0).length() == 0) {
            stringBuffers.remove(0);
            indentLevels.remove(0);
        }
        for(int i = 0; i < stringBuffers.size(); i++) {
            StringBuilder sb = stringBuffers.get(i);
            while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
                sb.deleteCharAt(sb.length() - 1);
            }
            dealIndent(indentLevels.get(i));
            System.out.println(sb);
        }
    }

    private void dealIndent(int indentLevel){
        System.out.print(" ".repeat(Math.max(0, indentLevel * 4)));
    }

}
