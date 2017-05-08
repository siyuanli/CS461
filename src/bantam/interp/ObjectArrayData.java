package bantam.interp;

/**
 * Created by Phoebe Hughes on 5/7/2017.
 */
public class ObjectArrayData extends ObjectData {

    private Object[] array;

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

    public int getLength(){
        return this.array.length;
    }

    public void setItem(int index, Object value){
        this.array[index] = value;
    }

    public Object[] getArray(){
        return this.array;
    }

    public Object getItem(int index){
        return this.array[index];
    }

    public void setArray(Object[] array){
        this.array = array;
    }
}
