import LLVMsymbol.Scope;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static org.bytedeco.llvm.global.LLVM.*;


public class VisitorLLVM extends SysYParserBaseVisitor<LLVMValueRef> {
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");

    public LLVMModuleRef getModule() {
        return module;
    }

    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();
    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();
    LLVMTypeRef voidType = LLVMVoidType();
    //创建一个常量,这里是常数0
    LLVMValueRef zero = LLVMConstInt(i32Type, 0, /* signExtend */ 0);

    private final String targetFilePath;
    private final Scope globalScope = new Scope(null);
    private final Map<String, LLVMTypeRef> funcReturnTypes = new HashMap<>();
    private final Stack<LLVMBasicBlockRef> breakStack = new Stack<>();
    private final Stack<LLVMBasicBlockRef> continueStack = new Stack<>();

    private Scope curScope = globalScope;
    private Boolean hasReturnStatement = false;
    private LLVMValueRef curFunction = null;

    VisitorLLVM(String targetFilePath) {
        this.targetFilePath = targetFilePath;

        //初始化LLVM
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
    }

    @Override
    public LLVMValueRef visitCompUnit(SysYParser.CompUnitContext ctx) {
        LLVMValueRef ret = super.visitCompUnit(ctx);
        if(targetFilePath != null)
            LLVMPrintModuleToFile(module, targetFilePath, new BytePointer());
//        LLVMDumpModule(module);
        return ret;
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        curScope = new Scope(curScope);
        LLVMValueRef ret = super.visitBlock(ctx);
        curScope = curScope.parent;
        return ret;
    }

    @Override
    public LLVMValueRef visitDefFunc(SysYParser.DefFuncContext ctx) {
        int paramsNum = 0;
        if (ctx.funcFParams() != null)
            paramsNum = ctx.funcFParams().funcFParam().size();

        LLVMTypeRef returnType = voidType;
        if (Objects.equals(ctx.funcType().getText(), "int"))
            returnType = i32Type;

        String functionName = ctx.funcName().IDENT().getText();
        PointerPointer<Pointer> paramsType = new PointerPointer<>(paramsNum);
        for (int i = 0; i < paramsNum; i++)
            paramsType.put(i, i32Type);
        /*
         * 1. LLVMTypeRef returnType：表示函数的返回类型，即函数执行完毕后的返回值类型。
         * 2. LLVMTypeRef *paramTypes：表示函数的参数类型数组，即函数接受的参数类型列表。
         * 3. unsigned paramCount：表示函数参数的数量，即 paramTypes 数组的长度。
         * 4. bool isVarArg：表示函数是否为可变参数函数。如果为 true，则表示函数接受可变数量的参数；如果为 false，则表示函数接受固定数量的参数。
         */
        LLVMTypeRef functionType = LLVMFunctionType(returnType, paramsType, paramsNum, 0);
        LLVMValueRef function = LLVMAddFunction(module, functionName, functionType);
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(function, functionName + "Entry");
        LLVMPositionBuilderAtEnd(builder, entryBlock);
        funcReturnTypes.put(functionName, returnType);
        globalScope.define(functionName, function);

        curScope = new Scope(curScope);
        curFunction = function;
        hasReturnStatement = false;
        for (int i = 0; i < paramsNum; i++) {
            SysYParser.FuncFParamContext funcFParamContext = ctx.funcFParams().funcFParam(i);
            String paramName = funcFParamContext.IDENT().getText();
            LLVMTypeRef paramType = i32Type;
            LLVMValueRef varPointer = LLVMBuildAlloca(builder, paramType, "param_" + paramName);
            LLVMBuildStore(builder, LLVMGetParam(function, i), varPointer);
            curScope.define(paramName, varPointer);
        }
        LLVMValueRef ret = super.visitDefFunc(ctx);
        curScope = curScope.parent;
        curFunction = null;
        if (!hasReturnStatement)
            LLVMBuildRetVoid(builder);
        return ret;
    }

    @Override
    public LLVMValueRef visitStatementReturnWithExp(SysYParser.StatementReturnWithExpContext ctx) {
        hasReturnStatement = true;
        LLVMValueRef result = visit(ctx.exp());
        LLVMBuildRet(builder, result);
        return null;
    }

    @Override
    public LLVMValueRef visitStatementReturnWithoutExp(SysYParser.StatementReturnWithoutExpContext ctx) {
        hasReturnStatement = true;
        LLVMBuildRetVoid(builder);
        return null;
    }

    @Override
    public LLVMValueRef visitExpressionFunc(SysYParser.ExpressionFuncContext ctx) {
        String funcName = ctx.funcName().IDENT().getText();
        LLVMValueRef function = globalScope.getSymbolFromName(funcName);
        int argNum = 0;
        if (ctx.funcRParams() != null)
            argNum = ctx.funcRParams().param().size();
        PointerPointer<Pointer> args = new PointerPointer<>(argNum);
        for (int i = 0; i < argNum; i++)
            args.put(i, visit(ctx.funcRParams().param(i)));
        if (funcReturnTypes.get(funcName) == voidType)
            return LLVMBuildCall(builder, function, args, argNum, "");
        else if(funcReturnTypes.get(funcName) == i32Type)
            return LLVMBuildCall(builder, function, args, argNum, "tmp_");
        else
            throw new RuntimeException("Unexpected function name!");
    }

    @Override
    public LLVMValueRef visitVarDecl(SysYParser.VarDeclContext ctx) {
        for (SysYParser.VarDefContext varDef : ctx.varDef()) {
            String varName = varDef.IDENT().getText();
            LLVMTypeRef varType = i32Type;
            LLVMValueRef pointer;
            // 全局变量
            if (curScope == globalScope)
                pointer = LLVMAddGlobal(module, varType, "global_" + varName);
            // 局部变量
            else
                pointer = LLVMBuildAlloca(builder, varType, "pointer_" + varName);
            // 有赋值语句
            if (varDef.ASSIGN() != null) {
                // 不是声明为数组
                if (varDef.initVal().L_BRACE() == null) {
                    LLVMValueRef initVal = visit(varDef.initVal().exp());
                    if (curScope == globalScope)
                        LLVMSetInitializer(pointer, initVal);
                    else
                        LLVMBuildStore(builder, initVal, pointer);
                }
                // 是声明为数组
                else {

                }
            }
            // 没有赋值语句，默认初始化
            else {
                // 不是声明为数组
                if (varDef.L_BRACKT() == null || varDef.L_BRACKT().isEmpty()) {
                    if (curScope == globalScope)
                        LLVMSetInitializer(pointer, zero);
                    else
                        LLVMBuildStore(builder, zero, pointer);
                }
                // 声明为数组，需要进行数组的默认初始化
                else {

                }
            }
            curScope.define(varName, pointer);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitConstDecl(SysYParser.ConstDeclContext ctx) {
        for (SysYParser.ConstDefContext constDef : ctx.constDef()) {
            String varName = constDef.IDENT().getText();
            LLVMTypeRef constType = i32Type;
            LLVMValueRef pointer;
            // 全局变量
            if (curScope == globalScope)
                pointer = LLVMAddGlobal(module, constType, "global_" + varName);
            // 局部变量
            else
                pointer = LLVMBuildAlloca(builder, constType, "pointer_" + varName);

            // 一定有赋值语句，不是声明为数组
            if (constDef.constInitVal().L_BRACE() == null) {
                LLVMValueRef initVal = visit(constDef.constInitVal().constExp());
                if (curScope == globalScope)
                    LLVMSetInitializer(pointer, initVal);
                else
                    LLVMBuildStore(builder, initVal, pointer);
            }
            // 是声明为数组
            else {

            }
            curScope.define(varName, pointer);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitStatementLVal(SysYParser.StatementLValContext ctx) {
        LLVMValueRef leftValue = visit(ctx.lVal());
        LLVMValueRef rightValue = visit(ctx.exp());
        LLVMBuildStore(builder, rightValue, leftValue);
        return null;
    }

    @Override
    public LLVMValueRef visitExpressionLVal(SysYParser.ExpressionLValContext ctx) {
        LLVMValueRef lVal = visitLVal(ctx.lVal());
        return LLVMBuildLoad(builder, lVal, ctx.lVal().getText());
    }

    @Override
    public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        String varName = ctx.IDENT().getText();
        LLVMValueRef var = curScope.getSymbolFromName(varName);
        if (ctx.L_BRACKT() == null || ctx.L_BRACKT().isEmpty())
            return var;
        // 对数组元素的某项赋值
        else {
            return null;
        }
    }

    @Override
    public LLVMValueRef visitExpressionExp(SysYParser.ExpressionExpContext ctx) {
        return visit(ctx.exp());
    }

    @Override
    public LLVMValueRef visitStatementIf(SysYParser.StatementIfContext ctx) {
        LLVMValueRef condVal = this.visit(ctx.cond());
        condVal = LLVMBuildZExt(builder, condVal, i32Type, "tmp_");
        condVal = LLVMBuildICmp(builder, LLVMIntNE, condVal, zero, "tmp_");
        LLVMBasicBlockRef trueBranch = LLVMAppendBasicBlock(curFunction, "true_branch");
        LLVMBasicBlockRef falseBranch = null;
        if(ctx.statementElse() != null)
            falseBranch = LLVMAppendBasicBlock(curFunction, "false_branch");
        LLVMBasicBlockRef nextBlock = LLVMAppendBasicBlock(curFunction, "next");

        // 创建分支选择
        if(ctx.statementElse() == null)
            LLVMBuildCondBr(builder, condVal, trueBranch, nextBlock);
        else
            LLVMBuildCondBr(builder, condVal, trueBranch, falseBranch);

        // 布尔值为true时，进行相应处理
        LLVMPositionBuilderAtEnd(builder, trueBranch);
        this.visit(ctx.statement());
        LLVMBuildBr(builder, nextBlock);

        // 布尔值为false时，进行相应处理
        LLVMPositionBuilderAtEnd(builder, falseBranch);
        if (ctx.statementElse() != null)
            this.visit(ctx.statementElse());
        LLVMBuildBr(builder, nextBlock);
        LLVMPositionBuilderAtEnd(builder, nextBlock);
        return null;
    }

    @Override
    public LLVMValueRef visitStatementElse(SysYParser.StatementElseContext ctx) {
        return visit(ctx.statement());
    }

    @Override
    public LLVMValueRef visitStatementWhile(SysYParser.StatementWhileContext ctx) {
        LLVMBasicBlockRef cond = LLVMAppendBasicBlock(curFunction, "condition");
        LLVMBasicBlockRef trueBranch = LLVMAppendBasicBlock(curFunction, "true_branch");
        LLVMBasicBlockRef nextBlock = LLVMAppendBasicBlock(curFunction, "next");

        // 无条件跳转到condBlock计算布尔值，进而考虑下一步跳转
        LLVMBuildBr(builder, cond);
        LLVMPositionBuilderAtEnd(builder, cond);
        LLVMValueRef condVal = visit(ctx.cond());
        condVal = LLVMBuildZExt(builder, condVal, i32Type, "tmp_");
        condVal = LLVMBuildICmp(builder, LLVMIntNE, condVal, zero, "tmp_");
        LLVMBuildCondBr(builder, condVal, trueBranch, nextBlock);

        // 执行循环体
        LLVMPositionBuilderAtEnd(builder, trueBranch);
        breakStack.push(nextBlock);
        continueStack.push(cond);
        visit(ctx.statement());
        continueStack.pop();
        breakStack.pop();

        LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, nextBlock);

        return null;
    }

    @Override
    public LLVMValueRef visitStatementBreak(SysYParser.StatementBreakContext ctx) {
        LLVMBasicBlockRef exit = breakStack.peek();
        LLVMBuildBr(builder, exit);
        return null;
    }

    @Override
    public LLVMValueRef visitStatementContinue(SysYParser.StatementContinueContext ctx) {
        LLVMBasicBlockRef condBlock = continueStack.peek();
        LLVMBuildBr(builder, condBlock);
        return null;
    }

    @Override
    public LLVMValueRef visitExpressionPlusMinus(SysYParser.ExpressionPlusMinusContext ctx) {
        LLVMValueRef leftValue = visit(ctx.exp(0));
        LLVMValueRef rightValue = visit(ctx.exp(1));
        if (ctx.PLUS() != null)
            return LLVMBuildAdd(builder, leftValue, rightValue, "tmp_");
        else if (ctx.MINUS() != null)
            return LLVMBuildSub(builder, leftValue, rightValue, "tmp_");
        else
            throw new RuntimeException("Unexpected operator!");
    }

    @Override
    public LLVMValueRef visitExpressionMulDivMod(SysYParser.ExpressionMulDivModContext ctx) {
        LLVMValueRef leftValue = visit(ctx.exp(0));
        LLVMValueRef rightValue = visit(ctx.exp(1));
        if (ctx.MUL() != null)
            return LLVMBuildMul(builder, leftValue, rightValue, "tmp_");
        else if (ctx.DIV() != null)
            return LLVMBuildSDiv(builder, leftValue, rightValue, "tmp_");
        else if (ctx.MOD() != null)
            return LLVMBuildSRem(builder, leftValue, rightValue, "tmp_");
        else
            throw new RuntimeException("Unexpected operator!");
    }

    @Override
    public LLVMValueRef visitExpressionUnaryOp(SysYParser.ExpressionUnaryOpContext ctx) {
        LLVMValueRef expValue = visit(ctx.exp());
        if(ctx.unaryOp().PLUS() != null)
            return expValue;
        else if(ctx.unaryOp().MINUS() != null)
            return LLVMBuildNeg(builder, expValue, "tmp_");
        else if(ctx.unaryOp().NOT() != null) {
            long value = LLVMConstIntGetZExtValue(expValue);
            if (value == 0)
                return LLVMConstInt(i32Type, 1, 0);
            else
                return LLVMConstInt(i32Type, 0, 0);
        }
        else
            throw new RuntimeException("Unexpected operator!");
    }

    @Override
    public LLVMValueRef visitCondOr(SysYParser.CondOrContext ctx) {
        LLVMBasicBlockRef leftBranch = LLVMAppendBasicBlock(curFunction, "and_left");
        LLVMBasicBlockRef rightBranch = LLVMAppendBasicBlock(curFunction, "and_right");
        LLVMBasicBlockRef nextBlock = LLVMAppendBasicBlock(curFunction, "next");
        LLVMValueRef result = LLVMBuildAlloca(builder, i32Type, "result");

        LLVMBuildBr(builder, leftBranch);
        LLVMPositionBuilderAtEnd(builder, leftBranch);
        LLVMValueRef leftValue = LLVMBuildZExt(builder, visit(ctx.cond(0)), i32Type, "tmp_");
        LLVMValueRef leftResult = LLVMBuildICmp(builder, LLVMIntNE, leftValue, zero, "tmp_");
        LLVMBuildStore(builder, leftValue, result);
        LLVMBuildCondBr(builder, leftResult, nextBlock, rightBranch);

        LLVMPositionBuilderAtEnd(builder, rightBranch);
        LLVMValueRef rightValue = LLVMBuildZExt(builder, visit(ctx.cond(1)), i32Type, "tmp_");
        LLVMBuildStore(builder, rightValue, result);
        LLVMBuildBr(builder, nextBlock);

        LLVMPositionBuilderAtEnd(builder, nextBlock);
        return LLVMBuildLoad(builder, result, "loadFromResult");
    }

    @Override
    public LLVMValueRef visitCondAnd(SysYParser.CondAndContext ctx) {
        LLVMBasicBlockRef leftBranch = LLVMAppendBasicBlock(curFunction, "or_left");
        LLVMBasicBlockRef rightBranch = LLVMAppendBasicBlock(curFunction, "or_right");
        LLVMBasicBlockRef nextBlock = LLVMAppendBasicBlock(curFunction, "next");
        LLVMValueRef result = LLVMBuildAlloca(builder, i32Type, "result");

        LLVMBuildBr(builder, leftBranch);
        LLVMPositionBuilderAtEnd(builder, leftBranch);
        LLVMValueRef leftValue = LLVMBuildZExt(builder, visit(ctx.cond(0)), i32Type, "tmp_");
        LLVMValueRef leftResult = LLVMBuildICmp(builder, LLVMIntNE, leftValue, zero, "tmp_");
        LLVMBuildStore(builder, leftValue, result);
        LLVMBuildCondBr(builder, leftResult, rightBranch, nextBlock);

        LLVMPositionBuilderAtEnd(builder, rightBranch);
        LLVMValueRef rightValue = LLVMBuildZExt(builder, visit(ctx.cond(1)), i32Type, "tmp_");
        LLVMBuildStore(builder, rightValue, result);
        LLVMBuildBr(builder, nextBlock);

        LLVMPositionBuilderAtEnd(builder, nextBlock);
        return LLVMBuildLoad(builder, result, "loadFromResult");
    }

    @Override
    public LLVMValueRef visitCondExp(SysYParser.CondExpContext ctx) {
        return visit(ctx.exp());
    }

    @Override
    public LLVMValueRef visitCondCompare(SysYParser.CondCompareContext ctx) {
        LLVMValueRef leftValue = LLVMBuildZExt(builder, visit(ctx.cond(0)), i32Type, "tmp_");
        LLVMValueRef rightValue = LLVMBuildZExt(builder, visit(ctx.cond(1)), i32Type, "tmp_");
        if(ctx.GT() != null)
            return LLVMBuildICmp(builder, LLVMIntSGT, leftValue, rightValue, "tmp_");
        else if(ctx.GE() != null)
            return LLVMBuildICmp(builder, LLVMIntSGE, leftValue, rightValue, "tmp_");
        else if(ctx.LT() != null)
            return LLVMBuildICmp(builder, LLVMIntSLT, leftValue, rightValue, "tmp_");
        else if(ctx.LE() != null)
            return LLVMBuildICmp(builder, LLVMIntSLE, leftValue, rightValue, "tmp_");
        else
            throw new RuntimeException("Unexpected operator!");
    }

    @Override
    public LLVMValueRef visitCondEqual(SysYParser.CondEqualContext ctx) {
        LLVMValueRef leftValue = visit(ctx.cond(0));
        LLVMValueRef rightValue = visit(ctx.cond(1));
        if(ctx.EQ() != null)
            return LLVMBuildICmp(builder, LLVMIntEQ,  leftValue, rightValue, "tmp_");
        else if(ctx.NEQ() != null)
            return LLVMBuildICmp(builder, LLVMIntNE,  leftValue, rightValue, "tmp_");
        else
            throw new RuntimeException("Unexpected operator!");
    }

    // LLVMConstInt的第三个参数 bool sign_extend: 表示是否进行符号扩展。
    // 如果为 true，则表示要进行符号扩展，否则为 false。
    @Override
    public LLVMValueRef visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == SysYParser.INTEGER_CONST)
            return LLVMConstInt(i32Type, toDecimal(node.getText()), 1);
        return super.visitTerminal(node);
    }

    private int toDecimal(String numString) {
        if (numString.startsWith("0x") || numString.startsWith("0X"))
            return Integer.parseInt(numString.substring(2), 16); // 十六进制转换
        else if (numString.startsWith("0"))
            return Integer.parseInt(numString, 8); // 八进制转换
        else
            return Integer.parseInt(numString); // 十进制转换
    }
}
