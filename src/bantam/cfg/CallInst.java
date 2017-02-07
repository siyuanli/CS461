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

/** A generic class representing a three-address code call instruction (CallInst).
  * It is extended by two specific call instructions: InDirCallInst and DirCallInst.
  * A call instruction consists of a target (possibly a variable or address) to jump
  * to, an optional destination variable to assign the result of the call to, and
  * the number of parameters (ref. and standard) used by the call.  From TACInst it
  * also inherits an opcode.
  * 
  * Note about parameters: 
  *  if at least one parameter is used, then a reference parameter must be supplied.  
  *  For indirect calls, a reference parameter must always be supplied.  If 
  *  parameters are supplied (excluding error parameters), then the reference 
  *  parameter must be supplied first followed by the standard parameters in the 
  *  order that they were listed in the source program. the ordering of error
  *  parameters does not matter.
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public abstract class CallInst extends TACInst {
    /** Type of call (e.g., "indircall", "dircall") */
    protected String callType;
	
    /** Target to jump to */
    protected String target;

    /** (Optional) destination variable to assign result of call (null if none) */
    protected String destVar;

    /** Number of parameters 
	(both reference and standard parameters but not error parameters) */
    private int numParams;

    /** CallInst constructor
      * @param opcode opcode of call instruction
      * @param callType type of call (e.g., "indircall", "dircall")
      * @param target target to jump to
      * @param destVar (Optional) destination variable to assign result of call (null if none)
      * @param numParams number of parameters (both ref. and standard but not error)
      * */
    public CallInst(int opcode, String callType,
		    String target, String destVar, int numParams) {
	super(opcode);
	this.callType = callType;
	this.target = target;
	this.destVar = destVar;
	this.numParams = numParams;

	// some error checking

	// check that destination variable is valid
	if (destVar != null && !TACInst.checkVar(destVar))
	    throw new IllegalArgumentException("Bad destination variable '" +
					       destVar + "' in CallInst constructor; " +
					       "must be a legal variable name");

	// check that number of parameters is non-negative
	if (numParams < 0)
	    throw new IllegalArgumentException("Number of parameters '" + numParams + 
					       "' in CallInst constructor cannot be negative");

	// error checking for target done in subclass
    }

    /** Get the target of the call
      * @return target
      * */
    public String getTarget() {
	return target;
    }

    /** Set the target of the call
      * @param target new target
      * */
    public abstract void setTarget(String target);

    /** Get the destination variable (variable to assign result to -- null if none)
      * @return destination variable
      * */
    public String getDestVar() {
	return destVar;
    }

    /** Set the destination variable (variable to assign result to -- null if none)
      * @param destVar new destination variable
      * */
    public void setDestVar(String destVar) {
	if (destVar != null && !TACInst.checkVar(destVar))
	    throw new IllegalArgumentException("Bad destination variable '" +
					       destVar + "' in CallInst.setDestVar; " +
					       "must be a legal variable name");
	this.destVar = destVar;
    }

    /** Get the number of parameters (both ref. and standard but not error)
      * @return number of parameters
      * */
    public int getNumParam() {
	return numParams;
    }

    /** Set the number of parameters (both ref. and standard but not error)
      * @param numParams new number of parameters
      * */
    public void setNumParam(int numParams) {
	// check that number of parameters is non-negative
	if (numParams < 0)
	    throw new IllegalArgumentException("Number of parameters '" + numParams + 
					       "' in CallInst.setNumParam cannot be negative");
	this.numParams = numParams;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof CallInst))
	    return false;
	CallInst inst = (CallInst)o;
	if (opcode != inst.getOpcode() || 
	    !target.equals(inst.getTarget()) ||
	    numParams != inst.getNumParam())
	    return false;
	if (destVar != null && inst.getDestVar() != null &&
	    !destVar.equals(inst.getDestVar()))
	    return false;
	else if (destVar != inst.getDestVar())
	    return false;	    
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	if (destVar != null)
	    return destVar + " = " + callType + " " + target + ", " + numParams + ";";
	return callType + " " + target + ", " + numParams + ";";
    }
}
