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

package bantam.opt;

import bantam.cfg.*;
import bantam.ast.*;
import bantam.util.*;

import java.util.*;

/** The <tt>Optimizer</tt> class optimizes the program.  It first
  * converts the AST into a control flow graph of basic blocks 
  * containing three address code.  It then performs data-flow analyses
  * on the CFG and uses these analyses to transform the CFG.  Finally,
  * it converts the CFG to assembly code.
  * 
  * This class is incomplete and will need to be implemented by the student.
  * */
public class Optimizer {
    /** Root of the class hierarchy tree */
    private ClassTreeNode root;

    /** Level of optimization */
    private int optLevel = 0;

    /** Boolean indicating whether debugging is enabled */
    private boolean debug = false;

    /** Ordered list of class tree nodes (in depth-first order).
      * Will need to add class tree nodes to this list. */
    private static Vector<ClassTreeNode> orderedClassList = new Vector<ClassTreeNode>();

    /** Optimizer constructor
      * @param root root of the class hierarchy tree
      * @param optLevel level of optimization
      * @param debug boolean indicating whether debugging is enabled
      * */
    public Optimizer(ClassTreeNode root, int optLevel, boolean debug) {
	this.root = root;
	this.optLevel = optLevel;
	this.debug = debug;
    }

    /** Optimize the program
      * In particular, will need to do the following:
      *   0 - build any data structures needed like an ordered
      *       class list (which is needed by print)
      *   1 - build the control flow graph (bantam.cfg)
      *       (a) if bantam.opt level > 1 then perform high-level
      *           transformations as building bantam.cfg
      *   2 - if bantam.opt level > 2 then perform low-level bantam.opt.
      * See the lab manual for the details.
      * */
    public void optimize() {
	// comment out
	throw new RuntimeException("Optimizer unimplemented");

	// add code below...
    }

    /** Print out program
      * */
    public void print() {
	// print the control flow graphs for each initialization subroutine
	for (int i = 0; i < orderedClassList.size(); i++) {
	    // get the next class tree node and its method symbol table
	    ClassTreeNode ctn = orderedClassList.elementAt(i);
	    SymbolTable st = ctn.getMethodSymbolTable();

	    // get the name for the subroutine and look it up
	    // in the symbol table
	    String name = ctn.getName() + "_init";
	    BasicBlock entrance = (BasicBlock)st.lookup(name);

	    // print out the control flow graph
	    if (i > 0) System.out.println("\n\n");
	    System.out.println(name + ":");
	    entrance.printAll();
	}

	// print the control flow graphs for each method
	for (int i = 0; i < orderedClassList.size(); i++) {
	    // get the next class tree node and its method symbol table
	    ClassTreeNode ctn = orderedClassList.elementAt(i);
	    SymbolTable st = ctn.getMethodSymbolTable();

	    // only need to generate code for non-builtin methods
	    if (!ctn.isBuiltIn()) {
		// iterate over members looking for methods
		Iterator iter = ctn.getASTNode().getMemberList().iterator();
		while (iter.hasNext()) {
		    // get next member and see if it is a method
		    Member m = (Member)iter.next();
		    if (m instanceof Method) {
			// if m is a method then get the name for the 
			// method and look it up in the symbol table
			String name = ctn.getName() + "." + ((Method)m).getName();
			BasicBlock entrance = (BasicBlock)st.lookup(name);

			// print out the control flow graph
			System.out.println("\n\n");
			System.out.println(name + ":");
			entrance.printAll();
		    }
		}
	    }
	}
    }
}
