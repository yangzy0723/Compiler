import LLVMsymbol.Scope;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import static org.bytedeco.llvm.global.LLVM.*;


public class MyLLVMVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");
    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();
    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();
    //创建一个常量,这里是常数0
    LLVMValueRef zero = LLVMConstInt(i32Type, 0, /* signExtend */ 0);

    private String targetFilePath;

    private final Scope globalScope = new Scope(null);
    private Scope curScope = globalScope;

    MyLLVMVisitor(String targetFilePath) {
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
        LLVMPrintModuleToFile(module, targetFilePath, new BytePointer());
        // LLVMDumpModule(module);
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
        String functionName = ctx.funcName().IDENT().getText();
        LLVMTypeRef returnType = i32Type;
        /*
         * 1. LLVMTypeRef returnType：表示函数的返回类型，即函数执行完毕后的返回值类型。
         * 2. LLVMTypeRef *paramTypes：表示函数的参数类型数组，即函数接受的参数类型列表。
         * 3. unsigned paramCount：表示函数参数的数量，即 paramTypes 数组的长度。
         * 4. bool isVarArg：表示函数是否为可变参数函数。如果为 true，则表示函数接受可变数量的参数；如果为 false，则表示函数接受固定数量的参数。
         */
        LLVMTypeRef functionType = LLVMFunctionType(returnType, LLVMVoidType(), 0, 0);
        LLVMValueRef function = LLVMAddFunction(module, functionName, functionType);
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(function, functionName + "Entry");
        LLVMPositionBuilderAtEnd(builder, entryBlock);

        globalScope.define(functionName, function);
        curScope = new Scope(curScope);
        LLVMValueRef ret = super.visitDefFunc(ctx);
        curScope = curScope.parent;
        return ret;
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
                if (varDef.initVal().L_BRACE() == null) {
                    LLVMValueRef initVal = visit(varDef.initVal().exp());
                    if (curScope == globalScope)
                        LLVMSetInitializer(pointer, initVal);
                    else
                        LLVMBuildStore(builder, initVal, pointer);
                }
                // 是对数组的定义
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
    public LLVMValueRef visitStatementReturnWithExp(SysYParser.StatementReturnWithExpContext ctx) {
        LLVMValueRef result = visit(ctx.exp());
        LLVMBuildRet(builder, result);
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
        String op = ctx.unaryOp().getText();
        LLVMValueRef expValue = visit(ctx.exp());
        switch (op) {
            case "+":
                return expValue;
            case "-":
                return LLVMBuildNeg(builder, expValue, "tmp_");
            case "!":
                long value = LLVMConstIntGetZExtValue(expValue);
                if (value == 0)
                    return LLVMConstInt(i32Type, 1, 0);
                else
                    return LLVMConstInt(i32Type, 0, 0);
            default:
                throw new RuntimeException("Unexpected operator!");
        }
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
