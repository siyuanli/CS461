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

/** A class representing a three-address code if instruction (IfInst).
  * An if instruction consists of a condition type ('==', '!=', '<', '>', 
  * '>='), a left and right source operand to conditionally compare, a true 
  * target block, which is taken when the condition is true, and a false 
  * target block, which is taken when the condition is false.  From TACInst 
  * it also inherits an opcode (INDIRCALL).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class IfInst extends TACInst {
    /** Equals ('==') condition type ID */
    public static final int EQ = 0;

    /** Equals ('!=') condition type ID */
    public static final int NE = 1;

    /** Equals ('<') condition type ID */
    public static final int LT = 2;

    /** Equals ('<=') condition type ID */
    public static final int LE = 3;

    /** Equals ('>') condition type ID */
    public static final int GT = 4;

    /** Equals ('>=') condition type ID */
    public static final int GE = 5;

    /** Maximum valid type ID */
    private static final int MAX_TYPE = 5;

    /** Type of condition (e.g., IfInst.EQ for '==', etc.) */
    protected int type;
    
    /** Left source operand in condition */
    protected String leftSource;
    
    /** Right source operand in condition */
    protected String rightSource;
    
    /** Target block when condition is true (jump target) */
    protected BasicBlock trueTarg;

    /** Target block when condition is false (fall through target) */
    protected BasicBlock falseTarg;

    /** IfInst constructor
      * @param type type of condition (e.g., IfInst.EQ for '==', etc.)
      * @param leftSource left source operand in condition
      * @param rightSource right source operand in condition
      * @param trueTarg target block when condition is true (jump target)
      * @param falseTarg target block when condition is false (fall through target)
      * */
    public IfInst(int type, String leftSource, String rightSource,  
		  BasicBlock trueTarg, BasicBlock falseTarg) {
	super(TACInst.IF);
	this.type = type;
	this.leftSource = leftSource;
	this.rightSource = rightSource;
	this.trueTarg = trueTarg;
	this.falseTarg = falseTarg;

	// some error checking

	// check that type is valid
	if (type < 0 || type > MAX_TYPE)
	    throw new IllegalArgumentException("Bad type '" + type +
					       "' in IfInst constructor; " +
					       "must be between 0 and " + 
					       MAX_TYPE);

	// check that left source is valid
	if (!TACInst.checkVar(leftSource) && !TACInst.checkConst(leftSource))
	    throw new IllegalArgumentException("Bad left source operand '" +
					       leftSource + "' in IfInst constructor; " +
					       "must be a legal variable name or constant");

	// check that right source is valid
	if (!TACInst.checkVar(rightSource) && !TACInst.checkConst(rightSource))
	    throw new IllegalArgumentException("Bad right source operand '" +
					       rightSource + "' in IfInst constructor; " +
					       "must be a legal variable name or constant");

	// check that true target is non-null
	if (trueTarg == null)
	    throw new IllegalArgumentException("True target '" + trueTarg + 
					       "' in IfInst constructor cannot be null ");

	// check that false target is non-null
	if (falseTarg == null)
	    throw new IllegalArgumentException("False target '" + falseTarg + 
					       "' in IfInst constructor cannot be null ");
    }

    /** Get the type of If condition
      * @return type of If
      * */
    public int getType() {
	return type;
    }

    /** Set the type of If condition
      * @param type the new type of If
      * */
    public void setType(int type) {
	if (type < 0 || type > MAX_TYPE)
	    throw new IllegalArgumentException("Bad type '" + type +
					       "' in IfInst.setType; " +
					       "must be between 0 and " + 
					       MAX_TYPE);
	this.type = type;
    }

    /** Get the left source operand
      * @return left source operand
      * */
    public String getLeftSource() {
	return leftSource;
    }

    /** Set the left soure operand
      * @param leftSource new left source operand
      * */
    public void setLeftSource(String leftSource) {
	if (!TACInst.checkVar(leftSource) && !TACInst.checkConst(leftSource))
	    throw new IllegalArgumentException("Bad left source operand '" +
					       leftSource + "' in IfInst.setLeftSource; " +
					       "must be a legal variable name or constant");
	this.leftSource = leftSource;
    }

    /** Get the right source operand
      * @return right source operand
      * */
    public String getRightSource() {
	return rightSource;
    }

    /** Set the right soure operand
      * @param rightSource new right source operand
      * */
    public void setRightSource(String rightSource) {
	if (!TACInst.checkVar(rightSource) && !TACInst.checkConst(rightSource))
	    throw new IllegalArgumentException("Bad right source operand '" +
					       rightSource + "' in IfInst.setRightSource; " +
					       "must be a legal variable name or constant");
	this.rightSource = rightSource;
    }

    /** Get the true target block
      * @return true target block
      * */
    public BasicBlock getTrueTarg() {
	return trueTarg;
    }

    /** Set the true target block
      * @param trueTarg new true target block
      * */
    public void setTrueTarg(BasicBlock trueTarg) {
	if (trueTarg == null)
	    throw new IllegalArgumentException("True target '" + trueTarg + 
					       "' in IfInst.setTrueTarg cannot be null ");
	this.trueTarg = trueTarg;
    }

    /** Get the false target block
      * @return false target block
      * */
    public BasicBlock getFalseTarg() {
	return falseTarg;
    }

    /** Get the false target block
      * @param falseTarg new false target block
      * */
    public void setFalseTarg(BasicBlock falseTarg) {
	if (falseTarg == null)
	    throw new IllegalArgumentException("False target '" + falseTarg + 
					       "' in IfInst.setFalseTarg cannot be null ");
	this.falseTarg = falseTarg;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof IfInst))
	    return false;
	IfInst inst = (IfInst)o;
	if (opcode != inst.getOpcode() || 
	    !leftSource.equals(inst.getLeftSource()) ||
	    !rightSource.equals(inst.getRightSource()) ||
	    trueTarg != inst.getTrueTarg() ||
	    falseTarg != inst.getFalseTarg())
	    return false;
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	if (type == EQ)
	    return "if (" + leftSource + " == " + rightSource + ") goto " + trueTarg + ";";
	if (type == NE)
	    return "if (" + leftSource + " != " + rightSource + ") goto " + trueTarg + ";";
	if (type == LT)
	    return "if (" + leftSource + " < " + rightSource + ") goto " + trueTarg + ";";
	if (type == LE)
	    return "if (" + leftSource + " <= " + rightSource + ") goto " + trueTarg + ";";
	if (type == GT)
	    return "if (" + leftSource + " > " + rightSource + ") goto " + trueTarg + ";";
	// if we make it here it must be GE ('>=')
	return "if (" + leftSource + " >= " + rightSource + ") goto " + trueTarg + ";";
    }
}
