package bantam.interp;

import bantam.ast.ExprList;

import java.util.List;


public interface MethodBody {

    public Object execute(ExprList actualParams);


}
