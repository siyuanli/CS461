package bantam.interp;

import bantam.ast.ExprList;
import bantam.ast.Field;
import bantam.ast.Method;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;

import java.util.HashMap;


public class InstantiationVisitor extends Visitor {
    private ObjectData objectData;
    private InterpreterVisitor interpreterVisitor;
    private HashMap<String, Object> fields;
    private HashMap<String, MethodBody> methods;

    public InstantiationVisitor(InterpreterVisitor interpreterVisitor){
        this.interpreterVisitor = interpreterVisitor;
    }

    public void initObject(ObjectData objectData, ClassTreeNode classTreeNode){
        this.objectData = objectData;
        this.addMembers(classTreeNode);
    }

    private void addMembers(ClassTreeNode classTreeNode){
        BuiltInMemberGenerator memberGenerator = new BuiltInMemberGenerator(this.interpreterVisitor);

        this.fields = new HashMap<>();
        HashMap<String, Object> childFields = this.fields;
        this.objectData.pushFields(this.fields);

        this.methods = new HashMap<>();
        HashMap<String, MethodBody> childMethods = this.methods;
        this.objectData.pushMethods(this.methods);

        if (classTreeNode.getParent() != null) {
            this.addMembers(classTreeNode.getParent());
            this.fields = childFields;
            this.methods = childMethods;
        }

        if (classTreeNode.isBuiltIn()) {
            switch (classTreeNode.getName()) {
                case "Object":
                    memberGenerator.genObjectMembers(this.methods, objectData);
                    break;

                case "String":
                    memberGenerator.genStringMembers(this.methods, this.fields);
                    break;

                case "Sys":
                    memberGenerator.genSysMembers(this.methods);
                    break;

                case "TextIO":
                    memberGenerator.genTextIOMembers(this.methods, this.fields, objectData);
                    break;
            }
        }
        else {
            classTreeNode.getASTNode().accept(this);
        }

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
        this.methods.put(node.getName(), new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                HashMap<String, Object> methodScope = new HashMap<>();
                for(int i = 0; i< actualParams.getSize();i++){
                    String name = (String)node.getFormalList().get(i).accept(interpreterVisitor);
                    Object data = actualParams.get(i).accept(interpreterVisitor);
                    methodScope.put(name, data);
                }
                ObjectData oldThisObject = interpreterVisitor.getThisObject();
                interpreterVisitor.setThisObject(objectData);
                interpreterVisitor.pushMethodScope(methodScope);
                Object returnValue = node.accept(interpreterVisitor);
                interpreterVisitor.popMethodScope();
                interpreterVisitor.setThisObject(oldThisObject);
                return returnValue;
            }
        });
        return null;
    }

}
