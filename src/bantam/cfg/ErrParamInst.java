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

/** A class representing a three-address code error parameter instruction 
  * (ErrParamInst).  These parameters are used in calls to error subroutines
  * to hold things such as filenames and line numbers, etc.  An error parameter 
  * instruction consists of a type (e.g., ErrParamInst.FILENAME, etc.) and a
  * source value (inherited from ParamInst).  From TACInst it also inherits an 
  * opcode (ERRPARAM).
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class ErrParamInst extends ParamInst {
    /** ID for filename error parameter (used in all error calls) */
    public static final int FILENAME = 0;

    /** ID for line number error parameter (used in all error calls) */
    public static final int LINENUM = 1;

    /** ID for object type error parameter (used in class cast errors only) */
    public static final int OBJECTID = 2;

    /** ID for target type error parameter (used in class cast errors only) */
    public static final int TARGETID = 3;
	
    /** ID for array size error parameter (used in extended compiler only) */
    public static final int ARRAYSIZEID = 4;
	
    /** ID for array index error parameter (used in extended compiler only) */
    public static final int ARRAYIDXID = 5;
	
    /** Maximum legal ID */
    private static final int MAX_ID = 5;
	
    /** Type (ID) of error parameter (e.g., ErrParamInst.FILENAME, etc.) */
    protected int type;

    /** ErrParamInst constructor
      * @param type type (ID) of error parameter (e.g., ErrParamInst.FILENAME, etc.)
      * @param value source value of error parameter (as a string) 
      * */
    public ErrParamInst(int type, String value) {
	super(TACInst.ERRPARAM, "errparam", value);
	this.type = type;

	// some error checking

	// note: value is error checked in parent

	// check that type is valid
	if (type < 0 || type > MAX_ID)
	    throw new IllegalArgumentException("Bad type (ID) '" +
					       type + "' in ErrParamInst constructor; " +
					       "must be >=0 and <=" + MAX_ID);

	// check that value is valid -- depends on type of error parameter
	// we ignore filename
	// LINENUM must be non-negative integers,
	if (type == LINENUM) {
	    boolean badVal = true;
	    try {
		int n = Integer.parseInt(value);
		if (n >= 0) badVal = false;
	    }
	    catch (NumberFormatException e) {}

	    if (badVal)
		throw new IllegalArgumentException("Bad value for error parameter '" +
						   value + "' in ErrParamInst constructor " +
						   "(for linenum); " + 
						   "must be integer, >=0");
	}

	// OBJECTID must be a variable
	if (type == OBJECTID) {
	    if (!TACInst.checkVar(value))
		throw new IllegalArgumentException("Bad value for error parameter '" +
						   value + "' in ErrParamInst constructor " +
						   "(for objectid); " +
						   "must be valid variable");
	}

	// TARGETID must be a non-negative integer or variable
	if (type == TARGETID) {
	    if (!TACInst.checkVar(value)) {
		boolean badVal = true;
		try {
		    int n = Integer.parseInt(value);
		    if (n >= 0) badVal = false;
		}
		catch (NumberFormatException e) {}

		if (badVal)
		    throw new IllegalArgumentException("Bad value for error parameter '" +
						       value + "' in ErrParamInst constructor " +
						       "(for typeid); " +
						       "must be non-negative integer or " +
						       "valid variable");
	    }
	}
    }

    /** Get the type (ID) of error parameter (e.g., ErrParamInst.FILENAME, etc.)
      * @return type
      * */
    public int getType() {
	return type;
    }

    /** Compares this object with the parameter object
      * @param o object to compare the reference object with
      * @return boolean indicating whether the objects are equivalent
      * */
    public boolean equals(Object o) {
	if (!(o instanceof ErrParamInst))
	    return false;
	ErrParamInst inst = (ErrParamInst)o;
	if (opcode != inst.getOpcode() || 
	    !source.equals(inst.getSource()) ||
	    type != inst.getType())
	    return false;
	return true;
    }

    /** Get string representation of instruction (without comments)
      * (note: in general, you want to use toString() to get instruction string)
      * @return string representation of three address code instruction (without comments)
      * */
    public String getString() {
	if (type == FILENAME)
	    return "errparam filename, " + source + ";";
	else if (type == LINENUM)
	    return "errparam linenum, " + source + ";";
	else if (type == OBJECTID)
	    return "errparam objectid, " + source + ";";
	else
	    return "errparam targetid, " + source + ";";
    }
}
