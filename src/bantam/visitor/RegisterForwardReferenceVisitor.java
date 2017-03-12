/*
 * File: RegisterForwardReferenceVisitor.java
 * CS461 Project 3
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 3/11/17
 */

package bantam.visitor;

import bantam.ast.ArrayExpr;
import bantam.ast.VarExpr;
import bantam.util.ClassTreeNode;
import bantam.util.ErrorHandler;
import bantam.util.ErrorHandlerUtilities;

/**
 * Registers an error if there is a forward references in the expr of a variable
 *
 * @author Joseph Maionek
 * @author Siyuan Li
 * @author Phoebe Hughes
 */
public class RegisterForwardReferenceVisitor extends Visitor {

    private ClassTreeNode classTreeNode;
    private ErrorHandlerUtilities errorUtil;
    private String varName;


    /**
     * Creates a register forward reference visitor that visits the expr of a variable
     * @param classTreeNode the class tree nodes from with the nodes are come from
     * @param errorHandler the error handler that it registers errors with
     * @param varName the name of the variable it visits
     */
    public RegisterForwardReferenceVisitor(ClassTreeNode classTreeNode,
                                           ErrorHandler errorHandler, String varName){
        this.classTreeNode = classTreeNode;
        this.errorUtil = new ErrorHandlerUtilities(errorHandler, null,
                this.classTreeNode.getASTNode().getFilename(),
                this.classTreeNode.getClassMap());
        this.varName = varName;
    }


    /**
     * Creates a register forward reference visitor that visits the expr of a variable
     * @param classTreeNode the class tree nodes from with the nodes are come from
     * @param errorUtil the utilities the help handle errors
     * @param varName the name of the variable it visits
     */
    public RegisterForwardReferenceVisitor(ClassTreeNode classTreeNode,
                                           ErrorHandlerUtilities errorUtil,
                                           String varName){
        this.classTreeNode = classTreeNode;
        this.errorUtil = errorUtil;
        this.varName = varName;
    }

    /**
     * Determines if there is a forward reference or the variable references itself.
     * @param exprName the name of the expr it is checking
     * @param lineNum the line number
     */
    private void checkForwardReference(String exprName, int lineNum) {
        if (exprName == null){
            this.errorUtil.registerError(lineNum, "Illegal forward reference.");
        }
        else if(exprName.equals(this.varName) &&
                this.classTreeNode.getVarSymbolTable().peek(exprName) != null){
            this.errorUtil.registerError(lineNum, "Cannot reference itself.");
        }
    }

    /**
     * Visits a var exp, checking if it is a forward reference
     * @param varExpr the varExpr node
     * @return null
     */
    @Override
    public Object visit(VarExpr varExpr){
        String exprName;
        VarExpr ref = (VarExpr)varExpr.getRef();

        if (varExpr.getName().equals("this") || varExpr.getName().equals("super")){
            return null;
        }
        //checks if var name in array.length is valid name
        else if (varExpr.getName().equals("length") && ref != null
                && !"this".equals(ref.getName()) && !"super".equals(ref.getName())){
            exprName = (String)this.classTreeNode.getVarSymbolTable()
                                    .lookup(ref.getName());
        }
        else{
            exprName = (String)this.classTreeNode.getVarSymbolTable()
                                    .lookup(varExpr.getName());
        }

        checkForwardReference(exprName, varExpr.getLineNum());

        return null;
    }


    /**
     * Visits an array Expr, checking if it is a forward reference
     * @param arrayExpr the array expr
     * @return null
     */
    @Override
    public Object visit(ArrayExpr arrayExpr){
        String exprName = (String)this.classTreeNode.getVarSymbolTable()
                                                    .lookup(arrayExpr.getName());
        checkForwardReference(exprName, arrayExpr.getLineNum());
        return null;
    }

}
