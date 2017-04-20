package bantam.interp;

import bantam.ast.Class_;
import bantam.ast.Method;

import java.util.*;

/**
 * Created by Siyuan on 4/19/17.
 */
public class ObjectData {

    private List<HashMap<String, ObjectData>> fields;

    private List<HashMap<String, Method>> methods;

    private int hierarchyLevel;

    public ObjectData(Class_ classTreeNode){
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.hierarchyLevel = 0;
    }

    public ObjectData getField(String name){
        for(HashMap<String, ObjectData> item : fields){
            if(item.containsKey(name)){
                return item.get(name);
            }
        }
        return null;
    }

    public Method getMethod(String name){
        for(HashMap<String, Method> item: methods){
           if(item.containsKey(name)){
               return item.get(name);
           }
        }
        return null;
    }

    public void setHierarchyLevel(int value){
        this.hierarchyLevel = value;
    }

}
