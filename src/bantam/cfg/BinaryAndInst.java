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

/** A class representing a three-address code binary boolean AND instruction 
  * (BinaryAndInst).  A binary AND instruction consists of a destination variable
  * a left source operand (inherited from BinaryInst) and a right source operand
  * (inherited from BinaryInst).  From TACInst it also inherits an opcode 
  * (BINAND).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class BinaryAndInst extends BinaryInst {
    /** BinaryAndInst constructor
      * @param destination destination variable
      * @param leftSource left source variable
      * @param rightSource right source variable
      * */
    public BinaryAndInst(String destination, String leftSource, String rightSource) {
	super(TACInst.BINAND, destination, leftSource, rightSource);

	// some error checking

	// opcode and destination error checked in super class
	// only need to error check source

	// check that left source is either valid variable or boolean constant
	if (!TACInst.checkVar(leftSource) && !TACInst.checkBoolConst(leftSource))
	    throw new IllegalArgumentException("Bad left source operand '" +
					       leftSource + "' in BinaryAndInst constructor; " +
					       "must be a legal variable or boolean constant");

	// check that right source is either valid variable or boolean constant
	if (!TACInst.checkVar(rightSource) && !TACInst.checkBoolConst(rightSource))
	    throw new IllegalArgumentException("Bad right source operand '" +
					       rightSource + "' in BinaryAndInst constructor; " +
					       "must be a legal variable or boolean constant");
    }

    /** Set the left soure operand
      * @param leftSource new left source operand
      * */
    public void setLeftSource(String leftSource) {
	if (!TACInst.checkVar(leftSource) && !TACInst.checkBoolConst(leftSource))
	    throw new IllegalArgumentException("Bad left source operand '" +
					       leftSource + "' in BinaryAndInst.setSource; " +
					       "must be a legal variable or boolean constant");

	this.leftSource = leftSource;
    }

    /** Set the right soure operand
      * @param rightSource new right source operand
      * */
    public void setRightSource(String rightSource) {
	if (!TACInst.checkVar(rightSource) && !TACInst.checkBoolConst(rightSource))
	    throw new IllegalArgumentException("Bad right source operand '" +
					       rightSource + "' in BinaryAndInst.setSource; " +
					       "must be a legal variable or boolean constant");
	this.rightSource = rightSource;
    }
}
