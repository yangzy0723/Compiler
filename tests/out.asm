.data
global_x:
  .word 1
.data
global_y:
  .word 2
.data
global_z:
  .word 3
.data
global_a:
  .word 4
.data
global_b:
  .word 5
.data
global_c:
  .word 6
.data
global_d:
  .word 7
.data
global_e:
  .word 8
.data
global_f:
  .word 9
.data
global_g:
  .word 10
.data
global_h:
  .word 11
.data
global_i:
  .word 12
.data
global_j:
  .word 13
.data
global_k:
  .word 14
.data
global_l:
  .word 15
.data
global_m:
  .word 16
.data
global_n:
  .word 17
.data
global_o:
  .word 18
.data
global_p:
  .word 19
.data
global_q:
  .word 20

.text
.global main
main:
  addi sp, sp, 716
mainEntry:
  li a6, 1
  mv t0, a6
  li a6, 2
  mv t1, a6
  li a6, 3
  mv t2, a6
  li a6, 4
  mv t3, a6
  li a6, 5
  mv t4, a6
  li a6, 6
  mv t5, a6
  li a6, 7
  mv t6, a6
  li a6, 8
  sw t6, 24(sp)
  mv t6, a6
  li a6, 9
  sw t6, 28(sp)
  mv t6, a6
  li a6, 10
  sw t6, 32(sp)
  mv t6, a6
  li a6, 11
  sw t6, 36(sp)
  mv t6, a6
  li a6, 12
  sw t6, 40(sp)
  mv t6, a6
  li a6, 13
  sw t6, 44(sp)
  mv t6, a6
  li a6, 14
  sw t6, 48(sp)
  mv t6, a6
  li a6, 15
  sw t6, 52(sp)
  mv t6, a6
  li a6, 16
  sw t6, 56(sp)
  mv t6, a6
  li a6, 17
  sw t6, 60(sp)
  mv t6, a6
  li a6, 18
  sw t6, 64(sp)
  mv t6, a6
  li a6, 19
  sw t6, 68(sp)
  mv t6, a6
  li a6, 20
  sw t6, 72(sp)
  mv t6, a6
  lw a7, global_x
  sw t6, 76(sp)
  mv t6, a7
  li a7, 1
  add a6, t6, a7
  sw t5, 20(sp)
  mv t5, a6
  la a7, global_x
  sw t5, 0(a7)
  lw a7, global_y
  mv t6, a7
  li a7, 2
  add a6, t6, a7
  mv t5, a6
  la a7, global_y
  sw t5, 0(a7)
  lw a7, global_z
  mv t6, a7
  li a7, 3
  add a6, t6, a7
  mv t5, a6
  la a7, global_z
  sw t5, 0(a7)
  lw a7, global_a
  mv t6, a7
  li a7, 4
  add a6, t6, a7
  mv t5, a6
  la a7, global_a
  sw t5, 0(a7)
  lw a7, global_b
  mv t6, a7
  li a7, 5
  add a6, t6, a7
  mv t5, a6
  la a7, global_b
  sw t5, 0(a7)
  lw a7, global_c
  mv t6, a7
  li a7, 6
  add a6, t6, a7
  mv t5, a6
  la a7, global_c
  sw t5, 0(a7)
  lw a7, global_d
  mv t6, a7
  li a7, 7
  add a6, t6, a7
  mv t5, a6
  la a7, global_d
  sw t5, 0(a7)
  lw a7, global_e
  mv t6, a7
  li a7, 8
  add a6, t6, a7
  mv t5, a6
  la a7, global_e
  sw t5, 0(a7)
  lw a7, global_f
  mv t6, a7
  li a7, 9
  add a6, t6, a7
  mv t5, a6
  la a7, global_f
  sw t5, 0(a7)
  lw a7, global_g
  mv t6, a7
  li a7, 10
  add a6, t6, a7
  mv t5, a6
  la a7, global_g
  sw t5, 0(a7)
  lw a7, global_h
  mv t6, a7
  li a7, 11
  add a6, t6, a7
  mv t5, a6
  la a7, global_h
  sw t5, 0(a7)
  lw a7, global_i
  mv t6, a7
  li a7, 12
  add a6, t6, a7
  mv t5, a6
  la a7, global_i
  sw t5, 0(a7)
  lw a7, global_j
  mv t6, a7
  li a7, 13
  add a6, t6, a7
  mv t5, a6
  la a7, global_j
  sw t5, 0(a7)
  lw a7, global_k
  mv t6, a7
  li a7, 14
  add a6, t6, a7
  mv t5, a6
  la a7, global_k
  sw t5, 0(a7)
  lw a7, global_l
  mv t6, a7
  li a7, 15
  add a6, t6, a7
  mv t5, a6
  la a7, global_l
  sw t5, 0(a7)
  lw a7, global_m
  mv t6, a7
  li a7, 16
  add a6, t6, a7
  mv t5, a6
  la a7, global_m
  sw t5, 0(a7)
  lw a7, global_n
  mv t6, a7
  li a7, 17
  add a6, t6, a7
  mv t5, a6
  la a7, global_n
  sw t5, 0(a7)
  lw a7, global_o
  mv t6, a7
  li a7, 18
  add a6, t6, a7
  mv t5, a6
  la a7, global_o
  sw t5, 0(a7)
  lw a7, global_p
  mv t6, a7
  li a7, 19
  add a6, t6, a7
  mv t5, a6
  la a7, global_p
  sw t5, 0(a7)
  lw a7, global_q
  mv t6, a7
  li a7, 20
  add a6, t6, a7
  mv t5, a6
  la a7, global_q
  sw t5, 0(a7)
  mv a7, t0
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  mv t5, a6
  mv t0, t5
  mv a7, t1
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  mv t5, a6
  mv t1, t5
  mv a7, t2
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  mv t5, a6
  mv t2, t5
  mv a7, t3
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  mv t5, a6
  mv t3, t5
  mv a7, t4
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  mv t5, a6
  mv t4, t5
  lw t6, 20(sp)
  mv a7, t6
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 20(sp)
  mv t6, a6
  mv t5, t6
  lw t6, 24(sp)
  mv a7, t6
  sw t6, 24(sp)
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  sw t5, 20(sp)
  mv t5, a6
  mv t6, t5
  lw t5, 28(sp)
  mv a7, t5
  sw t5, 28(sp)
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 24(sp)
  mv t6, a6
  mv t5, t6
  lw t6, 32(sp)
  mv a7, t6
  sw t6, 32(sp)
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  sw t5, 28(sp)
  mv t5, a6
  mv t6, t5
  lw t5, 36(sp)
  mv a7, t5
  sw t5, 36(sp)
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 32(sp)
  mv t6, a6
  mv t5, t6
  lw t6, 40(sp)
  mv a7, t6
  sw t6, 40(sp)
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  sw t5, 36(sp)
  mv t5, a6
  mv t6, t5
  lw t5, 44(sp)
  mv a7, t5
  sw t5, 44(sp)
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 40(sp)
  mv t6, a6
  mv t5, t6
  lw t6, 48(sp)
  mv a7, t6
  sw t6, 48(sp)
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  sw t5, 44(sp)
  mv t5, a6
  mv t6, t5
  lw t5, 52(sp)
  mv a7, t5
  sw t5, 52(sp)
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 48(sp)
  mv t6, a6
  mv t5, t6
  lw t6, 56(sp)
  mv a7, t6
  sw t6, 56(sp)
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  sw t5, 52(sp)
  mv t5, a6
  mv t6, t5
  lw t5, 60(sp)
  mv a7, t5
  sw t5, 60(sp)
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 56(sp)
  mv t6, a6
  mv t5, t6
  lw t6, 64(sp)
  mv a7, t6
  sw t6, 64(sp)
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  sw t5, 60(sp)
  mv t5, a6
  mv t6, t5
  lw t5, 68(sp)
  mv a7, t5
  sw t5, 68(sp)
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 64(sp)
  mv t6, a6
  mv t5, t6
  lw t6, 72(sp)
  mv a7, t6
  sw t6, 72(sp)
  mv t6, a7
  li a7, 2
  mul a6, t6, a7
  sw t5, 68(sp)
  mv t5, a6
  mv t6, t5
  lw t5, 76(sp)
  mv a7, t5
  sw t5, 76(sp)
  mv t5, a7
  li a7, 2
  mul a6, t5, a7
  sw t6, 72(sp)
  mv t6, a6
  mv t5, t6
  mv a7, t0
  mv t6, a7
  mv a7, t1
  mv t0, a7
  add a6, t6, t0
  mv t1, a6
  mv a7, t2
  mv t6, a7
  add a6, t1, t6
  mv t2, a6
  mv a7, t3
  mv t0, a7
  add a6, t2, t0
  mv t3, a6
  mv a7, t4
  mv t1, a7
  add a6, t3, t1
  mv t4, a6
  lw t6, 20(sp)
  mv a7, t6
  mv t2, a7
  add a6, t4, t2
  mv t0, a6
  lw t3, 24(sp)
  mv a7, t3
  mv t1, a7
  add a6, t0, t1
  mv t4, a6
  lw t6, 28(sp)
  mv a7, t6
  mv t2, a7
  add a6, t4, t2
  mv t0, a6
  lw t3, 32(sp)
  mv a7, t3
  mv t1, a7
  add a6, t0, t1
  mv t4, a6
  lw t6, 36(sp)
  mv a7, t6
  mv t2, a7
  add a6, t4, t2
  mv t0, a6
  lw t3, 40(sp)
  mv a7, t3
  mv t1, a7
  add a6, t0, t1
  mv t4, a6
  lw t6, 44(sp)
  mv a7, t6
  mv t2, a7
  add a6, t4, t2
  mv t0, a6
  lw t3, 48(sp)
  mv a7, t3
  mv t1, a7
  add a6, t0, t1
  mv t4, a6
  lw t6, 52(sp)
  mv a7, t6
  mv t2, a7
  add a6, t4, t2
  mv t0, a6
  lw t3, 56(sp)
  mv a7, t3
  mv t1, a7
  add a6, t0, t1
  mv t4, a6
  lw t6, 60(sp)
  mv a7, t6
  mv t2, a7
  add a6, t4, t2
  mv t0, a6
  lw t3, 64(sp)
  mv a7, t3
  mv t1, a7
  add a6, t0, t1
  mv t4, a6
  lw t6, 68(sp)
  mv a7, t6
  mv t2, a7
  add a6, t4, t2
  mv t0, a6
  lw t3, 72(sp)
  mv a7, t3
  mv t1, a7
  add a6, t0, t1
  mv t4, a6
  mv a7, t5
  mv t6, a7
  add a6, t4, t6
  mv t5, a6
  lw a7, global_x
  mv t2, a7
  add a6, t5, t2
  mv t0, a6
  lw a7, global_y
  mv t3, a7
  add a6, t0, t3
  mv t1, a6
  lw a7, global_z
  mv t4, a7
  add a6, t1, t4
  mv t6, a6
  lw a7, global_a
  mv t5, a7
  add a6, t6, t5
  mv t2, a6
  lw a7, global_b
  mv t0, a7
  add a6, t2, t0
  mv t3, a6
  lw a7, global_c
  mv t1, a7
  add a6, t3, t1
  mv t4, a6
  lw a7, global_d
  mv t6, a7
  add a6, t4, t6
  mv t5, a6
  lw a7, global_e
  mv t2, a7
  add a6, t5, t2
  mv t0, a6
  lw a7, global_f
  mv t3, a7
  add a6, t0, t3
  mv t1, a6
  lw a7, global_g
  mv t4, a7
  add a6, t1, t4
  mv t6, a6
  lw a7, global_h
  mv t5, a7
  add a6, t6, t5
  mv t2, a6
  lw a7, global_i
  mv t0, a7
  add a6, t2, t0
  mv t3, a6
  lw a7, global_j
  mv t1, a7
  add a6, t3, t1
  mv t4, a6
  lw a7, global_k
  mv t6, a7
  add a6, t4, t6
  mv t5, a6
  lw a7, global_l
  mv t2, a7
  add a6, t5, t2
  mv t0, a6
  lw a7, global_m
  mv t3, a7
  add a6, t0, t3
  mv t1, a6
  lw a7, global_n
  mv t4, a7
  add a6, t1, t4
  mv t6, a6
  lw a7, global_o
  mv t5, a7
  add a6, t6, t5
  mv t2, a6
  lw a7, global_p
  mv t0, a7
  add a6, t2, t0
  mv t3, a6
  lw a7, global_q
  mv t1, a7
  add a6, t3, t1
  mv t4, a6
  mv a0, t4
  addi sp, sp, -716
  li a7, 93
  ecall
