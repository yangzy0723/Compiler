; ModuleID = 'module'
source_filename = "module"

@global_a = global i32 1
@global_b = global i32 0

define i32 @main() {
mainEntry:
  %pointer_c = alloca i32, align 4
  store i32 3, i32* %pointer_c, align 4
  %c = load i32, i32* %pointer_c, align 4
  %a = load i32, i32* @global_a, align 4
  %tmp_ = add i32 %c, %a
  %tmp_1 = add i32 %tmp_, 1
  store i32 %tmp_1, i32* @global_b, align 4
  %pointer_d = alloca i32, align 4
  store i32 10, i32* %pointer_d, align 4
  %a2 = load i32, i32* @global_a, align 4
  %b = load i32, i32* @global_b, align 4
  %tmp_3 = add i32 %a2, %b
  %c4 = load i32, i32* %pointer_c, align 4
  %tmp_5 = add i32 %tmp_3, %c4
  %d = load i32, i32* %pointer_d, align 4
  %tmp_6 = add i32 %tmp_5, %d
  ret i32 %tmp_6
}
