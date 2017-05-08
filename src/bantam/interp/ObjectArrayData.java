/*
 * ObjectArrayData.java
 * CS461 Project 6 Second Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 5/10/17
 */

package bantam.interp;

/**
 * An ObjectData representation for an array object.
 */
public class ObjectArrayData extends ObjectData {

    /**
     * The array which holds the contents of the array
     */
    private Object[] array;

    /**
     * Creates a new ObjectArrayData with the given type of elements and array size
     * @param type the array type
     * @param length the array size
     */
    public ObjectArrayData(String type, int length){
        super(type);

        array = new Object[length];

        type = type.substring(0, type.length() - 2);

        if (type.equals("int")){
            for (int i = 0 ; i<length; i++){
                array[i] = 0;
            }
        }
        else if (type.equals("boolean")){
            for (int i = 0 ; i<length; i++){
                array[i] = false;
            }
        }
    }

    /**
     * Gets the size of the array
     * @return the size
     */
    public int getLength(){
        return this.array.length;
    }

    /**
     * Sets the item at the given index to the given value
     * @param index the array index
     * @param value the value to be set
     */
    public void setItem(int index, Object value){
        this.array[index] = value;
    }

    /**
     * Returns the array of objects this objectData represents
     * @return the array
     */
    public Object[] getArray(){
        return this.array;
    }

    /**
     * Gets the item at the given index in the array
     * @param index the index
     * @return the item at the index
     */
    public Object getItem(int index){
        return this.array[index];
    }

    /**
     * Sets the array object to be the given array
     * @param array the new array
     */
    public void setArray(Object[] array){
        this.array = array;
    }
}
