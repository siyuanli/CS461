/*
 * File: DispatchTableAdderVisitor.java
 * CS461 Project 4A
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/2/17
 */

package bantam.codegenmips;

import bantam.ast.Class_;
import bantam.ast.Field;
import bantam.ast.Method;
import bantam.util.ClassTreeNode;
import bantam.visitor.Visitor;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a list of methods that are in a class.
 *
 */
public class DispatchTableAdderVisitor extends Visitor {

    /**
     * The name of the class being visited.
     */
    private String className;

    /**
     * The tree node of the class we are visiting.
     */
    private ClassTreeNode treeNode;

    /**
     * The list of methods in the set,
     * where each method has a reference(value) and a name(key).
     */
    private List<Pair<String, String>> methodList;

    /**
     * Creates a list of methods given a class and the methods of the parent.
     * @param parentList the methods of the parent class
     * @param treeNode the tree node of the class
     * @return a list of pairs that is the names of the methods in the class,
     *         pair contains the name of the method and the class in which it is defined
     */
    public List<Pair<String,String>> getMethodList(List<Pair<String,String>> parentList, ClassTreeNode treeNode){
        this.className = treeNode.getName();
        this.treeNode = treeNode;
        this.methodList = new ArrayList<>();
        treeNode.getMethodSymbolTable().exitScope();
        treeNode.getMethodSymbolTable().enterScope();
        for(Pair<String,String> pair : parentList){
            methodList.add(new Pair<>(pair.getKey(), pair.getValue()));
        }
        treeNode.getASTNode().accept(this);
        return this.methodList;
    }

    /**
     * Returns null
     * @param node the field node
     * @return null
     */
    public Object visit(Field node) {
        return null;
    }

    /**
     * Visits a method node,
     * adding the name, key, and name of the class each method is in, value
     * @param node the method node
     * @return null
     */
    public Object visit(Method node) {
        Pair<String, String> newPair =new Pair<>(node.getName(),this.className);
        for(int i = 0;i<methodList.size();i++){
            if(methodList.get(i).getKey().equals(node.getName())){
                methodList.set(i,newPair);
                return null;
            }
        }
        this.treeNode.getMethodSymbolTable().add(node.getName(), methodList.size());
        methodList.add(newPair);
        return null;
    }

}
