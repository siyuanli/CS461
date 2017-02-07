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

package bantam.codegenmips;

import java.io.PrintStream;

/**
 * Mips assembly support
 * create an object from this class for use in generating Mips code
 */
public class MipsSupport {
    /* Syscall constants - passed to genSyscall() to perform a specific system call*/
    /**
     * Exit syscall
     */
    public final int SYSCALL_EXIT = 0;
    /**
     * File open syscall
     */
    public final int SYSCALL_FILE_OPEN = 1;
    /**
     * File close syscall
     */
    public final int SYSCALL_FILE_CLOSE = 2;
    /**
     * File read syscall
     */
    public final int SYSCALL_FILE_READ = 3;
    /**
     * File write syscall
     */
    public final int SYSCALL_FILE_WRITE = 4;
    /**
     * Get time syscall
     */
    public final int SYSCALL_GET_TIME = 5;
    /**
     * sbrk syscall
     */
    public final int SYSCALL_SBRK = 6;

    /**
     * MIPS register set
     */
    private final String[] registers =
            {"$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
                    "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9",
                    "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
                    "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"};

    /**
     * The next label number - for use in generating unique labels
     */
    private int labelNum = 0;

    /**
     * Next available stack offset
     */
    private int nextAvailStackOffset;

    /**
     * The print stream for printing to an assembly file
     */
    private PrintStream out;

    /**
     * MipsSupport constructor
     *
     * @param out print stream
     */
    public MipsSupport(PrintStream out) {
        this.out = out;
    }

    /* Methods for manipulating the next available stack offset */

    /**
     * Get next available stack offset
     *
     * @return next available stack offset
     */
    public int getNextAvailStackOffset() {
        return nextAvailStackOffset;
    }

    /**
     * Set next available stack offset
     *
     * @param offset new available stack offset
     */
    public void setNextAvailStackOffset(int offset) {
        nextAvailStackOffset = offset;
    }

    /* Registers used by the code generator */

    /**
     * Get the zero register
     *
     * @return register that holds zero
     */
    public String getZeroReg() {
        return registers[0];
    }

    /**
     * Get the stack pointer register
     *
     * @return register name
     */
    public String getSPReg() {
        return registers[29];
    }

    /**
     * Get the frame pointer register
     *
     * @return register name
     */
    public String getFPReg() {
        return registers[30];
    }

    /**
     * Get the return address register
     *
     * @return register name
     */
    public String getRAReg() {
        return registers[31];
    }

    /**
     * Get the global pointer register
     *
     * @return register name
     */
    public String getGPReg() {
        return registers[28];
    }

    /**
     * Get the register that holds 'this' pointer
     *
     * @return register name
     */
    public String getThisReg() {
        return registers[18];
    }

    /**
     * Get the argument register
     *
     * @return register name
     */
    public String getArg0Reg() {
        return registers[4];
    }

    /**
     * Get the second argument register
     *
     * @return register name
     */
    public String getArg1Reg() {
        return registers[5];
    }

    /**
     * Get the third argument register
     *
     * @return register name
     */
    public String getArg2Reg() {
        return registers[6];
    }

    /**
     * Get the result register
     *
     * @return register name
     */
    public String getResultReg() {
        return registers[2];
    }

    /**
     * Get the first temporary register
     *
     * @return register name
     */
    public String getT0Reg() {
        return registers[8];
    }

    /**
     * Get the second temporary register
     *
     * @return register name
     */
    public String getT1Reg() {
        return registers[9];
    }

    /**
     * Get the third temporary register
     *
     * @return register name
     */
    public String getT2Reg() {
        return registers[10];
    }

    /**
     * Get the fourth temporary register
     *
     * @return register name
     */
    public String getT3Reg() {
        return registers[11];
    }

    /**
     * Get the first callee-saved register
     *
     * @return register name
     */
    public String getS0Reg() {
        return registers[19];
    }

    /**
     * Get the second callee-saved register
     *
     * @return register name
     */
    public String getS1Reg() {
        return registers[20];
    }

    /**
     * Get the third callee-saved register
     *
     * @return register name
     */
    public String getS2Reg() {
        return registers[21];
    }

    /**
     * Get the fourth callee-saved register
     *
     * @return register name
     */
    public String getS3Reg() {
        return registers[22];
    }

    /* Methods for generating code to start the data and text sections */

    /**
     * Generate a comment
     *
     * @param text text to put in comment
     */
    public void genComment(String text) {
        out.println("\t# " + text);
    }

    /**
     * Generate the code to start the data section
     */
    public void genDataStart() {
        out.println("\t.data");
        genGlobal("gc_flag");
        genGlobal("class_name_table");
//        genGlobal("Main_template");
//        genGlobal("String_template");
//        genGlobal("String_dispatch_table");
    }

    /**
     * Generate the code to start the text section
     */
    public void genTextStart() {
        out.println("\t.text");
        genGlobal("main");
        genGlobal("Main_init");
        genGlobal("bantam.Main.main");
        // main (below) defined only because SPIM requires it -- not used
        genLabel("main");
        // if this gets called for some reason then just call __start
        genDirCall("__start");
    }

    /* Data generation methods used by the code generator */

    /**
     * Generate a global
     *
     * @param label label to make global
     */
    public void genGlobal(String label) {
        out.println("\t.globl\t" + label);
    }

    /**
     * Generate a data word
     *
     * @param dataWord word string
     */
    public void genWord(String dataWord) {
        out.println("\t.word\t" + dataWord);
    }

    /**
     * Generate a data byte
     *
     * @param dataByte byte string
     */
    public void genByte(String dataByte) {
        out.println("\t.byte\t" + dataByte);
    }

    /**
     * Generate a data segment of size n
     *
     * @param n size of data segment
     */
    public void genSpace(int n) {
        out.println("\t.space\t" + n);
    }

    /**
     * Generate an ASCII string (terminates with zero byte and aligns)
     *
     * @param ascii ASCII string
     */
    public void genAscii(String ascii) {
        out.print("\t.ascii\t\"");

//        for (int i = 0; i < ascii.length(); i++) {
//            if (ascii.charAt(i) == '\n') {
//                out.print("\\n");
//            }
//            else if (ascii.charAt(i) == '\t') {
//                out.print("\\t");
//            }
//            else if (ascii.charAt(i) == '\f') {
//                out.print("\\f");
//            }
//            else if (ascii.charAt(i) == '\"') {
//                out.print("\\\"");
//            }
//            else if (ascii.charAt(i) == '\\') {
//                out.println("\"");
//                out.println("\t.byte\t0xA");
//                out.print("\t.ascii\t\"");
//            }
//            else {
//                out.print(ascii.charAt(i));
//            }
//        }
        /* DJS: the code above was replaced by the code below to work with MARS 4.4 */
        for (int i = 0; i < ascii.length(); i++) {
            if (ascii.charAt(i) == '\\' & i < ascii.length()-1) {
                out.println("\"");
                if (ascii.charAt(i+1) == 'n') {
                    out.println("\t.byte\t0xA");
                }
                else if (ascii.charAt(i+1) == 't') {
                    out.println("\t.byte\t0x9");
                }
                else if (ascii.charAt(i+1) == 'f') {
                    out.println("\t.byte\t0xC");
                }
                else if (ascii.charAt(i+1) == '"') {
                    out.println("\t.byte\t0x22");
                }
                out.print("\t.ascii\t\"");
                i++;
            }
            else if (ascii.charAt(i) == '\\') {
                out.println("\"");
                out.println("\t.byte\t0x5c");
                out.print("\t.ascii\t\"");
            }
            else {
                out.print(ascii.charAt(i));
            }
        }

        out.println("\"");
        out.println("\t.byte\t0");
        genAlign();
    }

    /**
     * Generate word alignment directive
     */
    public void genAlign() {
        out.println("\t.align\t" + (getWordSize() / 2));
    }

    /* Code generation methods used by the code generator */

    /**
     * Check whether a register is a valid Mips register
     * Throws exception if not valid
     *
     * @param reg register to check
     */
    private void checkReg(String reg) {
        for (String register : registers) {
            if (reg.equals(register)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid register name");
    }

    /**
     * Check whether an immediate is word aligned
     * Throws exception if not word aligned
     *
     * @param offset register to check
     */
    private void checkWordOffset(int offset) {
        if (offset % getWordSize() != 0) {
            throw new IllegalArgumentException("Word offset ('" + offset + "') must be multiple of " +
                    getWordSize());
        }
    }

    /**
     * Get a unique label for use with control flow
     *
     * @return label string
     */
    public String getLabel() {
        return "label" + labelNum++;
    }

    /**
     * Get the word size
     *
     * @return word size
     */
    public int getWordSize() {
        return 4;
    }

    /**
     * Generate a load word instruction
     *
     * @param destReg string containing the destination register
     * @param offset  integer offset (must be a multiple of the word size)
     * @param baseReg string containing the base register
     */
    public void genLoadWord(String destReg, int offset, String baseReg) {
        checkReg(destReg);
        checkReg(baseReg);
        checkWordOffset(offset);
        out.println("\tlw " + destReg + " " + offset + "(" + baseReg + ")");
    }

    /**
     * Generate a load byte instruction
     *
     * @param destReg string containing the destination register
     * @param offset  integer offset
     * @param baseReg string containing the base register
     */
    public void genLoadByte(String destReg, int offset, String baseReg) {
        checkReg(destReg);
        checkReg(baseReg);
        out.println("\tlb " + destReg + " " + offset + "(" + baseReg + ")");
    }

    /**
     * Generate a store word instruction
     *
     * @param srcReg  string containing the source register
     * @param offset  integer offset (must be a multiple of the word size)
     * @param baseReg string containing the base register
     */
    public void genStoreWord(String srcReg, int offset, String baseReg) {
        checkReg(srcReg);
        checkReg(baseReg);
        checkWordOffset(offset);
        out.println("\tsw " + srcReg + " " + offset + "(" + baseReg + ")");
    }

    /**
     * Generate a store byte instruction
     *
     * @param srcReg  string containing the source register
     * @param offset  integer offset (must be a multiple of the word size)
     * @param baseReg string containing the base register
     */
    public void genStoreByte(String srcReg, int offset, String baseReg) {
        checkReg(srcReg);
        checkReg(baseReg);
        out.println("\tsb " + srcReg + " " + offset + "(" + baseReg + ")");
    }

    /**
     * Generate a load address instruction
     *
     * @param destReg string containing the destination register
     * @param label   address to load into destination register
     */
    public void genLoadAddr(String destReg, String label) {
        checkReg(destReg);
        out.println("\tla " + destReg + " " + label);
    }

    /**
     * Generate a load immediate instruction
     *
     * @param destReg string containing the destination register
     * @param imm     immediate to load into destination register
     */
    public void genLoadImm(String destReg, int imm) {
        checkReg(destReg);
        out.println("\tli " + destReg + " " + imm);
    }

    /**
     * Generate a generic binary operation
     *
     * @param op       string containing the particular operator (e.g., add, sub)
     * @param destReg  string containing the destination register
     * @param srcReg   string containing the source register
     * @param operand2 string containing the second operand (either a register or an immediate)
     */
    public void genBinaryOp(String op, String destReg, String srcReg,
                             String operand2) {
        checkReg(destReg);
        checkReg(srcReg);
        out.println("\t" + op + " " + destReg + " " + srcReg + " " + operand2);
    }

    /**
     * Generate an add instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genAdd(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("add", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate an add instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genAdd(String destReg, String srcReg, int imm) {
        genBinaryOp("add", destReg, srcReg, "" + imm);
    }

    /**
     * Generate a subtraction instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genSub(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("sub", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate a subtraction instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genSub(String destReg, String srcReg, int imm) {
        genBinaryOp("sub", destReg, srcReg, "" + imm);
    }

    /**
     * Generate a multiply instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genMul(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("mul", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate a multiply instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genMul(String destReg, String srcReg, int imm) {
        genBinaryOp("mul", destReg, srcReg, "" + imm);
    }

    /**
     * Generate a divide instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genDiv(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("div", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate a divide instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genDiv(String destReg, String srcReg, int imm) {
        genBinaryOp("div", destReg, srcReg, "" + imm);
    }

    /**
     * Generate a modulus instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genMod(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("rem", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate a modulus instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genMod(String destReg, String srcReg, int imm) {
        genBinaryOp("rem", destReg, srcReg, "" + imm);
    }

    /**
     * Generate an and instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genAnd(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("and", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate an and instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genAnd(String destReg, String srcReg, int imm) {
        genBinaryOp("and", destReg, srcReg, "" + imm);
    }

    /**
     * Generate an or instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genOr(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("or", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate an or instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genOr(String destReg, String srcReg, int imm) {
        genBinaryOp("or", destReg, srcReg, "" + imm);
    }

    /**
     * Generate an xor instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genXor(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("xor", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate an xor instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genXor(String destReg, String srcReg, int imm) {
        genBinaryOp("xor", destReg, srcReg, "" + imm);
    }

    /**
     * Generate a shift left instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genShiftLeft(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("sll", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate a shift left instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genShiftLeft(String destReg, String srcReg, int imm) {
        genBinaryOp("sll", destReg, srcReg, "" + imm);
    }

    /**
     * Generate a shift right instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg1 string containing the first source register
     * @param srcReg2 string containing the second source register
     */
    public void genShiftRight(String destReg, String srcReg1, String srcReg2) {
        checkReg(srcReg2);
        genBinaryOp("srl", destReg, srcReg1, srcReg2);
    }

    /**
     * Generate a shift right instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     * @param imm     signed immediate value
     */
    public void genShiftRight(String destReg, String srcReg, int imm) {
        genBinaryOp("srl", destReg, srcReg, "" + imm);
    }

    /**
     * Generate a generic unary operation
     *
     * @param op      string containing the particular operator (.e.g., not)
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     */
    private void genUnaryOp(String op, String destReg, String srcReg) {
        checkReg(destReg);
        checkReg(srcReg);
        out.println("\t" + op + " " + destReg + " " + srcReg);
    }

    /**
     * Generate a move instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     */
    public void genMove(String destReg, String srcReg) {
        genUnaryOp("move", destReg, srcReg);
    }

    /**
     * Generate a negation instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     */
    public void genNeg(String destReg, String srcReg) {
        genUnaryOp("neg", destReg, srcReg);
    }

    /**
     * Generate a not instruction
     *
     * @param destReg string containing the destination register
     * @param srcReg  string containing the source register
     */
    public void genNot(String destReg, String srcReg) {
        genUnaryOp("not", destReg, srcReg);
    }

    /**
     * Generate a reference label
     *
     * @param label label string
     */
    public void genLabel(String label) {
        out.println(label + ":");
    }

    /**
     * Generate a direct call
     *
     * @param label label string
     */
    public void genDirCall(String label) {
        out.println("\tjal " + label);
    }

    /**
     * Generate an indirect call
     *
     * @param reg register containing callee address
     */
    public void genInDirCall(String reg) {
        checkReg(reg);
        out.println("\tjalr " + reg);
    }

    /**
     * Generate a return
     */
    public void genRetn() {
        out.println("\tjr " + getRAReg());
    }

    /**
     * Generate an unconditional branch
     *
     * @param label label string
     */
    public void genUncondBr(String label) {
        out.println("\tb " + label);
    }

    /**
     * Generate a conditional branch
     * branches if first operand is equal to second operand
     *
     * @param reg1  first register to compare
     * @param reg2  second register to compare
     * @param label label to branch to
     */
    private void genCondBr(String op, String reg1, String reg2,
                           String label) {
        checkReg(reg1);
        checkReg(reg2);
        out.println("\t" + op + " " + reg1 + " " + reg2 + " " + label);
    }

    /**
     * Generate a conditional branch
     * branches if first operand is equal to second operand
     *
     * @param reg1  first register to compare
     * @param reg2  second register to compare
     * @param label label to branch to
     */
    public void genCondBeq(String reg1, String reg2, String label) {
        genCondBr("beq", reg1, reg2, label);
    }

    /**
     * Generate a conditional branch
     * branches if first operand is not equal to second operand
     *
     * @param reg1  first register to compare
     * @param reg2  second register to compare
     * @param label label to branch to
     */
    public void genCondBne(String reg1, String reg2, String label) {
        genCondBr("bne", reg1, reg2, label);
    }

    /**
     * Generate a conditional branch
     * branches if first operand is less than or equal to second operand
     *
     * @param reg1  first register to compare
     * @param reg2  second register to compare
     * @param label label to branch to
     */
    public void genCondBlt(String reg1, String reg2, String label) {
        genCondBr("blt", reg1, reg2, label);
    }

    /**
     * Generate a conditional branch
     * branches if first operand is less than second operand
     *
     * @param reg1  first register to compare
     * @param reg2  second register to compare
     * @param label label to branch to
     */
    public void genCondBleq(String reg1, String reg2, String label) {
        genCondBr("ble", reg1, reg2, label);
    }

    /**
     * Generate a conditional branch
     * branches if first operand is greater than second operand
     *
     * @param reg1  first register to compare
     * @param reg2  second register to compare
     * @param label label to branch to
     */
    public void genCondBgt(String reg1, String reg2, String label) {
        genCondBr("bgt", reg1, reg2, label);
    }

    /**
     * Generate a conditional branch
     * branches if first operand is greater than or equal to second operand
     *
     * @param reg1  first register to compare
     * @param reg2  second register to compare
     * @param label label to branch to
     */
    public void genCondBgeq(String reg1, String reg2, String label) {
        genCondBr("bge", reg1, reg2, label);
    }

    /**
     * Generate a system call
     *
     * @param syscallId the system call number
     */
    public void genSyscall(int syscallId) {
        // syscall number is passed via $v0 (for spim)
        if (syscallId == SYSCALL_EXIT) {
            out.println("\tli $v0 17");
        }
        else if (syscallId == SYSCALL_FILE_OPEN) {
            out.println("\tli $v0 13");
        }
        else if (syscallId == SYSCALL_FILE_CLOSE) {
            out.println("\tli $v0 16");
        }
        else if (syscallId == SYSCALL_FILE_READ) {
            out.println("\tli $v0 14");
        }
        else if (syscallId == SYSCALL_FILE_WRITE) {
            out.println("\tli $v0 15");
        }
        else if (syscallId == SYSCALL_GET_TIME) {
            out.println("\tli $v0 18");
        }
        else if (syscallId == SYSCALL_SBRK) {
            out.println("\tli $v0 9");
        }
        else {
            throw new RuntimeException("bad syscall identifier");
        }
        out.println("\tsyscall");
    }
}
