import org.antlr.v4.runtime.tree.TerminalNode;
import symbol.*;
import symbol.Error;
import symbol.Void;

import java.util.ArrayList;
import java.util.List;

public class MyTypeVisitor extends SysYParserBaseVisitor<Type> {
    private final Scope globalScope = new Scope(null);
    private Scope curScope = globalScope;

    @Override
    public Type visitConstDecl(SysYParser.ConstDeclContext ctx) {
        Type type = globalScope.getTypeFromName(ctx.bType().getText());
        boolean hasError = false;
        for (SysYParser.ConstDefContext constDef : ctx.constDef()) {
            String varName = constDef.IDENT().getText();
            Symbol varSymbol;
            Type subType = null;
            if (constDef.constInitVal() != null)
                subType = visit(constDef.constInitVal());
            if (constDef.L_BRACKT().isEmpty()){
                varSymbol = new BaseSymbol(varName, type);
                if (subType != null && !(subType instanceof Error)) {
                    if (!subType.equals(type)) {
                        hasError = true;
                        OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_ASSIGN.ordinal(), ctx.getStart().getLine());
                    }
                }
            }
            else {
                int dimension = constDef.L_BRACKT().size();
                Type currentType = new Int();
                for (int i = 0; i < dimension; i++)
                    currentType = new Array(currentType);
                varSymbol = new BaseSymbol(varName, currentType);
            }
            if (checkVariableRedefined(varName)) {
                OutputHelper.printSemanticError(ErrorType.REDEFINED_VARIABLE.ordinal(), ctx.getStart().getLine());
                hasError = true;
            }
            else
                curScope.define(varSymbol);
        }
        if (hasError)
            return new Error();
        return null;
    }

    @Override
    public Type visitVarDecl(SysYParser.VarDeclContext ctx) {
        Type type = globalScope.getTypeFromName(ctx.bType().getText());
        boolean hasError = false;
        for (SysYParser.VarDefContext varDef : ctx.varDef()) {
            String varName = varDef.IDENT().getText();
            Symbol varSymbol;
            Type subType = null;
            if (varDef.initVal() != null)
                subType = visit(varDef.initVal());
            if (varDef.L_BRACKT().isEmpty()){
                varSymbol = new BaseSymbol(varName, type);
                if (subType != null && !(subType instanceof Error)) {
                    if (!subType.equals(type)) {
                        hasError = true;
                        OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_ASSIGN.ordinal(), ctx.getStart().getLine());
                    }
                }
            }
            else {
                int dimension = varDef.L_BRACKT().size();
                Type currentType = new Int();
                for (int i = 0; i < dimension; i++)
                    currentType = new Array(currentType);
                varSymbol = new BaseSymbol(varName, currentType);
            }
            if (checkVariableRedefined(varName)) {
                OutputHelper.printSemanticError(ErrorType.REDEFINED_VARIABLE.ordinal(), ctx.getStart().getLine());
                hasError = true;
            }
            else
                curScope.define(varSymbol);

        }
        if (hasError)
            return new Error();
        return null;
    }

    @Override
    public Type visitDefFunc(SysYParser.DefFuncContext ctx) {
        Type returnType = globalScope.getTypeFromName(ctx.funcType().getText());
        String funcName = ctx.funcName().getText();

        if (globalScope.getSymbolFromName(funcName) != null) {
            OutputHelper.printSemanticError(ErrorType.REDEFINED_FUNCTION.ordinal(), ctx.getStart().getLine());
            return new Error();
        }

        List<Type> paramsType = new ArrayList<>();
        List<BaseSymbol> paramSymbols = new ArrayList<>();

        if (ctx.funcFParams() != null) {
            for (SysYParser.FuncFParamContext param : ctx.funcFParams().funcFParam()) {
                Type paramType = globalScope.getTypeFromName(param.bType().getText());
                String paramName = param.IDENT().getText();

                if (param.R_BRACKT().isEmpty())
                    paramSymbols.add(new BaseSymbol(paramName, paramType));
                else {
                    Type currentType = paramType;
                    int dimension = param.R_BRACKT().size();
                    for (int i = 0; i < dimension; i++)
                        currentType = new Array(currentType);
                    paramSymbols.add(new BaseSymbol(paramName, currentType));
                }
            }
        }

        Function function = new Function(returnType, paramsType);
        FunctionSymbol funcSymbol = new FunctionSymbol(funcName, function, curScope);
        globalScope.define(funcSymbol);
        curScope = funcSymbol;

        for (BaseSymbol param : paramSymbols) {
            if (checkVariableRedefined(param.getName()))
                OutputHelper.printSemanticError(ErrorType.REDEFINED_VARIABLE.ordinal(), ctx.getStart().getLine());
            else {
                paramsType.add(param.getType());
                funcSymbol.define(param);
            }
        }

        visitBlock(ctx.block());
        curScope = curScope.parent;

        return null;
    }

    @Override
    public Type visitBlock(SysYParser.BlockContext ctx) {
        curScope = new Scope(curScope);
        visitChildren(ctx);
        curScope = curScope.parent;
        return null;
    }

    @Override
    public Type visitExpressionFunc(SysYParser.ExpressionFuncContext ctx) {
        Symbol symbol = curScope.getSymbolFromName(ctx.funcName().getText());

        if (symbol == null) {
            OutputHelper.printSemanticError(ErrorType.UNDEFINED_FUNCTION.ordinal(), ctx.funcName().getStart().getLine());
            return new Error();
        }

        if (symbol instanceof FunctionSymbol) {
            FunctionSymbol funcSymbol = (FunctionSymbol) symbol;
            Function funcType = (Function) funcSymbol.getType();
            List<Type> paramTypes = funcType.paramsType;
            if (ctx.funcRParams() == null) {
                if (!paramTypes.isEmpty()) {
                    OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_PARAMETERS.ordinal(), ctx.funcName().getStart().getLine());
                    return new Error();
                }
            }
            else {
                List<SysYParser.ParamContext> realParams = ctx.funcRParams().param();
                if (realParams.size() != paramTypes.size()) {
                    OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_PARAMETERS.ordinal(), ctx.funcName().getStart().getLine());
                    return new Error();
                }
                int argc = realParams.size();
                for (int i = 0; i < argc; i ++) {
                    // 底下就出错了, 默认对的类型
                    Type subType = visit(realParams.get(i).exp());
                    if (subType instanceof Error)
                        continue;
                    Type expectedType = paramTypes.get(i);
                    if (!expectedType.equals(subType)) {
                        OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_PARAMETERS.ordinal(), ctx.funcName().getStart().getLine());
                        return new Error();
                    }
                }
            }
            return funcType.returnType;
        }
        OutputHelper.printSemanticError(ErrorType.VARIABLE_NOT_CALLABLE.ordinal(), ctx.funcName().getStart().getLine());
        return new Error();
    }

    @Override
    public Type visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == SysYParser.IDENT) {
            String name = node.getText();
            Symbol symbol = curScope.getSymbolFromName(name);
            if (symbol == null) {
                OutputHelper.printSemanticError(ErrorType.UNDECLARED_VARIABLE.ordinal(), node.getSymbol().getLine());
                return new Error();
            }
            return symbol.getType();
        }
        if (node.getSymbol().getType() == SysYParser.INTEGER_CONST)
            return new Int();
        return null;
    }

    @Override
    public Type visitStatementReturnWithoutExp(SysYParser.StatementReturnWithoutExpContext ctx) {
        Scope scope = curScope;
        while (!(scope instanceof FunctionSymbol))
            scope = scope.parent;
        FunctionSymbol functionSymbol = (FunctionSymbol) scope;
        Type returnType = ((Function) functionSymbol.getType()).returnType;
        if(!(returnType instanceof Void)){
            OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_RETURN_VALUE.ordinal(), ctx.getStart().getLine());
            return new Error();
        }
        return null;
    }

    @Override
    public Type visitStatementReturnWithExp(SysYParser.StatementReturnWithExpContext ctx) {
        Type subType = visit(ctx.exp());
        if (!(subType instanceof Error)) {
            Scope scope = curScope;
            while (!(scope instanceof FunctionSymbol))
                scope = scope.parent;
            FunctionSymbol functionSymbol = (FunctionSymbol) scope;
            Type returnType = ((Function) functionSymbol.getType()).returnType;
            if(returnType instanceof Int){
                if(!(subType instanceof Int)){
                    OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_RETURN_VALUE.ordinal(), ctx.getStart().getLine());
                    return new Error();
                }
            }
            if(returnType instanceof Void){
                if(!(subType instanceof Void)){
                    OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_RETURN_VALUE.ordinal(), ctx.getStart().getLine());
                    return new Error();
                }
            }
        }
        return null;
    }

    @Override
    public Type visitStatementIVal(SysYParser.StatementIValContext ctx) {
        Type leftType = visitLVal(ctx.lVal());
        Type rightType = visit(ctx.exp());
        if (!(leftType instanceof Error)) {
            if (leftType instanceof Function) {
                OutputHelper.printSemanticError(ErrorType.ASSIGN_TO_FUNCTION.ordinal(), ctx.getStart().getLine());
                return new Error();
            }
        }
        if (!(leftType instanceof Error) && !(rightType instanceof Error)) {
            if (!leftType.equals(rightType)) {
                OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_ASSIGN.ordinal(), ctx.getStart().getLine());
                return new Error();
            }
        }
        return null;
    }

    @Override
    public Type visitExpressionUnaryOp(SysYParser.ExpressionUnaryOpContext ctx) {
        Type subType = visit(ctx.exp());
        if (subType instanceof Error)
            return new Error();
        if (subType instanceof Int)
            return subType;
        OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_OPERATION.ordinal(), ctx.getStart().getLine());
        return new Error();
    }

    @Override
    public Type visitExpressionMulDivMod(SysYParser.ExpressionMulDivModContext ctx) {
        return checkExp(ctx.exp(0), ctx.exp(1));
    }
    @Override
    public Type visitExpressionPlusMinus(SysYParser.ExpressionPlusMinusContext ctx) {
        return checkExp(ctx.exp(0), ctx.exp(1));
    }
    @Override
    public Type visitCondOr(SysYParser.CondOrContext ctx) {
        return checkCond(ctx.cond(0), ctx.cond(1));
    }
    @Override
    public Type visitCondAnd(SysYParser.CondAndContext ctx) {
        return checkCond(ctx.cond(0), ctx.cond(1));
    }
    @Override
    public Type visitCondEqual(SysYParser.CondEqualContext ctx) {
        return checkCond(ctx.cond(0), ctx.cond(1));
    }
    @Override
    public Type visitCondCompare(SysYParser.CondCompareContext ctx) {
        return checkCond(ctx.cond(0), ctx.cond(1));
    }
    private Type checkExp(SysYParser.ExpContext leftCtx, SysYParser.ExpContext rightCtx){
        int lineNumber = leftCtx.getStart().getLine();
        Type left = visit(leftCtx);
        Type right = visit(rightCtx);
        return check(left, right, lineNumber);
    }
    private Type checkCond(SysYParser.CondContext leftCond, SysYParser.CondContext rightCond){
        int lineNumber = leftCond.getStart().getLine();
        Type left = visit(leftCond);
        Type right = visit(rightCond);
        return check(left, right, lineNumber);
    }
    private Type check(Type leftType, Type rightType, int lineNumber){
        if (leftType instanceof Error || rightType instanceof Error)
            return new Error();
        if (!leftType.equals(rightType) || !(leftType instanceof Int)) {
            OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_OPERATION.ordinal(), lineNumber);
            return new Error();
        }
        return leftType;
    }

    @Override
    public Type visitLVal(SysYParser.LValContext ctx) {
        List<SysYParser.ExpContext> indexes = ctx.exp();
        if (indexes == null || indexes.isEmpty())
            return visitTerminal(ctx.IDENT());
        Type currentType = visitTerminal(ctx.IDENT());
        for (SysYParser.ExpContext index : indexes) {
            // 对函数或者变量使用下标运算符
            if (!(currentType instanceof Array)) {
                OutputHelper.printSemanticError(ErrorType.VARIABLE_NOT_ADDRESSABLE.ordinal(), ctx.getStart().getLine());
                return new Error();
            }
            currentType = ((Array) currentType).contained;
            visit(index);
        }
        return currentType;
    }

    private boolean checkVariableRedefined(String varName) {
        if (curScope == globalScope)
            if (globalScope.getSymbolFromName(varName) != null)
                return true;
        Scope parentScope = curScope.parent;
        if (parentScope instanceof FunctionSymbol)
            parentScope = parentScope.parent;
        if (parentScope == null)
            parentScope = globalScope;
        return curScope.getSymbolFromName(varName) != parentScope.getSymbolFromName(varName);
    }
}

class OutputHelper {
    public static void printSemanticError(int errorType, int errorLine){
        System.err.println("Error type " + errorType + " at Line " + errorLine + ": ERROR!");
    }
}
