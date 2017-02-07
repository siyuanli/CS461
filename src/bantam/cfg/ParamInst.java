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

/** A generic class representing a three-address code parameter instruction 
  * (ParamInst).  It is extended by three classes: StdParamInst, RefParamInst,
  * and ErrParamInst.  A parameter instruction consists of a source operand 
  * holding the value of the actual parameter expression.  From TACInst it also 
  * inherits an opcode.
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public abstract class ParamInst extends TACInst {
    /** Type of the parameter (e.g., "stdparam", "refparam", errparam") */
    protected String paramType;

    /** Source operand holding result of the actual parameter expression */
    protected String source;

    /** ParamInst constructor
      * @param opcode opcode of load instruction
      * @param paramType type of the parameter (e.g., "stdparam", "refparam", errparam")
      * @param source source operand holding value of the parameter expression
      * */
    public ParamInst(int opcode, String paramType, String source) {
	super(opcode);
	this.paramType = paramType;
	this.source = source;

	// some error checking

	// check that source is valid variable or constant
	if (!TACInst.checkVar(source) && !TACInst.checkConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in ParamInst constructor; " +
					       "must be a legal variable or constant");
    }

    /** Get the soure operand (result of parameter expression)
      * @return source operand
      * */
    public String getSource() {
	return source;
    }

    /** Set the source operand (result of parameter expression)
      * @param source new source operand
      * */
    public void setSource(String source) {
	if (!TACInst.checkVar(source) && !TACInst.checkConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in ParamInst constructor; " +
					       "must be a legal variable or constant");
	this.source = source;
    }

    /** Get the parameter type (e.g., "stdparam", "refparam", errparam")
      * @return parameter type
      * */
    public String getParamType() {
	return paramType;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof ParamInst))
	    return false;
	ParamInst inst = (ParamInst)o;
	if (opcode != inst.getOpcode() || 
	    !source.equals(inst.getSource()))
	    return false;
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	return paramType + " " + source + ";";
    }
}
