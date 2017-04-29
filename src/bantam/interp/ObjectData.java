/*
 * File: ObjectData.java
 * CS461 Project 5 First Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/30/17
 */

package bantam.interp;
import java.util.*;

/**
 * Created by Siyuan on 4/19/17.
 */
public class ObjectData{

    private List<HashMap<String, Object>> fields;

    private List<HashMap<String, MethodBody>> methods;

    private String type;

    private int hierarchyLevel;

    public ObjectData(String type){
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.hierarchyLevel = 0;
        this.type = type;
    }

    public void pushFields(HashMap<String, Object> hashMap){
        this.fields.add(hashMap);
    }

    public void pushMethods(HashMap<String, MethodBody> hashMap){
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

    //TODO: Fix this (0-level methods don't exist?)
    public int getMethodScope(String name, boolean hasRefSuper){
        int startI = this.hierarchyLevel;
        if (hasRefSuper){
            startI++;
        }

        for (int i = startI; i < this.methods.size(); i++){
            HashMap<String, MethodBody> scope = this.methods.get(i);

            if(scope.containsKey(name)){
                return i;
            }
        }
        return -1;
    }

    public MethodBody getMethod(String name, int scope){
        return this.methods.get(scope).get(name);
    }

    public void setHierarchyLevel(int hierarchyLevel){
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getType(){
        return this.type;
    }

    public void copyFields(ObjectData objectData){
        for(int i = 0; i< this.fields.size();i++){
            for(Map.Entry<String, Object> entry : this.fields.get(i).entrySet()){
                objectData.fields.get(i).put(entry.getKey(),entry.getValue());
            }
        }
    }
}
