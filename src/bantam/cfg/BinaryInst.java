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

/** A generic class representing a three-address code binary instruction 
  * (BinaryInst).  It is extended by several specific binary instructions:
  * (BinaryAddInst, BinarySubInst, BinaryMulInst, BinaryDivInst, BinaryModInst,
  * BinaryAndInst, BinaryOrInst, BinaryEqInst, BinaryNEInst, BinaryLTInst,
  * BinaryLEInst, and BinaryGTInst, and BinaryGEInst).  A binary instruction 
  * consists of a destination variable and two source operands (the types 
  * allowed for the two sources depends on the particular type of binary 
  * instruction).  From TACInst it also inherits an opcode.
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public abstract class BinaryInst extends TACInst {
    /** Destination variable */
    protected String destination;

    /** Left source operand (type of source allowed varies in different binary instructions) */
    protected String leftSource;

    /** Right source operand (type of source allowed varies in different binary instructions) */
    protected String rightSource;

    /** BinaryInst constructor
      * @param opcode opcode of binary instruction
      * @param destination destination variable
      * @param leftSource source operand (type of source allowed varies in different binary insn)
      * @param rightSource source operand (type of source allowed varies in different binary insn)
      * */
    public BinaryInst(int opcode, String destination, String leftSource, String rightSource) {
	super(opcode);
	this.destination = destination;
	this.leftSource = leftSource;	
	this.rightSource = rightSource;

	// some error checking

	// check that destination is valid variable
	if (!TACInst.checkVar(destination))
	    throw new IllegalArgumentException("Bad destination operand '" +
					       destination + "' in BinaryInst constructor; " +
					       "must be a legal variable");

	// error checking for sources done in subclass
    }

    /** Get the destination operand variable
      * @return destination operand
      * */
    public String getDestination() {
	return destination;
    }

    /** Set the destination operand variable
      * @param destination new destination operand
      * */
    public void setDestination(String destination) {
	if (!TACInst.checkVar(destination))
	    throw new IllegalArgumentException("Bad destination operand '" +
					       destination + "' in BinaryInst.setDestination; " +
					       "must be a legal variable");
	this.destination = destination;
    }

    /** Get the left source operand
      * @return left source operand
      * */
    public String getLeftSource() {
	return leftSource;
    }

    /** Set the left source operand
      * @param leftSource new left source operand
      * */
    public abstract void setLeftSource(String leftSource);

    /** Get the right source operand
      * @return right source operand
      * */
    public String getRightSource() {
	return rightSource;
    }

    /** Set the right source operand
      * @param rightSource new right source operand
      * */
    public abstract void setRightSource(String rightSource);

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof BinaryInst))
	    return false;
	BinaryInst inst = (BinaryInst)o;
	if (!destination.equals(inst.getDestination()) ||
	    !leftSource.equals(inst.getLeftSource()) ||
	    !rightSource.equals(inst.getRightSource()))
	    return false;
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	return destination + " = " + leftSource + " " + getOp() + " " + rightSource + ";";
    }
}
