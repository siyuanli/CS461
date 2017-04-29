/*
 * File: InterpreterVisitor.java
 * CS461 Project 5 First Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/30/17
 */
package bantam.interp;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;
import java.util.*;

/**
 * The visitor that interprets bantam java code
 */
public class InterpreterVisitor extends Visitor{

    /**
     * The object that it is interpreting currently
     */
    private ObjectData thisObject;

    /**
     * The map of classes and their class tree nodes
     */
    private Hashtable<String, ClassTreeNode> classMap;

    /**
     * The local variables, each hash map is a method body
     */
    private List<HashMap<String, Object>> localVars;

    /**
     * Creates a new interpreter visitor
     * @param classMap the class map
     * @param mainObject the main object data
     */
    public InterpreterVisitor(Hashtable<String, ClassTreeNode> classMap, ObjectData mainObject){
        this.localVars = new ArrayList<>();
        this.localVars.add(new HashMap<>());
        this.classMap = classMap;
        this.thisObject = mainObject;
    }

    /**
     * Adds a new scope to the local variables
     * @param hashMap
     */
    public void pushMethodScope(HashMap<String, Object> hashMap){
        this.localVars.add(hashMap);
    }

    /**
     * Pops off the top method scope
     */
    public void popMethodScope(){
        this.localVars.remove(this.localVars.size()-1);
    }

    /**
     * Gets the current scope of local vairables
     * @return
     */
    public HashMap<String,Object> getCurrentMethodScope(){
        return this.localVars.get(this.localVars.size()-1);
    }

    /**
     * Gets the current object it is interpreting
     * @return
     */
    public ObjectData getThisObject() {
        return thisObject;
    }

    /**
     * Sets the current object it is interpreting
     * @param thisObject
     */
    public void setThisObject(ObjectData thisObject) {
        this.thisObject = thisObject;
    }


    /**
     * Gets the value of a variable
     * @param ref the reference name of the variable
     * @param name the name of the variable
     * @return the value of the variable
     *
     */
    private Object getVariableValue(String ref, String name){
        if(name.equals("this")){
            return thisObject;
        }

        boolean isLocal = this.isLocal(ref,name);
        if(isLocal){
            return this.getCurrentMethodScope().get(name);
        }
        else{
            return this.thisObject.getField(name,"super".equals(ref));
        }
    }

    /**
     * Determines if a variable is a local variable
     * @param ref the reference of the variable
     * @param name the name of the variable
     * @return if it is a local variable then returns true
     */
    private boolean isLocal(String ref, String name){
        if(ref!=null){
            return false;
        }
        else{
            return this.getCurrentMethodScope().containsKey(name);
        }
    }

    /**
     * Increments or decrements a node by a given value
     * @param node the node to increment/decrement
     * @param incrementValue the value to increment or decrement by
     * @return the value of the node
     */
    private Object incrDecrHelper(UnaryExpr node, int incrementValue) {
        VarExpr expr = (VarExpr)node.getExpr();

        //finding the correct variable/field
        String refName = null;
        if (expr.getRef() != null){
            refName = ((VarExpr)expr.getRef()).getName();
        }

        //calculating the new value
        int oldValue = (int)this.getVariableValue(refName,expr.getName());
        int newValue = oldValue + incrementValue;

        //reassigning the value
        if (this.isLocal(refName,expr.getName())){
            this.getCurrentMethodScope().put(expr.getName(), newValue);
        }
        else{
            this.thisObject.setField(expr.getName(), newValue, "super".equals(refName));
        }

        //returning the correct value
        if (node.isPostfix()) {
            return oldValue;
        }
        else{
            return newValue;
        }
    }

    /**
     * Visits a method
     * @param node the method node
     * @return the return value of the method
     */
    public Object visit(Method node) {
        Object returnValue = null;
        try {
            node.getStmtList().accept(this);
        } catch (ReturnStmtException e){
            returnValue = e.getReturnValue();
        }
        return returnValue;
    }


    /**
     * Visits a dispatch expr, executing the method body
     * @param node the dispatch expression node
     * @return the return value of the method
     */
    public Object visit(DispatchExpr node) {
        //Change "this" object over to new method callee
        ObjectData objectData = this.thisObject;

        //figuring out the correct reference
        String refName = null;
        if(node.getRefExpr() instanceof VarExpr){
            refName = ((VarExpr)node.getRefExpr()).getName();
        }
        if(node.getRefExpr() == null){//If the method has no reference, it is "this"
            refName = "this";
        }

        //getting the correct reference to call the method on
        boolean isSuper = false;
        if("super".equals(refName)){
            isSuper = true;
        }
        else if (!"this".equals(refName)){ //different object
            objectData = (ObjectData)node.getRefExpr().accept(this);
            if(objectData == null){
                throw new NullPointerException("Reference object of call to "
                        +node.getMethodName()+" on line "+node.getLineNum()+" is null");
            }
        }



        int scope = objectData.getMethodScope(node.getMethodName(), isSuper);

        //Set hierarchy to new hierarchy level while saving old one
        int oldHierarchy = objectData.getHierarchyLevel();
        objectData.setHierarchyLevel(scope);
        MethodBody methodBody = objectData.getMethod(node.getMethodName(), scope);

        Object returnValue = methodBody.execute(node.getActualList());

        //Set hierarchy back to before value
        objectData.setHierarchyLevel(oldHierarchy);

        return returnValue;
    }

    /**
     * Visits a formal node getting its name
     * @param node the formal node
     * @return the name of the formal node
     */
    public Object visit(Formal node) {
        return node.getName();
    }

    /**
     * Visits a new expr, creating a new object of the given type
     * @param newExpr the new expr
     * @return a new object data
     */
    public Object visit(NewExpr newExpr){
        ClassTreeNode classTreeNode = this.classMap.get(newExpr.getType());
        ObjectData objectData = new ObjectData(newExpr.getType());
        new InstantiationVisitor(this, objectData, classTreeNode);
        return objectData;
    }

    /**
     * Visit a declaration statement node, updating the value of a given method
     *
     * @param node the declaration statement node
     * @return null;
     */
    public Object visit(DeclStmt node) {
        Object value = node.getInit().accept(this);
        this.getCurrentMethodScope().put(node.getName(),value);
        return null;
    }

    /**
     * Visit an if statement node, executing the then statement if the condition is true
     * or the else statement if it is false
     *
     * @param node the if statement node
     * @return null
     */
    public Object visit(IfStmt node) {
        if((boolean)node.getPredExpr().accept(this)){
            node.getThenStmt().accept(this);
        }
        else if (node.getElseStmt() != null) {
            node.getElseStmt().accept(this);
        }
        return null;
    }

    /**
     * Visit a while statement node, excecuting the body while the condition is true
     *
     * @param node the while statement node
     * @return null
     */
    public Object visit(WhileStmt node) {
        try {
            while((boolean)node.getPredExpr().accept(this)) {
                node.getBodyStmt().accept(this);
            }
        } catch(BreakStmtException ignored){}
        return null;
    }

    /**
     * Visit a for statement node, executing the body while the condition is true
     *
     * @param node the for statement node
     * @return null
     */
    public Object visit(ForStmt node) {
        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
        }
        try{
            //while condition is true execute the body
            while(node.getPredExpr()==null || (boolean)node.getPredExpr().accept(this)) {
                node.getBodyStmt().accept(this);
                if (node.getUpdateExpr() != null) {
                    node.getUpdateExpr().accept(this);
                }
            }
        } catch (BreakStmtException ignored){}
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return null;
     */
    public Object visit(BreakStmt node) {
        throw new BreakStmtException();
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return null
     */
    public Object visit(ReturnStmt node) {
        Object returnValue = null;
        if (node.getExpr() != null) {
            returnValue = node.getExpr().accept(this);
        }
        throw new ReturnStmtException(returnValue);

    }

    /**
     * Visit an instanceof expression node, checking if the
     * object is of a specified type
     *
     * @param node the instanceof expression node
     * @return true if the given object is of the specified type
     */
    public Object visit(InstanceofExpr node) {
        ObjectData obj = (ObjectData)node.getExpr().accept(this);
        if(node.getUpCheck() || obj == null){
            return true;
        }
        else{
            ClassTreeNode classTreeNode = this.classMap.get(obj.getType());
            //checks the ancestors types
            while(classTreeNode!=null){
                if(classTreeNode.getName().equals(node.getType())){
                    return true;
                }
                classTreeNode = classTreeNode.getParent();
            }
            return false;
        }
    }

    /**
     * Visit a cast expression node, casting an object to a given type if
     * it is legal
     *
     * @param node the cast expression node
     * @return the object
     */
    public Object visit(CastExpr node) {
        ObjectData obj = (ObjectData)node.getExpr().accept(this);
        if(node.getUpCast() || obj == null){
            return obj;
        }
        else{
            ClassTreeNode classTreeNode = this.classMap.get(obj.getType());
            //checks if object is of an ancestors type
            while (classTreeNode != null) {
                if (classTreeNode.getName().equals(node.getType())) {
                    return obj;
                }
                classTreeNode = classTreeNode.getParent();
            }
            throw new ClassCastException("Cannot cast object of type " + obj.getType() +
                    " to type " + node.getType() + " on line " + node.getLineNum());
        }
    }

    /**
     * Visit an assignment expression node, assigning a given value to the given
     * field or method
     *
     * @param node the assignment expression node
     * @return the object
     */
    public Object visit(AssignExpr node) {
        Object obj = node.getExpr().accept(this);
        boolean isLocal = this.isLocal(node.getRefName(),node.getName());
        if(isLocal){
            this.getCurrentMethodScope().put(node.getName(),obj);
        }
        else{
            this.thisObject.setField(node.getName(),obj,"super".equals(node.getRefName()));
        }
        return obj;
    }

    /**
     * Visit a binary comparison equals expression node, comparing two nodes for equality
     *
     * @param node the binary comparison equals expression node
     * @return if the given nodes are equal
     */
    public Object visit(BinaryCompEqExpr node) {
        Object left = node.getLeftExpr().accept(this);
        Object right = node.getRightExpr().accept(this);
        return left == right;
    }

    /**
     * Visit a binary comparison not equals expression node, comparing two nodes
     * for inequality
     *
     * @param node the binary comparison not equals expression node
     * @return if the two nodes are not equal
     */
    public Object visit(BinaryCompNeExpr node) {
        Object left = node.getLeftExpr().accept(this);
        Object right = node.getRightExpr().accept(this);
        return left != right;
    }

    /**
     * Visit a binary comparison less than expression node, comparing if
     * the left subtree < right subtree
     *
     * @param node the binary comparison less than expression node
     * @return if the left subtree < right subtree
     */
    public Object visit(BinaryCompLtExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left < right;
    }

    /**
     * Visit a binary comparison less than or equal to expression node, comparing if
     * the left subtree <= right subtree
     *
     * @param node the binary comparison less than or equal to expression node
     * @return if the left subtree <= right subtree
     */
    public Object visit(BinaryCompLeqExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left <= right;
    }

    /**
     * Visit a binary comparison greater than expression node, comparing if
     * the left subtree > right subtree
     *
     * @param node the binary comparison greater than expression node
     * @return if the left subtree > right subtree
     */
    public Object visit(BinaryCompGtExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left > right;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node,  comparing if
     * the left subtree >= right subtree
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return if the left subtree >= right subtree
     */
    public Object visit(BinaryCompGeqExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left >= right;
    }


    /**
     * Visit a binary arithmetic plus expression node, adding the left and the right
     * sub trees
     *
     * @param node the binary arithmetic plus expression node
     * @return result of adding the left and right subtree
     */
    public Object visit(BinaryArithPlusExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left + right;
    }

    /**
     * Visit a binary arithmetic minus expression node, subtracts the right from the left
     * subtree
     *
     * @param node the binary arithmetic minus expression node
     * @return result subtracting the right from the left
     */
    public Object visit(BinaryArithMinusExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left - right;
    }

    /**
     * Visit a binary arithmetic times expression node, multiplying the right and left
     * subtrees
     *
     * @param node the binary arithmetic times expression node
     * @return result of multiplying the right and the left subtrees
     */
    public Object visit(BinaryArithTimesExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left*right;
    }

    /**
     * Visit a binary arithmetic divide expression node, dividing the left tree by the
     * right sub tree
     *
     * @param node the binary arithmetic divide expression node
     * @return result of dividing them
     */
    public Object visit(BinaryArithDivideExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        if(right == 0){
            throw new ArithmeticException("Divisor is 0 on line " + node.getLineNum());
        }
        return left/right;
    }

    /**
     * Visit a binary arithmetic modulus expression node, moding the left subtree
     * by the right one
     *
     * @param node the binary arithmetic modulus expression node
     * @return result of moding
     */
    public Object visit(BinaryArithModulusExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        if(right == 0){
            throw new ArithmeticException("Divisor is 0 on line " + node.getLineNum());
        }
        return left%right;
    }

    /**
     * Visit a binary logical AND expression node, anding the right and left subtrees
     *
     * @param node the binary logical AND expression node
     * @return result of anding them together
     */
    public Object visit(BinaryLogicAndExpr node) {
        boolean left = (boolean)node.getLeftExpr().accept(this);
        if(left) {
            return node.getRightExpr().accept(this);
        }
        return false;
    }

    /**
     * Visit a binary logical OR expression node, binary or-ing the left subtree and
     * right subtree together
     *
     * @param node the binary logical OR expression node
     * @return result of or-ing
     */
    public Object visit(BinaryLogicOrExpr node) {
        boolean left = (boolean)node.getLeftExpr().accept(this);
        if(!left) {
            return node.getRightExpr().accept(this);
        }
        return true;
    }

    /**
     * Visit a unary negation expression node, negating the given value
     *
     * @param node the unary negation expression node
     * @return result of the negation
     */
    public Object visit(UnaryNegExpr node) {
        int val = (int)node.getExpr().accept(this);
        return -val;
    }

    /**
     * Visit a unary NOT expression node, not-ing the given value
     *
     * @param node the unary NOT expression node
     * @return result of not-ing the value
     */
    public Object visit(UnaryNotExpr node) {
        boolean val = (boolean)node.getExpr().accept(this);
        return !val;
    }

    /**
     * Visit a unary increment expression node, incrementing the given variable/field
     *
     * @param node the unary increment expression node
     * @return resulting value from incrementing
     */
    public Object visit(UnaryIncrExpr node) {
        return this.incrDecrHelper(node, 1);
    }

    /**
     * Visit a unary decrement expression node, decrementing the given variable/field
     *
     * @param node the unary decrement expression node
     * @return resulting value from decrementing
     */
    public Object visit(UnaryDecrExpr node) {
        return this.incrDecrHelper(node, -1);
    }

    /**
     * Visit a variable expression node, getting the value from the variable
     *
     * @param node the variable expression node
     * @return the value of the variable
     */
    public Object visit(VarExpr node) {
        String refName = null;
        if (node.getRef() != null) {
            refName = ((VarExpr)node.getRef()).getName();
        }
        return this.getVariableValue(refName,node.getName());
    }

    /**
     * Visit an int constant expression node, getting the integer value of the node
     *
     * @param node the int constant expression node
     * @return the integer value of the node
     */
    public Object visit(ConstIntExpr node) {
        return Integer.parseInt(node.getConstant());
    }

    /**
     * Visit a boolean constant expression node, getting the boolean value of the node
     *
     * @param node the boolean constant expression node
     * @return the boolean value of the node
     */
    public Object visit(ConstBooleanExpr node) {
        return Boolean.parseBoolean(node.getConstant());
    }

    /**
     * Visit a string constant expression node, getting the Object Data representation of
     * the string
     *
     * @param node the string constant expression node
     * @return the object data representation of the string
     */
    public Object visit(ConstStringExpr node) {
        ObjectData strObjectData = new ObjectData("String");
        new InstantiationVisitor(this, strObjectData,this.classMap.get("String"));

        //replacing "\n" string with new line character
        String oldstr = node.getConstant();
        String newstr = "";
        //Taken from mipsSupport, edited by PYYLCH, SL, JDM
        for (int i = 0; i < oldstr.length(); i++) {
            if (oldstr.charAt(i) == '\\' && i < oldstr.length() - 1) {
                if (oldstr.charAt(i + 1) == 'n') {
                    newstr+="\n";
                } else if (oldstr.charAt(i + 1) == 't') {
                    newstr+="\t";
                } else if (oldstr.charAt(i + 1) == 'f') {
                    newstr+="\f";
                } else if (oldstr.charAt(i + 1) == '"') {
                    newstr+="\"";
                } else if (oldstr.charAt(i + 1) == '\\') {
                    newstr+="\\";
                }
                // backslash is not allowed in front of any other char
                i++;
            } else {
                newstr+=oldstr.charAt(i);
            }
        }
        strObjectData.setField("length",newstr.length(),false);
        strObjectData.setField("*str",newstr,false);
        return strObjectData;
    }

}
