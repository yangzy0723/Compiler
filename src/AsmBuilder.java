
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
    static Map<LLVMValueRef, String> used_reg = new HashMap<>();
    static Vector<String> empty_reg = new Vector<>();

    public static String buildAsmCode(LLVMModuleRef module) {
        int now_line = 0;
        int stack_size = 0;
        for (int i = 0; i <= 6; i++)
            empty_reg.add("t" + i);

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
                    // 有三种情况：
                    // 一是使用立即数【不使用寄存器】
                    // 二是使用全局变量（@开头）【不使用寄存器】
                    // 三是使用寄存器（%开头）
                    int operandNum = LLVMGetNumOperands(inst);
                    LLVMValueRef op1, op2;
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
//        for (LLVMValueRef v : value_period.keySet())
//            System.out.println(LLVMGetValueName(v).getString() + ": " + value_period.get(v));

        // 处理全局变量
        for (LLVMValueRef value = LLVMGetFirstGlobal(module); value != null; value = LLVMGetNextGlobal(value)) {
            global_value.add(value);
            LLVMValueRef globalVarValue = LLVMGetInitializer(value);
            int v = (int)LLVMConstIntGetZExtValue(globalVarValue);

            buffer.append(".data\n");
            buffer.append(LLVMGetValueName(value).getString()).append(":\n");
            buffer.append("  .word ").append(v).append("\n");
        }
        buffer.append("\n");

        // 进入main函数
        int count = 0;
        now_line = 0;
        for (LLVMValueRef func = LLVMGetFirstFunction(module); func != null; func = LLVMGetNextFunction(func)) {
            buffer.append(".text\n");
            buffer.append(".global main\n");
            buffer.append(LLVMGetValueName(func).getString()).append(":\n");
            myPrologue(stack_size);
            for (LLVMBasicBlockRef basicBlock = LLVMGetFirstBasicBlock(func); basicBlock != null; basicBlock = LLVMGetNextBasicBlock(basicBlock)) {
                buffer.append(LLVMGetBasicBlockName(basicBlock).getString()).append(":\n");
                for (LLVMValueRef inst = LLVMGetFirstInstruction(basicBlock); inst != null; inst = LLVMGetNextInstruction(inst)) {
                    now_line++;

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
                                asm1op("li", "a6", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                                asm1op("la", "a7", LLVMGetValueName(op2).getString());
                                asm1op("sw", "a6", "0(a7)");
                            }
                            // 目标地址是普通栈帧
                            else {
                                asm1op("li", "a6", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                                if (used_reg.containsKey(op2))
                                    asm1op("mv", used_reg.get(op2), "a6");
                                else {
                                    String emptyReg = getEmptyReg(now_line);
                                    used_reg.put(op2, emptyReg);
                                    asm1op("mv", used_reg.get(op2), "a6");
                                }
                            }
                        }
                        // 将寄存器中数存入目标地址
                        else {
                            // 目标地址是全局变量
                            if (global_value.contains(op2)) {
                                if (!used_reg.containsKey(op1)) {
                                    String emptyReg = getEmptyReg(now_line);
                                    used_reg.put(op1, emptyReg);
                                    asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                                }
                                asm1op("la", "a7", LLVMGetValueName(op2).getString());
                                asm1op("sw", used_reg.get(op1), "0(a7)");
                            }
                            // 目标地址是普通栈帧
                            else {
                                if (used_reg.containsKey(op1)) {
                                    if (used_reg.containsKey(op2))
                                        asm1op("mv", used_reg.get(op2), used_reg.get(op1));
                                    else {
                                        String emptyReg = getEmptyReg(now_line);
                                        used_reg.put(op2, emptyReg);
                                        asm1op("mv", used_reg.get(op2), used_reg.get(op1));
                                    }
                                }
                                else {
                                    String emptyReg = getEmptyReg(now_line);
                                    used_reg.put(op1, emptyReg);
                                    asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                                    if (used_reg.containsKey(op2))
                                        asm1op("mv", used_reg.get(op2), used_reg.get(op1));
                                    else {
                                        String emptyReg1 = getEmptyReg(now_line);
                                        used_reg.put(op2, emptyReg1);
                                        asm1op("mv", used_reg.get(op2), used_reg.get(op1));
                                    }
                                }
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
                            asm1op("lw", "a7", LLVMGetValueName(op1).getString());
                        // 从栈帧提出数，存在寄存器中
                        else {
                            if (used_reg.containsKey(op1))
                                asm1op("mv", "a7", used_reg.get(op1));
                            else {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                                asm1op("mv", "a7", used_reg.get(op1));
                            }
                        }
                        if (used_reg.containsKey(inst))
                            asm1op("mv", used_reg.get(inst), "a7");
                        else {
                            String emptyReg = getEmptyReg(now_line);
                            used_reg.put(inst, emptyReg);
                            asm1op("mv", used_reg.get(inst), "a7");
                        }
                    }
                    else if (opcode == LLVMAdd) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm2op("add", "a6", "a7", used_reg.get(op2));
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("add", "a6", used_reg.get(op1), "a7");
                        }
                        else {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm2op("add", "a6", used_reg.get(op1), used_reg.get(op2));
                        }
                        if (used_reg.containsKey(inst))
                            asm1op("mv", used_reg.get(inst), "a6");
                        else {
                            String emptyReg = getEmptyReg(now_line);
                            used_reg.put(inst, emptyReg);
                            asm1op("mv", used_reg.get(inst), "a6");
                        }
                    }
                    else if (opcode == LLVMMul) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm2op("mul", "a6", "a7", used_reg.get(op2));
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("mul", "a6", used_reg.get(op1), "a7");
                        }
                        else {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm2op("mul", "a6", used_reg.get(op1), used_reg.get(op2));
                        }
                        if (used_reg.containsKey(inst))
                            asm1op("mv", used_reg.get(inst), "a6");
                        else {
                            String emptyReg = getEmptyReg(now_line);
                            used_reg.put(inst, emptyReg);
                            asm1op("mv", used_reg.get(inst), "a6");
                        }
                    }
                    else if (opcode == LLVMSub) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm2op("sub", "a6", "a7", used_reg.get(op2));
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("sub", "a6", used_reg.get(op1), "a7");
                        }
                        else {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm2op("sub", "a6", used_reg.get(op1), used_reg.get(op2));
                        }
                        if (used_reg.containsKey(inst))
                            asm1op("mv", used_reg.get(inst), "a6");
                        else {
                            String emptyReg = getEmptyReg(now_line);
                            used_reg.put(inst, emptyReg);
                            asm1op("mv", used_reg.get(inst), "a6");
                        }
                    }
                    else if (opcode == LLVMSDiv) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm2op("div", "a6", "a7", used_reg.get(op2));
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("div", "a6", used_reg.get(op1), "a7");
                        }
                        else {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm2op("div", "a6", used_reg.get(op1), used_reg.get(op2));
                        }
                        if (used_reg.containsKey(inst))
                            asm1op("mv", used_reg.get(inst), "a6");
                        else {
                            String emptyReg = getEmptyReg(now_line);
                            used_reg.put(inst, emptyReg);
                            asm1op("mv", used_reg.get(inst), "a6");
                        }
                    }
                    else if (opcode == LLVMSRem) {
                        stack_pointers.put(inst, count++);
                        if (LLVMIsAConstant(op1) != null) {
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                            asm2op("rem", "a6", "a7", used_reg.get(op2));
                        }
                        else if (LLVMIsAConstant(op2) != null) {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            asm1op("li", "a7", String.valueOf((int) LLVMConstIntGetZExtValue(op2)));
                            asm2op("rem", "a6", used_reg.get(op1), "a7");
                        }
                        else {
                            if (!used_reg.containsKey(op1)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                            }
                            if (!used_reg.containsKey(op2)) {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op2, emptyReg);
                                asm1op("lw", used_reg.get(op2), stack_pointers.get(op2) * 4 + "(sp)");
                            }
                            asm2op("rem", "a6", used_reg.get(op1), used_reg.get(op2));
                        }
                        if (used_reg.containsKey(inst))
                            asm1op("mv", used_reg.get(inst), "a6");
                        else {
                            String emptyReg = getEmptyReg(now_line);
                            used_reg.put(inst, emptyReg);
                            asm1op("mv", used_reg.get(inst), "a6");
                        }
                    }
                    else if (opcode == LLVMRet) {
                        // 返回立即数
                        if (LLVMIsAConstant(op1) != null)
                            asm1op("li", "a0", String.valueOf((int) LLVMConstIntGetZExtValue(op1)));
                        // 返回寄存器中值
                        else {
                            if (used_reg.containsKey(op1))
                                asm1op("mv", "a0", used_reg.get(op1));
                            else {
                                String emptyReg = getEmptyReg(now_line);
                                used_reg.put(op1, emptyReg);
                                asm1op("lw", used_reg.get(op1), stack_pointers.get(op1) * 4 + "(sp)");
                                asm1op("mv", "a0", used_reg.get(op1));
                            }
                        }

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

    private static String getEmptyReg(int now_line) {
        if (!empty_reg.isEmpty())
            return empty_reg.removeFirst();
        else {
            for (LLVMValueRef llvmValueRef : used_reg.keySet()) {
                if (value_period.get(llvmValueRef).getSecond() < now_line) {
                    empty_reg.add(used_reg.get(llvmValueRef));
                    used_reg.remove(llvmValueRef);
                    return getEmptyReg(now_line);
                }
            }
            int max_end = 0;
            LLVMValueRef spilled_one = null;
            for (LLVMValueRef llvmValueRef : used_reg.keySet()) {
                if (value_period.get(llvmValueRef).getSecond() > max_end) {
                    max_end = value_period.get(llvmValueRef).getSecond();
                    spilled_one = llvmValueRef;
                }
            }
            asm1op("sw", used_reg.get(spilled_one), stack_pointers.get(spilled_one) * 4 + "(sp)");
            empty_reg.add(used_reg.get(spilled_one));
            used_reg.remove(spilled_one);
            return getEmptyReg(now_line);
        }
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

    @Override
    public String toString() {
        return "Pair [first=" + first + ", second=" + second + "]";
    }

}