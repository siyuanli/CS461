package bantam.interp;

/**
 * Created by Phoebe Hughes on 5/6/2017.
 */
public class BantamException extends RuntimeException{
    private String type;
    private String message;
    private ObjectData error;

    public BantamException(String type, String message, ObjectData error){
        this.type = type;
        this.message = message;
        this.error = error;
    }

    @Override
    public String getMessage(){
        return this.type+": " + this.message;
    }

    public void setMessage(String newMessage){
        this.message = newMessage;
    }

    public String getType() {
        return type;
    }

    public ObjectData getError(){
        return this.error;
    }
}
