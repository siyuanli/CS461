package bantam.codegenmips;

import bantam.ast.Field;
import bantam.ast.Method;
import bantam.visitor.Visitor;

/**
 * Created by joseph on 4/5/17.
 */
public class FieldAdderVisitor extends Visitor {
    public Object visit(Method method){
        return null;
    }

    public Object visit(Field field){

        if(field.getInit()==null){

        }
        else{
            field.getInit().accept(new ASTNodeCodeGenVisitor());
        }
        return null;
    }
}
