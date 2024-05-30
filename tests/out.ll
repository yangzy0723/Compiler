; ModuleID = 'module'
source_filename = "module"

define i32 @main() {
mainEntry:
  %pointer_a = alloca i32, align 4
  store i32 0, i32* %pointer_a, align 4
  %result = alloca i32, align 4

andLeftBlock:                                     ; No predecessors!
  store i32 1, i32* %result, align 4
  br i1 true, label %afterBlock, label %andRightBlock

andRightBlock:                                    ; preds = %andLeftBlock
  %a = load i32, i32* %pointer_a, align 4
  %rightResult = icmp ne i32 %a, 0
  store i32 %a, i32* %result, align 4
  br label %afterBlock

afterBlock:                                       ; preds = %andRightBlock, %andLeftBlock
  %loadFromResult = load i32, i32* %result, align 4
  %tmp_ = icmp ne i32 %loadFromResult, 0
  br i1 %tmp_, label %trueBlock, label %falseBlock

trueBlock:                                        ; preds = %afterBlock
  ret i32 1
  br label %nextBlock

falseBlock:                                       ; preds = %afterBlock
  ret i32 0
  br label %nextBlock

nextBlock:                                        ; preds = %falseBlock, %trueBlock
}
