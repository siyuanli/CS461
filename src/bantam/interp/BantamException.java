/*
 * File: BantamException.java
 * CS461 Project 6 Second Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 5/10/17
 */

package bantam.interp;

/**
 * A runtime exception for Bantam Java
 */
public class BantamException extends RuntimeException{
    /**
     * The type of the exception
     */
    private String type;

    /**
     * The error message of the exception
     */
    private String message;

    /**
     * The object data that corresponds to the runtime error
     */
    private ObjectData exceptionObject;

    /**
     * Creata a Bantam exception
     * @param type the type of the exception
     * @param message the error message of the excpetion
     * @param exceptionObject the corresponding object data of the exception
     */
    public BantamException(String type, String message, ObjectData exceptionObject){
        this.type = type;
        this.message = message;
        this.exceptionObject = exceptionObject;
    }

    /**
     * Get the error message of the exception
     * @return the message as a string
     */
    @Override
    public String getMessage(){
        return this.type+": " + this.message;
    }

    /**
     * Set the error message of the exception
     * @param newMessage the given error message
     */
    public void setMessage(String newMessage){
        this.message = newMessage;
    }

    /**
     * Set the type of the exception
     * @return the type of the exception as a string
     */
    public String getType() {
        return type;
    }

    /**
     * Return the corresponding object data of the exception
     * @return the object data
     */
    public ObjectData getExceptionObject(){
        return this.exceptionObject;
    }
}
