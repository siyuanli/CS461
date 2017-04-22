package bantam.interp;

import bantam.ast.Method;
import java.util.*;

/**
 * Created by Siyuan on 4/19/17.
 */
public class ObjectData{

    private List<HashMap<String, Object>> fields;

    private List<HashMap<String, Method>> methods;

    private String type;

    private int hierarchyLevel;

    public ObjectData(String type){
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.hierarchyLevel = 0;
        this.type = type;
    }

    public void pushField(HashMap<String, Object> hashMap){
        this.fields.add(hashMap);
    }

    public void pushMethods(HashMap<String, Method> hashMap){
        this.methods.add(hashMap);
    }

    public Object getField(String name, boolean hasRefSuper){
        int startI = this.hierarchyLevel;
        if (hasRefSuper){
            startI++;
        }

        for (int i = startI; i < this.fields.size(); i++){
            HashMap<String, Object> scope = this.fields.get(i);
            if(scope.containsKey(name)){
                return scope.get(name);
            }
        }
        return null;
    }

    public int getHierarchyLevel() {
        return this.hierarchyLevel;
    }

    public void setField(String varName, Object value, boolean hasRefSuper){
        int startI = this.hierarchyLevel;
        if (hasRefSuper){
            startI++;
        }
        for (int i = startI; i < this.fields.size(); i++){
            HashMap<String, Object> scope = this.fields.get(i);
            if(scope.containsKey(varName)){
                scope.put(varName,value);
                return;
            }
        }
    }

    public int getMethodScope(String name, boolean hasRefSuper){
        int startI = this.hierarchyLevel;
        if (hasRefSuper){
            startI++;
        }

        for (int i = startI; i < this.methods.size(); i++){
            HashMap<String, Method> scope = this.methods.get(i);
            if(scope.containsKey(name)){
                return i;
            }
        }
        return -1;
    }

    public Method getMethod(String name, int scope){
        return this.methods.get(scope).get(name);
    }

    public void setHierarchyLevel(int hierarchyLevel){
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getType(){
        return this.type;
    }

    public boolean isBuiltIn(){
        return false;
    }
}
