; ModuleID = 'module'
source_filename = "module"

@global_g_var = global i32 2

define i32 @main() {
main_entry:
  %pointer_a = alloca i32, align 4
  store i32 1, i32* %pointer_a, align 4
  %a = load i32, i32* %pointer_a, align 4
  %g_var = load i32, i32* @global_g_var, align 4
  %tmp_ = add i32 %a, %g_var
  ret i32 %tmp_
}
