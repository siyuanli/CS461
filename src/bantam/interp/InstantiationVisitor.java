/*
 * File: Instantiation.java
 * CS461 Project 5 First Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/30/17
 */

package bantam.interp;

import bantam.ast.ExprList;
import bantam.ast.Field;
import bantam.ast.Method;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;

import java.util.HashMap;


public class InstantiationVisitor extends Visitor {

    /**The object being instantiated*/
    private ObjectData objectData;

    /**The interpreter visitor that this object will use in order to initialize fields*/
    private InterpreterVisitor interpreterVisitor;

    /**The hashmap from field names to values which is currently being worked on*/
    private HashMap<String, Object> fields;

    /**The hashmap from method names to methods which is currently being worked on*/
    private HashMap<String, MethodBody> methods;

    /**
     * Creates a new InstantiationVisitor with the given interpreter visitor which will
     * populate the given objectData with fields and methods from the given ClassTreeNode
     * @param interpreterVisitor the interpreter visitor to initialize fields with
     * @param objectData the object to be populated
     * @param classTreeNode the ClassTreeNode of the object type
     */
    public InstantiationVisitor(InterpreterVisitor interpreterVisitor, ObjectData objectData, ClassTreeNode classTreeNode){
        this.interpreterVisitor = interpreterVisitor;
        this.initObject(objectData, classTreeNode);
    }

    /**
     * Initilazes the given object with the fields and methods contained in ClassTreeNode
     * @param objectData the object to be populated
     * @param classTreeNode the ClassTreeNode corresponding to the object type
     */
    private void initObject(ObjectData objectData, ClassTreeNode classTreeNode){
        this.objectData = objectData;
        this.addMembers(classTreeNode);
    }

    /**
     * Adds the members for the given ClassTreeNode to the object being worked on
     * @param classTreeNode the ClassTreeNode object
     */
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

        //A separate class handles populating the fields and methods for built-ins*/
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

    /**
     * Initializes the given field and adds it to the hashmap for the current object
     * @param node the field node
     * @return null
     */
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

    /**
     * Initializes the given method and adds it to the hashmap for the current object
     * @param node the method node
     * @return null
     */
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
