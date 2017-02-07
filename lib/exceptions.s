# SPIM S20 MIPS simulator.
# The default exception handler for spim.
#
# Copyright (C) 1990-2004 James Larus, larus@cs.wisc.edu.
# ALL RIGHTS RESERVED.
#
# SPIM is distributed under the following conditions:
#
# You may make copies of SPIM for your own use and modify those copies.
#
# All copies of SPIM must retain my name and copyright notice.
#
# You may not sell SPIM or distributed SPIM in conjunction with a commerical
# product or service without the expressed written consent of James Larus.
#
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
# PURPOSE.
#

# $Header: $

# Modified for the Bantam Compiler Toolset
# Copyright (C) 2009 Marc Corliss, E Christopher Lewis, and David Furcy

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

#--------------- DJS ----------------
# Modified by DJS to use with MARS simulator.
# Includes changes to the _print_string system subroutine and the
# putString and putInt methods of TextIO so
# that extra unprintable characters are not inserted after each character
# or integer is printed.
#------------------------------------

# Define the exception handling code.  This must go first!
		
	.kdata
__m1_:	.asciiz "  Exception "
__m2_:	.asciiz " occurred and ignored\n"
__e0_:	.asciiz "  [Interrupt] "
__e1_:	.asciiz	"  [TLB]"
__e2_:	.asciiz	"  [TLB]"
__e3_:	.asciiz	"  [TLB]"
__e4_:	.asciiz	"  [Address error in inst/data fetch] "
__e5_:	.asciiz	"  [Address error in store] "
__e6_:	.asciiz	"  [Bad instruction address] "
__e7_:	.asciiz	"  [Bad data address] "
__e8_:	.asciiz	"  [Error in syscall] "
__e9_:	.asciiz	"  [Breakpoint] "
__e10_:	.asciiz	"  [Reserved instruction] "
__e11_:	.asciiz	""
__e12_:	.asciiz	"  [Arithmetic overflow] "
__e13_:	.asciiz	"  [Trap] "
__e14_:	.asciiz	""
__e15_:	.asciiz	"  [Floating point] "
__e16_:	.asciiz	""
__e17_:	.asciiz	""
__e18_:	.asciiz	"  [Coproc 2]"
__e19_:	.asciiz	""
__e20_:	.asciiz	""
__e21_:	.asciiz	""
__e22_:	.asciiz	"  [MDMX]"
__e23_:	.asciiz	"  [Watch]"
__e24_:	.asciiz	"  [Machine check]"
__e25_:	.asciiz	""
__e26_:	.asciiz	""
__e27_:	.asciiz	""
__e28_:	.asciiz	""
__e29_:	.asciiz	""
__e30_:	.asciiz	"  [Cache]"
__e31_:	.asciiz	""
__excp:	.word __e0_, __e1_, __e2_, __e3_, __e4_, __e5_, __e6_, __e7_, __e8_, __e9_
	.word __e10_, __e11_, __e12_, __e13_, __e14_, __e15_, __e16_, __e17_, __e18_,
	.word __e19_, __e20_, __e21_, __e22_, __e23_, __e24_, __e25_, __e26_, __e27_,
	.word __e28_, __e29_, __e30_, __e31_
_s1:	.word 0
_s2:	.word 0

	# Some garbage collection details
	#
	# The generated compiler must define a label (with a value)
	# called gc_flag.  To enable garbage collection, the value at
	# this label should be set to 1, to disable garbage collection,
	# the value at this label should be set to 0.
	#
	# Use a simple conservative mark and sweep algorithm.
	#
	# Garbage collector uses paging.  When there is no more memory
	# a new page is allocated.  Part of the page is used to satisfy
	# the memory request, the leftover space is put on the free list.  
	# Each garbage collection page is 8192 bytes.  To simplify
	# garbage collection, objects must be smaller than 8192 bytes
	# when garbage collection is enabled.  For user-defined classes 
	# and string constants this should be checked during compilation.  
	# For string objects, this must be checked at runtime.  In 
	# particular, String.concat checks that no string exceeds 5000 
	# characters.
	#
	# If your compiler supports arrays then the maximum array length is
	# 1500 when garbage collection is enabled.  However, when garbage
	# collection is disabled, your compiler can allow programmers to 
	# construct larger arrays.  (Note: unlike arrays, the maximum 
	# length of strings is always 5000, regardless of whether garbage 
	# collection is on or off.)
	# 
	# The garbage collector is conservative and may mark incorrect 
	# addresses, therefore, marking has to be done off to the side
	# each page that is allocated is split into a bit vector and
	# data.  the bit vector contains a bit for each word in the data
	# region.  it starts at the beginning of the page and contains
	# 64 words.  
	#		
	# Free list is encoded within the free regions themselves,
	# it is not built off to the side.  Each free region of memory
	# is treated as an entry in the free list.  It contains both
	# a size and a next pointer (which is 0 for the last entry)
	#
	# Free list entry format:
	#
	# <size>
	# <next pointer>
	# <rest of entry>
	#
	# Free list entries must be at least 12 bytes, since that
	# is the minimum size of an object.  When a free region is too
	# small to be put on the free list, it is ignored.  However,
	# during garbage collection it can be reclaimed if the memory
	# either above or below it is freed.

	# String and TextIO details
	#
	# String and TextIO methods build new Strings by writing into
	# a static String _string_buffer and cloning this string.
		
	# data structures needed by the runtime system
	.data
	
	# Some string constants needed by the runtime system
	# (mostly for error handling)
	# we don't have to define these as String objects, however,
	# it makes things more consistent since program character
	# sequences are String objects
_String_const_0:
	.word	1
	.word	48
	.word	String_dispatch_table
	.word	30
	.ascii	":runtime error: out of memory\n"
	.byte	0
	.align	2
_String_const_1:
	.word	1
	.word	60
	.word	String_dispatch_table
	.word	40
	.ascii	":runtime error: null pointer referenced\n"
	.byte	0
	.align	2
_String_const_2:
	.word	1
	.word	60
	.word	String_dispatch_table
	.word	40
	.ascii	":runtime error: string argument is null\n"
	.byte	0
	.align	2
_String_const_3:
	.word	1
	.word	48
	.word	String_dispatch_table
	.word	31
	.ascii	":runtime error: divide by zero\n"
	.byte	0
	.align	2
_String_const_4:
	.word	1
	.word	72
	.word	String_dispatch_table
	.word	53
	.ascii	":runtime error: concatenated string too long (>5000)\n"
	.byte	0
	.align	2
_String_const_5:
	.word	1
	.word	52
	.word	String_dispatch_table
	.word	33
	.ascii	":runtime error: bad string index\n"
	.byte	0
	.align	2
_String_const_6:
	.word	1
	.word	56
	.word	String_dispatch_table
	.word	38
	.ascii	":runtime error: can't read from file '"
	.byte	0
	.align	2
_String_const_7:
	.word	1
	.word	68
	.word	String_dispatch_table
	.word	51
	.ascii	":runtime error: I/O error while attempting to read\n"
	.byte	0
	.align	2
_String_const_8:
	.word	1
	.word	56
	.word	String_dispatch_table
	.word	37
	.ascii	":runtime error: can't write to file '"
	.byte	0
	.align	2
_String_const_9:
	.word	1
	.word	72
	.word	String_dispatch_table
	.word	52
	.ascii	":runtime error: I/O error while attempting to write\n"
	.byte	0
	.align	2
_String_const_10:
	.word	1
	.word	116
	.word	String_dispatch_table
	.word	99
	.ascii	"Start index must be >= 0 and < string length\nEnd index must be >= start index and <= string length\n"
	.byte	0
	.align	2
_String_const_11:
	.word	1
	.word	28
	.word	String_dispatch_table
	.word	9
	.ascii	"String: \""
	.byte	0
	.align	2
_String_const_12:
	.word	1
	.word	20
	.word	String_dispatch_table
	.word	2
	.ascii	"\"\n"
	.byte	0
	.align	2
_String_const_13:
	.word	1
	.word	28
	.word	String_dispatch_table
	.word	8
	.ascii	"Length: "
	.byte	0
	.align	2
_String_const_14:
	.word	1
	.word	32
	.word	String_dispatch_table
	.word	15
	.ascii	", start index: "
	.byte	0
	.align	2
_String_const_15:
	.word	1
	.word	32
	.word	String_dispatch_table
	.word	13
	.ascii	", end index: "
	.byte	0
	.align	2
_String_const_16:
	.word	1
	.word	52
	.word	String_dispatch_table
	.word	35
	.ascii	":runtime error: illegal class cast\n"
	.byte	0
	.align	2
_String_const_17:
	.word	1
	.word	48
	.word	String_dispatch_table
	.word	30
	.ascii	"Can't convert object of type '"
	.byte	0
	.align	2
_String_const_18:
	.word	1
	.word	28
	.word	String_dispatch_table
	.word	11
	.ascii	"' to type '"
	.byte	0
	.align	2
_String_const_19:
	.word	1
	.word	20
	.word	String_dispatch_table
	.word	1
	.ascii	":"
	.byte	0
	.align	2
_String_const_20:
	.word	1
	.word	20
	.word	String_dispatch_table
	.word	1
	.ascii	"\n"
	.byte	0
	.align	2
_String_const_21:
	.word	1
	.word	20
	.word	String_dispatch_table
	.word	2
	.ascii	"'\n"
	.byte	0
	.align	2
_String_const_22:
	.word	1
	.word	48
	.word	String_dispatch_table
	.word	29
	.ascii	":runtime error: array index '"
	.byte	0
	.align	2
_String_const_23:
	.word	1
	.word	36
	.word	String_dispatch_table
	.word	16
	.ascii	"' out of bounds\n"
	.byte	0
	.align	2
_String_const_24:
	.word	1
	.word	56
	.word	String_dispatch_table
	.word	36
	.ascii	":runtime error: illegal array size '"
	.byte	0
	.align	2
_String_const_25:
	.word	1
	.word	60
	.word	String_dispatch_table
	.word	41
	.ascii	"' (must be >=0 and <=1500 if GC enabled)\n"
	.byte	0
	.align	2
_String_const_26:
	.word	1
	.word	52
	.word	String_dispatch_table
	.word	34
	.ascii	":runtime error: array store error\n"
	.byte	0
	.align	2
_String_const_27:
	.word	1
	.word	48
	.word	String_dispatch_table
	.word	29
	.ascii	"Can't assign object of type '"
	.byte	0
	.align	2
_String_const_28:
	.word	1
	.word	48
	.word	String_dispatch_table
	.word	31
	.ascii	"' to element in array of type '"
	.byte	0
	.align	2
_String_const_29:
	.word	1
	.word	20
	.word	String_dispatch_table
	.word	1
	.ascii	"@"
	.byte	0
	.align	2

	# next random value.  initially seeded with the current time.
_random:
	.word	1
	
	# a string buffer used for building storing new strings
	# the data is overwritten in _string_buffer and then Object.clone
	# is used to copy the string to a new location in memory
	# used by methods in String and TextIO
_string_buffer:
	.word	0
	.word	256224
	.word	String_dispatch_table
	.word	256
	.space	258
	.align	2

	# data structures needed by the garbage collector
	# _gc_free_ptr is a pointer into the free list of available memory
	# regions, initialized by _gc_init
_gc_free_ptr:
	.word	0
	
	# _gc_heap_start is a pointer into the start of the heap ($gp 
	# points to the end of the heap), initialized by _gc_init
	# needed for walking the heap during sweeping phase
_gc_heap_start:
	.word	0
	
	# _gc_stack_start points to the start of the stack ($sp points 
	# to the end of the stack), initialized by _gc_init
	# note:	stack boundaries are needed because garbage collector
	# does not maintain a root set, instead it searches the stack
	# for possible heap pointers
_gc_stack_start:
	.word	0



# This is the exception handler code that the processor runs when
# an exception occurs. It only prints some information about the
# exception, but can server as a model of how to write a handler.
#
# Because we are running in the kernel, we can use $k0/$k1 without
# saving their old values.

# This is the exception vector address for MIPS-1 (R2000):
#	.ktext 0x80000080
# This is the exception vector address for MIPS32:
	.ktext 0x80000180
# Select the appropriate one for the mode in which SPIM is compiled.
	# DJS commented out .set noat
	move $k1 $at		# Save $at
	# DJS commented out .set at
	sw $v0 _s1		# Not re-entrant and we can't trust $sp
	sw $a0 _s2		# But we need to use these registers

	mfc0 $k0 $13		# Cause register
	srl $a0 $k0 2		# Extract ExcCode Field
	andi $a0 $a0 0x1f

	# Print information about exception.
	#
	li $v0 4		# syscall 4 (print_str)
	la $a0 __m1_
	syscall

	li $v0 1		# syscall 1 (print_int)
	srl $a0 $k0 2		# Extract ExcCode Field
	andi $a0 $a0 0x1f
	syscall

	li $v0 4		# syscall 4 (print_str)
	andi $a0 $k0 0x3c
	lw $a0 __excp($a0)
	nop
	syscall

	bne $k0 0x18 _ok_pc	# Bad PC exception requires special checks
	nop

	mfc0 $a0 $14		# EPC
	andi $a0 $a0 0x3	# Is EPC word-aligned?
	beq $a0 0 _ok_pc
	nop

	li $v0 10		# Exit on really bad PC
	syscall

_ok_pc:
	li $v0 4		# syscall 4 (print_str)
	la $a0 __m2_
	syscall

	srl $a0 $k0 2		# Extract ExcCode Field
	andi $a0 $a0 0x1f
	bne $a0 0 _ret		# 0 means exception was an interrupt
	nop

# Interrupt-specific code goes here!
# Don't skip instruction at EPC since it has not executed.


_ret:
# Return from (non-interrupt) exception. Skip offending instruction
# at EPC to avoid infinite loop.
#
	mfc0 $k0 $14		# Bump EPC register
	addiu $k0 $k0 4		# Skip faulting instruction
				# (Need to handle delayed branch case here)
	mtc0 $k0 $14


# Restore registers and reset procesor state
#
	lw $v0 _s1		# Restore other registers
	lw $a0 _s2

	# DJS commented out .set noat
	move $at $k1		# Restore $at
	# DJS commented out .set at

	mtc0 $0 $13		# Clear Cause register

	mfc0 $k0 $12		# Set Status register
	ori  $k0 0x1		# Interrupts enabled
	mtc0 $k0 $12

# Return from exception on MIPS32:
	eret

# Return sequence for MIPS-I (R2000):
#	rfe			# Return from exception handler
				# Should be in jr's delay slot
#	jr $k0
#	 nop


	.text
	
	# library subroutines:
	# 1) program initialization subroutines
	# 2) memory allocation subroutines
	# 3) garbage collection subroutines
	# 4) builtin methods from Object, Sys, String, and TextIO

	# builtin methods must be global so that user code can call them
	.globl Object.clone
	.globl Object.equals
	.globl Object.toString
	.globl Sys.exit
	.globl Sys.time
	.globl Sys.random
	.globl String.length
	.globl String.equals
	.globl String.toString
	.globl String.concat
	.globl String.substring
	.globl TextIO.readFile
	.globl TextIO.readStdin
	.globl TextIO.writeFile
	.globl TextIO.writeStdout
	.globl TextIO.writeStderr
	.globl TextIO.getString
	.globl TextIO.getInt
	.globl TextIO.putString
	.globl TextIO.putInt

	# three error routines must be used by error code as well
	# these start with `_' to distinguish them from subroutines in
	# the user program
	.globl _null_pointer_error
	.globl _array_index_error
	.globl _array_size_error
	.globl _array_store_error
	.globl _class_cast_error
	.globl _divide_zero_error


	
	# allocate a new object (called when garbage collection is disabled)
_mem_alloc:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# allocate blocks 1024 at a time until we have total size
	# FIXME: could we instead allocate entire region at once?
	move $t1 $a0
	li $t0 0
	# get number of 1024 blocks needed
	div $t2 $t1 1024
	li $t3 0
	# loop to get each block
_label0:
	beq $t3 $t2 _label1
	# call sbrk to get next 1024 block
	li $a0 1024
	li $v0 9
	syscall
	# check if first iteration
	bne $t0 $zero _label4
	# if first iteration then save pointer in $t0, will be the
	# pointer into the entire memory region
	move $t0 $v0
_label4:
	# check if sbrk returned 0, if so break loop
	beq $v0 $zero _label2
	add $t3 $t3 1
	b _label0
	# end of loop
_label1:
	# allocate the last block of memory, which will be less than 1024 bytes
	rem $a0 $t1 1024
	beq $a0 $zero _label3
	li $v0 9
	syscall
	# check if first block allocation
	bne $t0 $zero _label5
	# if first allocation, then save pointer in $t0, will be the
	# pointer into the entire memory region
	move $t0 $v0
_label5:
	# check if sbrk returned 0, if so proceed to error handling code in _label2
	beq $v0 $zero _label2
	b _label3
_label2:
	# if we reach here then we ran out of memory - call error subroutine
	jal _out_of_memory
_label3:
	# put $t0 (pointer into memory) into result register $v0
	move $v0 $t0
	# update the global pointer
	add $gp $v0 $t1
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# initialize the garbage collector data structures
	# only called if garbage collector is enabled
_gc_init:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# allocate an initial page - pass _gc_alloc_page 0 since
	# no memory needed initially
	li $a0 0
	jal _gc_alloc_page
	# set _gc_heap_start, which is 8192 (first page) minus current $gp
	sub $t0 $gp 8192
	la $t1 _gc_heap_start
	sw $t0 0($t1)
	# set _gc_stack_start to current stack pointer (will miss some stack
	# entries, but these don't contain objects)
	la $t0 _gc_stack_start
	sw $sp 0($t0)
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# allocate a new object in the heap, updates the free list to reflect
	# this allocation
	# object size is passed via $a0
_gc_mem_alloc:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	# body
	# save object size in $s0 (caller-saved register)
	move $s0 $a0
	# call _gc_find_free to find region of approriate size (passes size in $a0)
	jal _gc_find_free
	# check if _gc_find_free returned 0 (i.e., no region found)
	bne $v0 $zero _label17
	# if 0, then must perform garbage collection, otherwise, can return result ($v0)
	move $a0 $s0
	jal _gc_collect
_label17:
	# epilogue
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 8
	jr $ra


	
	# find next free region of memory, traverses free list to find region
	# returns 0 if no such region found
	# size of region passed in $a0	
_gc_find_free:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get free pointer
	la $v0 _gc_free_ptr
	lw $v0 0($v0)
	# $t2 is a previous pointer, used to potentially remove a free list entry
	# (initially 0)
	li $t2 0
_label18:
	# break if at end of free list
	beq $v0 $zero _label22
	# otherwise get size out of free list
	lw $t0 0($v0)
	# if size is large enough then break loop
	bge $t0 $a0 _label19
	# otherwise set previous pointer to point to the current entry
	move $t2 $v0
	# and continue onto next entry
	lw $v0 4($v0)
	b _label18
	# end of loop
_label19:
	# check if entry will remain after allocating part of it to new object
	# (must have 12 bytes leftover)
	add $t1 $a0 12
	bge $t0 $t1 _label20
	# if not then remove entry
	# get next pointer of current entry
	lw $t0 4($v0)
	# check if previous entry is null
	beq $t2 $zero _label21
	# if not then set previous entry's pointer to the next address of the current entry
	# (removes current entry)
	sw $t0 4($t2)
	b _label22
_label21:
	# otherwise, current entry is first entry so set address at _gc_free_ptr to
	# to point to current entry's next address
	la $t2 _gc_free_ptr
	sw $t0 0($t2)
	b _label22
_label20:
	# if entry will remain after allocation then only have to update size
	move $t1 $v0
	# compute leftover free portion
	sub $t0 $t0 $a0
	# set result register to point to part of free entry
	add $v0 $v0 $t0
	# update size in entry
	sw $t0 0($t1)
_label22:
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# allocate a new page for the garbage collector
	# takes a memory region size via $a0
	# the page is split into free space and allocated space
	# based on $a0
_gc_alloc_page:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	# body
	# save 
	move $s0 $a0
	# allocate a new page (8192 bytes)
	li $a0 8192
	jal _mem_alloc
	# save result to $t0
	move $t0 $v0
	# increment result by bit vector size
	add $v0 $v0 256
	# loop for zeroing out bit vector (everything is unmarked)
_label23:
	bge $t0 $v0 _label24
	sw $zero 0($t0)
	add $t0 $t0 4
	b _label23
	# end of loop
_label24:
	# get pointer into free portion of memory region, put in $t0
	add $t0 $v0 $s0
	# get size of free list entry, put in $t1
	li $t1 7936
	sub $t1 $t1 $s0
	# write size to free list entry
	sw $t1 0($t0)
	# add entry to front of free list
	la $t2 _gc_free_ptr
	lw $t3 0($t2)
	sw $t3 4($t0)
	sw $t0 0($t2)
	# epilogue	
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 8
	jr $ra


	
	# garbage collect heap
	# allocation size passed in $a0
	# algorithm:
	# 1) marking
	#    walk stack looking for heap pointers, for each pointer
	#      mark it using page bit vector
	#      perform depth-first search to find other heap pointers
	# 2) sweeping
	#    walk heap looking for unmarked regions, for each unmarked region
	#      add it to the free list if large enough
	#      keep track of deallocated space
	# 3) allocate region
	#    if more than one page deallocated, then try to find large enough region
	#      if found, then update free list and return pointer
	#      otherwise, allocate a new page
	#    if less than one page deallocated, then allocate a new page
_gc_collect:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	# body
	# save allocation size
	move $s0 $a0
	# mark reachable objects
	jal _gc_mark
	# sweep heap, freeing unmarked regions
	jal _gc_sweep
	# _gc_sweep returns deallocation size, check if larger
	# than page size
	li $t0 8192
	blt $v0 $t0 _label25
	# if larger than try to find a large enough free entry
	move $a0 $s0
	jal _gc_find_free
	# check if one was found (may not be found b/c of fragmentation)
	bne $v0 $zero _label26
_label25:
	# if we reach here then need to allocate a new page
	move $a0 $s0
	jal _gc_alloc_page
_label26:
	# epilogue
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 8
	jr $ra


	
	# mark all reachable objects
	# doesn't use a root set, instead walks stack
	# conservatively looking for heap pointers
_gc_mark:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	# body
	# get start of stack
	la $s0 _gc_stack_start
	lw $s0 0($s0)
	# loop over stack
_label27:
	# if we reach end of the stack, then break
	beq $s0 $sp _label28
	# load next value from stack
	lw $a0 0($s0)
	# call _gc_check_addr to see if a possible heap pointer
	jal _gc_check_addr
	# if _gc_check_addr returns 0 then not a heap pointer
	beq $v0 $zero _label29
	# otherwise, mark address by calling _gc_mark_addr
	lw $a0 0($s0)
	jal _gc_mark_addr
	lw $a0 0($s0)
	# also call _gc_find_reachable to find all reachable objects from
	# this one
	jal _gc_find_reachable
_label29:
	# decrement pointer into stack and continue looping
	sub $s0 $s0 4
	b _label27
_label28:
	# end of loop and subroutine
	# epilogue
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 8
	jr $ra


	
	# check to see if address is a possible heap pointer
	# takes as address in $a0
	# checks the following:
	#   heap start <= address <= heap end
	#   address is word-aligned
	#   address is not within page bit vector
	#   object id (at 0(address)) is valid (>= 0)
	#   object size (at 4(address)) is valid (between 12 and max size)
	#   object size is word aligned
	#   object is not already marked
_gc_check_addr:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# check if address >= heap start
	li $v0 0
	la $t0 _gc_heap_start
	lw $t0 0($t0)
	blt $a0 $t0 _label30
	# check if address <= heap end - 12 (minimum size of object)
	sub $t0 $gp 12
	bgt $a0 $t0 _label30
	# check if address is word aligned
	and $t0 $a0 3
	bne $t0 $zero _label30
	# check if address within bit vector
	and $t0 $a0 8191
	li $t1 256
	blt $t0 $t1 _label30
	# check if id < 0
	lw $t0 0($a0)
	blt $t0 $zero _label30
	# check if size of object is >= 12
	lw $t0 4($a0)
	li $t1 12
	blt $t0 $t1 _label30
	# check if size of object is <= 7936
	li $t1 7936
	bgt $t0 $t1 _label30
	# check if size is word aligned
	and $t0 $t0 3
	bne $t0 $zero _label30
	# check if already marked
	jal _gc_is_marked
	beq $v0 $zero _label31
	li $v0 0
	b _label30
_label31:
	# if we make it here, then potential heap address - return 1
	li $v0 1
_label30:
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# check if address is marked
	# address is passed via $a0
	# returns 1 if marked, 0 otherwise
_gc_is_marked:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get page address, put in $t0
	# DJS:  changed 'and $t0 $a0 -8192' to the following two lines
	li $t0 -8192
	and $t0 $a0 $t0
	# get offset, put in $t1
	and $t1 $a0 8191
	# word align offset, put in $t2
	srl $t2 $t1 2
	# get word number into page, put in $t3
	srl $t3 $t2 3
	# word align word number, put in $t3
	# DJS:  changed 'and $t3 $t3 -4' to the following two lines
	li $t4 -4
	and $t3 $t3 $t4
	# add to page address ($t0) to get address of word in bit vector, put in $t3
	add $t3 $t0 $t3
	# load bit vector word, put in $t3
	lw $t3 0($t3)
	# get bit number in word by masking off all but the last 5 bits of the offset ($t2)
	and $t2 $t2 31
	# get bit from word ($t3) and return it
	# DJS:  changed the command in the next line from sll to sllv
	sllv $t2 $t3 $t2
	srl $v0 $t2 31
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# mark an address
	# address is passed via $a0	
_gc_mark_addr:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get page address, put in $t0
	# DJS:  changed 'and $t0 $a0 -8192' to the following two lines
	li $t0 -8192
	and $t0 $a0 $t0
	# get offset, put in $t1	
	and $t1 $a0 8191
	# word align offset, put in $t2	
	srl $t2 $t1 2
	# get word number into page, put in $t3
	srl $t3 $t2 3
	# word align word number, put in $t3
	# DJS:  changed 'and $t3 $t3 -4' to the following two lines
	li $t4 -4
	and $t3 $t3 $t4
	# add to page address ($t0) to get address of word in bit vector, put in $t3
	add $t3 $t0 $t3
	# load bit vector word, put in $t3
	lw $t1 0($t3)
	# get bit number in word by masking off all but the last 5 bits of the offset ($t2)
	and $t2 $t2 31
	# store 1 in far left bit of $t0
	li $t0 0x80000000
	# shift $t0 by bit number ($t2)
	# DJS  changed the following instruction from srl to srlv
	srlv $t2 $t0 $t2
	# set this bit in the bit vector word ($t1)
	or $t1 $t1 $t2
	# write this word back to the bit vector
	sw $t1 0($t3)
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# find all reachable addresses from a (potential) heap address
	# uses a depth-first algorithm
	# address passed via $a0
_gc_find_reachable:
	# prologue
	add $sp $sp -16
	sw $ra 12($sp)
	sw $s0 8($sp)
	sw $s1 4($sp)
	sw $s2 0($sp)
	# body
	# set $s0 to point to first field in object
	add $s0 $a0 12
	# set $s1 to the end of the object
	lw $s1 4($a0)
	add $s1 $s1 $a0
	# loop until we reach end of object looking for other
	# fields, which could be heap pointers
_label32:
	# if pointer ($s0) is at end of the object ($s1) then break
	beq $s0 $s1 _label33
	# get next field value
	lw $s2 0($s0)
	# call _gc_check_addr to see if heap address
	move $a0 $s2
	jal _gc_check_addr
	# if 0 then continue looping
	beq $v0 $zero _label34
	# otherwise, call _gc_mark_addr to mark address
	move $a0 $s2
	jal _gc_mark_addr
	# call _gc_find_reachable to find other reachable objects
	# (depth-first algorithm)
	move $a0 $s2
	jal _gc_find_reachable
_label34:
	# increment field pointer
	add $s0 $s0 4
	# continue looping
	b _label32
_label33:
	# end of loop
	# epilogue
	lw $s2 0($sp)
	lw $s1 4($sp)
	lw $s0 8($sp)
	lw $ra 12($sp)
	add $sp $sp 16
	jr $ra


	
	# sweep the heap, freeing unreachable memory
	# returns the total amount freed
_gc_sweep:
	# prologue
	add $sp $sp -12
	sw $ra 8($sp)
	sw $s0 4($sp)
	sw $s1 0($sp)
	# body
	# remove free list
	la $t0 _gc_free_ptr
	sw $zero 0($t0)
	# $s0 holds the amount of freed memory (will be returned)
	li $s0 0
	# $s1 is a pointer into the heap
	la $s1 _gc_heap_start
	lw $s1 0($s1)
	# loop over heap
_label35:
	# if we reach end of the heap, then break
	bge $s1 $gp _label36
	# otherwise, call _gc_sweep_page sweep the next page
	move $a0 $s1
	jal _gc_sweep_page
	# update $s0 by the amount freed (returned by _gc_sweep_page)
	add $s0 $s0 $v0
	# increment heap pointer by page size
	add $s1 $s1 8192
	b _label35
_label36:
	# end of the loop
	# set return value to amount freed ($s0)
	move $v0 $s0
	# epilogue
	lw $s1 0($sp)
	lw $s0 4($sp)
	lw $ra 8($sp)
	add $sp $sp 12
	jr $ra


	
	# sweep the next page
	# returns the amount of memory freed
_gc_sweep_page:
	# prologue
	add $sp $sp -24
	sw $ra 20($sp)
	sw $s0 16($sp)
	sw $s1 12($sp)
	sw $s2 8($sp)
	sw $s3 4($sp)
	sw $s4 0($sp)
	# body
	# $s0 is the size of the current free list entry
	li $s0 0
	# $s1 is a pointer into the page
	add $s1 $a0 256
	# $s2 is the end page pointer
	add $s2 $a0 8192
	# $s3 is the pointer into the current free list entry
	move $s3 $s1
	# $s4 is the total freed size
	li $s4 0
_label37:
	# if we reach end of the page then break
	bge $s1 $s2 _label38
	# otherwise, check if next address is marked
	move $a0 $s1
	jal _gc_is_marked
	beq $v0 $zero _label39
	# if marked then call _gc_next_free_addr to get next free address
	move $a0 $s1
	jal _gc_next_free_addr
	# set $s1 to next free address
	move $s1 $v0
	# check if free entry size is >= minimum size (12)
	li $t0 12
	blt $s0 $t0 _label40
	# if it is, then set size and put onto front of free list
	sw $s0 0($s3)
	la $t0 _gc_free_ptr
	lw $t1 0($t0)
	sw $t1 4($s3)
	sw $s3 0($t0)
	# increment the total freed size ($s4) by object size ($s0)
	add $s4 $s4 $s0
_label40:
	# set current entry pointer ($s3) to current heap pointer ($s1)
	move $s3 $s1
	# set size of current entry ($s0) to 0
	li $s0 0
	# continue looping
	b _label37
_label39:
	# increment heap pointer ($s1) and size of current entry ($s0)
	add $s1 $s1 4
	add $s0 $s0 4
	# continue looping
	b _label37
_label38:
	# end of the loop
	# get bit vector pointer ($s1) and pointer to data region ($s2)
	sub $s1 $s2 8192
	add $s2 $s1 256
	# loop, zeroing out bit vector
_label41:
	# break when bit vector pointer ($s1) reaches data region ($s2)
	bge $s1 $s2 _label42
	# write 0 to next word in bit vector
	sw $zero 0($s1)
	# increment bit vector pointer and continue looping
	add $s1 $s1 4
	b _label41
_label42:
	# end of loop
	# return total amount freed ($s4)
	move $v0 $s4
	# epilogue	
	lw $s4 0($sp)
	lw $s3 4($sp)
	lw $s2 8($sp)
	lw $s1 12($sp)
	lw $s0 16($sp)
	lw $ra 20($sp)
	add $sp $sp 24
	jr $ra


	
	# get next free address
	# address is passed via $a0
	# normally, this is the object address + size of object, however,
	# because of false positives (the garbage collector is conservative)
	# one marked object can overlap with another.  this subroutine finds
	# all such overlaps and returns the last address, which is not a
	# part of any preceeding object
_gc_next_free_addr:
	# prologue
	add $sp $sp -16
	sw $ra 12($sp)
	sw $s0 8($sp)
	sw $s1 4($sp)
	sw $s2 0($sp)
	# body
	# $s0 is the end address of page containing this address
	# DJS:  changed 'and $s0 $a0 -8192' to the following two lines
	li $t0 -8192
	and $s0 $a0 $t0
	add $s0 $s0 8192
	# $s1 is a pointer into the object
	move $s1 $a0
	# $s2 is the end of object address (initially set to $s1+4)
	add $s2 $a0 4
	# loop until we reach the end of the object
_label43:
	# if object pointer is at end of object or end of page then break
	bge $s1 $s0 _label44
	bge $s1 $s2 _label44
	# check if this is marked (overlapping can occur due to false positives)
	move $a0 $s1
	jal _gc_is_marked
	beq $v0 $zero _label45
	# if marked then get the new end address, put in $t0
	lw $t0 4($s1)
	add $t0 $s1 $t0
	# if less than current end object address then ignore
	ble $t0 $s2 _label45
	# if larger than end address of page then goto _label46
	bge $t0 $s0 _label46
	# if we make it here, then must update object end address
	move $s2 $t0
	b _label45
_label46:
	# if we make it here, we've reached end of the page, break loop and return this address
	move $s2 $s0
	b _label44
_label45:
	# increment object pointer ($s1) and continue looping
	add $s1 $s1 4
	b _label43
_label44:
	# move end object address ($s2) to result register ($v0)
	move $v0 $s2
	# epilogue
	lw $s2 0($sp)
	lw $s1 4($sp)
	lw $s0 8($sp)
	lw $ra 12($sp)
	add $sp $sp 16
	jr $ra


	
	# subroutine for converting a string to an int
	# if string contains any non-int characters then it returns 0
	# note:	cannot handle ints expressed in hexadecimal or scientific notation
_a2i:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# save string to $t0
	move $t0 $a0
	# set return value to 0 - if any non-int chars found then will return this
	move $v0 $zero
	# $t3 indicates sign of result (0 for positive, 1 for negative)
	move $t3 $zero
	# load first byte looking for `-'
	lb $t1 0($t0)
	li $t2 45
	bne $t1 $t2 _label6
	# if `-' (45) found set $t3 to 1 to indicate negative
	li $t3 1
	# move to next character
	add $t0 $t0 1
	# loop over characters
_label6:
	# get next character
	lb $t1 0($t0)
	# if null break out of loop
	beq $t1 $zero _label7
	# if newline (10) then break out of loop
	li $t2 10
	beq $t1 $t2 _label7
	# check if digit (48-57), if not set $v0 to 0 and break out of loop
	li $t2 57
	ble $t1 $t2 _label9
	li $v0 0
	b _label7
_label9:
	li $t2 48
	bge $t1 $t2 _label10
	li $v0 0
	b _label7
_label10:
	# if we make it here then character is a digit
	# update result ($v0) by multiplying old value by 10 and adding new
	# digit into result
	mul $v0 $v0 10
	# get digit by subtracting '0' (48)
	sub $t1 $t1 48
	add $v0 $v0 $t1
	add $t0 $t0 1
	# continue looping
	b _label6
	# end of loop
_label7:
	# check if sign is negative and negate value if it is
	beq $t3 $zero _label8
	mul $v0 $v0 -1
_label8:
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# subroutine for converting an int to a string
_i2a:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# loop to get order of int (1, 10, 100, ...)
	# order stored in $t3, initially 1
	li $t3 1
	# initially divide int ($t0) by 10
	# note:	original int saved in $a0
	div $t0 $a0 10
_label12:
	# if 0 then break
	beq $t0 $zero _label13
	# otherwise multiply order ($t3) by 10
	mul $t3 $t3 10
	# divide int ($t0) by 10
	div $t0 $t0 10
	b _label12
	# end of loop
_label13:
	# setup _string_buffer to hold new string
	la $v0 _string_buffer
	# get pointer ($t0) into char sequence of _string_buffer
	# note:	$v0 also points to char sequence - used later to compute length
	add $v0 $v0 16
	move $t0 $v0
	# if int is 0 then write 0 and skip over loop
	bne $a0 $zero _label11
	li $t1 48
	sb $t1 0($t0)
	add $t0 $t0 1
	b _label15
_label11:
	# check if int is negative, if so write `-' to char sequence
	bge $a0 $zero _label14
	li $t1 45
	sb $t1 0($t0)
	# negate int making it positive
	mul $a0 $a0 -1
	# increment char sequence pointer
	add $t0 $t0 1
	# loop writing digits into char sequence of _string_buffer
_label14:
	# if order is 0 then break
	beq $t3 $zero _label15
	# otherwise divide int by order to get next digit
	div $t1 $a0 $t3
	# add `0' to digit to get character
	add $t1 $t1 48
	# write it to char sequence
	sb $t1 0($t0)
	# compute remainder of int when dividing by order
	rem $a0 $a0 $t3
	# divide order by 10
	div $t3 $t3 10
	# increment char sequence pointer
	add $t0 $t0 1
	# continue looping
	b _label14
	# end of loop
_label15:
	# write null character to char sequence
	sb $zero 0($t0)
	# get length of string ($t0)
	sub $t0 $t0 $v0
	# compute total bytes ($t1) of string (17+length+alignment bytes)
	li $t1 17
	add $t1 $t0 $t1
	# get offset from 4
	rem $t2 $t1 4
	# if none needed skip over next few instructions
	beq $t2 $zero _label16
	# otherwise, subtract from 4 to get alignment bytes
	li $t3 4
	sub $t2 $t3 $t2
	# add to total bytes ($t1)
	add $t1 $t1 $t2
_label16:
	# set $a0 to point to _string_buffer
	la $a0 _string_buffer
	# write total bytes ($t1) to second entry in second_buffer
	sw $t1 4($a0)
	# write length ($t0) to first field in string
	sw $t0 12($a0)
	# clone _string_buffer and return result
	jal Object.clone
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# subroutine for reading a string from a file
	# used by TextIO.getString and TextIO.getInt
	# file descriptor passed in $a0	
_read_string_file:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# save filename and line number to $t2 and $t3 (so we can use $a1 and $a2 in syscall)	
	move $t2 $a2
	move $t3 $a1
	# set $a1 to char sequence in _string_buffer
	la $a1 _string_buffer
	add $a1 $a1 16
	# set $a2 to 1, going to read one character at a time
	li $a2 1
	# loop for up to 256 characters, $t0 is the current count
	li $t0 0
_label81:
	# branch if read 256 characters
	li $t1 256
	beq $t0 $t1 _label82
	# save $a0 to $t4 (note: result is also placed in $a0 in MipsPilot on a read syscall)
	move $t4 $a0
	# perform read system call
	li $v0 14
	syscall
	# restore $a0
	move $a0 $t4
	# check if returned <0, indicates an error
	bge $v0 $zero _label83
	# restore filename and line number to $a1 and $a2, call _read_error
	move $a2 $t2
	move $a1 $t3
	jal _read_error
_label83:
	# check if read '\0', if so break
	bne $v0 $zero _label84
	li $v0 0
	b _label85
_label84:
	# check if read '\n', if so break
	li $t1 10
	lb $v0 0($a1)
	beq $v0 $t1 _label82
	# otherwise, we increment _string_buffer pointer and counter and continue looping
	add $a1 $a1 1
	add $t0 $t0 1
	b _label81
_label82:
	# end of loop
	# write null character to end of char sequence
	sb $zero 0($a1)
	# restore filename and line number
	move $a2 $t2
	move $a1 $t3
	# compute size ($t1) of string object (17+length+alignment bytes)
	li $t1 17
	add $t1 $t0 $t1
	# compute alignment bytes
	# get offset from 4
	rem $t2 $t1 4
	# if 0, then already aligned
	beq $t2 $zero _label86
	# otherwise, subtract from 4 and add this to total size ($t1)
	li $t3 4
	sub $t2 $t3 $t2
	add $t1 $t1 $t2
_label86:
	# set size and length in _string_buffer
	la $a0 _string_buffer
	sw $t1 4($a0)
	sw $t0 12($a0)
	# clone _string_buffer and return cloned object
	jal Object.clone
_label85:
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra



	# write a string to a file
	# used by TextIO.putString and TextIO.putInt
	# file descriptor passed in $a0, string passed on the stack
_write_string_file:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# save filename and line number to $t2 and $t3 (so we can use $a1 and $a2 in syscall)	
	move $t2 $a2
	move $t3 $a1
	# get the string from the stack
	lw $a1 4($sp)
	# check if null, if so call error subroutine
	bne $a1 $zero _label76
	# restore filename and line number to $a1 and $a2, call _null_argument_error
	move $a2 $t2
	move $a1 $t3
	jal _null_argument_error
_label76:
	# set $t1 to length of string
	lw $t1 12($a1)
	# set $a1 to point to char sequence in string
	add $a1 $a1 16
	# set $a2 to 1 -- write one character at a time
	li $a2 1
	# loop for up to $t1 characters, $t0 is the current count
	li $t0 0
_label77:
	# branch if read $t1 characters
	beq $t0 $t1 _label79
	# save $a0 to $t4 (note: result is also placed in $a0 in MipsPilot on a read syscall)
	move $t4 $a0
	# perform write system call
	li $v0 15
	syscall
	# restore $a0
	move $a0 $t4
	# check if returned <=0, indicates an error
	bgt $v0 $zero _label78
	# restore filename and line number to $a1 and $a2, call _write_error
	move $a2 $t2
	move $a1 $t3
	jal _write_error
_label78:	
	# otherwise, we increment _string_buffer pointer and counter and continue looping
	add $a1 $a1 1
	add $t0 $t0 1
	b _label77
_label79:
	# end of loop
	# restore filename and line number
	move $a2 $t2
	move $a1 $t3
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 8
	jr $ra
	
	
		
	# subroutine for printing a string
	# takes string in $a0 and output stream as parameter in $t0
_print_string:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# save filename and linenumber in temporaries
	move $t3 $a2
	move $t2 $a1
	
# -------- DJS: removed -----------
	# put pointer to char sequence in $a1
#	add $a1 $a0 16
	# put size of char sequence in $a2
#	lw $a2 12($a0)
	# put output stream fd in $a0
#	move $a0 $t0
	# perform write system call
#	li $v0 15
#	syscall
# ---------- DJS: added -----------
    add $a0 $a0 16
    li $v0 4
    syscall
#-------------------------------
	# restore $a1 and $a2 with filename and linenumber
	move $a2 $t3
	move $a1 $t2
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# subroutine for printing a string to standard output
	# takes string in $a0
_print_string_stdout:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	li $t0 1
	jal _print_string
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra



	# subroutine for printing a string to standard error
	# takes string in $a0
_print_string_stderr:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	li $t0 2
	jal _print_string
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# subroutine for printing an int to standard output
	# takes int in $a0 and output stream in $t0
_print_int:
	# prologue
	add $sp $sp -8
	sw $t0 4($sp)
	sw $ra 0($sp)
	# body
	# call _i2a to convert int to string
	jal _i2a
	# call _print_string to print int
	move $a0 $v0
	lw $t0 4($sp)
	jal _print_string
	# epilogue
	lw $t0 4($sp)
	lw $ra 0($sp)
	add $sp $sp 8
	jr $ra


	
	# subroutine for printing an int to standard output
	# takes int in $a0
_print_int_stdout:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	li $t0 1
	jal _print_int
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra



	# subroutine for printing an int to standard error
	# takes int in $a0
_print_int_stderr:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	li $t0 2
	jal _print_int
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


		
	# generic error handling subroutine
	# error message is passed via $t0
	# takes an error message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_error_handler:
	add $sp $sp -4
	# save $t0 (error message) to the stack
	sw $t0 0($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr
	# print ":"
	lw $a0 0($sp)
	# _String_const_0
	# print error message
	jal _print_string_stderr
	# perform exit system call with status "1"
	li $a0 1
	li $v0 17
	syscall


	
	# out of memory error handling subroutine
	# simply calls _error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_out_of_memory:
	la $t0 _String_const_0
	jal _error_handler
	
	# null pointer error handling subroutine
	# simply calls _error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_null_pointer_error:
	la $t0 _String_const_1
	jal _error_handler

	# null argument error handling subroutine
	# simply calls _error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_null_argument_error:
	la $t0 _String_const_2
	jal _error_handler

	# divide by zero error handling subroutine
	# simply calls _error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_divide_zero_error:
	la $t0 _String_const_3
	jal _error_handler

	# string length error handling subroutine
	# simply calls _error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_string_length_error:
	la $t0 _String_const_4
	jal _error_handler

	# file read error handling subroutine
	# simply calls _error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_read_error:
	la $t0 _String_const_7
	jal _error_handler

	# file write error handling subroutine
	# simply calls _error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_write_error:
	la $t0 _String_const_9
	jal _error_handler

	
		
	# array index error handling subroutine
	# index is passed via $t0
_array_index_error:
	add $sp $sp -4
	# save $t0 (index) to the stack
	sw $t0 0($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr
	# print first part of error message
	la $a0 _String_const_22
	jal _print_string_stderr
	# print index
	lw $a0 0($sp)
	jal _print_int_stderr
	# print second part of error message
	la $a0 _String_const_23
	jal _print_string_stderr
	# perform exit system call with status "1"
	li $a0 1
	li $v0 17
	syscall


	
	# array size error handling subroutine
	# size is passed in $t0
_array_size_error:
	add $sp $sp -4
	# save $t0 (size) to the stack
	sw $t0 0($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr
	# print first part of error message
	la $a0 _String_const_24
	jal _print_string_stderr
	# print size
	lw $a0 0($sp)
	jal _print_int_stderr
	# print second part of error message
	la $a0 _String_const_25
	jal _print_string_stderr
	# perform exit system call with status "1"
	li $a0 1
	li $v0 17
	syscall


	
	# array store error handling subroutine
	# array's type id passed via $t0, assigned type id passed via $t1
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
_array_store_error:
	add $sp $sp -20
	# save $t0 (object's type) to stack	
	sw $t0 0($sp)
	# save $t1 (target type) to stack
	sw $t1 4($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr	
	# print error message
	la $a0 _String_const_26
	jal _print_string_stderr
	# print error message
	la $a0 _String_const_27
	jal _print_string_stderr
	# get assigned type name from class_name_table using id
	la $a0 class_name_table
	lw $t0 4($sp)
	mul $t0 $t0 4
	add $a0 $t0 $a0
	lw $a0 0($a0)
	# print object's type name
	jal _print_string_stderr
	# print error message
	la $a0 _String_const_28
	jal _print_string_stderr
	# get target type name from class_name_table using id
	la $a0 class_name_table
	lw $t1 0($sp)
	mul $t1 $t1 4
	add $a0 $t1 $a0
	lw $a0 0($a0)
	# print target type name
	jal _print_string_stderr
	# print error message
	la $a0 _String_const_21
	jal _print_string_stderr
	# perform exit system call with status "1"
	li $a0 1
	li $v0 17
	syscall


	
	# read file error handling subroutine 
	# read file name is passed via $t0
	# note:	difference between this error and _read_error is that
	# the former is an error with opening the file for reading
	# while the latter is an error during reading
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
_read_file_error:
	add $sp $sp -4
	# save $t0 (read file name) to the stack
	sw $t0 0($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr
	# print first half of error message
	la $a0 _String_const_6
	jal _print_string_stderr
	# get read file name from stack and print it
	lw $a0 0($sp)
	jal _print_string_stderr
	# print second half of error message
	la $a0 _String_const_21
	jal _print_string_stderr
	# perform exit system call with status "1"
	li $a0 1
	li $v0 17
	syscall


	
	# write file error handling subroutine
	# write file name is passed via $t0
	# note:	difference between this error and _write_error is that
	# the former is an error with opening the file for writing
	# while the latter is an error during writing
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
_write_file_error:
	add $sp $sp -4
	# save $t0 (write file name) to the stack
	sw $t0 0($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr
	# print first half of error message
	la $a0 _String_const_8
	jal _print_string_stderr
	# get write file name from the stack and print it
	lw $a0 0($sp)
	jal _print_string_stderr
	# print second half of error message
	la $a0 _String_const_21
	jal _print_string_stderr
	# perform exit system call with status "1"
	li $a0 1
	li $v0 17
	syscall


	
	# substring index error handling subroutine
	# length passed via $t0, beginning index passed via $t1, end index passed via $t2
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
_string_index_error:
	add $sp $sp -16
	# save $t0 (length) to the stack
	sw $t0 0($sp)
	# save $t1 (beginning index) to the stack
	sw $t1 4($sp)
	# save $t2 (end index) to the stack
	sw $t2 8($sp)
	# save string to the stack
	sw $a0 12($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr
	# print error message string
	la $a0 _String_const_5
	jal _print_string_stderr
	# print error message string
	la $a0 _String_const_10
	jal _print_string_stderr
	# print error message string
	la $a0 _String_const_11
	jal _print_string_stderr
	# print string itself
	lw $a0 12($sp)
	jal _print_string_stderr
	# print error message string
	la $a0 _String_const_12
	jal _print_string_stderr
	# print error message string
	la $a0 _String_const_13
	jal _print_string_stderr
	# print string length
	lw $a0 0($sp)
	jal _print_int_stderr
	# print error message string
	la $a0 _String_const_14
	jal _print_string_stderr
	# print beginning index
	lw $a0 4($sp)
	jal _print_int_stderr
	# print error message string
	la $a0 _String_const_15
	jal _print_string_stderr
	# print end index
	lw $a0 8($sp)
	jal _print_int_stderr
	# print error message string
	la $a0 _String_const_20
	jal _print_string_stderr
	# perform exit system call with status "1"
	li $a0 1
	li $v0 17
	syscall


	
	# class cast error handling subroutine
	# object's type id passed via $t0, target type id passed via $t1
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
_class_cast_error:
	add $sp $sp -20
	# save $t0 (object's type) to stack	
	sw $t0 0($sp)
	# save $t1 (target type) to stack
	sw $t1 4($sp)
	# print filename
	move $a0 $a2
	jal _print_string_stderr
	# print ":"
	la $a0 _String_const_19
	jal _print_string_stderr
	# print line number
	move $a0 $a1
	jal _print_int_stderr
	# print error message
	la $a0 _String_const_16
	jal _print_string_stderr
	# print error message
	la $a0 _String_const_17
	jal _print_string_stderr
	# get object's type name from class_name_table using id
	la $a0 class_name_table
	lw $t0 0($sp)
	mul $t0 $t0 4
	add $a0 $t0 $a0
	lw $a0 0($a0)
	# print object's type name
	jal _print_string_stderr
	# print error message
	la $a0 _String_const_18
	jal _print_string_stderr
	# get target type name from class_name_table using id
	la $a0 class_name_table
	lw $t1 4($sp)
	mul $t1 $t1 4
	add $a0 $t1 $a0
	lw $a0 0($a0)
	# print target type name
	jal _print_string_stderr
	# print error message
	la $a0 _String_const_21
	# perform exit system call with status "1"
	jal _print_string_stderr
	li $a0 1
	li $v0 17
	syscall


	
	# clone an object
	# object passed in $a0
	# returns pointer to cloned object
Object.clone:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
	# get size of object
	lw $a0 4($a0)
	# call garbage collecting use _gc_mem_alloc
	# otherwise use _mem_alloc	
	lw $t0 gc_flag
	beq $t0 $zero _label89
	jal _gc_mem_alloc
	b _label90
_label89:
	jal _mem_alloc
_label90:
	# $t1 is a pointer into new memory region
	move $t1 $v0
	# compute end address of original object, put in $t0
	lw $t0 4($s0)
	add $t0 $t0 $s0
	# $t3 is a pointer into the original object
	move $t3 $s0
	# loop over object, copying from original to new location
_label47:
	# if pointer into object is equal to end address then break
	beq $t3 $t0 _label48
	# get the next word from the original object
	lw $t2 0($t3)
	# write it to the new location
	sw $t2 0($t1)
	# update both pointers
	add $t3 $t3 4
	add $t1 $t1 4
	# continue looping
	b _label47
_label48:
	# end of loop
	# epilogue
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 8
	jr $ra



	# compare two objects (does pointer comparison)
	# reference string passed via $a0, parameter string passed on the stack
	# returns boolean (0 or -1) indicating whether objects are equal
Object.equals:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get parameter object
	lw $t0 4($sp)
	# compare with reference object (in $a0)
	beq $a0 $t0 _label56
	li $v0 0
	b _label80
_label56:	
	li $v0 1
_label80:	
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 8
	jr $ra

	

	# get string representation of object (address in memory)
	# object passed in $a0
	# returns string representation of object
Object.toString:
	# prologue
	add $sp $sp -12
	sw $s1 8($sp)
	sw $s0 4($sp)
	sw $ra 0($sp)
	move $s0 $a0
	# body
	# get string representation of dynamic type
	lw $t0 0($a0)
	mul $t0 $t0 4
	la $t1 class_name_table
	add $t0 $t0 $t1
	lw $a0 0($t0)
	# put "@" on stack
	la $t0 _String_const_29
	sw $t0 -4($sp)
	sub $sp $sp 4
	# perform string concatenation
	jal String.concat
	move $s1 $v0
	# convert address to string and push on the stack
	move $a0 $s0
	jal _i2a
	sw $v0 -4($sp)
	sub $sp $sp 4
	# concatenate with string (result is returned)
	move $a0 $s1
	jal String.concat
	# epilogue
	lw $s1 8($sp)
	lw $s0 4($sp)
	lw $ra 0($sp)
	add $sp $sp 12
	jr $ra

	
			
	# compute length of the reference string
	# string passed via $a0	
String.length:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body - loads length field into result register ($v0)
	lw $v0 12($a0)
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# compares if the reference string equals a parameter string
	# note:	the objects do not have to be the same, just the character sequences
	# note also: the parameter has static type Object, but must have type
	# string for this to return true (since strings can't be extended 
	# dynamic types must be identical)
	# reference string passed via $a0, parameter string passed on the stack
	# returns boolean (0 or -1) indicating whether strings are equal
String.equals:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get parameter object
	lw $t0 4($sp)
	# check if parameter object is null, call error subroutine if it is 
	bne $t0 $zero _label51
	jal _null_argument_error
_label51:
	# put true (-1) into result register ($v0), may get set to false (0) later
	li $v0 -1
	# get ID of reference string
	lw $t1 0($a0)
	# get ID of parameter object
	lw $t2 0($t0)
	# compare the IDs
	beq $t1 $t2 _label55
	# if different return false
	li $v0 0
	b _label50
_label55:
	# get length of reference string
	lw $t1 12($a0)
	# get length of parameter string
	lw $t2 12($t0)
	# compare lengths of two strings
	beq $t1 $t2 _label52
	# if different return false
	li $v0 0
	b _label50
_label52:
	# set $t1 to point to reference string char sequence
	add $t1 $a0 16
	# set $t0 to point to parameter string char sequence
	add $t0 $t0 16
_label49:
	# get the next character from the two strings
	lb $t2 0($t1)
	lb $t3 0($t0)
	# compare them, if different then return false
	beq $t2 $t3 _label53
	li $v0 0
	b _label50
_label53:
	# check if the characters are the null byte, if so break
	beq $t2 $zero _label50
	# otherwise increment pointers and continue looping
	add $t0 $t0 1
	add $t1 $t1 1
	b _label49
_label50:
	# end of loop
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 8
	jr $ra



	# get string representation of string (itself)
	# string passed in $a0
	# returns string representation of object
String.toString:
	# move ref argument (string) to result register and return
	move $v0 $a0
	jr $ra

	
		
	# concatenate the reference string with a parameter string
	# reference string passed via $a0, parameter string passed on the stack
	# returns concatenated string 
String.concat:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
	# get size of reference string, put in $t0
	lw $t0 12($s0)
	# get parameter string
	lw $t1 8($sp)
	# check if null, if so call error subroutine
	bne $t1 $zero _label54
	jal _null_argument_error
_label54:
	# get size of parameter string, put in $t1
	lw $t1 12($t1)
	# get total size of concatenated string, put in $t0
	add $t0 $t1 $t0
	# check that length is <= 5000, if not call an error subroutine
	li $t1 5000
	ble $t0 $t1 _label57
	jal _string_length_error
_label57:
	# compute new size in bytes of string (17+length+alignment bytes), put in $t1
	li $t1 17
	add $t1 $t0 $t1
	# find offset from 4
	rem $t2 $t1 4
	# if 0 then already aligned
	beq $t2 $zero _label58
	# otherwise subtract from 4 and add this to total bytes ($t1)
	li $t3 4
	sub $t2 $t3 $t2
	add $t1 $t1 $t2
_label58:
	# build new string in _string_buffer
	la $a0 _string_buffer
	# write size ($t1) to second entry in _string_buffer
	sw $t1 4($a0)
	# write length ($t0) to length field in _string_buffer
	sw $t0 12($a0)
	# clone _string_buffer
	jal Object.clone
	# write characters into cloned sequence
	# $t0 points to reference string char sequence
	add $t0 $s0 16
	# $t0 points to cloned string char sequence	
	add $t1 $v0 16
	# first write reference string characters
	# $t3 is used to run the loop below twice (once
	# to copy the reference string, and once to copy
	# parameter string).  first iteration $t3 is 0,
	# second iteration $t3 is set to 1
	li $t3 0
	# loop over reference string characters
_label59:
	# get next character from reference string
	lb $t2 0($t0)
	# if null byte then break
	beq $t2 $zero _label60
	# otherwise write it to new string
	sb $t2 0($t1)
	# increment pointers and continue looping
	add $t0 $t0 1
	add $t1 $t1 1
	b _label59
_label60:
	# end of loop
	# check if first or second time running loop
	# if second then goto end of subroutine
	bne $t3 $zero _label61
	# otherwise, set $t0 to point into parameter
	# string char sequence
	lw $t0 8($sp)
	add $t0 $t0 16
	# set $t3 to 1 to indicate second time through
	li $t3 1
	# rerun loop
	b _label59
_label61:
	# end of outer loop
	# write null byte to char sequence
	sb $zero 0($t1)
	# epilogue
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 12
	jr $ra


	
	# returns the substring of the reference string
	# reference string passed via $a0, beginning and end indices
	# passed via the stack
	# note:	(like java) beginning index is included and the end
	# index is excluded
String.substring:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
	# get size of reference string, put in $t0
	lw $t0 12($s0)
	# get beginning index, put in $t1
	lw $t1 12($sp)
	# get end index, put in $t2
	lw $t2 8($sp)
	# check that beginning index <= end index <= size
	blt $t1 $zero _label63
	bge $t1 $t0 _label63
	blt $t2 $zero _label63
	bgt $t2 $t0 _label63
	bgt $t1 $t2 _label63
	b _label62
_label63:
	# if we make it here indices were bad, so call error subroutine
	jal _string_index_error
_label62:
	# if we make it here indices are OK
	# subtract the end index from the beginning index, put in $t0 (total number
	# of characters to copy)
	sub $t0 $t2 $t1
	# compute number of bytes to copy (17+length+alignment bytes), put in $t1
	li $t1 17
	add $t1 $t0 $t1
	# get alignment bytes
	# compute offset of 4
	rem $t2 $t1 4
	# if 0 then aligned
	beq $t2 $zero _label64
	# otherwise subtract from 4 and add to total bytes in $t1
	li $t3 4
	sub $t2 $t3 $t2
	add $t1 $t1 $t2
_label64:
	# build new string in _string_buffer
	la $a0 _string_buffer
	# write size ($t1) to second entry
	sw $t1 4($a0)
	# write length to length field
	sw $t0 12($a0)
	# clone string
	jal Object.clone
	# get pointer into new string ($t1) and reference string ($t0)
	add $t1 $v0 16
	add $t0 $s0 16
	# get starting pointer into reference string, i.e., add beginning index
	# to $t0, put it in $t2
	lw $t2 8($sp)
	add $t2 $t2 $t0
	# get end address, i.e., add end index to $t0, put in $t0
	lw $t3 12($sp)
	add $t0 $t3 $t0
	# loop over characters between indices
_label65:
	# if starting pointer ($t0) >= end address ($t2) then break
	bge $t0 $t2 _label66
	# otherwise get next byte from reference string
	lb $t3 0($t0)
	# and write it to next byte in new string
	sb $t3 0($t1)
	# update pointers and continue looping
	add $t0 $t0 1
	add $t1 $t1 1
	b _label65
_label66:
	# end of loop
	# write null byte to end of cloned string
	sb $zero 0($t1)
	# epilogue
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 16
	jr $ra


	
	# Set TextIO reference object to read from stdin
	# reference object passed via $a0	
TextIO.readStdin:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
	# get previous read file descriptor
	lw $a0 12($s0)
	# check if >= 2, if so close file
	li $t0 2
	ble $a0 $t0 _label67
	li $v0 16
	syscall
_label67:
	# write 0 (stdin) to read file descriptor field
	sw $zero 12($s0)
	# epilogue
	lw $ra 4($sp)
	lw $s0 0($sp)
	add $sp $sp 8
	jr $ra


	
	# Set the read file for the reference TextIO object
	# reference TextIO object passed via $a0, read filename parameter passed on the stack
TextIO.readFile:
	# epilogue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# save reference object to $t4 for use later
	move $t4 $a0
	# get previous read file descriptor
	lw $t0 12($t4)
	# check if >= 2, if so close file
	li $t1 2
	ble $t0 $t1 _label68
	li $v0 16
	syscall
_label68:
	# get read filename from stack
	lw $a0 4($sp)
	# check if null, if so then call error subroutine
	bne $a0 $zero _label69
	jal _null_argument_error
_label69:
	# set $a0 to point to filename char sequence 
	add $a0 $a0 16
	# save filename and linenumber to $t2 and $t3 (so we can use $a1 and $a2 to make syscall)
	move $t3 $a1
	move $t2 $a2
	# set $a1 to 0 (i.e., reading file only)
	li $a1 0
	# set $a2 to 0 (unused)
	li $a2 0
	# save $a0 to $t1 (note: result is also placed in $a0 in MipsPilot on an open syscall)
	move $t1 $a0
	# perform open system call
	li $v0 13
	syscall
	# restore $a0
	move $a0 $t1
	# check if <0 return value, indicates an error
	bge $v0 $zero _label70
	# if error then restore filename and linenumber and call _read_file_error
	# _read_file_error also takes the filename string in $t0
	sub $t0 $a0 16
	move $a1 $t3
	move $a2 $t2
	jal _read_file_error
_label70:
	# if we make it here, open worked so write the returned file
	# descriptor to the read file descriptor field in the object
	sw $v0 12($t4)
	# prologue
	lw $ra 0($sp)
	add $sp $sp 8
	jr $ra


	
	# Set TextIO reference object to read from stdin
	# reference object passed via $a0	
TextIO.writeStdout:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
	# get previous write file descriptor
	lw $a0 16($s0)
	# check if >= 2, if so close file
	li $t0 2
	ble $a0 $t0 _label71
	li $v0 16
	syscall
_label71:
	# write 1 (stdout) to write file descriptor field
	li $t0 1
	sw $t0 16($s0)
	# epilogue
	lw $ra 4($sp)
	lw $s0 0($sp)
	add $sp $sp 8
	jr $ra


	
	# Set TextIO reference object to read from stdin
	# reference object passed via $a0	
TextIO.writeStderr:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
	# get previous write file descriptor
	lw $a0 16($s0)
	# check if >= 2, if so close file
	li $t0 2
	ble $a0 $t0 _label72
	li $v0 16
	syscall
_label72:
	# write 2 (stderr) to write file descriptor field
	li $t0 2
	sw $t0 16($s0)
	# epilogue
	lw $ra 4($sp)
	lw $s0 0($sp)
	add $sp $sp 8
	jr $ra


	
	# Set the write file for the reference TextIO object
	# reference TextIO object passed via $a0, write filename parameter passed on the stack
TextIO.writeFile:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# save reference object to $t4 for use later
	move $t4 $a0
	# get previous write file descriptor	
	lw $t0 16($t4)
	# check if >= 2, if so close file
	li $t1 2
	ble $t0 $t1 _label73
	li $v0 16
	syscall
_label73:
	# get write filename from stack, check if null
	lw $a0 4($sp)
	bne $a0 $zero _label74
	# if null then call error subroutine
	jal _null_argument_error
_label74:
	# set $a0 to char sequence in write filename string
	add $a0 $a0 16
	# save filename and linenumber to $t2 and $t3 (so we can use $a1 and $a2 to make syscall)
	move $t3 $a1
	move $t2 $a2
	# set $a1 (flags) to 577 or O_WRONLY|O_CREAT|O_TRUNC, which is 1|64|512 on most Linux systems
	# (FIXME: may fail if these values are different)
	# in other words we create a new file for writing (wiping out any existing file)
	li $a1 577
	# set $a2 (mode, which indicates permissions when creating a file) to 493 or 
	# S_IRUSR|S_IWUSR|S_IXUSR|S_IRGRP|S_IXGRP|S_IROTH|S_IXGRP, which is 
	# 256|128|64|32|8|4|1 on most Linux systems
	# (FIXME: may fail if these values are different)
	# in other words we set permissions to rwxr-xr-x
	li $a2 493
	# save $a0 to $t1 (note: result is also placed in $a0 in MipsPilot on an open syscall)
	move $t1 $a0
	# perform open syscall
	li $v0 13
	syscall
	# restore $a0
	move $a0 $t1
	# check if returned <,0 indicates an error
	bge $v0 $zero _label75
	# if error then restore filename and linenumber and call _write_file_error
	# _write_file_error also takes the filename string in $t0
	sub $t0 $a0 16
	move $a2 $t2
	move $a1 $t3
	jal _write_file_error
_label75:
	# set write file descriptor field
	sw $v0 16($t4)
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 8
	jr $ra


	
	# write a string to the current write file
	# reference TextIO object passed via $a0, string passed via the stack
	# returns the reference object
TextIO.putString:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
#----- DJS: added code -----
    # put pointer to string in $a0
    lw $a0 8($sp)
    la $a0 16($a0)
    li $v0 4
    syscall
#------ DJS: removed code ----
	# get the write file descriptor
#	lw $a0 16($a0)
	# put string to print on the stack
#	lw $t0 8($sp)
#	sw $t0 -4($sp)
#	add $sp $sp -4
	# call _write_string_file to write the string to the appropriate file	
#	jal _write_string_file
#----------------------------
	# set return register ($v0) to reference object ($s0)
	move $v0 $s0
	# epilogue
	lw $ra 4($sp)
	lw $s0 0($sp)
	add $sp $sp 12
	jr $ra


	
	# write an int to the current write file
	# reference TextIO object passed via $a0, int passed via the stack
	# returns the reference object
TextIO.putInt:
	# prologue
	add $sp $sp -8
	sw $ra 4($sp)
	sw $s0 0($sp)
	move $s0 $a0
	# body
	# get the int from the stack
	lw $a0 8($sp)
#----- DJS: added code ------
    li $v0 1
    syscall
#----- DJS: removed code ------
	# call _i2a to convert int to a string
#	jal _i2a
	# move string to the stack
#	sw $v0 -4($sp)
#	add $sp $sp -4
	# get the write file descriptor
#	lw $a0 16($s0)
	# call _write_string_file to write the string to the appropriate file
#	jal _write_string_file
#---------------------------
	# set return register ($v0) to reference object ($s0)
	move $v0 $s0
	# epilogue
	lw $s0 0($sp)
	lw $ra 4($sp)
	add $sp $sp 12
	jr $ra
	
	
				
	# read a string from the current read file
	# note:	actually reads a character at a time until '\n' or '\0' or 256 chars reached
	# reference TextIO object passed via $a0
	# returns the read string
TextIO.getString:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get the read file descriptor
	lw $a0 12($a0)
	# call _read_string_file to read the string from the appropriate file
	jal _read_string_file
	# string returned in $v0 so we can just return
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# read an int from the current read file
	# actually reads a string and interprets it as an int, if not an int then returns 0
	# note:	reads a character at a time until '\n' or '\0' or 256 chars reached
	# reference TextIO object passed via $a0
	# returns the read int
TextIO.getInt:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get the read file descriptor
	lw $a0 12($a0)
	# call _read_string_file read the string containing the int from the appropriate file
	jal _read_string_file
	# call _a2i to convert string to int and return int
	move $a0 $v0
	add $a0 $a0 16
	jal _a2i
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# exits the program
	# takes status value from stack
	# note: no prologue/epilogue since doesn't return	
Sys.exit:
	# get status value from stack, put in $a0
	lw $a0 0($sp)
	# perform exit system call
	li $v0 17
	syscall


	
	# get the current time as seconds since 1970 UTC
	# note:	performs system call that is not currently supported in Spim
	#       must use modified version of Spim provided with Bantam
	# returns the time
Sys.time:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# perform time system call
	# DJS changed the next line from "li $v0 18" to conform to MARS syscalls
	li $v0 30
	syscall
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# get a random 32-bit integer
	# returns a random integer
Sys.random:
	# prologue
	add $sp $sp -4
	sw $ra 0($sp)
	# body
	# get previous random number from memory
	la $t0 _random
	lw $v0 0($t0)
	# compute new random number using formula:
	# next = (next * 1103515245 + 12345) & 0x7fffffff
	# (note: not the best way to generate pseudo-random numbers)
	li $t1 1103515245
	mul $v0 $v0 $t1
	li $t1 12345
	addu $v0 $v0 $t1
	li $t1 0x7fffffff
	and $v0 $v0 $t1
	# save next random number to memory
	sw $v0 0($t0)
	# will return this value as well
	# epilogue
	lw $ra 0($sp)
	add $sp $sp 4
	jr $ra


	
	# initial subroutine that runs the program
	# creates a Main object and then dispatches to Main.main
	.globl __start
__start:
	# set id in _string_buffer - use String_template to get string id
	la $t0 String_template
	lw $t0 0($t0)
	la $t1 _string_buffer
	sw $t0 0($t1)
	
	# if gc_flag is set then must call _gc_init to initialize garbage
	# collection data structures and memory
	lw $t0 gc_flag
	beq $t0 $zero _label88
	jal _gc_init
_label88:

	# seed the random number generator by setting the
	# next value to the current time
	jal Sys.time
	la $t0 _random
	sw $v0 0($t0)

	# create a new Main object using the Main template and Object.clone
	la $a0 Main_template
	jal Object.clone
	move $a0 $v0
	# initialize the new Main object
	jal Main_init
	move $a0 $v0
	# call the main method within Main
	jal Main.main
	# exit the program returning "0" for success
	li $a0 0
	li $v0 17
	syscall


	
	.globl __eoth
__eoth:
