/*
 * File: TypeCheckVisitor.java
 * CS461 Project 3
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 3/11/17
 */

package bantam.util;

import java.util.Hashtable;
import java.util.Set;

/**
 * A class that creates helpful methods to register semantic analyzer related errors.
 *
 * @author Joseph Maionek
 * @author Siyuan Li
 * @author Phoebe Hughes
 */
public class ErrorHandlerUtilities {

    /**
     * the error handler it registers errors with
     */
    private ErrorHandler errorHandler;

    /**
     * the set of names that are disallowed for types, names, ect.
     */
    private Set<String> disallowedNames;

    /**
     * the name of the file of the error
     */
    private String filename;

    /**
     * the class map
     */
    private Hashtable<String, ClassTreeNode> classMap;

    /**
     * Creates an ErrorHandlerUtilities
     * @param errorHandler the error handler it registers errors with
     * @param disallowedNames a set of prohibited names
     * @param filename the filename that errors are in
     * @param classMap the class map
     */
    public ErrorHandlerUtilities(ErrorHandler errorHandler, Set<String> disallowedNames,
                                 String filename, Hashtable<String,
                                 ClassTreeNode> classMap){
        this.errorHandler = errorHandler;
        this.disallowedNames = disallowedNames;
        this.filename = filename;
        this.classMap = classMap;
    }

    /**
     * Sets the filename to the given file name
     * @param name the na,e
     */
    public void setFilename(String name){
        this.filename = name;
    }

    /**
     * Sets the class map to the given class map
     * @param classMap the class map
     */
    public void setClassMap(Hashtable<String, ClassTreeNode> classMap){
        this.classMap = classMap;
    }


    /**
     * Register a semantic error with given line number and error message.
     * @param lineNum the line number of the error
     * @param error the error message
     */
    public void registerError(int lineNum, String error) {
        this.errorHandler.register(2, this.filename, lineNum, error);
    }

    /**
     * Register an error if the given string is a reserved word.
     * @param name the string
     * @param lineNum the line number of the error
     */
    public void registerErrorIfReservedName(String name, int lineNum) {
        if (this.disallowedNames.contains(name)) {
            this.registerError(lineNum,
                "Reserved word,"+name+", cannot be used as a field or method " + "name");
        }
    }

    /**
     * Register an error if the given type invalid.
     * @param type the type that is currently checked
     * @param lineNum the line number of the error
     */
    public void registerErrorIfInvalidType(String type, int lineNum) {
        if (type.endsWith("[]")) {
            type = type.substring(0, type.length() - 2);
        }
        if (!this.classMap.containsKey(type)
                && !type.equals("int") && !type.equals("boolean")) {
            this.registerError(lineNum, "Invalid Type");
        }
    }
}
