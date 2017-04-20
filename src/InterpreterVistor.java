import bantam.interp.ObjectData;
import bantam.util.ClassTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Siyuan on 4/19/17.
 */
public class InterpreterVistor {

    private ObjectData currentObject;

    private List<HashMap<String, ObjectData>> localVars;

    public InterpreterVistor(ClassTreeNode classTreeNode){
        this.localVars = new ArrayList<>();
    }



}
