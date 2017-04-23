package bantam.interp;

/**
 * Created by joseph on 4/23/17.
 */
public class ReturnStmtException extends RuntimeException {
    private Object returnValue;

    public ReturnStmtException(Object returnValue){
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
