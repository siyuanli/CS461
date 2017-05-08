/*
 * File: ReturnStmtException.java
 * CS461 Project 6 Second Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 5/10/17
 */

package bantam.interp;

/**
 * An exception returned when returning a value.
 */
public class ReturnStmtException extends RuntimeException {

    /**
     * The value returned.
     */
    private Object returnValue;

    /**
     * Creates a return statement exception.
     * @param returnValue the value to return
     */
    public ReturnStmtException(Object returnValue){
        this.returnValue = returnValue;
    }

    /**
     * Gets the return value
     * @return the return value
     */
    public Object getReturnValue() {
        return returnValue;
    }
}
