.data
global_a:
  .word 1
.data
global_b:
  .word 0

.text
.global main
main:
  addi sp, sp, 52
mainEntry:
  li a6, 3
  mv t0, a6
  mv a7, t0
  mv t1, a7
  lw a7, global_a
  mv t2, a7
  add a6, t1, t2
  mv t3, a6
  li a7, 1
  add a6, t3, a7
  mv t4, a6
  la a7, global_b
  sw t4, 0(a7)
  li a6, 10
  mv t5, a6
  lw a7, global_a
  mv t6, a7
  lw a7, global_b
  mv t1, a7
  add a6, t6, t1
  mv t2, a6
  mv a7, t0
  mv t3, a7
  add a6, t2, t3
  mv t0, a6
  mv a7, t5
  mv t4, a7
  add a6, t0, t4
  mv t5, a6
  mv a0, t5
  addi sp, sp, -52
  li a7, 93
  ecall
