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

    private void pushMethodScope(){
        this.localVars.add(new HashMap<>());
    }

    private void popMethodScope(){
        this.localVars.remove(this.localVars.size()-1);
    }

    private HashMap<String,Object> getCurrentMethodScope(){
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
    private Pair<Object, Boolean> getObjectAndIsLocal(String ref, String name){
        if(ref!=null){
            Object obj = this.thisObject.getField(name,ref.equals("super"));
            return new Pair<>(obj,false);
        }
        else{
            if(this.getCurrentMethodScope().containsKey(name)){
                return new Pair<>(this.getCurrentMethodScope().get(name),true);
            }
            else{
                return new Pair<>(this.thisObject.getField(name,false),false);
            }
        }
    }

    //TODO: This
    public Object visit(Method node) {
        node.getStmtList().accept(this);

        this.popMethodScope();
        return null;
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
        Method method = objectData.getMethod(node.getMethodName(), scope);
        this.putParameters(method.getFormalList(), node.getActualList());
        Object returnValue;
        if (!this.classMap.get(objectData.getType()).isBuiltIn()) {
            returnValue = method.accept(this);
        }
        else{
            returnValue = null;
            //returnValue = (BuiltInObjectData) objectData.callMethod(name,paramList)
        }
        this.thisObject.setHierarchyLevel(currentHierarchy);
        return returnValue;
    }

    public Object visit(Formal node) {
        return node.getName();
    }

    private void putParameters(FormalList requiredParams, ExprList actualParams){
        this.pushMethodScope();

        for (int i = 0; i<requiredParams.getSize(); i++){
            String name = (String)requiredParams.get(i).accept(this);
            Object data = actualParams.get(i).accept(this);
            this.getCurrentMethodScope().put(name, data);
        }
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
        while((boolean)node.getPredExpr().accept(this)) {
            node.getBodyStmt().accept(this);
        }
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
        while(node.getPredExpr()==null || (boolean)node.getPredExpr().accept(this)) {
            node.getBodyStmt().accept(this);
            if (node.getUpdateExpr() != null) {
                node.getUpdateExpr().accept(this);
            }
        }
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    //TODO: whether to use jankily use exceptions or to jankily use flags
    public Object visit(BreakStmt node) {
        return null;
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return result of the visit
     */
    //TODO: whether to use jankily use exceptions or to jankily use flags
    public Object visit(ReturnStmt node) {
        if (node.getExpr() != null) {
            node.getExpr().accept(this);
        }
        return null;
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
        boolean isLocal = this.getObjectAndIsLocal(node.getRefName(),node.getName()).getValue();
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
        Pair<Object, Boolean> objAndLocation = this.getObjectAndIsLocal(refName, expr.getName());
        int newValue = (int) objAndLocation.getKey() + incrementValue;
        if (objAndLocation.getValue()){
            this.getCurrentMethodScope().put(expr.getName(), newValue);
        }
        else{
            this.thisObject.setField(expr.getName(), newValue, "super".equals(refName));
        }

        if (node.isPostfix()) {
            return objAndLocation.getKey();
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
        return this.getObjectAndIsLocal(refName,node.getName()).getKey();
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
    public Object visit(ConstStringExpr node) {
        return node.getConstant();
    }

}
