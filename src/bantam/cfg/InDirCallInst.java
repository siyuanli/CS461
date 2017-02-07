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

/** A class representing a three-address code indirect call instruction (InDirCallInst).
  * An indirect call instruction consists of a target variable holding the address to
  * jump to (inherited from CallInst), an optional destination variable to assign the 
  * result of the call to (inherited from CallInst), and the number of parameters 
  * (ref. and standard) used by the call (inherited from CallInst).  From TACInst it
  * also inherits an opcode (INDIRCALL).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class InDirCallInst extends CallInst {
    /** InDirCallInst constructor
      * @param targVar target variable holding address to jump to
      * @param destVar (Optional) destination variable to assign result of call (null if none)
      * @param numParams number of parameters (both ref. and standard but not error)
      * */
    public InDirCallInst(String targVar, String destVar, int numParams) {
	super(TACInst.INDIRCALL, "indircall", targVar, destVar, numParams);

	// some error checking

	// opcode, destVar, and numParams error checked in super class
	// only need to error check targVar

	// check that target variable is valid
	if (!TACInst.checkVar(targVar))
	    throw new IllegalArgumentException("Bad target variable '" +
					       targVar + "' in InDirCallInst constructor; " +
					       "must be a legal variable name");
    }

    /** Set the target of the call
      * @param target new target
      * */
    public void setTarget(String target) {
	if (!TACInst.checkVar(target))
	    throw new IllegalArgumentException("Bad target variable '" +
					       target + "' in InDirCallInst.setTarget; " +
					       "must be a legal variable name");
	this.target = target;
    }
}
