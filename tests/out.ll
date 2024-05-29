; ModuleID = 'module'
source_filename = "module"

@global_a = global i32 10

define i32 @main() {
mainEntry:
  br label %condition

condition:                                        ; preds = %mainEntry
  %a = load i32, i32* @global_a, align 4
  %tmp_ = icmp ne i32 %a, 10
  %condValue_ = zext i1 %tmp_ to i32
  %tmp_1 = icmp ne i32 %condValue_, 0
  br i1 %tmp_1, label %trueBranch, label %falseBranch

trueBranch:                                       ; preds = %condition
  store i32 2, i32* @global_a, align 4
  br label %exit

falseBranch:                                      ; preds = %condition
  store i32 20, i32* @global_a, align 4
  br label %exit

exit:                                             ; preds = %falseBranch, %trueBranch
  %a2 = load i32, i32* @global_a, align 4
  ret i32 %a2
}
