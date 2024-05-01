import org.antlr.v4.runtime.tree.TerminalNode;
import symbol.*;
import symbol.type.*;
import symbol.type.Void;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyTypeVisitor extends SysYParserBaseVisitor<Type> {
    public static boolean error;

    private final Scope globalScope = new Scope(null);
    private Scope curScope = globalScope;

    @Override
    public Type visitConstDecl(SysYParser.ConstDeclContext ctx) {
        boolean hasError = false;
        Type type = getPrimitiveType(ctx.bType().getText());
        for (SysYParser.ConstDefContext constDef : ctx.constDef()) {
            String varName = constDef.IDENT().getText();
            Symbol varSymbol;
            Type subType = null;
            if (constDef.constInitVal() != null)
                subType = visit(constDef.constInitVal());
            if (constDef.L_BRACKT().isEmpty()){
                varSymbol = new VarSymbol(varName, type);
                if (subType != null && !(subType instanceof Undefined)) {
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
                varSymbol = new VarSymbol(varName, currentType);
            }
            if (checkRedefined(varName)) {
                hasError = true;
                OutputHelper.printSemanticError(ErrorType.REDEFINED_VARIABLE.ordinal(), ctx.getStart().getLine());
            }
            else
                curScope.define(varSymbol);
        }
        if (hasError) {
            error = true;
            return new Undefined();
        }
        return null;
    }

    @Override
    public Type visitVarDecl(SysYParser.VarDeclContext ctx) {
        boolean hasError = false;
        Type type = getPrimitiveType(ctx.bType().getText());
        for (SysYParser.VarDefContext varDef : ctx.varDef()) {
            String varName = varDef.IDENT().getText();
            Symbol varSymbol;
            Type subType = null;
            if (varDef.initVal() != null)
                subType = visit(varDef.initVal());
            if (varDef.L_BRACKT().isEmpty()){
                varSymbol = new VarSymbol(varName, type);
                if (subType != null && !(subType instanceof Undefined)) {
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
                varSymbol = new VarSymbol(varName, currentType);
            }
            if (checkRedefined(varName)) {
                hasError = true;
                OutputHelper.printSemanticError(ErrorType.REDEFINED_VARIABLE.ordinal(), ctx.getStart().getLine());
            }
            else
                curScope.define(varSymbol);

        }
        if (hasError) {
            error = true;
            return new Undefined();
        }
        return null;
    }

    @Override
    public Type visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == SysYParser.IDENT) {
            String name = node.getText();
            Symbol symbol = curScope.getSymbolFromName(name);
            if (symbol == null) {
                error = true;
                OutputHelper.printSemanticError(ErrorType.UNDECLARED_VARIABLE.ordinal(), node.getSymbol().getLine());
                return new Undefined();
            }
            return symbol.getType();
        }
        if (node.getSymbol().getType() == SysYParser.INTEGER_CONST)
            return new Int();
        return null;
    }

    @Override
    public Type visitDefFunc(SysYParser.DefFuncContext ctx) {
        Type returnType = getPrimitiveType(ctx.funcType().getText());
        String funcName = ctx.funcName().getText();

        if (globalScope.getSymbolFromName(funcName) != null) {
            OutputHelper.printSemanticError(ErrorType.REDEFINED_FUNCTION.ordinal(), ctx.getStart().getLine());
            error = true;
            return new Undefined();
        }

        List<Type> paramsType = new ArrayList<>();
        List<VarSymbol> paramSymbols = new ArrayList<>();

        if (ctx.funcFParams() != null) {
            for (SysYParser.FuncFParamContext param : ctx.funcFParams().funcFParam()) {
                Type paramType = getPrimitiveType(param.bType().getText());
                String paramName = param.IDENT().getText();

                if (param.R_BRACKT().isEmpty())
                    paramSymbols.add(new VarSymbol(paramName, paramType));
                else {
                    Type currentType = paramType;
                    int dimension = param.R_BRACKT().size();
                    for (int i = 0; i < dimension; i++)
                        currentType = new Array(currentType);
                    paramSymbols.add(new VarSymbol(paramName, currentType));
                }
            }
        }

        Function function = new Function(returnType, paramsType);
        FunctionSymbol funcSymbol = new FunctionSymbol(funcName, function, curScope);
        globalScope.define(funcSymbol);
        curScope = funcSymbol;
        for (VarSymbol param : paramSymbols) {
            if (checkRedefined(param.getName())) {
                error = true;
                OutputHelper.printSemanticError(ErrorType.REDEFINED_VARIABLE.ordinal(), ctx.getStart().getLine());
            }
            else {
                paramsType.add(param.getType());
                funcSymbol.define(param);
            }
        }
        // Function有两层作用域
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
            error = true;
            OutputHelper.printSemanticError(ErrorType.UNDEFINED_FUNCTION.ordinal(), ctx.funcName().getStart().getLine());
            return new Undefined();
        }

        if (symbol instanceof FunctionSymbol) {
            Function funcType = (Function) symbol.getType();
            List<Type> paramTypes = funcType.paramsType;
            if (ctx.funcRParams() == null) {
                if(!paramTypes.isEmpty()) {
                    error = true;
                    OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_PARAMETERS.ordinal(), ctx.funcName().getStart().getLine());
                    return new Undefined();
                }
            }
            else {
                List<SysYParser.ParamContext> realParams = ctx.funcRParams().param();
                if (realParams.size() != paramTypes.size()) {
                    error = true;
                    OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_PARAMETERS.ordinal(), ctx.funcName().getStart().getLine());
                    return new Undefined();
                }
                for (int i = 0; i < realParams.size(); i++) {
                    Type subType = visit(realParams.get(i).exp());
                    Type expectedType = paramTypes.get(i);
                    if (subType instanceof Undefined)
                        continue;
                    if (!expectedType.equals(subType)) {
                        error = true;
                        OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_PARAMETERS.ordinal(), ctx.funcName().getStart().getLine());
                        return new Undefined();
                    }
                }
            }
            return funcType.returnType;
        }
        else {
            error = true;
            OutputHelper.printSemanticError(ErrorType.VARIABLE_NOT_CALLABLE.ordinal(), ctx.funcName().getStart().getLine());
            return new Undefined();
        }
    }

    @Override
    public Type visitStatementReturnWithoutExp(SysYParser.StatementReturnWithoutExpContext ctx) {
        Scope scope = curScope;
        while (!(scope instanceof FunctionSymbol))
            scope = scope.parent;
        Type returnType = ((Function)((FunctionSymbol)scope).getType()).returnType;
        if(!(returnType instanceof Void)){
            error = true;
            OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_RETURN_VALUE.ordinal(), ctx.getStart().getLine());
            return new Undefined();
        }
        return null;
    }

    @Override
    public Type visitStatementReturnWithExp(SysYParser.StatementReturnWithExpContext ctx) {
        Type subType = visit(ctx.exp());
        if (!(subType instanceof Undefined)) {
            Scope scope = curScope;
            while (!(scope instanceof FunctionSymbol))
                scope = scope.parent;
            Type returnType = ((Function)((FunctionSymbol)scope).getType()).returnType;
            if(!returnType.equals(subType)) {
                error = true;
                OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_RETURN_VALUE.ordinal(), ctx.getStart().getLine());
                return new Undefined();
            }
        }
        return null;
    }

    @Override
    public Type visitStatementIVal(SysYParser.StatementIValContext ctx) {
        Type leftType = visitLVal(ctx.lVal());
        Type rightType = visit(ctx.exp());
        if (!(leftType instanceof Undefined)) {
            if (leftType instanceof Function) {
                error = true;
                OutputHelper.printSemanticError(ErrorType.ASSIGN_TO_FUNCTION.ordinal(), ctx.getStart().getLine());
                return new Undefined();
            }
        }
        if (!(leftType instanceof Undefined) && !(rightType instanceof Undefined)) {
            if (!leftType.equals(rightType)) {
                error = true;
                OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_ASSIGN.ordinal(), ctx.getStart().getLine());
                return new Undefined();
            }
        }
        return null;
    }

    @Override
    public Type visitExpressionUnaryOp(SysYParser.ExpressionUnaryOpContext ctx) {
        Type subType = visit(ctx.exp());
        if (subType instanceof Int)
            return subType;
        if (subType instanceof Undefined)
            return new Undefined();
        error = true;
        OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_OPERATION.ordinal(), ctx.getStart().getLine());
        return new Undefined();
    }

    @Override
    public Type visitExpressionExp(SysYParser.ExpressionExpContext ctx) {
        return visit(ctx.exp());
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
        if(leftType.equals(rightType) && (leftType instanceof Int))
            return leftType;
        if (!leftType.equals(rightType) || !(leftType instanceof Int))
            OutputHelper.printSemanticError(ErrorType.INCOMPATIBLE_OPERATION.ordinal(), lineNumber);
        error = true;
        return new Undefined();
    }

    @Override
    public Type visitLVal(SysYParser.LValContext ctx) {
        List<SysYParser.ExpContext> indexes = ctx.exp();
        if (indexes.isEmpty())
            return visitTerminal(ctx.IDENT());
        Type currentType = visitTerminal(ctx.IDENT());
        for (SysYParser.ExpContext index : indexes) {
            // 对函数或者变量使用下标运算符
            if (!(currentType instanceof Array)) {
                error = true;
                OutputHelper.printSemanticError(ErrorType.VARIABLE_NOT_ADDRESSABLE.ordinal(), ctx.getStart().getLine());
                return new Undefined();
            }
            currentType = ((Array) currentType).contained;
            visit(index);
        }
        return currentType;
    }

    private boolean checkRedefined(String varName) {
        if (curScope == globalScope)
            if (globalScope.getSymbolFromName(varName) != null)
                return true;
        Scope parentScope = curScope.parent;
        if (parentScope instanceof FunctionSymbol)
            parentScope = parentScope.parent;
        if(parentScope == null)
            parentScope = globalScope;
        return curScope.getSymbolFromName(varName) != parentScope.getSymbolFromName(varName);
    }

    private Type getPrimitiveType(String name) {
        if(Objects.equals(name, "int"))
            return new Int();
        else if(Objects.equals(name, "void"))
            return new Void();
        else
            return new Undefined();
    }
}

class OutputHelper {
    public static void printSemanticError(int errorType, int errorLine){
        System.err.println("Error type " + errorType + " at Line " + errorLine + ": ERROR!");
    }
}