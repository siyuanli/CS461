package bantam.interp;

/**
 * Created by joseph on 4/22/17.
 */
public class BuiltInObjectData extends ObjectData{

    public BuiltInObjectData(String type) {
        super(type);
    }

    @Override
    public boolean isBuiltIn(){
        return true;
    }
}
