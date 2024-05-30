; ModuleID = 'module'
source_filename = "module"

define i32 @main() {
mainEntry:
  br label %condition

condition:                                        ; preds = %mainEntry
  br i1 true, label %trueBranch, label %exit

trueBranch:                                       ; preds = %condition
  ret i32 0
  br label %exit

exit:                                             ; preds = %trueBranch, %condition
  ret i32 1
}
