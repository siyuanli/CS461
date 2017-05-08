/*
 * File: ObjectData.java
 * CS461 Project 6 Second Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 5/10/17
 */

package bantam.interp;
import java.util.*;

/**
 * Represents an object of a given type in our interpreter.
 */
public class ObjectData{

    /**
     * The fields of the object, where each new hash map is an ancestors field
     * 0ith is its own fields.
     */
    private List<HashMap<String, Object>> fields;

    /**
     * The methods of the object, where each hash map is an ancestors methods
     * the 0ith hash map is its own methods
     */
    private List<HashMap<String, MethodBody>> methods;


    /**
     * The type of the object
     */
    private String type;

    /**
     * The level to find the field or method at a given time.
     */
    private int hierarchyLevel;

    /**
     * Creates a new object
     * @param type the type of the object
     */
    public ObjectData(String type){
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.hierarchyLevel = 0;
        this.type = type;
    }

    /**
     * Adds a hash map of fields to the fields
     * @param hashMap the fields
     */
    public void pushFields(HashMap<String, Object> hashMap){
        this.fields.add(hashMap);
    }

    /**
     * Adss a hash map of methods to the methods
     * @param hashMap the methods
     */
    public void pushMethods(HashMap<String, MethodBody> hashMap){
        this.methods.add(hashMap);
    }

    /**
     * Gets the given field
     * @param name the name of the field
     * @param hasRefSuper if when accessing the field the reference super is given
     * @return the object stored in the field
     */
    public Object getField(String name, boolean hasRefSuper){
        int startI = this.hierarchyLevel;
        if (hasRefSuper){
            startI++;
        }

        //loops through the fields
        for (int i = startI; i < this.fields.size(); i++){
            HashMap<String, Object> scope = this.fields.get(i);
            if(scope.containsKey(name)){
                return scope.get(name);
            }
        }
        return null;
    }

    /**
     * Gets the current hierarchy level
     * @return the hierarchy level
     */
    public int getHierarchyLevel() {
        return this.hierarchyLevel;
    }

    /**
     * Changes the value of a given field
     * @param varName the name of the field
     * @param value the new value of the field
     * @param hasRefSuper if when accessing the field, the reference super is used
     */
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

    /**
     * Gets the hierarchy level of a method
     * @param name the name of a method
     * @param hasRefSuper if when accessing the method, the reference super is used
     * @return the hierarchy
     */
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

    /**
     * Gets a method from a given hierarchy
     * @param name the name of the method
     * @param scope the hierarchy level it is in
     * @return the method body of the method
     */
    public MethodBody getMethod(String name, int scope){
        return this.methods.get(scope).get(name);
    }

    /**
     * Changes the current hierarchy level
     * @param hierarchyLevel the hierarchy level
     */
    public void setHierarchyLevel(int hierarchyLevel){
        this.hierarchyLevel = hierarchyLevel;
    }

    /**
     * Gets the type of the object
     * @return the type of the object
     */
    public String getType(){
        return this.type;
    }

    /**
     * Copies the values of the fields into another object data
     * @param objectData the object data to copy the fields into
     */
    public void copyFields(ObjectData objectData){
        for(int i = 0; i< this.fields.size();i++){
            for(Map.Entry<String, Object> entry : this.fields.get(i).entrySet()){
                objectData.fields.get(i).put(entry.getKey(),entry.getValue());
            }
        }
    }
}
