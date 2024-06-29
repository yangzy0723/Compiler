import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class AsmBuilder {
    StringBuffer buffer;
    LLVMModuleRef module;

    public AsmBuilder(LLVMModuleRef module) {
        this.module = module;
    }

    public void op2(String op, String dest, String lhs, String rhs) {
        buffer.append(String.format("  %s %s, %s, %s\n", op, dest, lhs, rhs));
    }

    public String buildAsmCode() {
        for (LLVMValueRef value = LLVMGetFirstGlobal(module); value != null; value = LLVMGetNextGlobal(value)) {
            /** 判断全局变量和函数
             if () {
                break;
             }
             */
            for (LLVMBasicBlockRef basicBlock = LLVMGetFirstBasicBlock(value); basicBlock != null; basicBlock = LLVMGetNextBasicBlock(basicBlock)) {
                for (LLVMValueRef inst = LLVMGetFirstInstruction(basicBlock); inst != null; inst = LLVMGetNextInstruction(inst)) {

                }
            }
        }
        return null;
    }
}