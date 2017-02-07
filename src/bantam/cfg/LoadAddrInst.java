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

/** A class representing a three-address code load address instruction 
  * (LoadAddrInst).  A load address instruction consists of a 
  * destination variable and a source address (as a String label), which
  * are both inherited from LoadInst.  From TACInst it also inherits an 
  * opcode (LDADDR).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class LoadAddrInst extends LoadInst {
    /** LoadAddrInst constructor
      * @param destination destination variable
      * @param source source address (as a String label)
      * */
    public LoadAddrInst(String destination, String source) {
	super(TACInst.LDADDR, destination, source);

	// add comment so instruction can be differentiated from
	// other types of loads
	addComment("load address instruction");

	// some error checking

	// opcode and destination error checked in super class
	// only need to error check source

	// check that source is valid label
	if (!TACInst.checkLabel(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in LoadAddrInst constructor; " +
					       "must be a legal label");
    }

    /** Set the source operand
      * @param source new source operand
      * */
    public void setSource(String source) {
	if (!TACInst.checkLabel(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in LoadAddrInst.setSource; " +
					       "must be a legal label");
	this.source = source;
    }
}
