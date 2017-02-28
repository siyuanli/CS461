/*
 * File: StringConstantsVisitor.java
 * CS461 Project 2.5
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 2/28/17
 */
package bantam.visitor;

import bantam.ast.ConstStringExpr;
import bantam.ast.Method;
import bantam.ast.Program;
import bantam.ast.VarExpr;


import java.util.HashMap;
import java.util.Map;

/**
 * Class which uses the visitor pattern on an abstract syntax tree in order to catalogue
 * all the string constants.
 */
public class StringConstantsVisitor extends Visitor {

    /**
     * Hashmap which stores each string constant.
     */
    Map<String, String> stringConstants;

    /**
     * Gets all of the String constants from a specific abstract syntax tree and then
     * returns a Hashmap with each key a "StringConst_"+i where i is which number
     * String constant and the value it's mapped to is the actual String constant
     * @param ast the root of the abstract syntax tree
     * @return the Hashmap
     */
    public Map<String, String> getStringConstants(Program ast){
        this.stringConstants = new HashMap<>();
        ast.accept(this);
        return stringConstants;
    }

    /**
     * Visits a ConstStringExpr node and adds it to the Hashmap(if it isn't there already)
     * @param node the string constant expression node
     * @return returns null
     */
    public Object visit(ConstStringExpr node){
        if(!stringConstants.containsKey(node.getConstant())){
            stringConstants.put(node.getConstant(),"StringConst_"+stringConstants.size());
        }
        return null;
    }

    /**
     * Visits each of the nodes in the statement list (traverses the body of the method)
     * @param node the method node
     * @return
     */
    public Object visit(Method node){
        node.getStmtList().accept(this);
        return null;
    }

    /**
     * Visits a VarExpr node
     * @param node the variable expression node
     * @return returns null
     */
    public Object visit(VarExpr node){
        return null;
    }

}
