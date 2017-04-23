package bantam.interp;

import bantam.ast.ExprList;

import java.util.HashMap;

/**
 * Created by joseph on 4/23/17.
 */
public class BuiltInMemberGenerator {

    private InterpreterVisitor interpreterVisitor;

    public BuiltInMemberGenerator(InterpreterVisitor visitor){
        this.interpreterVisitor = visitor;
    }

    public void genStringMembers(HashMap<String,MethodBody> methods, HashMap<String,Object> fields){
        fields.put("length", 0);
        fields.put("*str","");
        methods.put("length", actualParams -> fields.get("length"));
        methods.put("equals", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                return fields.get("*str").equals(objectData.getField("*str",false));
            }
        });
        //TODO: Finish the String methods
    }

}
