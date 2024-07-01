; ModuleID = 'module'
source_filename = "module"

@global_x = global i32 1
@global_y = global i32 2
@global_z = global i32 3
@global_a = global i32 4
@global_b = global i32 5
@global_c = global i32 6
@global_d = global i32 7
@global_e = global i32 8
@global_f = global i32 9
@global_g = global i32 10
@global_h = global i32 11
@global_i = global i32 12
@global_j = global i32 13
@global_k = global i32 14
@global_l = global i32 15
@global_m = global i32 16
@global_n = global i32 17
@global_o = global i32 18
@global_p = global i32 19
@global_q = global i32 20

define i32 @main() {
mainEntry:
  %pointer_x1 = alloca i32, align 4
  store i32 1, i32* %pointer_x1, align 4
  %pointer_x2 = alloca i32, align 4
  store i32 2, i32* %pointer_x2, align 4
  %pointer_x3 = alloca i32, align 4
  store i32 3, i32* %pointer_x3, align 4
  %pointer_x4 = alloca i32, align 4
  store i32 4, i32* %pointer_x4, align 4
  %pointer_x5 = alloca i32, align 4
  store i32 5, i32* %pointer_x5, align 4
  %pointer_x6 = alloca i32, align 4
  store i32 6, i32* %pointer_x6, align 4
  %pointer_x7 = alloca i32, align 4
  store i32 7, i32* %pointer_x7, align 4
  %pointer_x8 = alloca i32, align 4
  store i32 8, i32* %pointer_x8, align 4
  %pointer_x9 = alloca i32, align 4
  store i32 9, i32* %pointer_x9, align 4
  %pointer_x10 = alloca i32, align 4
  store i32 10, i32* %pointer_x10, align 4
  %pointer_x11 = alloca i32, align 4
  store i32 11, i32* %pointer_x11, align 4
  %pointer_x12 = alloca i32, align 4
  store i32 12, i32* %pointer_x12, align 4
  %pointer_x13 = alloca i32, align 4
  store i32 13, i32* %pointer_x13, align 4
  %pointer_x14 = alloca i32, align 4
  store i32 14, i32* %pointer_x14, align 4
  %pointer_x15 = alloca i32, align 4
  store i32 15, i32* %pointer_x15, align 4
  %pointer_x16 = alloca i32, align 4
  store i32 16, i32* %pointer_x16, align 4
  %pointer_x17 = alloca i32, align 4
  store i32 17, i32* %pointer_x17, align 4
  %pointer_x18 = alloca i32, align 4
  store i32 18, i32* %pointer_x18, align 4
  %pointer_x19 = alloca i32, align 4
  store i32 19, i32* %pointer_x19, align 4
  %pointer_x20 = alloca i32, align 4
  store i32 20, i32* %pointer_x20, align 4
  %x = load i32, i32* @global_x, align 4
  %tmp_ = add i32 %x, 1
  store i32 %tmp_, i32* @global_x, align 4
  %y = load i32, i32* @global_y, align 4
  %tmp_1 = add i32 %y, 2
  store i32 %tmp_1, i32* @global_y, align 4
  %z = load i32, i32* @global_z, align 4
  %tmp_2 = add i32 %z, 3
  store i32 %tmp_2, i32* @global_z, align 4
  %a = load i32, i32* @global_a, align 4
  %tmp_3 = add i32 %a, 4
  store i32 %tmp_3, i32* @global_a, align 4
  %b = load i32, i32* @global_b, align 4
  %tmp_4 = add i32 %b, 5
  store i32 %tmp_4, i32* @global_b, align 4
  %c = load i32, i32* @global_c, align 4
  %tmp_5 = add i32 %c, 6
  store i32 %tmp_5, i32* @global_c, align 4
  %d = load i32, i32* @global_d, align 4
  %tmp_6 = add i32 %d, 7
  store i32 %tmp_6, i32* @global_d, align 4
  %e = load i32, i32* @global_e, align 4
  %tmp_7 = add i32 %e, 8
  store i32 %tmp_7, i32* @global_e, align 4
  %f = load i32, i32* @global_f, align 4
  %tmp_8 = add i32 %f, 9
  store i32 %tmp_8, i32* @global_f, align 4
  %g = load i32, i32* @global_g, align 4
  %tmp_9 = add i32 %g, 10
  store i32 %tmp_9, i32* @global_g, align 4
  %h = load i32, i32* @global_h, align 4
  %tmp_10 = add i32 %h, 11
  store i32 %tmp_10, i32* @global_h, align 4
  %i = load i32, i32* @global_i, align 4
  %tmp_11 = add i32 %i, 12
  store i32 %tmp_11, i32* @global_i, align 4
  %j = load i32, i32* @global_j, align 4
  %tmp_12 = add i32 %j, 13
  store i32 %tmp_12, i32* @global_j, align 4
  %k = load i32, i32* @global_k, align 4
  %tmp_13 = add i32 %k, 14
  store i32 %tmp_13, i32* @global_k, align 4
  %l = load i32, i32* @global_l, align 4
  %tmp_14 = add i32 %l, 15
  store i32 %tmp_14, i32* @global_l, align 4
  %m = load i32, i32* @global_m, align 4
  %tmp_15 = add i32 %m, 16
  store i32 %tmp_15, i32* @global_m, align 4
  %n = load i32, i32* @global_n, align 4
  %tmp_16 = add i32 %n, 17
  store i32 %tmp_16, i32* @global_n, align 4
  %o = load i32, i32* @global_o, align 4
  %tmp_17 = add i32 %o, 18
  store i32 %tmp_17, i32* @global_o, align 4
  %p = load i32, i32* @global_p, align 4
  %tmp_18 = add i32 %p, 19
  store i32 %tmp_18, i32* @global_p, align 4
  %q = load i32, i32* @global_q, align 4
  %tmp_19 = add i32 %q, 20
  store i32 %tmp_19, i32* @global_q, align 4
  %x1 = load i32, i32* %pointer_x1, align 4
  %tmp_20 = mul i32 %x1, 2
  store i32 %tmp_20, i32* %pointer_x1, align 4
  %x2 = load i32, i32* %pointer_x2, align 4
  %tmp_21 = mul i32 %x2, 2
  store i32 %tmp_21, i32* %pointer_x2, align 4
  %x3 = load i32, i32* %pointer_x3, align 4
  %tmp_22 = mul i32 %x3, 2
  store i32 %tmp_22, i32* %pointer_x3, align 4
  %x4 = load i32, i32* %pointer_x4, align 4
  %tmp_23 = mul i32 %x4, 2
  store i32 %tmp_23, i32* %pointer_x4, align 4
  %x5 = load i32, i32* %pointer_x5, align 4
  %tmp_24 = mul i32 %x5, 2
  store i32 %tmp_24, i32* %pointer_x5, align 4
  %x6 = load i32, i32* %pointer_x6, align 4
  %tmp_25 = mul i32 %x6, 2
  store i32 %tmp_25, i32* %pointer_x6, align 4
  %x7 = load i32, i32* %pointer_x7, align 4
  %tmp_26 = mul i32 %x7, 2
  store i32 %tmp_26, i32* %pointer_x7, align 4
  %x8 = load i32, i32* %pointer_x8, align 4
  %tmp_27 = mul i32 %x8, 2
  store i32 %tmp_27, i32* %pointer_x8, align 4
  %x9 = load i32, i32* %pointer_x9, align 4
  %tmp_28 = mul i32 %x9, 2
  store i32 %tmp_28, i32* %pointer_x9, align 4
  %x10 = load i32, i32* %pointer_x10, align 4
  %tmp_29 = mul i32 %x10, 2
  store i32 %tmp_29, i32* %pointer_x10, align 4
  %x11 = load i32, i32* %pointer_x11, align 4
  %tmp_30 = mul i32 %x11, 2
  store i32 %tmp_30, i32* %pointer_x11, align 4
  %x12 = load i32, i32* %pointer_x12, align 4
  %tmp_31 = mul i32 %x12, 2
  store i32 %tmp_31, i32* %pointer_x12, align 4
  %x13 = load i32, i32* %pointer_x13, align 4
  %tmp_32 = mul i32 %x13, 2
  store i32 %tmp_32, i32* %pointer_x13, align 4
  %x14 = load i32, i32* %pointer_x14, align 4
  %tmp_33 = mul i32 %x14, 2
  store i32 %tmp_33, i32* %pointer_x14, align 4
  %x15 = load i32, i32* %pointer_x15, align 4
  %tmp_34 = mul i32 %x15, 2
  store i32 %tmp_34, i32* %pointer_x15, align 4
  %x16 = load i32, i32* %pointer_x16, align 4
  %tmp_35 = mul i32 %x16, 2
  store i32 %tmp_35, i32* %pointer_x16, align 4
  %x17 = load i32, i32* %pointer_x17, align 4
  %tmp_36 = mul i32 %x17, 2
  store i32 %tmp_36, i32* %pointer_x17, align 4
  %x18 = load i32, i32* %pointer_x18, align 4
  %tmp_37 = mul i32 %x18, 2
  store i32 %tmp_37, i32* %pointer_x18, align 4
  %x19 = load i32, i32* %pointer_x19, align 4
  %tmp_38 = mul i32 %x19, 2
  store i32 %tmp_38, i32* %pointer_x19, align 4
  %x20 = load i32, i32* %pointer_x20, align 4
  %tmp_39 = mul i32 %x20, 2
  store i32 %tmp_39, i32* %pointer_x20, align 4
  %x140 = load i32, i32* %pointer_x1, align 4
  %x241 = load i32, i32* %pointer_x2, align 4
  %tmp_42 = add i32 %x140, %x241
  %x343 = load i32, i32* %pointer_x3, align 4
  %tmp_44 = add i32 %tmp_42, %x343
  %x445 = load i32, i32* %pointer_x4, align 4
  %tmp_46 = add i32 %tmp_44, %x445
  %x547 = load i32, i32* %pointer_x5, align 4
  %tmp_48 = add i32 %tmp_46, %x547
  %x649 = load i32, i32* %pointer_x6, align 4
  %tmp_50 = add i32 %tmp_48, %x649
  %x751 = load i32, i32* %pointer_x7, align 4
  %tmp_52 = add i32 %tmp_50, %x751
  %x853 = load i32, i32* %pointer_x8, align 4
  %tmp_54 = add i32 %tmp_52, %x853
  %x955 = load i32, i32* %pointer_x9, align 4
  %tmp_56 = add i32 %tmp_54, %x955
  %x1057 = load i32, i32* %pointer_x10, align 4
  %tmp_58 = add i32 %tmp_56, %x1057
  %x1159 = load i32, i32* %pointer_x11, align 4
  %tmp_60 = add i32 %tmp_58, %x1159
  %x1261 = load i32, i32* %pointer_x12, align 4
  %tmp_62 = add i32 %tmp_60, %x1261
  %x1363 = load i32, i32* %pointer_x13, align 4
  %tmp_64 = add i32 %tmp_62, %x1363
  %x1465 = load i32, i32* %pointer_x14, align 4
  %tmp_66 = add i32 %tmp_64, %x1465
  %x1567 = load i32, i32* %pointer_x15, align 4
  %tmp_68 = add i32 %tmp_66, %x1567
  %x1669 = load i32, i32* %pointer_x16, align 4
  %tmp_70 = add i32 %tmp_68, %x1669
  %x1771 = load i32, i32* %pointer_x17, align 4
  %tmp_72 = add i32 %tmp_70, %x1771
  %x1873 = load i32, i32* %pointer_x18, align 4
  %tmp_74 = add i32 %tmp_72, %x1873
  %x1975 = load i32, i32* %pointer_x19, align 4
  %tmp_76 = add i32 %tmp_74, %x1975
  %x2077 = load i32, i32* %pointer_x20, align 4
  %tmp_78 = add i32 %tmp_76, %x2077
  %x79 = load i32, i32* @global_x, align 4
  %tmp_80 = add i32 %tmp_78, %x79
  %y81 = load i32, i32* @global_y, align 4
  %tmp_82 = add i32 %tmp_80, %y81
  %z83 = load i32, i32* @global_z, align 4
  %tmp_84 = add i32 %tmp_82, %z83
  %a85 = load i32, i32* @global_a, align 4
  %tmp_86 = add i32 %tmp_84, %a85
  %b87 = load i32, i32* @global_b, align 4
  %tmp_88 = add i32 %tmp_86, %b87
  %c89 = load i32, i32* @global_c, align 4
  %tmp_90 = add i32 %tmp_88, %c89
  %d91 = load i32, i32* @global_d, align 4
  %tmp_92 = add i32 %tmp_90, %d91
  %e93 = load i32, i32* @global_e, align 4
  %tmp_94 = add i32 %tmp_92, %e93
  %f95 = load i32, i32* @global_f, align 4
  %tmp_96 = add i32 %tmp_94, %f95
  %g97 = load i32, i32* @global_g, align 4
  %tmp_98 = add i32 %tmp_96, %g97
  %h99 = load i32, i32* @global_h, align 4
  %tmp_100 = add i32 %tmp_98, %h99
  %i101 = load i32, i32* @global_i, align 4
  %tmp_102 = add i32 %tmp_100, %i101
  %j103 = load i32, i32* @global_j, align 4
  %tmp_104 = add i32 %tmp_102, %j103
  %k105 = load i32, i32* @global_k, align 4
  %tmp_106 = add i32 %tmp_104, %k105
  %l107 = load i32, i32* @global_l, align 4
  %tmp_108 = add i32 %tmp_106, %l107
  %m109 = load i32, i32* @global_m, align 4
  %tmp_110 = add i32 %tmp_108, %m109
  %n111 = load i32, i32* @global_n, align 4
  %tmp_112 = add i32 %tmp_110, %n111
  %o113 = load i32, i32* @global_o, align 4
  %tmp_114 = add i32 %tmp_112, %o113
  %p115 = load i32, i32* @global_p, align 4
  %tmp_116 = add i32 %tmp_114, %p115
  %q117 = load i32, i32* @global_q, align 4
  %tmp_118 = add i32 %tmp_116, %q117
  ret i32 %tmp_118
}
