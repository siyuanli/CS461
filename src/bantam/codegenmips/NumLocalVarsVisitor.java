/*
 * File: NumLocalVarsVisitor.java
 * CS461 Project 3
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 3/11/17
 */

package bantam.codegenmips;

import bantam.ast.*;
import bantam.visitor.Visitor;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a map of the number of variables in the program and the class +
 * method they are in
 *
 * @author Joseph Maionek
 * @author Siyuan Li
 * @author Phoebe Hughes
 */
public class NumLocalVarsVisitor extends Visitor {

    /**
     * The local variables within the program
     * the string is the className.methodName and the integer is the number of variables
     */
    private Map<String, Integer> localVars;

    /**
     * Gets the number of local variables in an ast node
     * @param ast the program
     * @return a map that is className.methodName and the number of variables
     */
    public Map<String, Integer> getNumsAllLocalVars(ASTNode ast){
        this.localVars = new HashMap<>();
        ast.accept(this);
        return this.localVars;
    }

    /**
     * Visits the class node, adding the className.methodName
     * and number of variables in the class to the map
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node) {
        List<Pair<String, Integer>> varPairList =
                (List<Pair<String, Integer>>) node.getMemberList().accept(this);
        for (Pair<String, Integer> varPair : varPairList){
            String name = node.getName() + "." + varPair.getKey();
            this.localVars.put(name, varPair.getValue());
        }
        return null;
    }

    /**
     * Visits a memberList node, getting the names of methods
     * and how many variables are in the method in a Member list
     * @param nodes the MemberList
     * @return the names of methods and number of variables in each
     */
    public Object visit(MemberList nodes) {
        List<Pair<String, Integer>> varPairList = new ArrayList<>();
        for (ASTNode node : nodes) {
            Pair<String, Integer> numVarPair = (Pair<String, Integer>)node.accept(this);
            if (numVarPair != null){
                varPairList.add(numVarPair);
            }
        }
        return varPairList;
    }

    /**
     * Visits a field node
     * @param node the field node
     * @return null
     */
    public Object visit(Field node){
        return null;
    }

    /**
     * Visits a method note, getting the name and number of variables in the given method
     * @param node the method node
     * @return the name of the method and number of variables in it
     */
    public Object visit(Method node){
        int numVars = node.getFormalList().getSize();
        numVars += (int)node.getStmtList().accept(this);
        return new Pair<>(node.getName(), numVars);
    }

    /**
     * Visits a statement list node, getting the number of variables in
     * each of the nodes
     * @param nodes the statement list node
     * @return the number of variables in the statementlist
     */
    public Object visit(StmtList nodes){
        int numVars = 0;
        for(ASTNode node: nodes){
            numVars += (int)node.accept(this);
        }
        return numVars;
    }

    /**
     * Visits an Expr Statement node
     * @param node the expression statement node
     * @return 0
     */
    public Object visit(ExprStmt node){
        return 0;
    }

    /**
     * Visits the Decl Statement node
     * @param node the declaration statement node
     * @return 1
     */
    public Object visit(DeclStmt node) {
        return 1;
    }

    /**
     * Visits the ifStmt node and its statement children, counting the number of
     * variables declared in it
     * @param node the if statement node
     * @return the number of variables in the if statement
     */
    public Object visit(IfStmt node){
        Stmt elseStmt = node.getElseStmt();
        int numVar = (int)node.getThenStmt().accept(this);
        if (elseStmt != null){
            numVar += (int)elseStmt.accept(this);
        }
        return numVar;
    }

    /**
     * Visits the whileStmt node, getting the number of variables in it's statement
     * @param node the while statement node
     * @return the number of variables
     */
    public Object visit(WhileStmt node){
        return node.getBodyStmt().accept(this);
    }

    /**
     * Visits a forStmt node, getting the number of variables in it
     * @param node the for statement node
     * @return the number of variables
     */
    public Object visit(ForStmt node){
        return node.getBodyStmt().accept(this);
    }

    /**
     * Visits a breakStmt node
     * @param node the break statement node
     * @return 0
     */
    public Object visit(BreakStmt node){
        return 0;
    }

    /**
     * Visits a returnStmt node
     * @param node the return statement node
     * @return 0
     */
    public Object visit(ReturnStmt node){
        return 0;
    }


    /**
     * Visits a blockStmt node, getting the number of variables within it
     * @param node the block statement node
     * @return the number of variables
     */
    public Object visit(BlockStmt node){
        return node.getStmtList().accept(this);
    }


}
