/* Bantam Java Compiler and Language Toolset.

   Copyright (C) 2007 by Marc Corliss (corliss@hws.edu) and 
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

package bantam.codegenx86;

import bantam.util.*;
import java.io.*;

/** The <tt>X86CodeGenerator</tt> class generates AT&T (Gnu assembler) 
  * x86 assembly code.
  * 
  * This class is incomplete and will need to be implemented by the student. 
  * */
public class X86CodeGenerator {
    /** Root of the class hierarchy tree */
    private ClassTreeNode root;

    /** Print stream for output assembly file */
    private PrintStream out;

    /** Assembly support object (using x86 assembly support) */
    private X86Support assemblySupport;

    /** Boolean indicating whether optimization is enabled */
    private boolean opt = false;

    /** Boolean indicating whether garbage collection is enabled */
    private boolean gc = false;

    /** Boolean indicating whether debugging is enabled */
    private boolean debug = false;

    /** X86CodeGenerator constructor
      * @param root root of the class hierarchy tree
      * @param outFile filename of the assembly output file
      * @param gc boolean indicating whether garbage collection is enabled
      * @param opt boolean indicating whether optimization is enabled
      * @param debug boolean indicating whether debugging is enabled
      * */
    public X86CodeGenerator(ClassTreeNode root, String outFile, 
			    boolean gc, boolean opt, boolean debug) {
	this.root = root;
	this.gc = gc;
	this.opt = opt;
	this.debug = debug;

	try {
	    out = new PrintStream(new FileOutputStream(outFile));
	    assemblySupport = new X86Support(out);
	}
	catch(IOException e) {
	    // if don't have permission to write to file then report an error and exit
	    System.err.println("Error: don't have permission to write to file '" + outFile + "'");
	    System.exit(1);
	}
    }

    /** Generate assembly file 
      * 
      * In particular, will need to do the following:
      *   1 - start the data section
      *   2 - generate data for the garbage collector
      *   3 - generate string constants
      *   4 - generate class name table
      *   5 - generate object templates
      *   6 - generate dispatch tables
      *   7 - start the text section
      *   8 - generate initialization subroutines
      *   9 - generate user-defined methods
      * See the lab manual for the details of each of these steps.
      * */
    public void generate() {
	// comment out
	throw new RuntimeException("x86 code generator unimplemented");

	// add code below...
    }
}
