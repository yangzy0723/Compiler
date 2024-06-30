.data
global_i:
  .word 15
.data
global_j:
  .word 3
.data
global_k:
  .word 2

.text
.global main
main:
  addi sp, sp, 60
mainEntry:
  lw t0, global_i
  sw t0, 4(sp)
  lw t0, global_j
  sw t0, 8(sp)
  lw t0, 4(sp)
  lw t1, 8(sp)
  add t0, t0, t1
  sw t0, 12(sp)
  lw t0, global_k
  sw t0, 16(sp)
  lw t0, 12(sp)
  lw t1, 16(sp)
  mul t0, t0, t1
  sw t0, 20(sp)
  lw t0, global_i
  sw t0, 24(sp)
  lw t0, global_j
  sw t0, 28(sp)
  lw t0, 24(sp)
  lw t1, 28(sp)
  div t0, t0, t1
  sw t0, 32(sp)
  lw t0, 20(sp)
  lw t1, 32(sp)
  sub t0, t0, t1
  sw t0, 36(sp)
  lw t0, global_i
  sw t0, 40(sp)
  lw t0, global_j
  sw t0, 44(sp)
  lw t0, 40(sp)
  lw t1, 44(sp)
  rem t0, t0, t1
  sw t0, 48(sp)
  lw t0, 36(sp)
  lw t1, 48(sp)
  add t0, t0, t1
  sw t0, 52(sp)
  lw t0, 52(sp)
  sw t0, 0(sp)
  lw t0, 0(sp)
  sw t0, 56(sp)
  lw a0, 56(sp)
  addi sp, sp, -60
  li a7, 93
  ecall
