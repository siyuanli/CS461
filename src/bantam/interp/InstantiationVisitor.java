package bantam.interp;

import bantam.ast.Field;
import bantam.ast.Method;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;

import java.util.HashMap;

/**
 * Created by Phoebe Hughes on 4/20/2017.
 */
public class InstantiationVisitor extends Visitor {
    private ObjectData objectData;
    private InterpreterVisitor interpreterVisitor;
    private HashMap<String, Object> fields;
    private HashMap<String, Method> methods;

    public InstantiationVisitor(InterpreterVisitor interpreterVisitor){
        this.interpreterVisitor = interpreterVisitor;
    }

    public void initObject(ObjectData objectData, ClassTreeNode classTreeNode){
        this.objectData = objectData;
        this.addFieldsAndMethods(classTreeNode);
    }

    private void addFieldsAndMethods(ClassTreeNode classTreeNode){
        HashMap<String, Object> childFields = this.fields;
        HashMap<String, Method> childMethods = this.methods;

        this.fields = new HashMap<>();
        this.objectData.pushField(fields);

        this.methods = new HashMap<>();
        this.objectData.pushMethods(methods);

        if (classTreeNode.getParent() != null){
            this.addFieldsAndMethods(classTreeNode.getParent());
            this.fields = childFields;
            this.methods = childMethods;
        }
        classTreeNode.getASTNode().accept(this);
    }

    public Object visit(Field node) {
        Object value = null;
        if (node.getType().equals("int")){
            value = 0;
        }
        else if (node.getType().equals("boolean")){
            value = false;
        }

        if (node.getInit() != null) {
            value = node.getInit().accept(this.interpreterVisitor);
        }

        this.fields.put(node.getName(), value);
        return null;
    }

    public Object visit(Method node) {
        this.methods.put(node.getName(), node);
        return null;
    }

}
