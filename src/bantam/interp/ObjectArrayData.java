package bantam.interp;

/**
 * Created by Phoebe Hughes on 5/7/2017.
 */
public class ObjectArrayData extends ObjectData {

    private Object[] array;

    public ObjectArrayData(String type, int length){
        super(type);
        array = new Object[length];
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
