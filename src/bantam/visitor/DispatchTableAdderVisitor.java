package bantam.visitor;

import bantam.ast.Class_;
import bantam.ast.Field;
import bantam.ast.Method;
import bantam.codegenmips.MipsSupport;
import bantam.util.ClassTreeNode;

import java.util.Map;

/**
 * Created by Phoebe Hughes on 3/21/2017.
 */
public class DispatchTableAdderVisitor extends Visitor {

    private MipsSupport assemblySupport;
    private Map<String, ClassTreeNode> classMap;
    private String className;


    public DispatchTableAdderVisitor(MipsSupport assemblySupport,
                                     Map<String, ClassTreeNode> classTreeNodeMap){
        this.assemblySupport = assemblySupport;
        this.classMap = classTreeNodeMap;
    }


    public Object visit(Class_ node) {
        if (node.getParent() != null){
            ClassTreeNode parent = this.classMap.get(node.getParent());
            parent.getASTNode().accept(this);
        }

        className = node.getName();
        node.getMemberList().accept(this);
        return null;
    }

    public Object visit(Field node) {
        return null;
    }

    public Object visit(Method node) {
        this.assemblySupport.genWord(className + "." + node.getName());
        return null;
    }

}
