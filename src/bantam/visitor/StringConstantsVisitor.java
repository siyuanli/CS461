package bantam.visitor;

import bantam.ast.ConstStringExpr;
import bantam.ast.Method;
import bantam.ast.Program;
import bantam.ast.VarExpr;
import com.sun.javafx.binding.StringConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joseph on 2/24/17.
 */
public class StringConstantsVisitor extends Visitor {

    Map<String, String> stringConstants;

    public StringConstantsVisitor(){
        stringConstants = new HashMap<>();
    }


    public Map<String, String> getStringConstants(Program ast){
        ast.accept(this);
        return stringConstants;
    }

    public Object visit(ConstStringExpr node){
        if(!stringConstants.containsKey(node.getConstant())){
            stringConstants.put(node.getConstant(),"StringConst_"+stringConstants.size());
        }
        return null;
    }

    public Object visit(Method node){
        node.getStmtList().accept(this);
        return null;
    }

    public Object visit(VarExpr node){
        return null;
    }

}
