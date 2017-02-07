# Part of the Bantam Compiler Toolset
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
	# a static String string_buffer and cloning this string.

	# Sys details
	#
	# Because the time() system call writes the result to memory,
	# some static memory must be set aside for holding this value
	# (_current_time).  When Sys.time is called, the OS writes
	# the result to this location, and then the method immediately
	# copies it to the accumulator.  In addition, _random is used
	# by Sys to store the next number in a list of pseudo-randomly
	# generated values.
	
	# Line numbering and filename
	#
	# Because x86 has a limited register set, the current user line 
	# number and filename are stored in static memory rather than
	# registers.  These must be updated by user code every time
	# a dispatch occurs, in case this results in an error.  In addition,
	# these should also be set if an error routine is called by user
	# compiled code.	

	# data structures needed by the runtime system
	.data
	
	# Some string constants needed by the runtime system
	# (mostly for error handling)
	# we don't have to define these as String objects, however,
	# it makes things more consistent since program character
	# sequences are String objects
String_const_0:
	.long	1
	.long	48
	.long	String_dispatch_table
	.long	30
	.ascii	":runtime error: out of memory\n"
	.byte	0
	.align	2
String_const_1:
	.long	1
	.long	60
	.long	String_dispatch_table
	.long	40
	.ascii	":runtime error: null pointer referenced\n"
	.byte	0
	.align	2
String_const_2:
	.long	1
	.long	60
	.long	String_dispatch_table
	.long	40
	.ascii	":runtime error: string argument is null\n"
	.byte	0
	.align	2
String_const_3:
	.long	1
	.long	48
	.long	String_dispatch_table
	.long	31
	.ascii	":runtime error: divide by zero\n"
	.byte	0
	.align	2
String_const_4:
	.long	1
	.long	72
	.long	String_dispatch_table
	.long	53
	.ascii	":runtime error: concatenated string too long (>5000)\n"
	.byte	0
	.align	2
String_const_5:
	.long	1
	.long	52
	.long	String_dispatch_table
	.long	33
	.ascii	":runtime error: bad string index\n"
	.byte	0
	.align	2
String_const_6:
	.long	1
	.long	56
	.long	String_dispatch_table
	.long	38
	.ascii	":runtime error: can't read from file '"
	.byte	0
	.align	2
String_const_7:
	.long	1
	.long	68
	.long	String_dispatch_table
	.long	51
	.ascii	":runtime error: I/O error while attempting to read\n"
	.byte	0
	.align	2
String_const_8:
	.long	1
	.long	56
	.long	String_dispatch_table
	.long	37
	.ascii	":runtime error: can't write to file '"
	.byte	0
	.align	2
String_const_9:
	.long	1
	.long	72
	.long	String_dispatch_table
	.long	52
	.ascii	":runtime error: I/O error while attempting to write\n"
	.byte	0
	.align	2
String_const_10:
	.long	1
	.long	116
	.long	String_dispatch_table
	.long	99
	.ascii	"Start index must be >= 0 and < string length\nEnd index must be >= start index and <= string length\n"
	.byte	0
	.align	2
String_const_11:
	.long	1
	.long	28
	.long	String_dispatch_table
	.long	9
	.ascii	"String: \""
	.byte	0
	.align	2
String_const_12:
	.long	1
	.long	20
	.long	String_dispatch_table
	.long	2
	.ascii	"\"\n"
	.byte	0
	.align	2
String_const_13:
	.long	1
	.long	28
	.long	String_dispatch_table
	.long	8
	.ascii	"Length: "
	.byte	0
	.align	2
String_const_14:
	.long	1
	.long	32
	.long	String_dispatch_table
	.long	15
	.ascii	", start index: "
	.byte	0
	.align	2
String_const_15:
	.long	1
	.long	32
	.long	String_dispatch_table
	.long	13
	.ascii	", end index: "
	.byte	0
	.align	2
String_const_16:
	.long	1
	.long	52
	.long	String_dispatch_table
	.long	35
	.ascii	":runtime error: illegal class cast\n"
	.byte	0
	.align	2
String_const_17:
	.long	1
	.long	48
	.long	String_dispatch_table
	.long	30
	.ascii	"Can't convert object of type '"
	.byte	0
	.align	2
String_const_18:
	.long	1
	.long	28
	.long	String_dispatch_table
	.long	11
	.ascii	"' to type '"
	.byte	0
	.align	2
String_const_19:
	.long	1
	.long	20
	.long	String_dispatch_table
	.long	1
	.ascii	":"
	.byte	0
	.align	2
String_const_20:
	.long	1
	.long	20
	.long	String_dispatch_table
	.long	1
	.ascii	"\n"
	.byte	0
	.align	2
String_const_21:
	.long	1
	.long	20
	.long	String_dispatch_table
	.long	2
	.ascii	"'\n"
	.byte	0
	.align	2
String_const_22:
	.long	1
	.long	32
	.long	String_dispatch_table
	.long	14
	.ascii	"<Unknown file>"
	.byte	0
	.align	2
String_const_23:
	.long	1
	.long	48
	.long	String_dispatch_table
	.long	29
	.ascii	":runtime error: array index '"
	.byte	0
	.align	2
String_const_24:
	.long	1
	.long	36
	.long	String_dispatch_table
	.long	16
	.ascii	"' out of bounds\n"
	.byte	0
	.align	2
String_const_25:
	.long	1
	.long	56
	.long	String_dispatch_table
	.long	36
	.ascii	":runtime error: illegal array size '"
	.byte	0
	.align	2
String_const_26:
	.long	1
	.long	60
	.long	String_dispatch_table
	.long	41
	.ascii	"' (must be >=0 and <=1500 if GC enabled)\n"
	.byte	0
	.align	2
String_const_27:
	.long	1
	.long	52
	.long	String_dispatch_table
	.long	34
	.ascii	":runtime error: array store error\n"
	.byte	0
	.align	2
String_const_28:
	.long	1
	.long	48
	.long	String_dispatch_table
	.long	29
	.ascii	"Can't assign object of type '"
	.byte	0
	.align	2
String_const_29:
	.long	1
	.long	48
	.long	String_dispatch_table
	.long	31
	.ascii	"' to element in array of type '"
	.byte	0
	.align	2
String_const_30:
	.long	1
	.long	20
	.long	String_dispatch_table
	.long	1
	.ascii	"@"
	.byte	0
	.align	2

	# data structures needed by the runtime system
	.globl _current_filename_ptr
	.globl _current_line_number
	# _current_filename_ptr is a pointer to a string representing
	# the current filename - used for error handling
_current_filename_ptr:
	.long	String_const_22
	# _current_line_number is the current line number - used for error
	# handling
_current_line_number:
	.long	-1
	# _current_time is used for retrieving the current time (in seconds)
	# from the operating system
_current_time:
	.long	0
	# _random is used for holding pseudo random numbers
_random:
	.long	1
	# string_buffer is used for building storing new strings
	# the data is overwritten in string_buffer and then Object.clone
	# is used to copy the string to a new location in memory
	# used by methods in String and TextIO
string_buffer:
	.long	0
	.long	284
	.long	String_dispatch_table
	.long	256
	.space	257
	.align	2
	
	# data structures needed by the garbage collector
	# gc_free_ptr is a pointer into the free list of available memory
	# regions, initialized by gc_init
gc_free_ptr:
	.long	0
	# gc_heap_start is a pointer into the start of the heap ($gp 
	# points to the end of the heap), initialized by gc_init
	# needed for walking the heap during sweeping phase
gc_heap_start:
	.long	0
	# gc_stack_start points to the start of the stack ($sp points 
	# to the end of the stack), initialized by gc_init
	# note:	stack boundaries are needed because garbage collector
	# does not maintain a root set, instead it searches the stack
	# for possible heap pointers
gc_stack_start:
	.long	0
	# heap_ptr is a pointer to the current end of the heap
	# (note: must go last in static data section!)
heap_ptr:
	.long	0

	
		
	.text
	
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
	
	.globl	main
	# initial subroutine that runs the program
	# creates a bantam.Main object and then dispatches to bantam.Main.main
main:
	# set id in string_buffer - use String_template to get string id
	movl $String_template,%ebx
	movl 0(%ebx),%ebx
	movl $string_buffer,%ecx
	movl %ebx,0(%ecx)
	
	# set starting heap pointer	
	movl $heap_ptr,%ebx
	movl $4,%eax
	add %ebx,%eax
	movl %eax,0(%ebx)
	# check if gc enabled - if so call gc_init
	movl $gc_flag,%ebx
	movl 0(%ebx),%ebx
	movl $0,%ecx
	cmpl %ebx,%ecx
	je label89
	call gc_init
label89:

	# seed the random number generator by setting the
	# next value to the current time
	call Sys.time
	movl $_random,%ebx
	movl %eax,0(%ebx)

	# create the bantam.Main object
	movl $Main_template,%eax
	# clone it
	call Object.clone
	# initialize it
	call Main_init
	# call main method
	call bantam.Main.main
	# exit with status "0"
	movl $0,%ebx
	movl $1,%eax
	int $0x80
	
	# allocate a new object (called when garbage collection is disabled)
mem_alloc:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save size in esi
	movl %eax,%esi
	# get heap pointer
	movl $heap_ptr,%ecx
	movl 0(%ecx),%eax
	# adjust heap pointer by size allocated (which is in esi)
	movl %eax,%ebx
	add %esi,%ebx
	# write modified heap pointer to memory
	movl %ebx,0(%ecx)
	# save original heap pointer in this register, will be the return value
	movl %eax,%esi
	# perform system call to brk
	movl $45,%eax
	int $0x80
	# check if pointer to new memory is 0 (i.e., out of memory)
	movl $0,%ebx
	cmpl %ebx,%eax
	jne label3
	call out_of_memory
label3:
	# restore return value
	movl %esi,%eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# initialize the garbage collector data structures
	# only called if garbage collector is enabled
gc_init:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# make heap pointer page aligned, do this by allocating some space at the beginning
	# that never gets used
	movl $heap_ptr,%eax
	movl 0(%eax),%eax
	movl %eax,%ebx
	movl $-8192,%ecx
	and %ecx,%eax
	movl $8192,%ecx
	add %ecx,%eax
	sub %ebx,%eax
	call mem_alloc
	# set the start heap pointer using the current heap pointer
	movl $heap_ptr,%ebx
	movl 0(%ebx),%ebx
	movl $gc_heap_start,%ecx
	movl %ebx,0(%ecx)
	# set the stack start
	movl $gc_stack_start,%ecx
	movl %esp,0(%ecx)
	# allocate the first page, pass 0 to gc_alloc_page since no object has yet been allocated
	movl $0,%eax
	call gc_alloc_page
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# allocate a new object in the heap, updates the free list to reflect
	# this allocation
	# object size is passed via eax
gc_mem_alloc:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# save memory size to stack
	pushl %eax
	# try to large enough find free region
	call gc_find_free
	popl %edx
	movl $0,%ebx
	cmpl %ebx,%eax
	# check if region found
	jne label18
	# if not must call collector
	movl %edx,%eax
	call gc_collect
label18:
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# find next free region of memory, traverses free list to find region
	# returns 0 if no such region found
	# size of region passed in eax
gc_find_free:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# save esi (callee-saved) to the stack
	pushl %esi
	# save argument to esi
	movl %eax,%esi
	# get free pointer, put in accumulator
	movl $gc_free_ptr,%eax
	movl 0(%eax),%eax
	# edx is a "previous" pointer used to potentially remove a free block
	movl $0,%edx
	# put 0 in edi for use in comparisons
	movl $0,%edi
	# loop to get free block
label19:
	# check if null, if null goto end of subroutine
	cmpl %edi,%eax
	je label23
	# load the size into ebx
	movl 0(%eax),%ebx
	# check if size is large enough (using first fit)
	cmpl %esi,%ebx
	jge label20
	# update "previous" pointer (edx)
	movl %eax,%edx
	# continue to next free block
	movl 4(%eax),%eax
	# continue looping
	jmp label19
	# end of loop
label20:
	# compute the size of the minimum free block in order to maintain
	# a free portion: object size + min. free block size (3 words)	
	movl $12,%edi
	movl %esi,%ecx
	add %edi,%ecx
	# see if there is enough room left for another free block
	cmpl %ecx,%ebx
	jge label21
	# remove free block -- too small (may be put back in free list later during collection)
	movl 4(%eax),%ebx
	# is this the first block in the list or not?
	movl $0,%edi
	cmpl %edi,%edx
	je label22
	# if not first block then set next pointer in previous block to current block's next pointer
	movl %ebx,4(%edx)
	jmp label23
label22:
	# if first block then free list pointer to current block's next pointer
	movl $gc_free_ptr,%edx
	movl %ebx,0(%edx)
	jmp label23
label21:
	# save old pointer to free block
	movl %eax,%ecx
	# get new size
	sub %esi,%ebx
	# get pointer to newly allocated block
	add %ebx,%eax
	# update size in free block (note: next pointer is unchanged)
	movl %ebx,0(%ecx)
label23:
	# restore esi (callee-saved) from the stack
	popl %esi
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# allocate a new page for the garbage collector
	# takes a memory region size via eax
	# the page is split into free space and allocated space
	# based on eax
gc_alloc_page:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# push argument onto the stack
	pushl %eax
	# allocate a new garbage collection page
	movl $8192,%eax
	call mem_alloc
	# zero out the bit vector
	movl %eax,%ebx
	movl $256,%ecx
	add %ecx,%eax
label24:
	cmpl %eax,%ebx
	jge label25
	movl $0,%edx
	movl %edx,0(%ebx)
	movl $4,%edx
	add %edx,%ebx
	jmp label24
label25:
	# pop argument from the stack
	popl %edx
	# get pointer into free portion of block
	movl %eax,%ebx
	add %edx,%ebx
	# get leftover size
	movl $7936,%ecx
	sub %edx,%ecx
	# update free list
	movl %ecx,0(%ebx)
	movl $gc_free_ptr,%edx
	movl 0(%edx),%edi
	movl %edi,4(%ebx)
	movl %ebx,0(%edx)
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# garbage collect heap
	# allocation size passed in eax
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
gc_collect:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# save argument to the stack
	# perform marking
	pushl %eax
	call gc_mark
	# perform sweeping
	call gc_sweep
	# if freed more than the page size, then don't need to allocate a page
	# otherwise, we do	
	movl $8192,%ebx
	cmpl %ebx,%eax
	jl label26
	movl 0(%esp),%eax
	call gc_find_free
	movl $0,%edx
	cmpl %edx,%eax
	jne label27
label26:
	movl 0(%esp),%eax
	call gc_alloc_page
label27:
	popl %ebx
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# mark all reachable objects
	# doesn't use a root set, instead walks stack
	# conservatively looking for heap pointers
gc_mark:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# get start of the stack
	movl $gc_stack_start,%ebx
	movl 0(%ebx),%ebx
	pushl %ebx
	# walk the stack looking for pointers to objects in the heap
label28:
	cmpl %esp,%ebx
	je label29
	# get next address
	movl 0(%esp),%ebx
	movl 0(%ebx),%eax
	# check if it is a potential heap address
	call gc_check_addr
	movl $0,%edx
	cmpl %edx,%eax
	je label30
	# if it is, then mark it
	movl 0(%esp),%ebx
	movl 0(%ebx),%eax
	call gc_mark_addr
	# and find all other addresses reachable from it
	movl 0(%esp),%ebx
	movl 0(%ebx),%eax
	call gc_find_reachable
label30:
	# otherwise continue walking the stack
	movl 0(%esp),%ebx
	movl $4,%edx
	sub %edx,%ebx
	movl %ebx,0(%esp)
	jmp label28
label29:
	# end of loop
	popl %ebx
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# check to see if address is a possible heap pointer
	# takes as address in eax
	# checks the following:
	#   heap start <= address <= heap end
	#   address is word-aligned
	#   address is not within page bit vector
	#   object id (at 0(address)) is valid (>= 0)
	#   object size (at 4(address)) is valid (between 12 and max size)
	#   object size is word aligned
	#   object is not already marked
gc_check_addr:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# set result to false
	movl $0,%edi
	# is the address in the heap?
	movl $gc_heap_start,%ebx
	movl 0(%ebx),%ebx
	cmpl %ebx,%eax
	jl label31
	movl $heap_ptr,%edx
	movl 0(%edx),%edx
	movl $12,%ebx
	sub %ebx,%edx
	cmpl %edx,%eax
	jg label31
	# is the address word aligned?
	movl $3,%edx
	movl %eax,%ebx
	and %edx,%ebx
	movl $0,%edx
	cmpl %edx,%ebx
	jne label31
	# is this address in the mark bit vector (if so, must not be a valid heap address) 
	movl $8191,%edx
	movl %eax,%ebx
	and %edx,%ebx
	movl $256,%ecx
	cmpl %ecx,%ebx
	jl label31
	# is the id in the object valid (should be >= 0)?
	movl 0(%eax),%ebx
	movl $0,%edx
	cmpl %edx,%ebx
	jl label31
	# is the size in object valid (should be >= 0 and <= max_size and word aligned)?
	movl 4(%eax),%ebx
	movl $12,%ecx
	cmpl %ecx,%ebx
	jl label31
	movl $7936,%ecx
	cmpl %ecx,%ebx
	jg label31
	movl $3,%edx
	and %edx,%ebx
	movl $0,%edx
	cmpl %edx,%ebx
	jne label31
	# is this object already marked?
	call gc_is_marked
	movl $0,%edx
	cmpl %edx,%eax
	je label32
	movl $0,%edi
	jmp label31
label32:
	# if we make it here then we assume this is a pointer to an unmarked object so
	# we set result to 1 (note: false positives can occur)
	movl $1,%edi
label31:
	movl %edi,%eax
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# check if address is marked
	# address is passed via eax
	# returns 1 if marked, 0 otherwise
gc_is_marked:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# get page address, put in ebx
	movl %eax,%ebx
	movl $-8192,%edx
	and %edx,%ebx
	# get page index, put in edx
	movl %eax,%edx
	movl $8191,%ecx
	and %ecx,%edx
	# get word number, put in edx (note: shift implicitly uses ecx as shift amount)
	movl $2,%ecx
	shr %edx
	# get address in bit vector, put in edi (note: shift implicitly uses ecx as shift amount)
	movl %edx,%edi
	movl $3,%ecx
	shr %edi
	movl $-4,%edx
	and %edx,%edi
	add %ebx,%edi
	# get bit vector word, put in edi
	movl 0(%edi),%edi
	# get bit number within bit vector word, put in edx
	movl $31,%ebx
	and %ebx,%edx
	# get bit within bit vector, put in edi (note: shift implicitly uses ecx as shift amount)
	movl %edx,%ecx
	shl %edi
	movl $31,%ecx
	shr %edi
	# return the bit
	movl %edi,%eax
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# mark an address
	# address is passed via eax
gc_mark_addr:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# get page address, put in ebx
	movl %eax,%ebx
	movl $-8192,%edx
	and %edx,%ebx
	# get page index, put in edx
	movl %eax,%edx
	movl $8191,%ecx
	and %ecx,%edx
	# get word number, put in edx (note: shift implicitly uses ecx as shift amount)
	movl $2,%ecx
	shr %edx
	# get address in bit vector, put in edi (note: shift implicitly uses ecx as shift amount)
	movl %edx,%edi
	movl $3,%ecx
	shr %edi
	movl $-4,%edx
	and %edx,%edi
	add %ebx,%edi
	# get bit vector word, put in ebx
	movl 0(%edi),%ebx
	# get bit number within bit vector word, put in ecx
	movl $31,%ecx
	and %ebx,%ecx
	# set bit within bit vector (note: shift implicitly uses ecx as shift amount)
	movl $-2147483648,%edx
	shr %edx
	or %edx,%ebx
	# write word back
	movl %ebx,0(%edi)
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# find all reachable addresses from a (potential) heap address
	# uses a depth-first algorithm
	# address passed via eax
gc_find_reachable:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# compute end pointer, put in ebx
	movl 4(%eax),%ebx
	add %eax,%ebx
	# compute pointer into fields, put in edx
	movl $12,%edx
	add %eax,%edx
	# save ebx, edx to stack
	pushl %ebx
	pushl %edx
	# loop through fields looking for unmarked objects
label33:
	movl 0(%esp),%edx
	movl 4(%esp),%ebx
	# if at end pointer then break
	cmpl %ebx,%edx
	je label34
	# call gc_check_addr to check if object on the heap
	movl %edx,%eax
	call gc_check_addr
	# check if gc_check_addr returned 0 (not an umarked heap object) or 1 (unmarked heap object)
	movl $0,%ebx
	cmpl %ebx,%eax
	je label35
	# if we make it here this is an unmarked object; mark it and then recursively find reachable
	movl 0(%esp),%eax
	call gc_mark_addr
	# call gc_find_reachable to recursively find reachable objects from this object
	movl 0(%esp),%eax
	call gc_find_reachable
label35:
	# increment field pointer (edx)
	movl $4,%edi
	movl 0(%esp),%edx
	add %edi,%edx
	movl %edx,0(%esp)
	jmp label33
label34:
	# pop stack values (can throw them away)
	popl %ebx
	popl %ebx
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# sweep the heap, freeing unreachable memory
	# returns the total amount freed
gc_sweep:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# ebx represents the total freed size (starts at 0)
	movl $0,%ebx
	# remove old free list (will be re-created during sweeping)
	movl $gc_free_ptr,%ecx
	movl %ebx,0(%ecx)
	# get start of heap (ecx)
	movl $gc_heap_start,%ecx
	movl 0(%ecx),%ecx
	# get end of heap (edx)
	movl $heap_ptr,%edx
	movl 0(%edx),%edx
	# loop over the entire heap, one page at a time
label36:
	cmpl %edx,%ecx
	jge label37
	movl %ecx,%eax
	# save ebx, ecx, edx
	pushl %ebx
	pushl %ecx
	pushl %edx
	# sweep the next page
	call gc_sweep_page
	# restore ebx, ecx, edx
	popl %edx
	popl %ecx
	popl %ebx
	# update freed size
	add %eax,%ebx
	movl $8192,%edi
	add %edi,%ecx
	jmp label36
label37:
	# set return value
	movl %ebx,%eax
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# sweep the next page
	# returns the amount of memory freed
gc_sweep_page:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# set page pointer (esp+4*word_size) to page_address + size of bit vector
	movl $256,%ebx
	add %eax,%ebx
	pushl %ebx
	# set end pointer (esp+3*word_size) to page_address + page_size
	movl $8192,%ecx
	add %eax,%ecx
	pushl %ecx
	# set free block addr (esp+2*word_size) to page pointer (esp+4*word_size)
	pushl %ebx
	# set total freed size (esp+word_size) to 0
	movl $0,%edx
	pushl %edx
	# set free block size (esp+0) to 0
	pushl %edx
	# loop through page (data only, not bit vector)
label38:
	# if we reach the end of the page then break
	movl 16(%esp),%eax
	movl 12(%esp),%ebx
	cmpl %ebx,%eax
	jge label39
	# otherwise call gc_is_marked to see if the current word is marked
	call gc_is_marked
	movl $0,%edx
	cmpl %edx,%eax
	je label40
	# if marked then call gc_next_free_addr
	movl 16(%esp),%eax
	call gc_next_free_addr
	# update address, skipping over marked data
	movl %eax,16(%esp)
	# check if need to add free block
	movl $12,%ebx
	movl 0(%esp),%ecx
	cmpl %ebx,%ecx
	jl label41
	# if we make it here then add free block
	movl 8(%esp),%edi
	movl %ecx,0(%edi)
	movl $gc_free_ptr,%ebx
	movl 0(%ebx),%ecx
	movl %ecx,4(%edi)
	movl %edi,0(%ebx)
	# increment the total freed size
	movl 4(%esp),%ebx
	movl 0(%esp),%ecx
	add %ecx,%ebx
	movl %ebx,4(%esp)
label41:
	# update free block address and size
	movl 16(%esp),%ebx
	movl %ebx,8(%esp)
	movl $0,%edx
	movl %edx,0(%esp)
	# continue looping
	jmp label38
label40:
	# end of loop
	# if not marked then simply increment address
	movl 16(%esp),%ebx
	movl $4,%ecx
	add %ecx,%ebx
	movl %ebx,16(%esp)
	# increment free block size
	movl 0(%esp),%ebx
	add %ecx,%ebx
	movl %ebx,0(%esp)
	# continue looping
	jmp label38
label39:
	# end of loop
	# zero out bit vector
	movl 12(%esp),%ebx
	movl $8192,%ecx
	sub %ecx,%ebx
	movl %ebx,%ecx
	movl $256,%edi
	add %edi,%ecx
	movl $4,%edi
	movl $0,%edx
label42:
	cmpl %ecx,%ebx
	jge label43
	movl %edx,0(%ebx)
	add %edi,%ebx
	jmp label42
label43:
	# pop off stack values, second pop holds return value (others can be thrown away)
	popl %ebx
	popl %eax
	popl %ebx
	popl %ebx
	popl %ebx
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# get next free address
	# address is passed via eax
	# normally, this is the object address + size of object, however,
	# because of false positives (the garbage collector is conservative)
	# one marked object can overlap with another.  this subroutine finds
	# all such overlaps and returns the last address, which is not a
	# part of any preceeding object
gc_next_free_addr:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body	
	# save the current pointer (esp+2*word_size)
	pushl %eax
	# save the end pointer (esp+word_size)
	movl %eax,%ebx
	movl $4,%ecx
	add %ecx,%ebx
	pushl %ebx
	# save the end page address (esp+0)
	movl $-8192,%ebx
	and %ebx,%eax
	movl $8192,%ebx
	add %ebx,%eax
	pushl %eax
	# loop to find next free address
label44:
	# if current pointer >= end of page then break
	movl 8(%esp),%eax
	movl 0(%esp),%ebx
	cmpl %ebx,%eax
	jge label45
	# if current pointer >= end pointer then break
	movl 4(%esp),%ebx
	cmpl %ebx,%eax
	jge label45
	# otherwise see if current pointer is marked
	call gc_is_marked
	movl $0,%ebx
	cmpl %ebx,%eax
	je label46
	# get end pointer of marked object
	movl 8(%esp),%ebx
	movl 4(%ebx),%ecx
	add %ebx,%ecx
	# see if this larger than previous end pointer
	movl 4(%esp),%ebx
	cmpl %ebx,%ecx
	jle label46
	# check if larger than end of page
	movl 0(%esp),%ebx
	cmpl %ebx,%ecx
	jge label47
	# if we make it here, then it is larger so update end pointer
	movl %ecx,4(%esp)
	jmp label46
label47:
	# if we make it here, then it is larger but goes over end of page, so set to the end of the page	
	movl %ebx,4(%esp)
	jmp label45
label46:
	# increment pointer and continue looping
	movl 8(%esp),%ebx
	movl $4,%ecx
	add %ecx,%ebx
	movl %ebx,8(%esp)
	jmp label44
label45:
	# end of loop
	# pop stack values, second value is the return value (others can be thrown away)	
	popl %ebx
	popl %eax
	popl %ebx
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret

	# subroutine for converting a string to an int
	# if string contains any non-int characters then it returns 0
	# note:	cannot handle ints expressed in hexadecimal or scientific notation		
a2i:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body	
	movl %eax,%esi
	# note: need %esi to point to character sequence (since lodsb uses esi)
	movl $16,%ebx
	add %ebx,%esi
	# ebx will hold the resulting integer
	movl $0,%ebx
	# put 0 to indicate result is positive (may be set to 1 later)
	movl $0,%edi
	# push flag on stack
	pushl %edi
	# get first character
	movl $0,%eax
	lodsb
	# check if first character is '-' (45)	
	movl $45,%edi
	cmpl %edi,%eax
	jne label7
	# set flag to indicate result must be negative
	movl $1,%edi
	movl %edi,0(%esp)
	jmp label4
label7:
	# if not negative must decrement pointer so we don't skip over first character	
	movl $1,%edi
	sub %edi,%esi
	# loop over string
label4:
	# load the next character
	movl $0,%eax
	lodsb
	# check if value is 0 or newline character (10), if so break
	movl $0,%edi
	cmpl %edi,%eax
	je label5
	movl $10,%edi
	cmpl %edi,%eax
	je label5
	# check if value is digit (48-57), if not, set result to 0 and break
	movl $57,%edi
	cmpl %edi,%eax
	jle label8
	movl $0,%ebx
	jmp label5
label8:
	movl $48,%edi
	cmpl %edi,%eax
	jge label9
	movl $0,%ebx
	jmp label5
label9:
	movl $48,%edi
	# convert character to int and add to result
	sub %edi,%eax
	movl %eax,%ecx
	movl %ebx,%eax
	movl $10,%edi
	movl $0,%edx
	imul %edi
	movl %eax,%ebx
	add %ecx,%ebx
	# continue looping
	jmp label4
label5:
	# end of loop
	movl %ebx,%eax
	popl %edx
	# check if result should be negative
	movl $0,%edi
	cmpl %edi,%edx
	je label6
	# if so, negate result
	movl $-1,%edi
	movl $0,%edx
	imul %edi
label6:
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# subroutine for converting an int to a string
i2a:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save int to esi
	movl %eax,%esi
	# get pointer to characters in string_buffer
	movl $string_buffer,%edi
	movl $16,%ecx
	add %ecx,%edi
	# check if number is zero
	movl $0,%ecx
	cmpl %ecx,%esi
	jne label10
	movl $48,%eax
	stosb
	jmp label14
label10:
	# check if number is negative
	movl $0,%ecx
	cmpl %ecx,%esi
	jge label11
	# write to '-' to character sequence
	movl $45,%ecx
	movl %ecx,%eax
	stosb
	# convert the number to a positive so algorithm below will work correctly
	movl $-1,%ecx
	movl %esi,%eax
	movl $0,%edx
	imul %ecx
	movl %eax,%esi
label11:
	# if number is non-negative
	# push edi onto the stack, we'll need the pointer later on
	pushl %edi
	# loop to find the starting modulus amount (needed to find each digit)
	# ebx will be the modulus amount (i.e., order of number: 1, 10, 100, ...)
	movl $1,%ebx
	# initially edi is int/10
	movl $10,%edi
	movl $0,%edx
	idiv %edi
	movl %eax,%edi
label12:
	# if edi is 0 then we're done
	movl $0,%ecx
	cmpl %ecx,%edi
	je label13
	# multiply order value (ebx) by 10
	movl $10,%ecx
	movl %ebx,%eax
	movl $0,%edx
	imul %ecx
	movl %eax,%ebx
	# divide number (edi) by 10 each iteration
	movl %edi,%eax
	movl $0,%edx
	idiv %ecx
	movl %eax,%edi
	# continue looping
	jmp label12
label13:
	# end of loop
	# restore edi (pointer into string)
	popl %edi
	# loop to write each digit into the string
label15:
	# check if order value (ebx) is 0, if so break
	movl $0,%ecx
	cmpl %ecx,%ebx
	je label16
	# get next digit of number (esi) using order value (ebx)
	movl %esi,%eax
	movl $0,%edx
	idiv %ebx
	# convert to character
	movl $48,%ecx
	add %ecx,%eax
	# write it to the next character in the string
	stosb
	# remove upper digit from input number by setting quotient to be remainder
	movl %edx,%esi
	# shift modulus value to right by 1 decimal point
	movl $10,%ecx
	movl %ebx,%eax
	movl $0,%edx
	idiv %ecx
	movl %eax,%ebx
	# continue looping
	jmp label15
label16:
label14:
	# end of loop
	# write null byte to next character in string
	movl $0,%eax
	stosb
	# set eax to string_buffer
	movl $string_buffer,%eax
	# compute length of string, put in ebx
	movl $17,%ebx
	add %eax,%ebx
	sub %ebx,%edi
	movl %edi,%ebx
	# compute size of string (17+length+alignment bytes), put in ecx
	movl $17,%ecx
	add %ebx,%ecx
	# compute alignment bytes
	movl %ecx,%eax
	movl $4,%edi
	movl $0,%edx
	idiv %edi
	movl $0,%edi
	cmpl %edi,%edx
	je label17
	# add to size of string
	movl $4,%edi
	sub %edx,%edi
	add %edi,%ecx
label17:
	# update string_buffer
	movl $string_buffer,%eax
	# set size (ecx)
	movl %ecx,4(%eax)
	# set length (ebx)
	movl %ebx,12(%eax)
	# clone string_buffer
	call Object.clone
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# subroutine for printing a string to standard output
print_string:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save string to esi
	movl %eax,%esi
	# set ebx to 1 (stdin)
	movl $1,%ebx
	# set ecx to string's char sequence
	movl $16,%ecx
	add %eax,%ecx
	# set edx to length of string
	movl 12(%eax),%edx
	# perform write system call
	movl $4,%eax
	int $0x80
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# subroutine for printing an int to standard output
print_int:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save int to esi
	movl %eax,%esi
	# convert in to a string
	call i2a
	# print the converted string
	call print_string
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# generic error handling subroutine
	# error message is passed via eax
	# takes an error message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
error_handler:
	# save message string to the stack
	pushl %eax
	# print filename
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print error message
	popl %eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80

	# out of memory error handling subroutine
	# simply calls error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
out_of_memory:
	movl $String_const_0,%eax
	call error_handler
	
	# null pointer error handling subroutine
	# simply calls error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_null_pointer_error:
	movl $String_const_1,%eax
	call error_handler
	
	# null argument error handling subroutine
	# simply calls error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
null_argument_error:
	movl $String_const_2,%eax
	call error_handler
	
	# divide by zero error handling subroutine
	# simply calls error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
_divide_zero_error:
	movl $String_const_3,%eax
	call error_handler
	
	# string length error handling subroutine
	# simply calls error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
string_length_error:
	movl $String_const_4,%eax
	call error_handler
	
	# read error handling subroutine
	# simply calls error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
read_error:
	movl $String_const_7,%eax
	call error_handler
	
	# write error handling subroutine
	# simply calls error_handler passing it message as an argument
	# note:	doesn't need prologue/epilogue since it doesn't return
write_error:
	movl $String_const_9,%eax
	call error_handler

	# array index error handling subroutine
	# index is passed via ebx
	# note:	doesn't need prologue/epilogue since it doesn't return
_array_index_error:
	# save ebx (index) to the stack
	pushl %ebx
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print first part of error message
	movl $String_const_23,%eax
	call print_string
	# print index
	popl %eax
	call print_int
	# print second part of error message
	movl $String_const_24,%eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80

	# array size error handling subroutine
	# size is passed via ebx
_array_size_error:
	# save ebx (size) to the stack
	pushl %ebx
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print first part of error message
	movl $String_const_25,%eax
	call print_string
	# print index
	popl %eax
	call print_int
	# print second part of error message
	movl $String_const_26,%eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80

	# array store error handling subroutine
	# array's type id passed via ebx, assigned type id passed via ecx
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
_array_store_error:
	# save class ids to the stack
	pushl %ebx
	pushl %ecx
	# print filename
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print error message
	movl $String_const_27,%eax
	call print_string
	# print error message
	movl $String_const_28,%eax
	call print_string
	# get assigned type name from class_name_table using id
	popl %eax
	movl $4,%ebx
	movl $0,%edx
	imul %ebx
	movl %eax,%ebx
	movl $class_name_table,%eax
	add %ebx,%eax
	movl 0(%eax),%eax
	# print object's type name
	call print_string
	# print error message
	movl $String_const_29,%eax
	call print_string
	# get target type name from class_name_table using id
	popl %eax
	movl $4,%ecx
	movl $0,%edx
	imul %ecx
	movl %eax,%ecx
	movl $class_name_table,%eax
	add %ecx,%eax
	movl 0(%eax),%eax
	# print target type name
	call print_string
	# print error message
	movl $String_const_21,%eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80

	# read file error handling subroutine 
	# read file name is passed via ebx
	# note:	difference between this error and read_error is that
	# the former is an error with opening the file for reading
	# while the latter is an error during reading
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
read_file_error:
	# save write filename to the stack
	pushl %ebx
	# print filename
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print ":"
	movl $String_const_6,%eax
	call print_string
	# print write filename
	popl %eax
	call print_string
	# print error message
	movl $String_const_21,%eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80
	
	# write file error handling subroutine
	# write file name is passed via ebx
	# note:	difference between this error and write_error is that
	# the former is an error with opening the file for writing
	# while the latter is an error during writing
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
write_file_error:
	# save write filename to the stack
	pushl %ebx
	# print filename
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print ":"
	movl $String_const_8,%eax
	call print_string
	# print write filename
	popl %eax
	call print_string
	# print error message
	movl $String_const_21,%eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80
	
	# substring index error handling subroutine
	# length passed via ebx, beginning index passed via ecx, end index passed via edx
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
string_index_error:
	# save arguments (length and indices) to the stack
	pushl %edx
	pushl %ecx
	pushl %ebx
	# print the filename
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print the line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print error message
	movl $String_const_5,%eax
	call print_string
	# print error message
	movl $String_const_10,%eax
	call print_string
	# print error message
	movl $String_const_11,%eax
	call print_string
	# print reference string
	movl %esi,%eax
	call print_string
	# print error message
	movl $String_const_12,%eax
	call print_string
	# print error message
	movl $String_const_13,%eax
	call print_string
	# print length
	popl %ebx
	movl %ebx,%eax
	call print_int
	# print error message
	movl $String_const_14,%eax
	call print_string
	# print beginning index
	popl %ecx
	movl %ecx,%eax
	call print_int
	# print error message
	movl $String_const_15,%eax
	call print_string
	# print end index
	popl %edx
	movl %edx,%eax
	call print_int
	# print error message
	movl $String_const_20,%eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80
	
	# class cast error handling subroutine
	# object's type id passed via ebx, target type id passed via ecx
	# unlike errors above, does not use generic error handler
	# note:	doesn't need prologue/epilogue since it doesn't return	
_class_cast_error:
	# save class ids to the stack
	pushl %ecx
	pushl %ebx
	# print filename
	movl $_current_filename_ptr,%edx
	movl 0(%edx),%eax
	call print_string
	# print ":"
	movl $String_const_19,%eax
	call print_string
	# print line number
	movl $_current_line_number,%edx
	movl 0(%edx),%eax
	call print_int
	# print error message
	movl $String_const_16,%eax
	call print_string
	# print error message
	movl $String_const_17,%eax
	call print_string
	# get object's type name from class_name_table using id
	popl %eax
	movl $4,%ebx
	movl $0,%edx
	imul %ebx
	movl %eax,%ebx
	movl $class_name_table,%eax
	add %ebx,%eax
	movl 0(%eax),%eax
	# print object's type name
	call print_string
	# print error message
	movl $String_const_18,%eax
	call print_string
	# get target type name from class_name_table using id
	popl %eax
	movl $4,%ecx
	movl $0,%edx
	imul %ecx
	movl %eax,%ecx
	movl $class_name_table,%eax
	add %ecx,%eax
	movl 0(%eax),%eax
	# print target type name
	call print_string
	# print error message
	movl $String_const_21,%eax
	call print_string
	# perform exit system call with status "1"
	movl $1,%ebx
	movl $1,%eax
	int $0x80
	
	# clone an object
	# object passed in eax
	# returns pointer to cloned object
Object.clone:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save object to esi
	movl %eax,%esi
	# put size in eax
	movl 4(%esi),%eax
	# check if gc is enabled, if so call gc_mem_alloc, otherwise mem_alloc
	movl $gc_flag,%ebx
	movl 0(%ebx),%ebx
	movl $0,%ecx
	cmpl %ebx,%ecx
	je label90
	call gc_mem_alloc
	jmp label91
label90:
	call mem_alloc
label91:
	# copy words from original object to new object
	# edx is pointer to new object, esi points to old object
	movl %eax,%edx
	# ebx points to end of old object
	movl 4(%esi),%ebx
	add %esi,%ebx
	# put 4 in edi for incrementing within the loop
	movl $4,%edi
	# loop to perform copy
label48:
	# check if reached end of object
	cmpl %ebx,%esi
	je label49
	# if not get next word in original and copy to new
	movl 0(%esi),%ecx
	movl %ecx,0(%edx)
	# increment pointers
	add %edi,%edx
	add %edi,%esi
	# continue looping
	jmp label48
label49:
	# end of loop
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret

	# compare two objects (does pointer comparison)
	# reference string passed via eax, parameter string passed on the stack
	# returns boolean (0 or -1) indicating whether objects are equal
Object.equals:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# get parameter object
	movl 12(%esp),%ebx
	# set edi to 0 (false)
	movl $0,%edi
	# compare with reference object (in eax)
	cmpl %eax,%ebx
	jne label1
	movl $1,%edi
label1:
	movl %edi,%eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret

	# get string representation of object (address in memory)
	# object passed in eax
	# returns string representation of object
Object.toString:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save address to stack
	pushl %eax
	# get string representation of dynamic type
	movl 0(%eax),%eax
	movl $4,%ebx
	movl $0,%edx
	imul %ebx
	movl $class_name_table,%ebx
	addl %ebx,%eax
	movl 0(%eax),%eax
	# put "@" on stack
	movl $String_const_30,%ebx
	pushl %ebx
	# perform string concatenation and save result to stack
	call String.concat
	# save result to ebx
	movl %eax,%ebx
	# pop off parameter and throw away
	popl %ecx
	# pop address of stack and put in eax
	popl %eax
	# save concat result to the stack
	pushl %ebx
	# convert address to string and push on the stack
	call i2a
	# pop previous concat result back off of stack
	popl %ebx
	# push i2a result onto the stack
	pushl %eax
	# concatenate with string (result is returned)
	movl %ebx,%eax
	call String.concat
	popl %ebx
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# compute length of the reference string
	# string passed via eax
String.length:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	movl %ebx,%ebp
	# body
	# put length in eax
	movl 12(%eax),%eax
	# epilogue
	movl %ebp,%ebx
	popl %ebp
	movl %ebx,%esp
	ret
	
	# compares if the reference string equals a parameter string
	# note:	the objects do not have to be the same, just the character sequences
	# reference string passed via eax, parameter string passed on the stack
	# returns boolean (0 or -1) indicating whether strings are equal
String.equals:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save reference string to esi
	movl %eax,%esi
	# check lengths of strings
	movl 12(%esi),%ecx
	movl 12(%esp),%ebx
	# check if parameter string is null
	movl $0,%edi
	cmpl %edi,%ebx
	jne label52
	# if null call error subroutine (doesn't return)
	call null_argument_error
label52:
	# string is not null
	# move true (-1) into result entry on the stack
	movl $-1,%edx
	pushl %edx
	movl 12(%ebx),%edx
	cmpl %edx,%ecx
	je label53
	# lengths are different so put false (0) in result entry on the stack and branch to end
	movl $0,%edx
	movl %edx,0(%esp)
	jmp label51
label53:
	# otherwise continue checking strings
	# get pointer to characters in reference string
	movl $16,%edi
	movl %esi,%ecx
	add %edi,%ecx
	# get pointer to characters in parameter string
	add %edi,%ebx
	# loop over characters
label50:
	# load the next character from reference string
	movl %ecx,%esi
	movl $0,%eax
	lodsb
	movl %esi,%ecx
	# save result to edx
	movl %eax,%edx
	# load the next character from parameter string
	movl %ebx,%esi
	movl $0,%eax
	lodsb
	movl %esi,%ebx
	# check if they are equivalent
	cmpl %edx,%eax
	je label54
	# different so put false (0) in result entry on the stack and branch to end
	movl $0,%edx
	movl %edx,0(%esp)
	jmp label51
label54:
	# otherwise continue checking strings
	# break loop if zero byte (only need to test one since they are equal)
	movl $0,%esi
	cmpl %esi,%edx
	je label51
	# otherwise continue looping
	jmp label50
label51:
	# end of loop
	# put result in accumulator
	popl %eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret

	# get string representation of this string (itself)
	# string passed in eax
	# returns string representation of object
String.toString:
	# just need to return (string already in eax)
	ret
	
	# concatenate the reference string with a parameter string
	# reference string passed via eax, parameter string passed on the stack
	# returns concatenated string 
String.concat:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save reference string in esi
	movl %eax,%esi
	# get number of characters in the reference string, put in ebx
	movl 12(%esi),%ebx
	# get argument string
	movl 12(%esp),%ecx
	# check if null
	movl $0,%edi
	cmpl %edi,%ecx
	jne label55
	# if null, call error routine
	call null_argument_error
label55:
	# get number of characters in the parameter string, put in ecx
	movl 12(%ecx),%ecx
	# compute total characters in concatenated string, put in ebx
	add %ecx,%ebx
	# make sure doesn't exceed 5000 (restriction due to garbage collector)
	movl $5000,%ecx
	cmpl %ecx,%ebx
	jle label58
	call string_length_error
label58:
	# compute size of concatenated string (17+length+alignment bytes), put in ecx
	movl $17,%ecx
	add %ebx,%ecx
	# compute alignment bytes
	movl %ecx,%eax
	movl $4,%edi
	movl $0,%edx
	# get offset from 4
	idiv %edi
	# check if 0, if so already aligned
	movl $0,%edi
	cmpl %edi,%edx
	je label59
	# check not, then subtract from 4 and to total size (ecx)
	movl $4,%edi
	sub %edx,%edi
	add %edi,%ecx
label59:
	# create string in string_buffer
	movl $string_buffer,%eax
	# update size (ecx) of string
	movl %ecx,4(%eax)
	# update length (ebx) of string
	movl %ebx,12(%eax)
	# clone string (note: characters still need to be set after cloning)
	call Object.clone
	# write char sequence
	# esi points to reference string char sequence
	movl $16,%edi
	add %edi,%esi
	# edi points to new string char sequence
	movl $16,%edx
	movl %eax,%edi
	add %edx,%edi
	# save new string pointer to edx (will be returned)
	movl %eax,%edx
	# run loop twice, once for reference string and once for parameter string
	# ecx indicates which time through
	movl $0,%ecx
	# loop writing characters to new string
label60:
	# get next character
	movl $0,%eax
	lodsb
	movl $0,%ebx
	# if null character, then break
	cmpl %ebx,%eax
	je label61
	# if not null, then write to new string
	stosb
	# continue looping
	jmp label60
label61:
	# end of loop
	# see if ebx is 0 (first time running loop)
	movl $0,%ebx
	cmpl %ebx,%ecx
	jne label62
	# if so, set esi to point to char sequence in parameter string
	# and rerun loop
	movl 12(%esp),%esi
	movl $16,%ebx
	add %ebx,%esi
	# set ecx to 1 to indicate second time running loop
	movl $1,%ecx
	jmp label60
label62:
	# write null byte to end of new string
	movl $0,%eax
	stosb
	# set return value
	movl %edx,%eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# returns the substring of the reference string
	# reference string passed via eax, beginning and end indices
	# passed via the stack
	# note:	(like java) beginning index is included and the end
	# index is excluded
String.substring:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# save reference string to esi
	movl %eax,%esi
	# get number of characters of reference string, put in ebx
	movl 12(%esi),%ebx
	# get beginning index, put in ecx
	movl 8(%ebp),%ecx
	# get end index, put in edx
	movl 4(%ebp),%edx
	# check that beginning index >= 0
	movl $0,%edi
	cmpl %edi,%ecx
	jl label64
	# check that beginning index < length
	cmpl %ebx,%ecx
	jge label64
	# check that end index >= 0
	cmpl %edi,%edx
	jl label64
	# check that end index <= length
	cmpl %ebx,%edx
	jg label64
	# check that end index >= beginning index
	cmpl %edx,%ecx
	jg label64
	# if we make it here, indices are OK, jump over call to error routine
	jmp label63
label64:
	call string_index_error
label63:
	# compute end-beginning => size of new string, put in ebx
	movl %edx,%ebx
	sub %ecx,%ebx
	# compute size of new string (17+length+alignment bytes), put in ecx
	movl $17,%ecx
	add %ebx,%ecx
	# compute alignment bytes, get offset from 4
	movl %ecx,%eax
	movl $4,%edi
	movl $0,%edx
	idiv %edi
	# if offset is 0, then already aligned
	movl $0,%edi
	cmpl %edi,%edx
	je label65
	# otherwise subtract from 4 and add to total size (ecx)
	movl $4,%edi
	sub %edx,%edi
	add %edi,%ecx
label65:
	# write string to string_buffer
	movl $string_buffer,%eax
	# update size (ecx) in string
	movl %ecx,4(%eax)
	# update length (ebx) in string
	movl %ebx,12(%eax)
	# clone string (note: still have to copy characters afterwards)
	call Object.clone
	# save new string to the stack (returned by subroutine)
	pushl %eax
	# set edi to point to char sequence in new string
	movl $16,%ecx
	movl %eax,%edi
	add %ecx,%edi
	# set esi to point to char sequence in reference string
	add %ecx,%esi
	movl 8(%ebp),%ecx
	add %ecx,%esi
	# set edx to end address of reference string
	movl 4(%ebp),%edx
	add %esi,%edx
	# loop to copy reference string substring to new string
label66:
	# if reached end address of reference string then break
	cmpl %edx,%esi
	jge label67
	# otherwise get the next character from the reference string and
	# copy it to the new string
	movl $0,%eax
	lodsb
	stosb
	# continue looping
	jmp label66
label67:
	# end of loop
	# write null byte to end of new string
	movl $0,%eax
	stosb
	# set return value
	popl %eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# Set TextIO reference object to read from stdin
	# reference object passed via eax
TextIO.readStdin:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# copy reference object to esi
	movl %eax,%esi
	# get previous read file descriptor
	movl 12(%esi),%ebx
	# if not <=2, then must be closed
	movl $2,%ecx
	cmpl %ecx,%ebx
	jle label68
	# >2 so perform close system call
	movl $6,%eax
	int $0x80
label68:
	# set read file descriptor to 0 (stdin)
	movl $0,%ebx
	movl %ebx,12(%esi)
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# Set the read file for the reference TextIO object
	# reference TextIO object passed via eax, read filename parameter passed on the stack		
TextIO.readFile:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# epilogue
	# copy reference object to esi
	movl %eax,%esi
	# get read file descriptor
	movl 12(%esi),%ebx
	# if not <=2 then must close
	movl $2,%ecx
	cmpl %ecx,%ebx
	jle label69
	# >2 so perform close system call
	movl $6,%eax
	int $0x80
label69:
	# get read filename from stack
	movl 12(%esp),%eax
	# check if it's null
	movl $0,%edi
	cmpl %edi,%eax
	jne label70
	# if it is then call error subroutine
	call null_argument_error
label70:
	# set ebx to point to char sequence within string
	movl $16,%edi
	movl %eax,%ebx
	add %edi,%ebx
	# set ecx to 0 (opening file for reading only)
	movl $0,%ecx
	# perform open system call
	movl $5,%eax
	int $0x80
	# check if result is <0
	movl $0,%ecx
	cmpl %ecx,%eax
	jge label71
	# if <0 call error routine (filename passed via ebx)
	movl $16,%ecx
	sub %ecx,%ebx
	call read_file_error
label71:
	# set read file descriptor
	movl %eax,12(%esi)
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# Set TextIO reference object to read from stdin
	# reference object passed via eax
TextIO.writeStdout:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# copy reference object to eax
	movl %eax,%esi
	# get write file descriptor from object
	movl 16(%esi),%ebx
	# if >2 then must close
	movl $2,%ecx
	cmpl %ecx,%ebx
	jle label72
	# perform close system call
	movl $6,%eax
	int $0x80
label72:
	# otherwise set write file descriptor to 1 (stdout)
	movl $1,%ebx
	movl %ebx,16(%esi)
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# Set TextIO reference object to read from stdin
	# reference object passed via eax
TextIO.writeStderr:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# copy reference object to esi
	movl %eax,%esi
	# get write file descriptor from object
	movl 16(%esi),%ebx
	# if >2 then must close
	movl $2,%ecx
	cmpl %ecx,%ebx
	jle label73
	# perform close system call
	movl $6,%eax
	int $0x80
label73:
	# set write file descriptor to 2 (stderr)
	movl $2,%ebx
	movl %ebx,16(%esi)
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# Set the write file for the reference TextIO object
	# reference TextIO object passed via eax, write filename parameter passed on the stack		
TextIO.writeFile:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# copy reference object to esi
	movl %eax,%esi
	# get write file descriptor from object
	movl 16(%esi),%ebx
	# if >2 then must close
	movl $2,%ecx
	cmpl %ecx,%ebx
	jle label74
	# perform close system call
	movl $6,%eax
	int $0x80
label74:
	# get write filename from stack
	movl 12(%esp),%eax
	# check if null
	movl $0,%edi
	cmpl %edi,%eax
	jne label75
	# if null call error subroutine
	call null_argument_error
label75:
	# set ebx to char sequence within string
	movl $16,%edi
	movl %eax,%ebx
	add %edi,%ebx
	# set ecx to 101 (open file for writing)
	movl $101,%ecx
	# set edx to 509 (use permissions rwxr-xr-x)
	movl $509,%edx
	# perform open system call
	movl $5,%eax
	int $0x80
	# check <0, indicates an error
	movl $0,%ecx
	cmpl %ecx,%eax
	jge label76
	# <0 so call error subroutine (pass filename via ebx)
	movl $16,%edi
	sub %edi,%ebx
	call write_file_error
label76:
	# set write file descriptor
	movl %eax,16(%esi)
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# write a string to the current write file
	# reference TextIO object passed via eax, string passed via the stack
	# returns the reference object
TextIO.putString:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# copy reference object to esi
	movl %eax,%esi
	# get string from stack
	movl 12(%esp),%eax
	# check if null, if so call error subroutine
	movl $0,%edi
	cmpl %edi,%eax
	jne label77
	call null_argument_error
label77:
	# get pointer to char sequence within string, put in ecx
	movl $16,%edi
	movl %eax,%ecx
	add %edi,%ecx
	# get length of string, put in edx
	movl 12(%eax),%edx
	# get write file descriptor, put in ebx
	movl 16(%esi),%ebx
	# perform write system call
	movl $4,%eax
	int $0x80
	# check if <0, indicates error
	movl $0,%ebx
	cmpl %ebx,%eax
	jge label78
	# if <0 then call error subroutine
	call write_error
label78:
	# return reference object
	movl %esi,%eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# write an int to the current write file
	# reference TextIO object passed via eax, int passed via the stack
	# returns the reference object
TextIO.putInt:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# copy reference object to esi
	movl %eax,%esi
	# get int from stack
	movl 12(%esp),%eax
	# convert to a string
	call i2a
	# get write file descriptor, put in ebx
	movl 16(%esi),%ebx
	# set ecx to point to char sequence in converted string
	movl %eax,%ecx
	movl $16,%edx
	add %edx,%ecx
	# put length in edx
	movl 12(%eax),%edx
	# perform write system call
	movl $4,%eax
	int $0x80
	# check if <0, indicates an error
	movl $0,%ebx
	cmpl %ebx,%eax
	jge label81
	# if <0 call error subroutine
	call write_error
label81:
	# return reference object
	movl %esi,%eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# read a string from the current read file
	# note:	actually reads a character at a time until '\n' or '\0' or 256 chars reached
	# reference TextIO object passed via eax
	# returns the read string
TextIO.getString:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# set esi to point to char sequence in string_buffer
	movl $string_buffer,%esi
	movl $16,%ecx
	add %ecx,%esi
	# copy reference object to edi
	movl %eax,%edi
	# set edx to 1 (read 1 character at a time)
	movl $1,%edx
	# push 0 onto the stack, used to indicate number of characters read
	movl $0,%ebx
	pushl %ebx
	# loop for up to 256 characters
label82:
	# check if reached 256, if so break
	popl %ecx
	movl $1,%ebx
	add %ebx,%ecx
	pushl %ecx
	movl $256,%ebx
	cmpl %ebx,%ecx
	je label83
	# get read file descriptor, put in ebx
	movl 12(%edi),%ebx
	# get pointer to next character in string, put in ecx
	movl %esi,%ecx
	# perform read system call
	movl $3,%eax
	int $0x80
	# check if <0, indicates an error
	movl $0,%ebx
	cmpl %ebx,%eax
	jge label84
	# if <0 call error subroutine
	call read_error
label84:
	# otherwise see if any bytes were read
	# if not set result to null and break loop
	# if bytes were read, then just break loop
	movl $0,%ebx
	cmpl %ebx,%eax
	jne label85
	movl $0,%eax
	jmp label86
label85:
	# otherwise get next character
	# put newline character (10) in ebx
	movl $10,%ebx
	movl $0,%eax
	lodsb
	# check if newline (10), if so break
	cmpl %ebx,%eax
	je label83
	# otherwise continue looping
	jmp label82
label83:
	# end of loop
	# write null byte to end of string
	movl $0,%eax
	movl %esi,%edi
	movl $1,%ebx
	sub %ebx,%edi
	stosb
	# copy string_buffer
	movl $string_buffer,%eax
	movl %esi,%ebx
	# put length in ebx
	movl $17,%ecx
	add %eax,%ecx
	sub %ecx,%ebx
	# compute size of object (17+length+alignment bytes), put in ecx
	movl $17,%ecx
	add %ebx,%ecx
	# compute alignment bytes, get offset from 4
	movl %ecx,%eax
	movl $4,%edi
	movl $0,%edx
	idiv %edi
	# if offset is 0, then already aligned
	movl $0,%edi
	cmpl %edi,%edx
	je label87
	# otherwise, subtract from 4 and add to size (ecx)
	movl $4,%edi
	sub %edx,%edi
	add %edi,%ecx
label87:
	# update string_buffer
	movl $string_buffer,%eax
	# update size (ecx)
	movl %ecx,4(%eax)
	# update length (ebx)
	movl %ebx,12(%eax)
	# clone string_buffer and return it
	call Object.clone
label86:
	# pop off stack entry (and throw away)
	popl %ebx
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
	
	# read an int from the current read file
	# actually reads a string and interprets it as an int, if not an int then returns 0
	# note:	reads a character at a time until '\n' or '\0' or 256 chars reached
	# reference TextIO object passed via eax
	# returns the read int
TextIO.getInt:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	# body
	# copy reference object to esi
	movl %eax,%esi
	# get read file descriptor, put in ebx
	movl 12(%esi),%ebx
	# set ecx to char sequence within string_buffer
	movl $string_buffer,%ecx
	movl $16,%edi
	add %edi,%ecx
	# read 256 characters (edx)
	movl $256,%edx
	# perform read system call
	movl $3,%eax
	int $0x80
	# check if <0, if so call error subroutine
	movl $0,%edi
	cmpl %edi,%eax
	jge label88
	call read_error
label88:
	# convert string to int and return result
	movl $string_buffer,%eax
	call a2i
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret

		
	# exits the program
	# takes status value from stack
	# note: no prologue/epilogue since doesn't return	
Sys.exit:
	# get the status value from stack, put in ebx
	movl 12(%esp),%ebx
	# perform exit system call
	movl $1,%eax
	int $0x80
	
	# get the current time as seconds since 1970 UTC
	# returns the time
Sys.time:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	movl %eax,%esi
	# body
	# set ebx to _current_time, location in memory where time can be written
	movl $_current_time,%ebx
	# perform time system call
	movl $13,%eax
	int $0x80
	# read time from memory and put in result register (eax)
	movl 0(%ebx),%eax
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret



	# get a random 32-bit integer
	# returns a random integer
Sys.random:
	# prologue
	movl %esp,%ebx
	pushl %ebp
	pushl %esi
	movl %ebx,%ebp
	movl %eax,%esi
	# body	
	# get previous random number from memory
	movl $_random,%ebx
	movl 0(%ebx),%eax
	# compute new random number using formula:
	# next = (next * 1103515245 + 12345) & 0x7fffffff
	# (note: not the best way to generate pseudo-random numbers)
	movl $1103515245,%ecx
	movl $0,%edx
	imul %ecx
	movl $12345,%ecx
	add %ecx,%eax
	movl $0x7fffffff,%ecx
	and %ecx,%eax
	# save next random number to memory
	movl %eax,0(%ebx)
	# will return this value as well
	# epilogue
	movl %ebp,%ebx
	popl %esi
	popl %ebp
	movl %ebx,%esp
	ret
