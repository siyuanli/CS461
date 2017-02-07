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

/** A class representing a three-address code return instruction (ReturnInst). 
  * An return instruction consists of an optional source operand holding the
  * result of the return expression.  From TACInst it also inherits an opcode 
  * (RETN).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class ReturnInst extends TACInst {
    /** Source operand holding result of the return expression (null if none) */
    protected String source;

    /** ReturnInst constructor
      * @param source source operand holding result of the return expression
      * */
    public ReturnInst(String source) {
	super(TACInst.RETN);
	this.source = source;

	// some error checking

	// check that source is valid variable or constant
	if (source != null) {
	    if (!TACInst.checkVar(source) && !TACInst.checkConst(source))
		throw new IllegalArgumentException("Bad source operand '" +
						   source + "' in ReturnInst constructor; " +
						   "must be a legal variable or constant");
	}
    }

    /** Get the soure operand (result of return expression, null if none)
      * @return source operand
      * */
    public String getSource() {
	return source;
    }

    /** Set the soure operand (result of return expression, to null if none)
      * @param source new source operand
      * */
    public void setSource(String source) {
	if (source != null) {
	    if (!TACInst.checkVar(source) && !TACInst.checkConst(source))
		throw new IllegalArgumentException("Bad source operand '" +
						   source + "' in ReturnInst constructor; " +
						   "must be a legal variable or constant");
	}
	this.source = source;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof ReturnInst))
	    return false;
	ReturnInst inst = (ReturnInst)o;
	if (!source.equals(inst.getSource()))
	    return false;
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	if (source != null)
	    return "retn " + source + ";";
	return "retn;";
    }
}
