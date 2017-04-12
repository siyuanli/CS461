/*
 * File: MipsCodeGenerator.java
 * CS461 Project 4A
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/2/17
 */

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

package bantam.codegenmips;

import bantam.util.ClassTreeNode;
import bantam.visitor.DispatchTableAdderVisitor;
import javafx.util.Pair;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The <tt>MipsCodeGenerator</tt> class generates mips assembly code
 * targeted for the SPIM emulator.  Note: this code will only run
 * under SPIM.
 * <p/>
 * This class is incomplete and will need to be implemented by the student.
 */
public class MipsCodeGenerator {
    /**
     * Root of the class hierarchy tree
     */
    private ClassTreeNode root;

    /**
     * Print stream for output assembly file
     */
    private PrintStream out;

    /**
     * Assembly support object (using Mips assembly support)
     */
    private MipsSupport assemblySupport;

    /**
     * Boolean indicating whether garbage collection is enabled
     */
    private boolean gc = false;

    /**
     * Boolean indicating whether optimization is enabled
     */
    private boolean opt = false;

    /**
     * Boolean indicating whether debugging is enabled
     */
    private boolean debug = false;

    /**
     * A map which maps string constants to their IDs
     */
    private Map<String, String> stringConstantsMap;

    /**
     *
     */
    private List<String> classNamesList;
    /**
     * MipsCodeGenerator constructor
     *
     * @param root    root of the class hierarchy tree
     * @param outFile filename of the assembly output file
     * @param gc      boolean indicating whether garbage collection is enabled
     * @param opt     boolean indicating whether optimization is enabled
     * @param debug   boolean indicating whether debugging is enabled
     */
    public MipsCodeGenerator(ClassTreeNode root, String outFile,
                             boolean gc, boolean opt, boolean debug) {
        this.root = root;
        this.gc = gc;
        this.opt = opt;
        this.debug = debug;

        try {
            out = new PrintStream(new FileOutputStream(outFile));
            assemblySupport = new MipsSupport(out);
        } catch (IOException e) {
            // if don't have permission to write to file then report an error and exit
            System.err.println("Error: don't have permission to write to file '" + outFile + "'");
            System.exit(1);
        }
    }

    /**
     * Generate assembly file
     * <p/>
     * In particular, will need to do the following:
     * 1 - start the data section
     * 2 - generate data for the garbage collector
     * 3 - generate string constants
     * 4 - generate class name table
     * 5 - generate object templates
     * 6 - generate dispatch tables
     * 7 - start the text section
     * 8 - generate initialization subroutines
     * 9 - generate user-defined methods
     * See the lab manual for the details of each of these steps.
     */
    public void generate() {
        System.out.println("Generating");
        this.classNamesList = new ArrayList<>();
        this.classNamesList.add("Object");
        this.classNamesList.add("String");
        this.genClassNamesList(this.root);

        // 1 - start the data section
        this.startData();

        // 2 - generate data for the garbage collector
        this.genGarbageCollector();

        // 3 - generate string constants
        this.genStringConsts();

        // 4 - generate class name table
        this.genClassNameTable();

        // 5 - generate object templates
        this.genObjectTemplates();

        // 6 - generate dispatch tables
        for(String name : this.classNamesList){
            this.assemblySupport.genGlobal(name+"_dispatch_table");
        }
        this.genDispatchTables(this.root,new ArrayList<>());

        this.out.println("\n");
        //7 - start the text section
        this.assemblySupport.genTextStart();

        // 8 - generate initialization subroutines
        this.genInitMethods();

        // 9 - generate user-defined methods
        this.genMethods();
    }

    private void genClassNamesList(ClassTreeNode treeNode) {
        String className = treeNode.getName();
        if(!className.equals("Object") && !className.equals("String")){
            this.classNamesList.add(className);
        }
        Iterator<ClassTreeNode> childrenIterator = treeNode.getChildrenList();
        while(childrenIterator.hasNext()){
            ClassTreeNode child = childrenIterator.next();
            this.genClassNamesList(child);
        }
    }

    /**
     * Gets the filenames of all of the classes
     * @param getBuiltIns the built-in classes
     * @return the set of filenames
     */
    public Set<String> getFilenames(boolean getBuiltIns){
        Set<String> filenames = new HashSet<>();
        for(ClassTreeNode classNode : this.root.getClassMap().values()){
            String name = classNode.getASTNode().getFilename().replace("\\", "/");
            if ((!getBuiltIns && !name.equals("<built-in class>")) || getBuiltIns){
                filenames.add(name);
            }
        }
        return filenames;
    }

    /**
     * The file-header for the generated file
     */
    private void startData(){
        //Create comments
        assemblySupport.genComment("Authors: Phoebe Hughes, Siyuan Li, Joseph Malionek");
        assemblySupport.genComment("Date: " + LocalDateTime.now());
        assemblySupport.genComment("Compiled from: " + this.getFilenames(false));
        assemblySupport.genDataStart();
    }

    /**
     * Sets the garbage collect flag
     */
    private void genGarbageCollector(){
        assemblySupport.genLabel("gc_flag");
        assemblySupport.genWord("0");
    }

    /**
     * Generates string objects in the .data section for all of the built-in strings.
     */
    private void genStringConsts(){
        StringConstantsVisitor stringConstantsVisitor = new StringConstantsVisitor();
        for (ClassTreeNode classNode : this.root.getClassMap().values()){
            classNode.getASTNode().accept(stringConstantsVisitor);
        }

        Map<String, String> classNamesMap = new HashMap<>();
        for (int i = 0; i < this.classNamesList.size(); i++){
            classNamesMap.put(this.classNamesList.get(i), "class_name_" + i);
        }

        Map<String, String> filenames = new HashMap<>();
        for (String filename : this.getFilenames(true)){
            filenames.put(filename, "filename_" + filenames.size());
        }
        this.stringConstantsMap = stringConstantsVisitor.getStringConstants();
        this.genStringConstsFromMap(this.stringConstantsMap);
        this.genStringConstsFromMap(classNamesMap);
        this.genStringConstsFromMap(filenames);
    }

    /**
     * Given a map between string constants and their labels, generates a String object
     * for each entry in the map.
     * @param stringConsts the map
     */
    private void genStringConstsFromMap(Map<String, String> stringConsts) {
        for (Map.Entry<String, String> entry : stringConsts.entrySet()) {
            int length = stringSize(entry.getKey());
            int totalSize = (4 - (length + 17)%4)  + length + 17;

            assemblySupport.genLabel(entry.getValue());
            assemblySupport.genComment("String object");
            assemblySupport.genWord("1");
            assemblySupport.genComment("Size of object");
            assemblySupport.genWord(Integer.toString(totalSize));
            assemblySupport.genWord("String_dispatch_table");
            assemblySupport.genComment("Length of string");
            assemblySupport.genWord(Integer.toString(length)); //size
            assemblySupport.genAscii(entry.getKey());
        }
    }

    private int stringSize(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            //If we have a \ then we know it must be followed by a char in rtfn\
            if (str.charAt(i) == '\\') {
                i++;
            }
            count++;
        }
        return count;
    }


    /**
     * Generates the class name table
     */
    private void genClassNameTable(){
        assemblySupport.genLabel("class_name_table");
        for (int i = 0; i< this.classNamesList.size(); i++){
            assemblySupport.genWord("class_name_" + i);
        }

        for(String name: this.classNamesList){
            assemblySupport.genGlobal(name + "_template");
        }
    }

    /**
     * Generates a template object for each of the classes in the given list.
     */
    private void genObjectTemplates(){
        for (ClassTreeNode classTreeNode : this.root.getClassMap().values()){
            String name = classTreeNode.getName();
            this.assemblySupport.genLabel(name + "_template");
            this.assemblySupport.genComment("The integer ID of the class");
            this.assemblySupport.genWord(Integer.toString(this.classNamesList.indexOf(name)));
            int numFields = classTreeNode.getVarSymbolTable().getSize();
            this.assemblySupport.genComment("The size of the object");
            this.assemblySupport.genWord(Integer.toString(numFields*4 + 12));
            this.assemblySupport.genWord(name + "_dispatch_table");
            this.assemblySupport.genComment("The remainder of this object is fields.");
            for (int i = 0; i<numFields; i++){
                this.assemblySupport.genWord("0");
            }
        }
    }

    /**
     * Generates the dispatch table for the given object with the given parents method and
     * then does the same for its children.
     * @param treeNode the given object
     * @param parentList the list of methods from the parent
     */
    private void genDispatchTables(ClassTreeNode treeNode,
                                  List<Pair<String,String>> parentList){
        DispatchTableAdderVisitor visitor = new DispatchTableAdderVisitor();
        List<Pair<String,String>> methodList = visitor.getMethodList(parentList,treeNode.getASTNode());
        assemblySupport.genLabel(treeNode.getName() + "_dispatch_table");
        for(Pair<String,String> pair : methodList){
            String methodName = pair.getValue() + "." + pair.getKey();
            this.assemblySupport.genWord(methodName);
        }
        Iterator<ClassTreeNode> childrenIterator = treeNode.getChildrenList();
        while(childrenIterator.hasNext()){
            ClassTreeNode childNode = childrenIterator.next();
            this.genDispatchTables(childNode,methodList);
        }
    }

    /**
     * Generates init methods for each of the given classes
     */
    private void genInitMethods(){
        FieldAdderVisitor fieldAdderVisitor =
                new FieldAdderVisitor(this.assemblySupport, new ASTNodeCodeGenVisitor(
                        this.assemblySupport,this.stringConstantsMap,this.classNamesList));
        for(String name : this.classNamesList){
            this.assemblySupport.genLabel(name+"_init");
            ClassTreeNode treeNode = this.root.getClassMap().get(name);
            ClassTreeNode parent = treeNode.getParent();
            if(parent!=null) {
                this.assemblySupport.genDirCall(parent.getName() + "_init");
            }
            else{
                this.assemblySupport.genMove("$v0","$a0");
            }
            fieldAdderVisitor.initField(treeNode);
            this.assemblySupport.genRetn();
        }
    }

    /**
     * Generates user-defined methods
     */
    private void genMethods(){
        ASTNodeCodeGenVisitor codeGenVisitor = new ASTNodeCodeGenVisitor(
                this.assemblySupport,this.stringConstantsMap,this.classNamesList);
        for(ClassTreeNode treeNode: this.root.getClassMap().values()) {
            if (!treeNode.isBuiltIn()) {
                codeGenVisitor.genMips(treeNode);
            }
        }
    }

}
