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

/** A class representing a three-address code store entry instruction 
  * (StoreEntryInst), which stores a value an entry from an indexable 
  * variable or string constant.  A store entry instruction consists 
  * of a destination operand (which must be indexable, e.g., an
  * object variable), a source operand, and an index (which must be
  * either an int variable or constant).  From TACInst it also 
  * inherits an opcode (STENTRY).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class StoreEntryInst extends TACInst {
    /** Destination operand, which must be indexable (e.g., object variable) */
    protected String destination;

    /** Source operand */
    protected String source;

    /** Index to store entry to in destination operand (either int variable or constant) */
    private String index;

    /** StoreEntryInst constructor
      * @param destination destination operand (which must be indexable, e.g., an object variable)
      * @param source source operand
      * @param index index to store entry to in destination operand (either int variable or constant)
      * */
    public StoreEntryInst(String destination, String source, String index) {
	super(TACInst.STENTRY);

	this.destination = destination;
	this.source = source;
	this.index = index;

	// some error checking

	// opcode and destination error checked in super class
	// only need to error check source and index

	// check that destination is valid variable or string constant
	if (!TACInst.checkVar(destination) && !TACInst.checkStrConst(destination))
	    throw new IllegalArgumentException("Bad destination operand '" +
					       destination + "' in StoreEntryInst constructor; " +
					       "must be a legal variable or string constant");

	// check that source is valid variable or constant
	if (!TACInst.checkVar(source) && !TACInst.checkConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in StoreEntryInst constructor; " +
					       "must be a legal variable or constant");

	// check that index is a variable or int constant
	if (!TACInst.checkVar(index) && !TACInst.checkIntConst(index)) {
	    throw new IllegalArgumentException("Bad index '" +
					       index + "' in StoreEntryInst constructor; " +
					       "must be an int variable or constant");
	}
	if (TACInst.checkIntConst(index)) {
	    // if it's an int constant make sure it's non-negative
	    int idx = Integer.parseInt(index);
	    if (idx < 0)
		throw new IllegalArgumentException("Bad index '" +
						   idx + "' in StoreEntryInst constructor; " +
						   "must be non-negative");
	}
    }

    /** Get the destination operand
      * @return destination operand
      * */
    public String getDestination() {
	return destination;
    }

    /** Set the destination operand
      * @param destination new destination operand
      * */
    public void setDestination(String destination) {
	if (!TACInst.checkVar(destination) && !TACInst.checkStrConst(destination))
	    throw new IllegalArgumentException("Bad destination operand '" +
					       destination + "' in StoreEntryInst.setDestination; " +
					       "must be a legal variable or string constant");
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
    public void setSource(String source) {
	if (!TACInst.checkVar(source) && !TACInst.checkConst(source))
	    throw new IllegalArgumentException("Bad source operand '" +
					       source + "' in StoreEntryInst.setSource; " +
					       "must be a legal variable or constant");
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
					       index + "' in StoreEntryInst.setIndex; " +
					       "must be an int variable or constant");
	}
	if (TACInst.checkIntConst(index)) {
	    int idx = Integer.parseInt(index);
	    if (idx < 0)
		throw new IllegalArgumentException("Bad index '" +
						   idx + "' in StoreEntryInst.setIndex; " +
						   "must be non-negative");
	}
	this.index = index;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof StoreEntryInst))
	    return false;
	StoreEntryInst inst = (StoreEntryInst)o;
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
	return destination + "[" + index + "] = " + source + ";";
    }
}
