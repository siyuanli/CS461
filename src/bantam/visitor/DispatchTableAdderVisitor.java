package bantam.visitor;

import bantam.ast.Class_;
import bantam.ast.Field;
import bantam.ast.Method;
import bantam.codegenmips.MipsSupport;
import bantam.util.ClassTreeNode;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Phoebe Hughes on 3/21/2017.
 */
public class DispatchTableAdderVisitor extends Visitor {


    private String className;
    private List<Pair<String, String>> methodList;

    public List<Pair<String,String>> getMethodList(List<Pair<String,String>> parentList, Class_ classNode){
        this.className = classNode.getName();
        this.methodList = new ArrayList<>();
        for(Pair<String,String> pair : parentList){
            methodList.add(new Pair<>(pair.getKey(), pair.getValue()));
        }
        classNode.accept(this);
        return this.methodList;
    }

    public Object visit(Field node) {
        return null;
    }

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
