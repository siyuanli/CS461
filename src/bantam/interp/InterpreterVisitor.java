package bantam.interp;

import bantam.ast.*;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

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

    private void pushScope(){
        this.localVars.add(new HashMap<>());
    }

    private void popScope(){
        this.localVars.remove(this.localVars.size()-1);
    }

    private void addToCurrentScope(String name, Object data){
        this.localVars.get(this.localVars.size()-1).put(name, data);
    }

    public Object visit(Method node) {
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);

        this.popScope();
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

        int scope = objectData.getMethodScope(node.getMethodName(), isSuper);
        objectData.setHierarchyLevel(scope);
        Method method = objectData.getMethod(node.getMethodName(), scope);
        this.putParameters(method.getFormalList(), node.getActualList());
        return method.accept(this.instantiationVisitor);
    }

    public Object visit(Formal node) {
        return node.getName();
    }

    private void putParameters(FormalList requiredParams, ExprList actualParams){
        this.pushScope();
        for (int i = 0; i<requiredParams.getSize(); i++){
            String name = (String)requiredParams.get(i).accept(this);
            Object data = actualParams.get(i).accept(this);
            this.addToCurrentScope(name, data);
        }
    }


    public Object accept(NewExpr newExpr){
        ClassTreeNode classTreeNode = this.classMap.get(newExpr.getType());
        ObjectData objectData = new ObjectData(newExpr.getType());
        this.instantiationVisitor.initObject(objectData, classTreeNode);
        return objectData;
    }

}
