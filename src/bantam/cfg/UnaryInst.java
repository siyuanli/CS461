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

/** A generic class representing a three-address code unary instruction 
  * (UnaryInst).  It is extended by several specific unary instructions
  * (UnaryNegInst and UnaryNotInst).  A unary instruction consists of a 
  * destination variable and a source operand  (the types allowed for the 
  * source depends on the particular type of unary instruction).  From 
  * TACInst it also inherits an opcode.
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public abstract class UnaryInst extends TACInst {
    /** Destination variable */
    protected String destination;

    /** Source operand (type of source allowed varies in different unary instructions) */
    protected String source;

    /** UnaryInst constructor
      * @param opcode opcode of unary instruction
      * @param destination destination variable
      * @param source source operand (type of source allowed varies in different unary insn)
      * */
    public UnaryInst(int opcode, String destination, String source) {
	super(opcode);
	this.destination = destination;
	this.source = source;

	// some error checking

	// check that destination is valid variable
	if (!TACInst.checkVar(destination))
	    throw new IllegalArgumentException("Bad destination operand '" +
					       destination + "' in UnaryInst constructor; " +
					       "must be a legal variable");

	// error checking for source done in subclass
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
					       destination + "' in UnaryInst.setDestination; " +
					       "must be a legal variable");
	this.destination = destination;
    }

    /** Get the source operand
      * @return source operand
      * */
    public String getSource() {
	return source;
    }

    /** Set the source operand
      * @param source new source operand
      * */
    public abstract void setSource(String source);

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof UnaryInst))
	    return false;
	UnaryInst inst = (UnaryInst)o;
	if (opcode != inst.getOpcode() || 
	    !destination.equals(inst.getDestination()) ||
	    !source.equals(inst.getSource()))
	    return false;
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	return destination + " = " + getOp() + " " + source + ";";
    }
}
