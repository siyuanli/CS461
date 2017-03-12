/*
 * File: MemberAdderVisitor.java
 * CS461 Project 3
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 3/11/17
 */

package bantam.visitor;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.util.ErrorHandler;
import bantam.util.SymbolTable;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adds fields and methods to a class tree node's symbol tables
 *
 *
 * @author Joseph Maionek
 * @author Siyuan Li
 * @author Phoebe Hughes
 */
public class MemberAdderVisitor extends Visitor {

    /**
     * The class tree node that's symbol tables are being updated
     */
    private ClassTreeNode classTreeNode;

    /**
     * The error handler to register errors with
     */
    private ErrorHandler errorHandler;

    /**
     * Strings of words that cannot be names of variables, methods, ect.
     */
    private Set<String> disallowedNames;

    /**
     * Creates a new member adder visitor
     * @param errorHandler the error handler to register errors with
     * @param disallowedNames a set of prohibited names for variables, ect.
     */
    public MemberAdderVisitor(ErrorHandler errorHandler, Set<String> disallowedNames){
        this.errorHandler = errorHandler;
        this.disallowedNames = disallowedNames;
    }

    /**
     * Populates the symbol tables of the given class tree node
     * @param classTreeNode the class tree node that variables/methods are added to
     */
    public void getSymbolTables(ClassTreeNode classTreeNode){
        this.classTreeNode = classTreeNode;
        this.classTreeNode.getVarSymbolTable().enterScope();
        this.classTreeNode.getMethodSymbolTable().enterScope();
        this.classTreeNode.getASTNode().accept(this);
    }

    /**
     * Registers an error if the given names is not allowed
     * @param name the name of a variable, method ect.
     * @param lineNum the line number this name occurs on
     */
    private void registerErrorIfReservedName(String name, int lineNum) {
        if (disallowedNames.contains(name)) {
            this.registerError(lineNum,
                    "Reserved word," + name + ",cannot be used as a field or method name");
        }
    }

    /**
     * Registers error if the given type is not allowed
     * @param type the type of a variable, method, ect.
     * @param lineNum the line number this type occurs on
     */
    private void registerErrorIfInvalidType(String type, int lineNum) {
        if (type.endsWith("[]")) { //if array, remove []
            type = type.substring(0, type.length() - 2);
        }

        //if type is not primitive and does not exist
        if (!this.classTreeNode.getClassMap().containsKey(type)
                && !type.equals("int") && !type.equals("boolean")) {
            this.registerError(lineNum, "Invalid Type");
        }
    }

    /**
     * Registers error with a given string
     * @param lineNum the line the error occurs on
     * @param error the string of the error
     */
    private void registerError(int lineNum, String error) {
        this.errorHandler.register(2, this.classTreeNode.getASTNode().getFilename(),
                lineNum, error);
    }

    /**
     * Visits a field node
     *
     * To be a valid field:
     * Must have valid name and type
     * Cannot already exist within the class
     * Cannot have a forward reference
     *
     * @param node the field node
     * @return null
     */
    @Override
    public Object visit(Field node) {
        SymbolTable varSymbolTable = this.classTreeNode.getVarSymbolTable();
        String name = node.getName();
        int lineNum = node.getLineNum();
        this.registerErrorIfInvalidType(node.getType(),node.getLineNum());
        if (varSymbolTable.peek(name) == null) {
            this.registerErrorIfReservedName(name,lineNum);
            varSymbolTable.add(name, node.getType());
        }
        else{
            this.registerError(lineNum,"Field already declared." );
        }

        //prevents forward referencing
        if (node.getInit() != null) {
            node.getInit().accept(new RegisterForwardReferenceVisitor(this.classTreeNode,
                    this.errorHandler, node.getName()));
        }

        return null;
    }

    /**
     * Visits method node
     *
     * To be a valid method:
     * Must have a valid name and type
     * Must have valid return type
     * Cannot overload another method
     * Parameters must be valid
     *
     * @param node the method node
     * @return null
     */
    public String visit(Method node) {
        SymbolTable methodSymbolTable = this.classTreeNode.getMethodSymbolTable();
        String name = node.getName();
        int lineNum = node.getLineNum();

        //checks if it has a valid return type
        if(!node.getReturnType().equals("void")) {
            this.registerErrorIfInvalidType(node.getReturnType(), node.getLineNum());
        }
        if (methodSymbolTable.peek(name) == null) {
            this.registerErrorIfReservedName(name,lineNum );
            List<String> paramTypes = (List) node.getFormalList().accept(this);

            //check if it is a valid over ride
            if(methodSymbolTable.lookup(name)!=null){
                Pair<String, List<String>> parentPair =
                        (Pair<String,List<String>>) methodSymbolTable.lookup(name);
                List<String> parentParamList = parentPair.getValue();

                //must have the same number of parameters
                if(parentParamList.size()!=paramTypes.size()){
                    this.registerError(node.getLineNum(),
                            "Overriding method must have same number" +
                                    " of parameters as the inherited method.");
                }
                else{
                    //checks for the same order/type of parameters
                    for(int i = 0;i<paramTypes.size();i++){
                        if(!paramTypes.get(i).equals(parentParamList.get(i))){
                            this.registerError(node.getLineNum(),
                                    "Overriding method must have same " +
                                            "signature as the inherited method.");
                        }
                    }
                }
            }
            Pair<String, List<String>> methodData =
                    new Pair<>(node.getReturnType(), paramTypes);
            methodSymbolTable.add(name, methodData);
        }
        else{
            this.registerError(lineNum,"Method already declared." );
        }
       return null;
    }


    /**
     * Visits the list of parameters, FormalList
     *
     * Records the type of each parameter
     * @param node the formal list node
     * @return the list of types of the formal nodes
     */
    @Override
    public Object visit(FormalList node) {
        List<String> paramTypes = new ArrayList<>();
        for (ASTNode element : node){
            paramTypes.add((String)element.accept(this));
        }
        return paramTypes;
    }

    /**
     * Visits formal node
     *
     * Registers if it does not have a valid type
     * @param node the formal node
     * @return the type of the formal
     */
    @Override
    public Object visit(Formal node){
        this.registerErrorIfInvalidType(node.getType(),node.getLineNum());
        return node.getType();
    }
}
