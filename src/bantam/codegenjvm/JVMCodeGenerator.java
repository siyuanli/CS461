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

package bantam.codegenjvm;

import bantam.util.*;

/** The <tt>JVMCodeGenerator</tt> class generates bytecodes targeted for the JVM.
  * 
  * This class is incomplete and will need to be implemented by the student.
  * */
public class JVMCodeGenerator {
    /** Root of the class hierarchy tree */
    private ClassTreeNode root;

    /** Boolean indicating whether debugging is enabled */
    private boolean debug = false;

    /** JVMCodeGenerator constructor
      * @param root root of the class hierarchy tree
      * @param debug flag indicating whether debugging enabled
      * */
    public JVMCodeGenerator(ClassTreeNode root, boolean debug) {
	this.root = root;
	this.debug = debug;
    }

    /** Generate program 
      * Creates a textual bytecode file for each class, which can be 
      * translated into bytecodes via a program like Jasmin
      * */
    public void generate() {
	// comment out
	throw new RuntimeException("JVM code generator unimplemented");

	// add code below...
    }
}
