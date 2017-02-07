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

package bantam.lexer;

/** A class for representing tokens in the bantam compiler, passed
  * from the bantam.lexer to the bantam.parser (as the value within a java_cup
  * Symbol). 
  * */
public class Token {
    /** name of the token */
    protected String name;
    /** attribute of the token (represented as a string -- null 
	for no attribute) */
    protected String attribute;
    /** starting line number of token */
    protected int lineNum;

    /** Token constructor
      * @param name name of the token
      * @param attribute attribute of the token (might be null)
      * @param lineNum starting line number of the token
      * */
    public Token(String name, String attribute, int lineNum) {
	this.name = name;
	this.attribute = attribute;
	this.lineNum = lineNum;
    }

    /** Token constructor
      * @param name name of the token
      * @param lineNum starting line number of the token
      * */
    public Token(String name, int lineNum) {
	this.name = name;
	this.lineNum = lineNum;
    }

    /** Get token name
      * @return token name
      * */
    public String getName() {
	return name;
    }

    /** Get token attribute
      * @return token attribute
      * */
    public String getAttribute() {
	return attribute;
    }

    /** Get line number of token 
      * @return line number of token
      * */
    public int getLineNum() {
	return lineNum;
    }

    /** Returns the token's lexeme (as a string)
      * Returns the name if no attribute, otherwise,
      * it returns the attribute
      * @return token lexeme (as a string)
      * */
    public String getLexeme() {
	if (attribute == null)
	    return name;
	return attribute;
    }

    /** Return a pretty-print string representation of the token
      * format: <name, value>
      * @return string representation of token
      * */
    public String toString() { 
	String str = "# line " + lineNum + "\n";
	String newName = name;

	if (name.charAt(0) < 'A' || name.charAt(0) > 'Z')
	    newName = "'" + name + "'";

	if (attribute != null) {
	    if (name.equals("STRING_CONST") || name.equals("LEX_ERROR"))
		str += "<" + newName + ", \"" + attribute + "\">";
	    else
		str += "<" + newName + ", " + attribute + ">";
	}
	else
	    str += "<" + newName + ", >";

	return str;
    }
}
