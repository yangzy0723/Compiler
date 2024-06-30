
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.*;

import static org.bytedeco.llvm.global.LLVM.*;

public class AsmBuilder {
    static StringBuffer buffer = new StringBuffer();

    static Set<LLVMValueRef> global_value = new HashSet<>();
    static Map<LLVMValueRef, Integer> stack_pointers = new HashMap<>();
    static Map<LLVMValueRef, Pair<Integer, Integer>> value_period = new HashMap<>();

    public static String buildAsmCode(LLVMModuleRef module) {
        int now_line = 0;
        int stack_size = 0;
        // 处理全局变量
        for (LLVMValueRef value = LLVMGetFirstGlobal(module); value != null; value = LLVMGetNextGlobal(value)) {
            global_value.add(value);
            value_period.put(value, new Pair<>(now_line, now_line));
        }

        for (LLVMValueRef func = LLVMGetFirstFunction(module); func != null; func = LLVMGetNextFunction(func))
            for (LLVMBasicBlockRef basicBlock = LLVMGetFirstBasicBlock(func); basicBlock != null; basicBlock = LLVMGetNextBasicBlock(basicBlock))
                for (LLVMValueRef inst = LLVMGetFirstInstruction(basicBlock); inst != null; inst = LLVMGetNextInstruction(inst)) {
                    if (!Objects.equals(LLVMGetValueName(inst).getString(), ""))
                        stack_size += 4;

                    // 处理变量生命周期
                    now_line++;
                    // 定义了某变量
                    if (!Objects.equals(LLVMGetValueName(inst).getString(), ""))
                        value_period.put(inst, new Pair<>(now_line, now_line));

                    // 研究对变量的使用
                    // 有三种情况，一是使用立即数、二是使用全局变量（@开头），三是使用寄存器（%开头）
                    int operandNum = LLVMGetNumOperands(inst);
                    LLVMValueRef op1 = null, op2 = null;
                    if (operandNum == 1) {
                        op1 = LLVMGetOperand(inst, 0);
                        if (LLVMIsAConstant(op1) == null)
                            value_period.get(op1).setSecond(now_line);
                    }
                    else if (operandNum == 2) {
                        op1 = LLVMGetOperand(inst, 0);
                        op2 = LLVMGetOperand(inst, 1);
                        if (LLVMIsAConstant(op1) == null)
                            value_period.get(op1).setSecond(now_line);
                        if (LLVMIsAConstant(op2) == null)
                            value_period.get(op2).setSecond(now_line);
                    }
                }

        // 处理全局变量
        for (LLVMValueRef value = LLVMGetFirstGlobal(module); value != null; value = LLVMGetNextGlobal(value)) {
            LLVMValueRef globalVarValue = LLVMGetInitializer(value);
            int v = (int)LLVMConstIntGetZExtValue(globalVarValue);

            buffer.append(".data\n");
            buffer.append(LLVMGetValueName(value).getString()).append(":\n");
            buffer.append("  .word ").append(v).append("\n");
        }
        buffer.append("\n");

        // 进入main函数
        int count = 0;
        for (LLVMValueRef func = LLVMGetFirstFunction(module); func != null; func = LLVMGetNextFunction(func)) {
            buffer.append(".text\n");
            buffer.append(".global main\n");
            buffer.append(LLVMGetValueName(func).getString()).append(":\n");
            myPrologue(stack_size);
            for (LLVMBasicBlockRef basicBlock = LLVMGetFirstBasicBlock(func); basicBlock != null; basicBlock = LLVMGetNextBasicBlock(basicBlock)) {
                buffer.append(LLVMGetBasicBlockName(basicBlock).getString()).append(":\n");
                for (LLVMValueRef inst = LLVMGetFirstInstruction(basicBlock); inst != null; inst = LLVMGetNextInstruction(inst)) {
                    int opcode = LLVMGetInstructionOpcode(inst);
                    int operandNum = LLVMGetNumOperands(inst);
                    LLVMValueRef op1 = null, op2 = null;
                    if (operandNum == 1) {
                        op1 = LLVMGetOperand(inst, 0);
                    }
                    else if (operandNum == 2) {
                        op1 = LLVMGetOperand(inst, 0);
                        op2 = LLVMGetOperand(inst, 1);
                    }

                    /**
                     *  %pointer_b = alloca i32, align 4
                     *  像这种语句，其inst本身其实就是%pointer_b的引用，也是定义了一个新变量；
                     *  System.out.println(LLVMGetValueName(inst).getString()); 得到pointer_b
                     */
                    if (opcode == LLVMAlloca) {
                        stack_pointers.put(inst, count++);
                    }
                    /**
                     *  store i32 3, i32* %c, align 4
                     *  store i32 %tmp_2, i32* @b, align 4
                     */
                    else if (opcode == LLVMStore) {
                        // 将常数存入目标地址
                        if (LLVMIsAConstant(op1) != null) {
                            // 目标地址是全局变量
                            if (global_value.contains(op2)) {
                                asm1op("li", "t0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                                asm1op("la", "t1", LLVMGetValueName(op2).getString());
                                asm1op("sw", "t0", "0(t1)");
                            }
                            // 目标地址是普通栈帧
                            else {
                                asm1op("li", "t0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                                asm1op("sw", "t0", stack_pointers.get(op2) * 4 + "(sp)");
                            }
                        }
                        // 将寄存器中数存入目标地址
                        else {
                            // 目标地址是全局变量
                            if (global_value.contains(op2)) {
                                asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                                asm1op("la", "t1", LLVMGetValueName(op2).getString());
                                asm1op("sw", "t0", "0(t1)");
                            }
                            // 目标地址是普通栈帧
                            else {
                                asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                                asm1op("sw", "t0", stack_pointers.get(op2) * 4 + "(sp)");
                            }
                        }
                    }
                    /**
                     *   %a = load i32, i32* @global_a, align 4
                     *   %b = load i32, i32* %pointer_b, align 4
                     */
                    else if (opcode == LLVMLoad) {
                        stack_pointers.put(inst, count++);
                        // 从全局变量中提出数，存在寄存器中
                        if (global_value.contains(op1))
                            asm1op("lw", "t0", LLVMGetValueName(op1).getString());
                        // 从栈帧提出数，存在寄存器中
                        else
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                        asm1op("sw", "t0", stack_pointers.get(inst) * 4 + "(sp)");
                    }
                    else if (opcode == LLVMAdd) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            asm1op("li", "t0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("add", "t0", "t0", "t1");
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("li", "t1", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("add", "t0", "t0", "t1");
                        }
                        else {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("add", "t0", "t0", "t1");
                        }
                        asm1op("sw", "t0", stack_pointers.get(inst) * 4 + "(sp)");
                    }
                    else if (opcode == LLVMMul) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            asm1op("li", "t0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("mul", "t0", "t0", "t1");
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("li", "t1", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("mul", "t0", "t0", "t1");
                        }
                        else {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("mul", "t0", "t0", "t1");
                        }
                        asm1op("sw", "t0", stack_pointers.get(inst) * 4 + "(sp)");
                    }
                    else if (opcode == LLVMSub) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            asm1op("li", "t0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("sub", "t0", "t0", "t1");
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("li", "t1", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("sub", "t0", "t0", "t1");
                        }
                        else {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("sub", "t0", "t0", "t1");
                        }
                        asm1op("sw", "t0", stack_pointers.get(inst) * 4 + "(sp)");
                    }
                    else if (opcode == LLVMSDiv) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            asm1op("li", "t0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("div", "t0", "t0", "t1");
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("li", "t1", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("div", "t0", "t0", "t1");
                        }
                        else {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("div", "t0", "t0", "t1");
                        }
                        asm1op("sw", "t0", stack_pointers.get(inst) * 4 + "(sp)");
                    }
                    else if (opcode == LLVMSRem) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            asm1op("li", "t0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("rem", "t0", "t0", "t1");
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("li", "t1", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("rem", "t0", "t0", "t1");
                        }
                        else {
                            asm1op("lw", "t0", stack_pointers.get(op1) * 4 + "(sp)");
                            asm1op("lw", "t1", stack_pointers.get(op2) * 4 + "(sp)");
                            asm2op("rem", "t0", "t0", "t1");
                        }
                        asm1op("sw", "t0", stack_pointers.get(inst) * 4 + "(sp)");
                    }
                    else
                    if (opcode == LLVMRet) {
                        // 返回立即数
                        if (LLVMIsAConstant(op1) != null)
                            asm1op("li", "a0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                        // 返回寄存器中值
                        else
                            asm1op("lw", "a0", stack_pointers.get(op1) * 4 + "(sp)");

                        myEpilogue(stack_size);
                        asm1op("li", "a7", String.valueOf(93));
                        buffer.append("  ecall\n");
                    }
                }
            }
        }
        return buffer.toString();
    }

    private static void asm1op(String op, String dest, String op1) {
        buffer.append(String.format("  %s %s, %s\n", op, dest, op1));
    }

    private static void asm2op(String op, String dest, String lhs, String rhs) {
        buffer.append(String.format("  %s %s, %s, %s\n", op, dest, lhs, rhs));
    }

    private static void myPrologue(int stack_size) {
        asm2op("addi", "sp", "sp", String.valueOf(stack_size));
    }

    private static void myEpilogue(int stack_size) {
        asm2op("addi", "sp", "sp", String.valueOf(-1 * stack_size));
    }
}

class Pair<T, U> {
    private T first;
    private U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }
}