/*
 * File: DispatchTableAdderVisitor.java
 * CS461 Project 4A
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/2/17
 */

package bantam.visitor;

import bantam.ast.Class_;
import bantam.ast.Field;
import bantam.ast.Method;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DispatchTableAdderVisitor extends Visitor {

    /**
     *
     */
    private String className;

    /**
     *
     */
    private List<Pair<String, String>> methodList;

    /**
     *
     * @param parentList
     * @param classNode
     * @return
     */
    public List<Pair<String,String>> getMethodList(List<Pair<String,String>> parentList, Class_ classNode){
        this.className = classNode.getName();
        this.methodList = new ArrayList<>();
        for(Pair<String,String> pair : parentList){
            methodList.add(new Pair<>(pair.getKey(), pair.getValue()));
        }
        classNode.accept(this);
        return this.methodList;
    }

    /**
     *
     * @param node the field node
     * @return
     */
    public Object visit(Field node) {
        return null;
    }

    /**
     *
     * @param node the method node
     * @return
     */
    public Object visit(Method node) {
        Pair<String, String> newPair =new Pair<>(node.getName(),this.className);
        for(int i = 0;i<methodList.size();i++){
            if(methodList.get(i).getKey().equals(node.getName())){
                methodList.set(i,newPair);
                return null;
            }
        }
        methodList.add(newPair);
        return null;
    }

}
