/* Bantam Java Compiler and Language Toolset.

   Copyright (C) 2009 by Marc Corliss (corliss@hws.edu) and 
                         David Furcy (furcyd@uwosh.edu) and
                         E Christopher Lewis (lewis@vmware.com).
   ALL RIGHTS RESERVED.

   The Bantam Java toolset is distributed under the following 
   conditions:

     You may make copies of the toolset for your own use and 
     modify those copies.

     All copies of the toolset must retain the author names and 
     copyright notice.

     You may not sell the toolset or distribute it in 
     conjunction with a commerical product or service without 
     the expressed written consent of the authors.

   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS 
   OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
   PARTICULAR PURPOSE. 
*/

package bantam.codegenx86;

import java.io.PrintStream;

/** x86 assembly support 
  * create an object from this class for use in generating x86 code
  * */
public class X86Support {
    /** Exit syscall */
    public final int SYSCALL_EXIT = 0;
    /** File open syscall */
    public final int SYSCALL_FILE_OPEN = 1;
    /** File close syscall */
    public final int SYSCALL_FILE_CLOSE = 2;
    /** File read syscall */
    public final int SYSCALL_FILE_READ = 3;
    /** File write syscall */
    public final int SYSCALL_FILE_WRITE = 4;
    /** Get time syscall */
    public final int SYSCALL_GET_TIME = 5;
    /** brk syscall */
    public final int SYSCALL_BRK = 6;

    /** x86 register set */
    private final String[] registers = 
    { "%eax", "%ebx", "%ecx", "%edx", "%edi", "%esi", "%ebp", "%esp" };

    /** The next label number - for use in generating unique labels */
    private int labelNum = 0;
    
    /** Next available stack offset */
    private int nextAvailStackOffset;

    /** The print stream for printing to an assembly file */
    private PrintStream out;

    /** X86Support constructor
      * @param out print stream
      * */
    public X86Support(PrintStream out) {
	this.out = out;
    }

    /* Methods for manipulating the next available stack offset */

    /** Get next available stack offset 
      * @return next available stack offset 
      * */
    public int getNextAvailStackOffset() {
	return nextAvailStackOffset;
    }

    /** Set next available stack offset 
      * @param offset new available stack offset 
      * */
    public void setNextAvailStackOffset(int offset) {
	nextAvailStackOffset = offset;
    }

    /* Registers used by the code generator */

    /** Get the stack pointer register
      * @return register name
      * */
    public String getSPReg() {
	return registers[7];
    }

    /** Get the frame pointer register
      * @return register name
      * */
    public String getFPReg() {
	return registers[6];
    }

    /** Get the register that holds 'this' pointer (note: this is esi)
      * @return register name
      * */
    public String getThisReg() {
	return registers[5];
    }

    /** Get the accumulator register (note: this is eax)
      * @return register name
      * */
    public String getAccReg() {
	return registers[0];
    }

    /** Get the eax register (note: this is the accumulator)
      * @return register name
      * */
    public String getEAXReg() {
	return registers[0];
    }

    /** Get the ebx register
      * @return register name
      * */
    public String getEBXReg() {
	return registers[1];
    }

    /** Get the ecx register
      * @return register name
      * */
    public String getECXReg() {
	return registers[2];
    }

    /** Get the edx register
      * @return register name
      * */
    public String getEDXReg() {
	return registers[3];
    }

    /** Get the edi register
      * @return register name
      * */
    public String getEDIReg() {
	return registers[4];
    }

    /** Get the esi register (note: this is 'this' register)
      * @return register name
      * */
    public String getESIReg() {
	return registers[5];
    }

    /* Methods for generating code to start the data and text sections */

    /** Generate a comment
      * @param text text to put in comment
      * */
    public void genComment(String text) {
	out.println("\t# " + text);
    }

    /** Generate the code to start the data section 
      * */
    public void genDataStart() {
	out.println("\t.data");
	genGlobal("gc_flag");
	genGlobal("class_name_table");
	genGlobal("Main_template");
	genGlobal("String_template");
	genGlobal("String_dispatch_table");
    }

    /** Generate the code to start the data section 
      * */
    public void genTextStart() {
	out.println("\t.text");
	genGlobal("Main_init");
	genGlobal("bantam.Main.main");
    }

    /* Data generation methods used by the code generator */

    /** Generate a global
      * @param label label to make global
      * */
    public void genGlobal(String label) {
	out.println("\t.globl\t" + label);
    }

    /** Generate a data word
      * @param dataWord word string
      * */
    public void genWord(String dataWord) {
	out.println("\t.long\t" + dataWord);
    }

    /** Generate a data byte
      * @param dataByte byte string
      * */
    public void genByte(String dataByte) {
	out.println("\t.byte\t" + dataByte);
    }

    /** Generate a data segment of size n
      * @param n size of data segment
      * */
    public void genSpace(int n) {
	out.println("\t.space\t" + n);
    }

    /** Generate an ASCII string (terminates with zero byte and aligns)
      * @param ascii ASCII string
      * */
    public void genAscii(String ascii) {
        out.print("\t.ascii\t\"");

        for (int i = 0; i < ascii.length(); i++) {
	    if (ascii.charAt(i) == '\n')
		out.print("\\n");
	    else if (ascii.charAt(i) == '\t')
		out.print("\\t");
	    else if (ascii.charAt(i) == '\f')
		out.print("\\f");
	    else if (ascii.charAt(i) == '\"')
		out.print("\\\"");
	    else if (ascii.charAt(i) == '\\') {
                out.println("\"");
                out.println("\t.byte\t0x5c");
                out.print("\t.ascii\t\"");
            }
            else
                out.print(ascii.charAt(i));
        }
	
        out.println("\"");
        out.println("\t.byte\t0");
        genAlign();
    }

    /** Generate word alignment directive
      * */
    public void genAlign() {
	out.println("\t.align\t" + (getWordSize()/2));
    }

    /* Code generation methods used by the code generator */

    /** Check whether a register is a valid Mips register 
      * Throws exception if not valid
      * @param reg register to check
      * */
    private void checkReg(String reg) {
	for (int i = 0; i < registers.length; i++) {
	    if (reg.equals(registers[i])) 
		return;
	}
	throw new IllegalArgumentException("Invalid register name");
    }

    /** Check whether an immediate is word aligned
      * Throws exception if not word aligned
      * @param offset register to check
      * */
    private void checkWordOffset(int offset) {
	if (offset % getWordSize() != 0)
	    throw new IllegalArgumentException("Word offset ('" + offset + "') must be multiple of " +
					       getWordSize());
    }

    /** Get a unique label for use with control flow 
      * @return label string 
      * */
    public String getLabel() {
	return "label" + labelNum++;
    }

    /** Get the word size 
     * @return word size 
     * */
    public int getWordSize() {
	return 4;
    }

    /** Generate a move instruction - a register-to-register move or
      * a load address depending on whether the source parameter is 
      * a register or a label
      * @param src string containing the source (register or label)
      * @param destReg string containing the destination register
      * */
    public void genMove(String src, String destReg) {
	checkReg(destReg);
	for (int i = 0; i < registers.length; i++) {
	    if (src.equals(registers[i])) {
		out.println("\tmovl " + src + "," + destReg);
		return;
	    }
	}
	out.println("\tmovl $" + src + "," + destReg);
    }

    /** Generate a move instruction - loads an immediate into a register
      * @param imm immediate to load into destination register
      * @param destReg string containing the destination register
      * */
    public void genMove(int imm, String destReg) {
	checkReg(destReg);
	out.println("\tmovl $" + imm + "," + destReg);
    }

    /** Generate a move instruction - loads a register value to memory
      * @param offset integer offset (must be a multiple of the word size)
      * @param baseReg string containing the base register
      * @param destReg string containing the destination register
      * */
    public void genMove(int offset, String baseReg, String destReg) {
	checkReg(destReg);
	checkReg(baseReg);
	checkWordOffset(offset);
	out.println("\tmovl " + offset + "(" + baseReg + ")," + destReg);
    }

    /** Generate a move instruction - stores a register value to memory
      * @param srcReg string containing the source register
      * @param offset integer offset (must be a multiple of the word size)
      * @param baseReg string containing the base register
      * */
    public void genMove(String srcReg, int offset, String baseReg) {
        checkReg(srcReg);
        checkReg(baseReg);
        checkWordOffset(offset);
        out.println("\tmovl " + srcReg + "," + offset + "(" + baseReg + ")");
    }

    /** Generate a load byte instruction
      * Note: uses the address in %esi (this register), increments %esi,
      * and automatically puts result in the accumulator (%eax)
      * */
    public void genLoadByte() {
	out.println("\tmovl $0," + getAccReg());
	out.println("\tlodsb");
    }

    /** Generate a store word instruction
      * Note: uses the address in %edi, increments %edi,
      * and takes the value from the accumulator (%eax)
      * */
    public void genStoreByte() {
	out.println("\tstosb");
    }

    /** Generate a push instruction
      * @param reg string containing the register to push onto the stack
      * */
    public void genPush(String reg) {
	checkReg(reg);
	out.println("\tpushl " + reg);
    }

    /** Generate a pop instruction
      * @param reg string containing the register to hold popped stack value
      * */
    public void genPop(String reg) {
	checkReg(reg);
	out.println("\tpopl " + reg);
    }

    /** Generate a generic binary operation
      * @param op string containing the particular operator (e.g., add, sub)
      * @param srcReg string containing the source register
      * @param destReg string containing the destination register
      * */
    private void genBinaryOp(String op, String srcReg, String destReg) {
	checkReg(srcReg);
	checkReg(destReg);
	out.println("\t" + op + " " + srcReg + "," + destReg);
    }

    /** Generate an add instruction
      * @param srcReg string containing the source register
      * @param destReg string containing the destination register
      * */
    public void genAdd(String srcReg, String destReg) {
	genBinaryOp("add", srcReg, destReg);
    }

    /** Generate a subtraction instruction
      * @param srcReg string containing the source register
      * @param destReg string containing the destination register
      * */
    public void genSub(String srcReg, String destReg) {
	genBinaryOp("sub", srcReg, destReg);
    }

    /** Generate a multiply instruction
      * (note: puts result in %eax, also overwrites %edx with 0)
      * @param srcReg string containing the source register
      * */
    public void genMul(String srcReg) {
	checkReg(srcReg);
	genMove(0, getEDXReg());
	out.println("\timul " + srcReg);
    }

    /** Generate a divide instruction
      * (note: puts quotient in %eax and remainder in %edx)
      * @param srcReg string containing the source register
      * */
    public void genDiv(String srcReg) {
	checkReg(srcReg);
	genMove(0, getEDXReg());
	out.println("\tidiv " + srcReg);
    }

    /** Generate a modulus instruction
      * (note: puts quotient in %eax and remainder in %edx)
      * @param srcReg string containing the source register
      * */
    public void genMod(String srcReg) {
	genDiv(srcReg);
    }

    /** Generate an and instruction
      * @param srcReg string containing the source register
      * @param destReg string containing the destination register
      * */
    public void genAnd(String srcReg, String destReg) {
	genBinaryOp("and", srcReg, destReg);
    }

    /** Generate an or instruction
      * @param srcReg string containing the source register
      * @param destReg string containing the destination register
      * */
    public void genOr(String srcReg, String destReg) {
	genBinaryOp("or", srcReg, destReg);
    }

    /** Generate an xor instruction
      * @param srcReg string containing the source register
      * @param destReg string containing the destination register
      * */
    public void genXor(String srcReg, String destReg) {
	genBinaryOp("xor", srcReg, destReg);
    }

    /** Generate a shift left instruction
      * (note: shift amount is taken from %ecx)
      * @param destReg string containing the destination register
      * */
    public void genShiftLeft(String destReg) {
	checkReg(destReg);
	out.println("\tshl " + destReg);
    }

    /** Generate a shift right instruction
      * (note: shift amount is taken from %ecx)
      * @param destReg string containing the destination register
      * */
    public void genShiftRight(String destReg) {
	checkReg(destReg);
	out.println("\tshr " + destReg);
    }

    /** Generate a generic unary operation
      * @param op string containing the particular operator (.e.g., not)
      * @param reg string containing the source and destination register
      * */
    private void genUnaryOp(String op, String reg) {
	checkReg(reg);
	out.println("\t" + op + " " + reg);
    }

    /** Generate a negation instruction
      * @param reg string containing the source and destination register
      * */
    public void genNeg(String reg) {
	genUnaryOp("neg", reg);
    }

    /** Generate a not instruction
      * @param reg string containing the source and destination register
      * */
    public void genNot(String reg) {
	genUnaryOp("not", reg);
    }

    /** Generate a reference label
      * @param label label string
      * */
    public void genLabel(String label) {
	out.println(label + ":");
    }

    /** Generate a direct call
      * @param label label string
      * */
    public void genDirCall(String label) {
	out.println("\tcall " + label);
    }

    /** Generate an indirect call
      * @param reg register containing callee address
      * */
    public void genInDirCall(String reg) {
	checkReg(reg);
	out.println("\tcall *" + reg);
    }

    /** Generate a return
      * */
    public void genRetn() {
	out.println("\tret");
    }

    /** Generate an unconditional branch
      * @param label label string
      * */
    public void genUncondBr(String label) {
	out.println("\tjmp " + label);
    }

    /** Generate a conditional branch
      * branches based on comparison between first and second operand
      * @param reg1 first register to compare
      * @param reg2 second register to compare
      * @param label label to branch to
      * */
    private void genCondBr(String op, String reg1, String reg2, 
			   String label) {
	checkReg(reg1);
	checkReg(reg2);
	// Note: in AT&T x86, comparison is relative to last argument, so
	// we must reorder these registers
	out.println("\tcmpl " + reg2 + "," + reg1);
	out.println("\t" + op + " " + label);
    }

    /** Generate a conditional branch
      * branches if first operand is equal to second operand
      * @param reg1 first register to compare
      * @param reg2 second register to compare
      * @param label label to branch to
      * */
    public void genCondBeq(String reg1, String reg2, String label) {
	genCondBr("je", reg1, reg2, label);
    }

    /** Generate a conditional branch
      * branches if first operand is not equal to second operand
      * @param reg1 first register to compare
      * @param reg2 second register to compare
      * @param label label to branch to
      * */
    public void genCondBne(String reg1, String reg2, String label) {
	genCondBr("jne", reg1, reg2, label);
    }

    /** Generate a conditional branch
      * branches if first operand is less than or equal to second operand
      * @param reg1 first register to compare
      * @param reg2 second register to compare
      * @param label label to branch to
      * */
    public void genCondBlt(String reg1, String reg2, String label) {
	genCondBr("jl", reg1, reg2, label);
    }

    /** Generate a conditional branch
      * branches if first operand is less than second operand
      * @param reg1 first register to compare
      * @param reg2 second register to compare
      * @param label label to branch to
      * */
    public void genCondBleq(String reg1, String reg2, String label) {
	genCondBr("jle", reg1, reg2, label);
    }

    /** Generate a conditional branch
      * branches if first operand is greater than second operand
      * @param reg1 first register to compare
      * @param reg2 second register to compare
      * @param label label to branch to
      * */
    public void genCondBgt(String reg1, String reg2, String label) {
	genCondBr("jg", reg1, reg2, label);
    }

    /** Generate a conditional branch
      * branches if first operand is greater than or equal to second operand
      * @param reg1 first register to compare
      * @param reg2 second register to compare
      * @param label label to branch to
      * */
    public void genCondBgeq(String reg1, String reg2, String label) {
	genCondBr("jge", reg1, reg2, label);
    }

    /** Generate a system call
      * @param syscallId the system call number
      * */
    public void genSyscall(int syscallId) {
	if (syscallId == SYSCALL_EXIT)
	    out.println("\tmovl $1,%eax");
	else if (syscallId == SYSCALL_FILE_OPEN)
	    out.println("\tmovl $5,%eax");
	else if (syscallId == SYSCALL_FILE_CLOSE)
	    out.println("\tmovl $6,%eax");
	else if (syscallId == SYSCALL_FILE_READ)
	    out.println("\tmovl $3,%eax");
	else if (syscallId == SYSCALL_FILE_WRITE)
	    out.println("\tmovl $4,%eax");
	else if (syscallId == SYSCALL_GET_TIME)
	    out.println("\tmovl $13,%eax");
	else if (syscallId == SYSCALL_BRK)
	    out.println("\tmovl $45,%eax");
	else
	    throw new RuntimeException("bad syscall identifier");
	out.println("\tint $0x80");
    }
}
