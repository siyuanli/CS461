/*
 * File: FieldAdderVisitor.java
 * CS461 Project 4B
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/17/17
 */

package bantam.codegenmips;

import bantam.ast.Field;
import bantam.ast.Method;
import bantam.util.ClassTreeNode;
import bantam.util.Location;
import bantam.visitor.Visitor;

/**
 * Visitor which adds fields to the variable symbol table for a specific object.
 */
public class FieldAdderVisitor extends Visitor {

    /**
     * Support class which formats and writes the resulting mips code.
     */
    private MipsSupport assemblySupport;
    /**
     * The classTreeNode that this object is generating the fields for.
     */
    private ClassTreeNode treeNode;
    /**
     * The current number of fields that have been generated for the current object.
     */
    private int numField;
    /**
     * Generates Bantam Java code for expressions, statements, and other nodes.
     */
    private ASTNodeCodeGenVisitor codeGenVisitor;

    /**
     * Creates a new FieldAdderVisitor with the given MipsSupport object and ASTNodeCodeGenVisitor
     * @param assemblySupport the MipsSupport object
     * @param codeGenVisitor ASTNodeCodeGenVisitor
     */
    public FieldAdderVisitor(MipsSupport assemblySupport, ASTNodeCodeGenVisitor codeGenVisitor){
        this.assemblySupport = assemblySupport;
        this.codeGenVisitor = codeGenVisitor;
    }

    /**
     * Initializes the field for the class corresponding to the given ClassTreeNode
     * @param treeNode the ClassTreeNode
     */
    public void initField(ClassTreeNode treeNode){
        this.numField = 0;
        this.treeNode = treeNode;
        this.codeGenVisitor.setTreeNode(treeNode);
        this.treeNode.getASTNode().accept(this);
    }

    /**
     * Does Nothing
     * @param method the method node
     * @return null
     */
    public Object visit(Method method){
        return null;
    }

    /**
     * Adds the given field to the symbol table with a valid location and generates the
     * code to initialize that field
     * @param field the ASTNode Field object
     * @return null
     */
    public Object visit(Field field){
        int offset = this.treeNode.getParent().getVarSymbolTable().getSize()*4 + 12 + 4*this.numField;
        Location loc = new Location("$a0", offset);
        this.treeNode.getVarSymbolTable().set(field.getName(),
                loc, treeNode.getVarSymbolTable().getCurrScopeLevel()-1);
        if(field.getInit()!=null){
            this.assemblySupport.genComment("Initializing Field: " + field.getName());
            field.getInit().accept(codeGenVisitor);
            //assume result is in $v0
            this.assemblySupport.genStoreWord("$v0", offset, "$a0");
        }
        this.assemblySupport.genMove("$v0", "$a0");
        this.numField++;
        return null;
    }
}
