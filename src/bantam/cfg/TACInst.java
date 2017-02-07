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

package bantam.cfg;

/** A class representing an individual Three-Adress Code (TAC) 
  * instruction.  This is a generic abstract class, which is extended
  * by the specific instructions:
  * IfInst, CallInst (which is extended by InDirCallInst and DirCallInst), 
  * ReturnInst, ParamInst (which is extended by StdParamInst, RefParamInst, 
  * and ErrParamInst), LoadInst (which is extended by LoadVarInst, 
  * LoadConstInst, LoadAddrInst, LoadEntryInst), StoreEntryInst, 
  * UnaryInst (which is extended by UnaryNegInst and UnaryNotInst), and 
  * BinaryInst (which is extended by BinaryAddInst, BinarySubInst, 
  * BinaryMulInst, BinaryDivInst, BinaryModInst, BinaryAndInst, and 
  * BinaryOrInst).
  * A TACInst contains an opcode and some comments associated with that 
  * instruction.
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
abstract public class TACInst {
    /* Control-flow-related operations */
    /** If opcode */
    public static final int IF = 0;
    /** Call opcode -- used with dispatches */
    public static final int INDIRCALL = 1;
    /** Call opcode -- used with non-dispatches (e.g., calls to error subroutines) */
    public static final int DIRCALL = 2;
    /** Return opcode */
    public static final int RETN = 3;
    /** Parameter opcode */
    public static final int STDPARAM = 4;
    /** Reference parameter opcode */
    public static final int REFPARAM = 5;
    /** Error handling parameter opcode */
    public static final int ERRPARAM = 6;

    /* Load operations */
    /** Load variable (move) opcode */
    public static final int LDVAR = 7;
    /** Load constant opcode */
    public static final int LDCONST = 8;
    /** Load address (i.e., label) opcode */
    public static final int LDADDR = 9;
    /** Load entry (i.e., read an entry in an object) opcode */
    public static final int LDENTRY = 10;

    /* Store operations */
    /** Store entry (i.e., write an entry in an object) opcode */
    public static final int STENTRY = 11;

    /* Unary operations: 
       of the form: d = op s */
    /** Unary (integer) negation opcode */
    public static final int UNNEG = 12;
    /** Unary (boolean) not opcode */
    public static final int UNNOT = 13;

    /* Binary operations: 
       of the form: d = s1 op s2 */
    /* Binary operations */
    /** Binary (integer) addition opcode */
    public static final int BINADD = 14;
    /** Binary (integer) subtraction opcode */
    public static final int BINSUB = 15;
    /** Binary (integer) multiplication opcode */
    public static final int BINMUL = 16;
    /** Binary (integer) division opcode */
    public static final int BINDIV = 17;
    /** Binary (integer) modulus opcode */
    public static final int BINMOD = 18;
    /** Binary (boolean) AND opcode */
    public static final int BINAND = 19;
    /** Binary (boolean) OR opcode */
    public static final int BINOR = 20;

    /** Number of instruction types */
    public static final int NUM_INSN_TYPES = 21;

    /** Instruction operation */
    protected int opcode;

    /** Comments for instruction */
    protected String comments = "";

    /** TACInst constructor
      * @param opcode opcode of instruction
      * */
    public TACInst(int opcode) {
	this.opcode = opcode;

	// check that the opcode is valid
	if (opcode < 0 || opcode >= NUM_INSN_TYPES)
	    throw new IllegalArgumentException("Bad opcode (" +
					       opcode + 
					       ") in TACInst constructor");
    }

    /** Is this an if instruction?
      * @return flag indicating whether if
      * */
    public boolean isIf() {
	return (this instanceof IfInst);
    }

    /** Is this a call instruction?
      * @return flag indicating whether call
      * */
    public boolean isCall() {
	return (this instanceof CallInst);
    }

    /** Is this a return instruction?
      * @return flag indicating whether return
      * */
    public boolean isReturn() {
	return (this instanceof ReturnInst);
    }

    /** Is this a parameter instruction?
      * @return flag indicating whether parameter
      * */
    public boolean isParam() {
	return (this instanceof ParamInst);
    }

    /** Is this a load instruction?
      * @return flag indicating whether load
      * */
    public boolean isLoad() {
	return (this instanceof LoadInst);
    }

    /** Is this a store instruction?
      * @return flag indicating whether store
      * */
    public boolean isStore() {
	return (this instanceof StoreEntryInst);
    }

    /** Is this an unary instruction?
      * @return flag indicating whether unary 
      * */
    public boolean isUnary() {
	return (this instanceof UnaryInst);
    }

    /** Is this a binary instruction?
      * @return flag indicating whether binary 
      * */
    public boolean isBinary() {
	return (this instanceof BinaryInst);
    }

    /** Get opcode of instruction
      * @return opcode 
      * */
    public int getOpcode() {
	return opcode;
    }

    /** Get the instruction operator as a string
      * Only works for unary and binary operators 
      * (no control flow or load operators)
      * @return instruction operator
      * */
    public String getOp() {
	if (opcode == UNNEG)
	    return "-";
	else if (opcode == UNNOT)
	    return "!";
	else if (opcode == BINADD)
	    return "+";
	else if (opcode == BINSUB)
	    return "-";
	else if (opcode == BINMUL)
	    return "*";
	else if (opcode == BINDIV)
	    return "/";
	else if (opcode == BINMOD)
	    return "%";
	else if (opcode == BINAND)
	    return "&&";
	else if (opcode == BINOR)
	    return "||";
	else
	    throw new RuntimeException("Bad opcode (" + opcode + 
				       ") in TACInst.getOp");
    }

    /** Convert string representation of unary operator to an opcode
      * (doesn't work for other operators)
      * @return instruction opcode
      * */
    public static int getUnaryOpcode(String op) {
	if (op.equals("-"))
	    return UNNEG;
	else if (op.equals("!"))
	    return UNNOT;
	else
	    throw new RuntimeException("Bad operator in " + 
				       "TACInst.getOpcode");
    }

    /** Convert string representation of binary operator to an opcode
      * (doesn't work for other operators)
      * @return instruction opcode
      * */
    public static int getBinaryOpcode(String op) {
	if (op.equals("+"))
	    return BINADD;
	else if (op.equals("-"))
	    return BINSUB;
	else if (op.equals("*"))
	    return BINMUL;
	else if (op.equals("/"))
	    return BINDIV;
	else if (op.equals("%"))
	    return BINMOD;
	else if (op.equals("&&"))
	    return BINAND;
	else if (op.equals("||"))
	    return BINOR;
	else
	    throw new RuntimeException("Bad operator in " + 
				       "TACInst.getBinaryOpcode");
    }

    /** Get comments for instruction
      * @return comments for instruction
      * */
    public String getComments() {
	return comments;
    }

    /** Add a comment to instruction (for debugging)
      * (this can be called multiple times for multi-line comments)
      * @param comment comment to add
      * */
    public void addComment(String comment) {
	// is this the first comment?
	if (comments.equals(""))
	    // if so set comments to this comment
	    comments = " # " + comment;
	// otherwise, multi-line comment
	else {
	    // for multi-line comments we need to align comments properly
	    String space = "";
	    int idx = getString().length();

	    // create space for aligning next comment
	    for (int i = 0; i < idx; i++) space = space + " ";

	    // append comment
	    comments = comments + "\n" + space + " # " + comment;
	}
    }

    /** Remove comments from an instruction
      * */
    public void removeComments() {
	comments = "";
    }

    /** Check that a label is legal
      * @param label label to check
      * @return boolean indicating whether label is legal
      * */
    public static boolean checkLabel(String label) {
	if (label == null)
	    return false;
	if (checkConst(label))
	    return false;
	// remove '.' and '_', which are legal in a label and then
	// check if what remains is a valid ID
	String newLabel = label.replace('_', 'a').replace('.', 'a');
	if (isValidID(newLabel))
	    return true;
	return false;
    }

    /** Check that a variable name is legal
      * @param var variable to check
      * @return boolean indicating whether variable is legal
      * */
    public static boolean checkVar(String var) {
	// variable name must be non-null and have at least 3 characters
	if (var == null || var.length() < 3)
	    return false;

	if (var.equals("this"))
	    return true;

	int index = var.indexOf("@");

	// variable name must contain special character '@'
	if (index < 0)
	    return false;

	// '@' cannot be the last character
	if (index == var.length()-1)
	    return false;

	// get strings before and after '@'
	String prefix = var.substring(0, index);
	String postfix = var.substring(index+1, var.length());

	// is there a prefix -- none for compiler-generated temporaries
	if (prefix.length() > 0) {
	    // if prefix is included, check that prefix is a valid source variable name
	    if (!TACInst.isValidID(prefix))
		return false;

	    // must also make sure that postfix is either "l", "p", or "f_<classname>"
	    // (can't be temporary 't' or 'o')

	    // check for local ('l') or parameter ('p')
	    if (postfix.equals("l") || postfix.equals("p"))
		return true;

	    // check for field ('f'), should be followed by '_' and class name
	    if (postfix.length() < 3 || postfix.charAt(0) != 'f' || 
		postfix.charAt(1) != '_')
		return false;

	    // get class name and check that it is a valid id
	    String cname = postfix.substring(2);
	    if (!TACInst.isValidID(cname))
		return false;

	    return true;
	}

	// otherwise, must be compiler generated temporary
	else {
	    if (postfix.length() < 2)
		return false;

	    if (postfix.charAt(0) != 't' && postfix.charAt(0) != 'o')
		return false;

	    String numStr = postfix.substring(1);
	    try {
		int n = Integer.parseInt(numStr);
		return true;
	    }
	    catch (NumberFormatException e) {
		return false;
	    }
	}
    }

    /** Check that string is int constant
      * @param str string to check
      * @return boolean indicating whether string is an int
      * */
    public static boolean checkIntConst(String str) {
	if (str == null) 
	    return false;
	try {
	    // try converting it to an int
	    int n = Integer.parseInt(str);
	    // if we make it here, then it's an int
	    return true;
	}
	catch (NumberFormatException e) {
	    // if an exception occurs then it's not an int
	    return false;
	}
    }

    /** Check that string is boolean constant
      * @param str string to check
      * @return boolean indicating whether string is a boolean
      * */
    public static boolean checkBoolConst(String str) {
	if (str == null) 
	    return false;
	if (str.equals("true") || str.equals("false"))
	    return true;
	return false;
    }

    /** Check that string is String constant
      * @param str string to check
      * @return boolean indicating whether string is a String
      * */
    public static boolean checkStrConst(String str) {
	if (str == null)
	    return false;
	if (str.length() >= 2 && str.charAt(0) == '\"' && 
	    str.charAt(str.length()-1) == '\"')
	    return true;
	return false;
    }

    /** Check that string is a constant (int, boolean, String, or null)
      * @param str string to check
      * @return boolean indicating whether string is a constant
      * */
    public static boolean checkConst(String str) {
	if (str == null)
	    return false;
	if (checkIntConst(str) || checkBoolConst(str) || checkStrConst(str))
	    return true;
	else if (str.equals("null"))
	    return true;
	return false;
    }

    /** Is this string a legal Bantam Java identifier?
      * @param s string to check
      * @return boolean indicating whether string is legal ID
      * */
    public static boolean isValidID(String s) {
	// if null or length is 0 then not valid
	if (s == null || s.length() == 0)
	    return false;

	// make sure not 'true' or 'false'
	if (s.equals("true") || s.equals("false"))
	    return false;

	// make sure first character is a letter
	char c = s.charAt(0);
	if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')))
	    return false;
	for (int i = 1; i < s.length(); i++) {
	    c = s.charAt(i);
	    if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
		  (c >= '0' && c <= '9') || c == '_'))
		return false;
	}

	return true;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public abstract boolean equals(Object o);

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public abstract String getString();

    /** Convert to a string
      * @return string representation of three address code instruction
      * */
    public String toString() {
	return getString() + comments;
    }
}
