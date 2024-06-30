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
  li t0, 3
  sw t0, 0(sp)
  lw t0, 0(sp)
  sw t0, 4(sp)
  lw t0, global_a
  sw t0, 8(sp)
  lw t0, 4(sp)
  lw t1, 8(sp)
  add t0, t0, t1
  sw t0, 12(sp)
  lw t0, 12(sp)
  li t1, 1
  add t0, t0, t1
  sw t0, 16(sp)
  lw t0, 16(sp)
  la t1, global_b
  sw t0, 0(t1)
  li t0, 10
  sw t0, 20(sp)
  lw t0, global_a
  sw t0, 24(sp)
  lw t0, global_b
  sw t0, 28(sp)
  lw t0, 24(sp)
  lw t1, 28(sp)
  add t0, t0, t1
  sw t0, 32(sp)
  lw t0, 0(sp)
  sw t0, 36(sp)
  lw t0, 32(sp)
  lw t1, 36(sp)
  add t0, t0, t1
  sw t0, 40(sp)
  lw t0, 20(sp)
  sw t0, 44(sp)
  lw t0, 40(sp)
  lw t1, 44(sp)
  add t0, t0, t1
  sw t0, 48(sp)
  lw a0, 48(sp)
  addi sp, sp, -52
  li a7, 93
  ecall
