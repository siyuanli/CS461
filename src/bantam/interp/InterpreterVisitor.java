package bantam.interp;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by Siyuan on 4/19/17.
 */
public class InterpreterVisitor extends Visitor{

    private ObjectData thisObject;

    private Hashtable<String, ClassTreeNode> classMap;

    private List<HashMap<String, Object>> localVars;

    private InstantiationVisitor instantiationVisitor;

    public InterpreterVisitor(Hashtable<String, ClassTreeNode> classMap, ObjectData mainObject){
        this.localVars = new ArrayList<>();
        this.classMap = classMap;
        this.instantiationVisitor = new InstantiationVisitor(this);
        this.thisObject = mainObject;
    }

    public void pushMethodScope(){
        this.localVars.add(new HashMap<>());
    }

    public void popMethodScope(){
        this.localVars.remove(this.localVars.size()-1);
    }

    public HashMap<String,Object> getCurrentMethodScope(){
        return  this.localVars.get(this.localVars.size()-1);
    }

    /**
     *
     * @param ref
     * @param name
     * @return a pair in which the key is the actual value of the variable and the value
     * is true if the variable was a local variable
     *
     */
    private Object getVariableValue(String ref, String name){
        boolean isLocal = this.isLocal(ref,name);
        if(isLocal){
            return this.getCurrentMethodScope().get(name);
        }
        else{
            return this.thisObject.getField(name,"super".equals(ref));
        }
    }

    private boolean isLocal(String ref, String name){
        if(ref!=null){
            return false;
        }
        else{
            return this.getCurrentMethodScope().containsKey(name);
        }
    }

    public Object visit(Method node) {
        Object returnValue = null;
        try {
            node.getStmtList().accept(this);
        } catch (ReturnStmtException e){
            returnValue = e.getReturnValue();
        }
        this.popMethodScope();
        return returnValue;
    }


    public Object visit(DispatchExpr node) {
        ObjectData objectData = this.thisObject;
        String refName = null;
        if(node.getRefExpr() instanceof VarExpr){
            refName = ((VarExpr)node.getRefExpr()).getName();
        }

        if(node.getRefExpr() == null){//If the method has no reference, it is "this"
            refName = "this";
        }

        boolean isSuper = false;
        if("super".equals(refName)){
            isSuper = true;
        }
        else if (!"this".equals(refName)){ //different object
            objectData = (ObjectData)node.getRefExpr().accept(this);
        }

        if(objectData == null){
            throw new NullPointerException("Reference object of call to "
                    +node.getMethodName()+" on line "+node.getLineNum()+" is null");
        }


        int scope = objectData.getMethodScope(node.getMethodName(), isSuper);
        int currentHierarchy = this.thisObject.getHierarchyLevel();
        objectData.setHierarchyLevel(scope);
        MethodBody methodBody = objectData.getMethod(node.getMethodName(), scope);

        Object returnValue = methodBody.execute(node.getActualList());
        this.thisObject.setHierarchyLevel(currentHierarchy);
        return returnValue;
    }

    public Object visit(Formal node) {
        return node.getName();
    }

    public Object visit(NewExpr newExpr){
        ClassTreeNode classTreeNode = this.classMap.get(newExpr.getType());
        ObjectData objectData = new ObjectData(newExpr.getType());
        this.instantiationVisitor.initObject(objectData, classTreeNode);
        return objectData;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    public Object visit(DeclStmt node) {
        Object value = node.getInit().accept(this);
        this.getCurrentMethodScope().put(node.getName(),value);
        return null;
    }

    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return result of the visit
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
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return result of the visit
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
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
     */
    public Object visit(ForStmt node) {
        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
        }
        try{
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
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
        throw new BreakStmtException();
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return result of the visit
     */
    public Object visit(ReturnStmt node) {
        Object returnValue = null;
        if (node.getExpr() != null) {
            returnValue = node.getExpr().accept(this);
        }
        throw new ReturnStmtException(returnValue);

    }

    /**
     * Visit an instanceof expression node
     *
     * @param node the instanceof expression node
     * @return result of the visit
     */
    public Object visit(InstanceofExpr node) {
        ObjectData obj = (ObjectData)node.getExpr().accept(this);
        if(node.getUpCheck()){
            return true;
        }
        else{
            ClassTreeNode classTreeNode = this.classMap.get(obj.getType());
            while(classTreeNode!=null){
                if(!classTreeNode.getName().equals(node.getType())){
                    return true;
                }
                classTreeNode = classTreeNode.getParent();
            }
            return false;
        }
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return result of the visit
     */
    public Object visit(CastExpr node) {
        ObjectData obj = (ObjectData)node.getExpr().accept(this);
        if(node.getUpCast()){
            return obj;
        }
        else{
            ClassTreeNode classTreeNode = this.classMap.get(obj.getType());
            while(classTreeNode!=null){
                if(!classTreeNode.getName().equals(node.getType())){
                    return obj;
                }
                classTreeNode = classTreeNode.getParent();
            }
            throw new ClassCastException("Cannot cast object of type "+obj.getType()+
                    " to type "+node.getType() + " on line "+ node.getLineNum());
        }
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return result of the visit
     */
    public Object visit(AssignExpr node) {
        Object obj = node.getExpr().accept(this);
        boolean isLocal = this.isLocal(node.getRefName(),node.getName());
        if(isLocal){
            this.getCurrentMethodScope().put(node.getName(),obj);
        }
        else{
            this.thisObject.setField(node.getName(),obj,node.getRefName().equals("super"));
        }
        return obj;
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompEqExpr node) {
        Object left = node.getLeftExpr().accept(this);
        Object right = node.getRightExpr().accept(this);
        return left == right;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompNeExpr node) {
        Object left = node.getLeftExpr().accept(this);
        Object right = node.getRightExpr().accept(this);
        return left != right;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompLtExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left < right;
    }

    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompLeqExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left <= right;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompGtExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left > right;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return result of the visit
     */
    public Object visit(BinaryCompGeqExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left >= right;
    }


    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithPlusExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left + right;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithMinusExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left - right;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return result of the visit
     */
    public Object visit(BinaryArithTimesExpr node) {
        int left = (int)node.getLeftExpr().accept(this);
        int right = (int)node.getRightExpr().accept(this);
        return left*right;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return result of the visit
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
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return result of the visit
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
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return result of the visit
     */
    public Object visit(BinaryLogicAndExpr node) {
        boolean left = (boolean)node.getLeftExpr().accept(this);
        boolean right = (boolean)node.getRightExpr().accept(this);
        return left && right;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return result of the visit
     */
    public Object visit(BinaryLogicOrExpr node) {
        boolean left = (boolean)node.getLeftExpr().accept(this);
        boolean right = (boolean)node.getRightExpr().accept(this);
        return left || right;
    }

    /**
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return result of the visit
     */
    public Object visit(UnaryNegExpr node) {
        int val = (int)node.getExpr().accept(this);
        return -val;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return result of the visit
     */
    public Object visit(UnaryNotExpr node) {
        boolean val = (boolean)node.getExpr().accept(this);
        return !val;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return result of the visit
     */
    public Object visit(UnaryIncrExpr node) {
        return this.incrDecrHelper(node, 1);
    }

    private Object incrDecrHelper(UnaryExpr node, int incrementValue) {
        VarExpr expr = (VarExpr)node.getExpr();
        String refName = null;
        if (expr.getRef() != null){
            refName = ((VarExpr)expr.getRef()).getName();
        }
        int oldValue = (int)this.getVariableValue(refName,expr.getName());
        int newValue = oldValue + incrementValue;
        if (this.isLocal(refName,expr.getName())){
            this.getCurrentMethodScope().put(expr.getName(), newValue);
        }
        else{
            this.thisObject.setField(expr.getName(), newValue, "super".equals(refName));
        }

        if (node.isPostfix()) {
            return oldValue;
        }
        else{
            return newValue;
        }
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return result of the visit
     */
    public Object visit(UnaryDecrExpr node) {
        return this.incrDecrHelper(node, -1);
    }

    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return result of the visit
     */
    public Object visit(VarExpr node) {
        String refName = null;
        if (node.getRef() != null) {
            refName = ((VarExpr)node.getRef()).getName();
        }
        return this.getVariableValue(refName,node.getName());
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return result of the visit
     */
    public Object visit(ConstIntExpr node) {
        return Integer.parseInt(node.getConstant());
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return result of the visit
     */
    public Object visit(ConstBooleanExpr node) {
        return Boolean.parseBoolean(node.getConstant());
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return result of the visit
     */
    //TODO: Fix this so that it makes an ObjectData object
    public Object visit(ConstStringExpr node) {
        return node.getConstant();
    }

}
