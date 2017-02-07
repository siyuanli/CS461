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

/** A class representing a three-address code load constant instruction 
  * (LoadConstInst).  A load constant instruction consists of a 
  * destination variable and a source constant (int, boolean, or 
  * String), which are both inherited from LoadInst.  It also contains
  * (non-inherited) type, which indicates the constant type (int, boolean,
  * or String).  From TACInst it also inherits an opcode (LDCONST).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class LoadConstInst extends LoadInst {
    /** Integer constant ID */
    public static final int INT = 0;
    /** Boolean constant ID */
    public static final int BOOL = 1;
    /** String constant ID */
    public static final int STR = 2;
    /** Null constant ID */
    public static final int NULL = 3;

    /** Type of the constant (e.g., LoadConstInt.INT) */
    private int type = -1;

    /** LoadConstInst constructor
      * @param destination destination variable
      * @param source source constant (int, boolean, String, or null -- String needs double quotes)
      * */
    public LoadConstInst(String destination, String source) {
	super(TACInst.LDCONST, destination, source);

	// add comment so instruction can be differentiated from
	// other types of loads
	addComment("load constant instruction");

	// some error checking

	// opcode and destination error checked in super class
	// only need to error check source

	// check that source is valid constant -- 3 possible cases: 
	// int, boolean, or String

	// first check if null
	if (source == null)
	    throw new IllegalArgumentException("Bad source constant in " +
					       "LoadConstInst constructor; " +
					       "must be non-null");
	
	if (TACInst.checkIntConst(source))
	    type = INT;
	else if (TACInst.checkBoolConst(source))
	    type = BOOL;
	else if (TACInst.checkStrConst(source))
	    type = STR;
	else if (source.equals("null"))
	    type = NULL;
	else
	    throw new IllegalArgumentException("Bad source constant '" +
					       source + "' in LoadConstInst constructor; " +
					       "must be a legal constant (boolean, int, " + 
					       "String, or null)");
    }

    /** Get the type of the constant (e.g., LoadConstInst.INT)
      * @return constant type
      * */
    public int getType() {
	return type;
    }

    /** Set the source operand
      * @param source new source operand
      * */
    public void setSource(String source) {
	if (TACInst.checkIntConst(source))
	    type = INT;
	else if (TACInst.checkBoolConst(source))
	    type = BOOL;
	else if (TACInst.checkStrConst(source))
	    type = STR;
	else if (source.equals("null"))
	    type = NULL;
	else
	    throw new IllegalArgumentException("Bad source constant '" +
					       source + "' in LoadConstInst.setSource; " +
					       "must be a legal constant (boolean, int, " + 
					       "String, or null)");
	this.source = source;
    }
}
