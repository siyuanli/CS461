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

/** A class representing a three-address code unary integer negation instruction 
  * (UnaryNegInst).  A unary negation instruction consists of a destination variable
  * and a source variable (inherited from UnaryInst).  From TACInst it also 
  * inherits an opcode (UNNEG).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class UnaryNegInst extends UnaryInst {
    /** UnaryNegInst constructor
      * @param destination destination variable
      * @param source source variable
      * */
    public UnaryNegInst(String destination, String source) {
	super(TACInst.UNNEG, destination, source);

	// some error checking

	// opcode and destination error checked in super class
	// only need to error check source

	// check that source is either valid variable or int constant
	if (!TACInst.checkVar(source) && !TACInst.checkIntConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in UnaryNegInst constructor; " +
					       "must be a legal variable or int constant");
    }

    /** Set the soure operand
      * @param source new source operand
      * */
    public void setSource(String source) {
	if (!TACInst.checkVar(source) && !TACInst.checkIntConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in UnaryNegInst.setSource; " +
					       "must be a legal variable or int constant");
	this.source = source;
    }
}
