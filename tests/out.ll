; ModuleID = 'module'
source_filename = "module"

define i32 @main() {
mainEntry:
  %pointer_a = alloca i32, align 4
  store i32 0, i32* %pointer_a, align 4
  %pointer_count = alloca i32, align 4
  store i32 0, i32* %pointer_count, align 4
  br label %condition

condition:                                        ; preds = %exit7, %mainEntry
  %a = load i32, i32* %pointer_a, align 4
  %tmp_ = icmp sle i32 %a, 0
  %condValue_ = zext i1 %tmp_ to i32
  %tmp_1 = icmp ne i32 %condValue_, 0
  br i1 %tmp_1, label %trueBranch, label %exit

trueBranch:                                       ; preds = %condition
  %a2 = load i32, i32* %pointer_a, align 4
  %tmp_3 = sub i32 %a2, 1
  store i32 %tmp_3, i32* %pointer_a, align 4
  %count = load i32, i32* %pointer_count, align 4
  %tmp_4 = add i32 %count, 1
  store i32 %tmp_4, i32* %pointer_count, align 4
  br label %condition5

exit:                                             ; preds = %trueBranch6, %condition
  %count12 = load i32, i32* %pointer_count, align 4
  ret i32 %count12

condition5:                                       ; preds = %trueBranch
  %a8 = load i32, i32* %pointer_a, align 4
  %tmp_9 = icmp slt i32 %a8, -20
  %condValue_10 = zext i1 %tmp_9 to i32
  %tmp_11 = icmp ne i32 %condValue_10, 0
  br i1 %tmp_11, label %trueBranch6, label %exit7

trueBranch6:                                      ; preds = %condition5
  br label %exit
  br label %exit7

exit7:                                            ; preds = %trueBranch6, %condition5
  br label %condition
}
