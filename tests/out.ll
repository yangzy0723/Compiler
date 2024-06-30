; ModuleID = 'module'
source_filename = "module"

@global_i = global i32 15
@global_j = global i32 3
@global_k = global i32 2

define i32 @main() {
mainEntry:
  %pointer_result = alloca i32, align 4
  %i = load i32, i32* @global_i, align 4
  %j = load i32, i32* @global_j, align 4
  %tmp_ = add i32 %i, %j
  %k = load i32, i32* @global_k, align 4
  %tmp_1 = mul i32 %tmp_, %k
  %i2 = load i32, i32* @global_i, align 4
  %j3 = load i32, i32* @global_j, align 4
  %tmp_4 = sdiv i32 %i2, %j3
  %tmp_5 = sub i32 %tmp_1, %tmp_4
  %i6 = load i32, i32* @global_i, align 4
  %j7 = load i32, i32* @global_j, align 4
  %tmp_8 = srem i32 %i6, %j7
  %tmp_9 = add i32 %tmp_5, %tmp_8
  store i32 %tmp_9, i32* %pointer_result, align 4
  %result = load i32, i32* %pointer_result, align 4
  ret i32 %result
}
