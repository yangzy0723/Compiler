import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class AsmBuilder {
    static StringBuffer buffer = new StringBuffer();

    public static void op1(String op, String dest, String lhs) {
        buffer.append(String.format("  %s %s, %s\n", op, dest, lhs));
    }

    public static void op2(String op, String dest, String lhs, String rhs) {
        buffer.append(String.format("  %s %s, %s, %s\n", op, dest, lhs, rhs));
    }

    public static String buildAsmCode(LLVMModuleRef module) {
        for (LLVMValueRef value = LLVMGetFirstGlobal(module); value != null; value = LLVMGetNextGlobal(value)) {
            /** 判断全局变量和函数
             if () {
             break;
             }
             */
        }
        for (LLVMValueRef func = LLVMGetFirstFunction(module); func != null; func = LLVMGetNextFunction(func)) {
            buffer.append(".text\n");
            buffer.append(".global main\n");
            buffer.append(LLVMGetValueName(func).getString()).append(":\n");
            myPrologue(0);
            for (LLVMBasicBlockRef basicBlock = LLVMGetFirstBasicBlock(func); basicBlock != null; basicBlock = LLVMGetNextBasicBlock(basicBlock)) {
                buffer.append(LLVMGetBasicBlockName(basicBlock).getString()).append(":\n");
                for (LLVMValueRef inst = LLVMGetFirstInstruction(basicBlock); inst != null; inst = LLVMGetNextInstruction(inst)) {
                    int opcode = LLVMGetInstructionOpcode(inst);
                    LLVMValueRef op1 = null, op2 = null, op3 = null;
                    int operandNum = LLVMGetNumOperands(inst);
                    if (operandNum == 1) {
                        op1 = LLVMGetOperand(inst, 0);
                    } else if (operandNum == 2) {
                        op1 = LLVMGetOperand(inst, 0);
                        op2 = LLVMGetOperand(inst, 1);
                    } else if (operandNum == 3) {
                        op1 = LLVMGetOperand(inst, 0);
                        op2 = LLVMGetOperand(inst, 1);
                        op3 = LLVMGetOperand(inst, 2);
                    }
                    // ret指令
                    if (opcode == 1) {
                        assert op1 != null;
                        int retValue = (int) LLVMConstIntGetZExtValue(op1);
                        op1("li", "a0", String.valueOf(retValue));
                        myEpilogue(0);
                        op1("li", "a7", String.valueOf(93));
                        buffer.append("  ecall\n");
                    }
                }
            }
        }
        return buffer.toString();
    }

    static void myPrologue(int the_size) {
        op2("addi", "sp", "sp", String.valueOf(the_size));
    }

    static void myEpilogue(int the_size) {
        op2("addi", "sp", "sp", String.valueOf(-1 * the_size));
    }
}