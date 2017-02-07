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

/** A class representing a three-address code load entry instruction 
  * (LoadEntryInst), which loads an entry from an indexable variable
  * or string constant.  A load entry instruction consists of a 
  * destination variable and a source operand (note: the operand 
  * must be indexable, e.g., an object variable), which are both 
  * inherited from LoadInst.  A LoadEntryInst also consists of an 
  * index, which is either an int variable or constant.  From 
  * TACInst it also inherits an opcode (LDENTRY).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class LoadEntryInst extends LoadInst {
    /** Index to load entry from in source operand (either int variable or constant) */
    private String index;

    /** LoadEntryInst constructor
      * @param destination destination variable
      * @param source source object (variable or string constant to load from)
      * @param index index to load entry from in source operand (either int variable or constant)
      * */
    public LoadEntryInst(String destination, String source, String index) {
	super(TACInst.LDENTRY, destination, source);

	this.index = index;

	// some error checking

	// opcode and destination error checked in super class
	// only need to error check source and index

	// check that source is valid variable or string constant
	if (!TACInst.checkVar(source) && !TACInst.checkStrConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in LoadEntryInst constructor; " +
					       "must be a legal variable or string constant");

	// check that index is a variable or int constant
	if (!TACInst.checkVar(index) && !TACInst.checkIntConst(index)) {
	    throw new IllegalArgumentException("Bad index '" +
					       index + "' in LoadEntryInst constructor; " +
					       "must be an int variable or constant");
	}
	if (TACInst.checkIntConst(index)) {
	    // if it's an int constant make sure it's non-negative
	    int idx = Integer.parseInt(index);
	    if (idx < 0)
		throw new IllegalArgumentException("Bad index '" +
						   idx + "' in LoadEntryInst constructor; " +
						   "must be non-negative");
	}
    }

    /** Set the source operand
      * @param source new source operand
      * */
    public void setSource(String source) {
	if (!TACInst.checkVar(source) && !TACInst.checkStrConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in LoadEntryInst.setSource; " +
					       "must be a legal variable or string constant");
	this.source = source;
    }

    /** Get the index
      * @return index
      * */
    public String getIndex() {
	return index;
    }

    /** Set the index
      * @param index new index
      * */
    public void setIndex(String index) {
	if (!TACInst.checkVar(index) && !TACInst.checkIntConst(index)) {
	    throw new IllegalArgumentException("Bad index '" +
					       index + "' in LoadEntryInst.setIndex; " +
					       "must be an int variable or constant");
	}
	if (TACInst.checkIntConst(index)) {
	    int idx = Integer.parseInt(index);
	    if (idx < 0)
		throw new IllegalArgumentException("Bad index '" +
						   idx + "' in LoadEntryInst.setIndex; " +
						   "must be non-negative");
	}
	this.index = index;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof LoadEntryInst))
	    return false;
	LoadEntryInst inst = (LoadEntryInst)o;
	if (!destination.equals(inst.getDestination()) ||
	    !source.equals(inst.getSource()) ||
	    !index.equals(inst.getIndex()))
	    return false;
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	return destination + " = " + source + "[" + index + "];";
    }
}
