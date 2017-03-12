package bantam.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Phoebe Hughes on 3/12/2017.
 */
public class ErrorHandlerUtilities {

    private ErrorHandler errorHandler;
    private Set<String> disallowedNames;
    private String filename;
    private Hashtable<String, ClassTreeNode> classMap;

    public ErrorHandlerUtilities(ErrorHandler errorHandler, Set<String> disallowedNames,
                                 String filename, Hashtable<String, ClassTreeNode> classMap){
        this.errorHandler = errorHandler;
        this.disallowedNames = disallowedNames;
        this.filename = filename;
        this.classMap = classMap;
    }

    public void setFilename(String name){
        this.filename = filename;
    }

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
                    "Reserved word," + name + ",cannot be used as a field or method name");
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
