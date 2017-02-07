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

package bantam.util;

/** A class for modeling variable/temporary locations, either in memory
  * or in a register 
  * */
public class Location {
    /** Boolean indicating whether a value is located in memory or in a register */
    private boolean inMemory;
    /** Base register when value is located in memory */
    private String baseReg;
    /** Offset when value is located in memory */
    private int offset;
    /** Register when value is located in a register */
    private String reg;

    /** Location constructor for memory locations
      * @param baseReg the name of the base register
      * @param offset the offset 
      * */
    public Location(String baseReg, int offset) {
	this.baseReg = baseReg;
	this.offset = offset;
	inMemory = true;
    }

    /** Location constructor for register locations
      * @param reg the name of the register
      * */
    public Location(String reg) {
	this.reg = reg;
	inMemory = false;
    }

    /** Is this location in memory?
      * @return boolean indicating whether stored in memory
      * */
    public boolean isInMemory() {
	return inMemory;
    }

    /** Get the base register (can only be used when location is in memory)
      * @return name of the base register
      * */
    public String getBaseReg() { 
	if (!inMemory)
	    throw new RuntimeException("Location is not in memory");
	return baseReg;
    }

    /** Get the offset (can only be used when location is in memory)
      * @return offset
      * */
    public int getOffset() { 
	if (!inMemory)
	    throw new RuntimeException("Location is not in memory");
	return offset;
    }

    /** Get the register (can only be used when location is in a register)
      * @return name of the register
      * */
    public String getReg() { 
	if (inMemory)
	    throw new RuntimeException("Location is in memory");
	return reg;
    }

    /** Override toString for printing purposes
      * */
    public String toString() {
	if (inMemory)
	    return "Memory address: " + offset + "(" + baseReg + ")";
	else
	    return "Register: " + reg;
    }

    /** Override equals for comparing locations
      * */
    public boolean equals(Object o) {
	if (!(o instanceof Location))
	    return false;
	Location l = (Location)o;
	if (inMemory != l.inMemory)
	    return false;
	if (inMemory) {
	    if (!baseReg.equals(l.baseReg) || offset != l.offset)
		return false;
	}
	else {
	    if (!reg.equals(l.reg))
		return false;
	}
	return true;
    }
}
