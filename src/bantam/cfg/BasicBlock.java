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

import java.util.Vector;
import java.util.Iterator;

/** A class representing a basic block, which is an individual
  * node in the control flow graph.  Each basic block is made
  * up of a label (for control transfers targetting this basic
  * block) and a list of Three-Address Code (TAC) instructions. 
  * For basic blocks that are not reached via a control transfer, 
  * the label is null.
  * 
  * @author Marc Corliss and Lori Pietraszek
  * */
public class BasicBlock {
    /** List of three-address code (TAC) instructions.
      * Empty at first. */
    private Vector<TACInst> insn = new Vector<TACInst>();

    /** List of incoming edges from other basic blocks.
      * Empty at first. */
    private Vector<BasicBlock> inEdges = new Vector<BasicBlock>();

    /** List of outgoing edges to other basic blocks.
      * Empty at first. */
    private Vector<BasicBlock> outEdges = new Vector<BasicBlock>();

    /** Number of total basic blocks */
    private static int numBB = 0;

    /** Basic block identifier */
    private int id;

    /** Starting source line number of this block (for debugging) */
    private int startLineNum;

    /** Basic block loop count (# of loops block is contained in -- 
      * note: predicate is considered part of loop) */
    private int loopCnt;

    /** Basic block if count (# of if statements block is contained in --
      * note: predicate is considered part of if statement) */
    private int ifCnt;

    /** Ordered list of blocks -- note: list only used only for
      * basic blocks that start a subroutine */
    private Vector<BasicBlock> orderedList = new Vector<BasicBlock>();

    /** Flag indicating whether ordered list has been built yet --
      * note: flag used only for basic blocks that start a subroutine */
    private boolean isOrdered = false;

    /** Comments for the basic block */
    private String comments = "";

    /** BasicBlock constructor
      * @param startLineNum starting source line number of this block
      * (for debugging -- does not have to be exact)
      * @param loopCnt loop count (# loops block is contained in -- 
      * note: predicate considered part of loop)
      * @param ifCnt if count (# if statements block is contained in -- 
      * note: predicate considered part of if statement)
      * */
    public BasicBlock(int startLineNum, int loopCnt, int ifCnt) {
	id = numBB++;
	this.startLineNum = startLineNum;
	this.loopCnt = loopCnt;
	this.ifCnt = ifCnt;

	// check that the loop count is valid
	if (loopCnt < 0)
	    throw new IllegalArgumentException("Bad loop count (" +
					       loopCnt + 
					       ") in BasicBlock constructor; " +
					       "must be non-negative");

	// check that the if count is valid
	if (ifCnt < 0)
	    throw new IllegalArgumentException("Bad if count (" +
					       ifCnt + 
					       ") in BasicBlock constructor; " +
					       "must be non-negative");
    }

    /** Get the identifier of this basic block
      * @return identifier
      * */
    public int getID() {
	return id;
    }

    /** Get the starting source line number of this basic block
      * @return starting line number
      * */
    public int getStartLineNum() {
	return startLineNum;
    }

    /** Append instruction into basic block
      * Throws exception if attempting to place an instruction after 
      * an IF (which should terminate block)
      * @param inst three-address code (TAC) instruction
      * */
    public void addInst(TACInst inst) {
	// call addInst using size as index
	addInst(inst, insn.size());
    }

    /** Add instruction into specified place in the basic block
      * Shifts instructions at specified position to the right one
      * Throws exception if n is greater than the size or attempt
      * to place an instruction after an IF (which should terminate block)
      * @param inst three-address code (TAC) instruction
      * @param n position to place 
      * */
    public void addInst(TACInst inst, int n) {
	if (insn.size() < n)
	    throw new IllegalArgumentException("Can't add instruction " +
					       "at index " + n + 
					       " which is greater than size (" +
					       insn.size() + ")");

	// check that call is not adding instruction beyond an IF
	if (insn.size() > 0) {
	    if (insn.elementAt(n-1).getOpcode() == TACInst.IF) {
		String bbStr = this.toString() + ":\n";
		for (int i = 0; i < insn.size(); i++)
		    bbStr += "\t" + insn.elementAt(i).toString() + "\n";
		throw new RuntimeException("Can't add an instruction (" +
					   inst.toString() + ") beyond IF in " +
					   "basic block:\n" + bbStr);
	    }
	}

	insn.add(n, inst);
    }

    /** Get the number of instructions at this basic block
      * @return number of instructions
      * */
    public int getNumInsn() {
	return insn.size();
    }

    /** Get the index of some instruction
      * @param inst instruction to find the index of
      * @return index (<0 if not found)
      * */
    public int indexOf(TACInst inst) {
	return insn.indexOf(inst);
    }

    /** Get the nth instruction
      * Note: throws an exception if there aren't at least n+1 instructions
      * @param n index of instruction to get
      * @return nth instruction
      * */
    public TACInst getNthInst(int n) {
	if (insn.size() <= n)
	    throw new IllegalArgumentException("There is no instruction " +
					       "at index " + n);
	return insn.elementAt(n);
    }

    /** Set the nth instruction -- replaces the previous nth instruction
      * with the specified instruction
      * Note: throws an exception if there aren't at least n+1 instructions
      * @param n index of instruction to get
      * @param inst replacement instruction
      * */
    public void setNthInst(int n, TACInst inst) {
	if (insn.size() <= n)
	    throw new IllegalArgumentException("There is no instruction " +
					       "at index " + n);
	insn.set(n, inst);
    }

    /** Remove the nth instruction
      * Note: throws an exception if there aren't at least n+1 instructions
      * @param n index of instruction to remove
      * */
    public void removeNthInst(int n) {
	if (insn.size() <= n)
	    throw new IllegalArgumentException("There is no instruction " +
					       "at index " + n);
	insn.removeElementAt(n);
    }

    /** Add an incoming edge from another control flow block
      * Also adds outgoing edge from parameter to this block
      * @param b incoming basic block
      * */
    public void addInEdge(BasicBlock b) {
	if (inEdges.size() == 3)
	    throw new RuntimeException("Block " + toString() + 
				       " already has max in edges (3); " +
				       "can't add another in BasicBlock.addInEdge");
	if (!inEdges.contains(b))
	    inEdges.add(b);
	if (!b.outEdges.contains(this))
	    b.outEdges.add(this);
    }

    /** Add an outgoing edge to another control flow block
      * Also adds incoming edge from this block to parameter
      * @param b outgoing basic block
      * */
    public void addOutEdge(BasicBlock b) {
	if (outEdges.size() == 2)
	    throw new RuntimeException("Block " + toString() + 
				       " already has max out edges (2); " +
				       "can't add another in BasicBlock.addOutEdge");
	if (!outEdges.contains(b))
	    outEdges.add(b);
	if (!b.inEdges.contains(this))
	    b.inEdges.add(this);
    }

    /** Remove an incoming edge to another control flow block
      * Also removes outgoing edge from parameter to this block
      * @param b incoming basic block
      * */
    public void removeInEdge(BasicBlock b) {
	if (inEdges.contains(b))
	    inEdges.remove(b);
	if (b.outEdges.contains(this))
	    b.outEdges.remove(this);
    }

    /** Remove an outgoing edge to another control flow block
      * Also removes incoming edge from parameter to this block
      * @param b outgoing basic block
      * */
    public void removeOutEdge(BasicBlock b) {
	if (outEdges.contains(b))
	    outEdges.remove(b);
	if (b.inEdges.contains(this))
	    b.inEdges.remove(this);
    }

    /** Remove all incoming edges from other control flow blocks
      * Also removes outgoing edges from source basic blocks
      * */
    public void removeAllInEdges() {
	while (inEdges.size() >= 0)
	    removeInEdge(inEdges.elementAt(0));
    }

    /** Remove all outgoing edges to other control flow blocks
      * Also removes incoming edges in source basic blocks
      * */
    public void removeAllOutEdges() {
	while (outEdges.size() >= 0)
	    removeOutEdge(outEdges.elementAt(0));
    }

    /** Get nth incoming edge (basic block)
      * @param n index of incoming edge to get
      * @return nth incoming basic block
      * */
    public BasicBlock getNthInEdge(int n) {
	if (n >= inEdges.size())
	    throw new IllegalArgumentException("Incoming edge " + n + 
					       " does not exist for basic block " +
					       toString());

	return inEdges.elementAt(n);
    }

    /** Get nth outgoing edge (basic block)
      * @param n index of outgoing edge to get
      * @return nth outgoing basic block
      * */
    public BasicBlock getNthOutEdge(int n) {
	if (n >= outEdges.size())
	    throw new IllegalArgumentException("Outgoing edge " + n + 
					       " does not exist for basic block " +
					       toString());

	return outEdges.elementAt(n);
    }

    /** Get number of incoming edges
      * @return number of incoming edges
      * */
    public int getNumInEdges() {
	return inEdges.size();
    }

    /** Get number of outgoing edges
      * @return number of outgoing edges
      * */
    public int getNumOutEdges() {
	return outEdges.size();
    }

    /** Does this block contain another block as an in edge?
      * @param bb block to check if contains
      * @return flag indicating whether block is contained as an in edge
      * */
    public boolean containsInEdge(BasicBlock bb) {
	return inEdges.contains(bb);
    }

    /** Does this block contain another block as an out edge?
      * @param bb block to check if contains
      * @return flag indicating whether block is contained as an out edge
      * */
    public boolean containsOutEdge(BasicBlock bb) {
	return outEdges.contains(bb);
    }

    /** Get the loop count for this block, 
      * i.e., number of loops this block is contained in 
      * (note: predicate considered part of the loop)
      * @return loop count
      * */
    public int getLoopCnt() {
	return loopCnt;
    }

    /** Set the loop count for this block, 
      * i.e., number of loops this block is contained in 
      * (note: predicate considered part of the loop)
      * @param loopCnt new loop count
      * */
    public void setLoopCnt(int loopCnt) {
	this.loopCnt = loopCnt;
    }


    /** Get the if count for this block, 
      * i.e., number of if statements this block is contained in 
      * (note: predicate considered part of the if statement)
      * @return if count
      * */
    public int getIfCnt() {
	return ifCnt;
    }

    /** Set the if count for this block, 
      * i.e., number of if statements this block is contained in 
      * (note: predicate considered part of the if)
      * @param ifCnt new if count
      * */
    public void setIfCnt(int ifCnt) {
	this.ifCnt = ifCnt;
    }

    /** Check block for errors
      * throws an exception if block is malformed
      * */
    public void check() {
	// check number of out edges
	if (insn.size() == 0) {
	    if (outEdges.size() != 1)
		throw new RuntimeException("Empty block " + toString() +
					   " with " + outEdges.size() + 
					   " out edges rather than 1; " +
					   "error block from source code near " +
					   "line " + startLineNum);
	    return;
	}
	TACInst last = insn.elementAt(insn.size()-1);
	if (last.isIf() && outEdges.size() != 2)
	    throw new RuntimeException("Block " + toString() + " ends with IF " +
				       "but has " + outEdges.size() +
				       " out edges rather than 2:\n" + 
				       getFullString());
	else if (!last.isIf() && !last.isReturn() && outEdges.size() != 1)
	    throw new RuntimeException("Block " + toString() + " does not end " +
				       "with IF or RETN, but has " + outEdges.size() +
				       " out edges rather than 1:\n" + 
				       getFullString());

	// if last instruction is an if check that if targets match out edges
	if (last.isIf()) {
	    IfInst ifInst = (IfInst)last;
	    if ((ifInst.getTrueTarg() != outEdges.elementAt(0) &&
		 ifInst.getTrueTarg() != outEdges.elementAt(1)) ||
		(ifInst.getFalseTarg() != outEdges.elementAt(0) &&
		 ifInst.getFalseTarg() != outEdges.elementAt(1)))
		throw new RuntimeException("Block " + toString() + " out edges " +
					   "do not match if instruction's targets:\n" +
					   getFullString());
	}

	// compare loop and if counts with predecessor blocks
	if (inEdges.size() > 0) {
	    boolean foundLoopDiff = false;

	    for (int i = 0; i < inEdges.size(); i++) {
		// check loop count
		int diff = (int)Math.abs(loopCnt - inEdges.elementAt(i).getLoopCnt());
		// should be off by at most 1
		if (diff > 1)
		    throw new RuntimeException("Block " + toString() + " and predecessor " +
					       "block differ in loop counts by more than 1; " +
					       "error block from source code near " +
					       "line " + startLineNum);
	    }
	}

	// compare loop counts with successor blocks
	if (outEdges.size() > 0) {
	    boolean foundDiff = false;
	    for (int i = 0; i < outEdges.size(); i++) {
		int diff = (int)Math.abs(loopCnt - outEdges.elementAt(i).getLoopCnt());
		// should be off by at most 1
		if (diff > 1)
		    throw new RuntimeException("Block " + toString() + " and successor " +
					       "block differ in loop counts by more than 1; " +
					       "error block from source code near " +
					       "line " + startLineNum);
		// should be off with no more than 1 block
		else if (diff > 0) {
		    if (foundDiff)
			throw new RuntimeException("Block " + toString() + " has multiple " +
						   "successors with different loop counts; " +
						   "error block from source code near " +
						   "line " + startLineNum);
		    foundDiff = true;
		}
	    }
	}
    }

    /** Get comments for instruction
      * @return comments for instruction
      * */
    public String getComments() {
	return comments;
    }

    /** Add a comment to basic block (for debugging)
      * (this can be called multiple times for multi-line comments)
      * @param comment comment to add
      * */
    public void addComment(String comment) {
	comments = comments + "# " + comment + "\n";
    }

    /** Remove comments from an instruction
      * */
    public void removeComments() {
	comments = "";
    }

    /** Returns string identifier for basic block
      * @return string identifier for basic block
      * */
    public String toString() {
	return "bb" + id;
    }

    /** Returns string representation of entire basic block
      * @return string representation of entire basic block
      * */
    public String getFullString() {
	String result = "# basic block near source line " + startLineNum + "\n" + 
	    comments + toString() + ":\n";
	for (int i = 0; i < insn.size(); i++) {
	    String str = insn.elementAt(i).toString();
	    // if instruction is over multiple lines must insert tabs
	    // after each newline
	    str = str.replaceAll("\n", "\n\t");
	    result += "\t" + str + "\n";
	}
	return result;
    }

    /** Prints basic block -- for debugging
      * */
    public void print() {
	System.out.print(getFullString());
    }

    /** Get ordered list of blocks -- should only be called
      * for starting blocks, otherwise exception is thrown
      * @return iterated list of ordered blocks
      * */
    public Iterator<BasicBlock> getOrderedBlocks() {
	// if not already ordered then order blocks
	if (!isOrdered)
	    orderBlocks();

	return orderedList.iterator();
    }

    /** Orders basic blocks -- uses breadth-first order except
      * for while loop sub-nodes, which are ordered in depth-first
      * fashion and return nodes, which are put last 
      * @return ordered blocks
      * */
    private Vector<BasicBlock> orderBlocksOld() {
	// set ordered flag
	isOrdered = true;

	// check for in edges, if there are in edges then this is not
	// an entrance block
	if (inEdges.size() != 0)
	    throw new RuntimeException("BasicBlock.printAll must be called " +
				       "with an entrance basic block");

	// sort blocks using a combination of breadth-first, depth-first order:
	// all blocks except those that are linked from a while are sorted
	// in breadth first fashion, blocks linked from a while are sorted in
	// depth-first order (so that end block follows the entire while).
	// note: we differentiate while blocks from other blocks since it
	// is the only block with multiple in edges
	Vector<BasicBlock> depthList = new Vector<BasicBlock>();
	BasicBlock lastNode = null;
	
	if (outEdges.size() == 0)
	    lastNode = this;
	else
	    orderedList.add(this);

	for (int i = 0; i < orderedList.size() || depthList.size() > 0; i++) {
	    BasicBlock bb;

	    // get the next block off of the orderedList if one exists
	    // or otherwise go to the depth-first list
	    if (i < orderedList.size())
		bb = orderedList.elementAt(i);
	    else {
		bb = depthList.remove(0);
		orderedList.add(bb);
	    }

	    for (int j = 0; j < bb.outEdges.size(); j++) {
		BasicBlock next = bb.outEdges.elementAt(j);

		// if not already in one of the lists then add it
		if (!orderedList.contains(next) && 
		    !depthList.contains(next) &&
		    lastNode != next) {
		    // is this the last (return node) -- yes if no out edges
		    if (next.outEdges.size() == 0) {
			// if so, store in lastNode, will add to last
			// make sure last node is null
			if (lastNode != null)
			    throw new RuntimeException("Two return nodes found");
			lastNode = next;
		    }
		    
		    else {
			// flag indicating when a node is a while node
			// (block containing the predicate from a while loop)
			boolean whileNode = false;

			// is this a while node -- possibly if it has at least two
			// in edges and exactly two out edges
			if (bb.inEdges.size() >= 2 && bb.outEdges.size() == 2) {
			    // also must be a back edge pointing to this block
			    for (int k = 0; k < bb.inEdges.size(); k++) {
				BasicBlock in = bb.inEdges.elementAt(k);
				if (!orderedList.contains(in) && 
				    !depthList.contains(in) &&
				    lastNode != in) {
				    whileNode = true;
				    break;
				}
			    }
			}

			// if while node and next is the second (end) block, 
			// add next block to separate list, using depth-first searching
			if (whileNode && j > 0)
			    depthList.add(0, next);
			// otherwise, add to main list and use breadth-first searching
			else
			    orderedList.add(next);
		    }
		}
	    }
	}

	// add last node to orderedList
	// make sure non-null
	if (lastNode == null) throw new RuntimeException("Last return node not found");
	orderedList.add(lastNode);

	return orderedList;
    }

    /** Orders basic blocks -- uses depth-first order
      * @return ordered blocks
      * */
    private Vector<BasicBlock> orderBlocks() {
	// set ordered flag
	isOrdered = true;

	// check for in edges, if there are in edges then this is not
	// an entrance block
	if (inEdges.size() != 0)
	    throw new RuntimeException("BasicBlock.printAll must be called " +
				       "with an entrance basic block");

	// sort blocks using depth-first order:
	// note: for blocks ending with if instructions, after ordering the
	// predicate, we go first to the false block (which will be placed
	// underneath the predicate) and then to the true block
	orderedList.add(this);

	for (int i = 0; i < orderedList.size(); i++) {
	    // get the next block off of the orderedList
	    BasicBlock bb = orderedList.elementAt(i);

	    // get successor blocks
	    Vector<BasicBlock> successors = new Vector<BasicBlock>();
	    if (bb.outEdges.size() == 1)
		successors.add(bb.outEdges.elementAt(0));
	    else if (bb.outEdges.size() > 1) {
		// with two out edges we add the block taken on a true condition first
		// followed by the block taken on a false condition

		// check block since we are going to use some important assumptions here
		// (e.g., no more than 2 successors, has at least 1 instruction, 
		// ends with if instruction)
		bb.check();

		// get if instruction and add linked blocks
		IfInst inst = (IfInst)(bb.insn.elementAt(bb.insn.size()-1));
		successors.add(inst.getTrueTarg());
		successors.add(inst.getFalseTarg());
	    }

	    // add successor blocks to ordered list
	    for (int j = 0; j < successors.size(); j++) {
		// get next successor of bb
		BasicBlock next = successors.elementAt(j);

		// if not already in one of the lists then add it
		// note: b/c depth-first we insert it right after bb
		// and the successors of bb we have already seen
		if (!orderedList.contains(next))
		    orderedList.add(i+j+1, next);
	    }
	}
	    
	return orderedList;
    }

    /** Prints basic block and all connected basic blocks -- for debugging
      * Must be called using entrance block as reference object
      * */
    public void printAll() {
	// if not already ordered, get ordered list of blocks before printing
	if (!isOrdered)
	    orderBlocks();

	// print out the basic blocks sorted order
	for (int i = 0; i < orderedList.size(); i++) {
	    // get the next basic block and print it
	    BasicBlock bb = orderedList.elementAt(i);
	    bb.print();

	    // if next element is not in the list of outgoing edges
	    // then we need a goto to jump to this instruction
	    // in this case, it must be a while loop back edge or
	    // a predicate or else block edge to the end of the if
	    BasicBlock next = null;
	    if (i < orderedList.size() - 1)
		next = orderedList.elementAt(i+1);

	    if (bb.outEdges.size() == 2) {
		TACInst ifInst = null;
		if (bb.insn.size() > 0)
		    ifInst = bb.insn.elementAt(bb.insn.size()-1);
		if (ifInst == null || !(ifInst instanceof IfInst))
		    throw new RuntimeException("Bad block; has more than 1 out edge " +
					       "and does not end with an if:\n" +
					       bb.getFullString());
		BasicBlock falseTarg = ((IfInst)ifInst).getFalseTarg();
		if (next != falseTarg)
		    System.out.println("\tgoto " + falseTarg.toString() + ";");
	    }
	    
	    else if (bb.outEdges.size() == 1) {
		if (bb.outEdges.elementAt(0) != next)
		    System.out.println("\tgoto " + 
				       bb.outEdges.elementAt(0).toString() + 
				       ";");
	    }
	    
	    else if (bb.outEdges.size() > 2)
		throw new RuntimeException("Bad block; has bad # of out edges:\n" +
					   bb.getFullString());
	    
	    System.out.println("");
	}

	// check that each block is OK -- done after printing so 
	// students can use the printed program to help debug problem
	for (int i = 0; i < orderedList.size(); i++) {
	    try {
		orderedList.elementAt(i).check();
	    }
	    catch (Exception e) {
		System.err.println("Error with control flow graph:");
		e.printStackTrace();
		System.exit(1);
	    }
	}
    }
}
