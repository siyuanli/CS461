/*
 * File: BuiltInMemberGenerator.java
 * CS461 Project 5 First Extension
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 4/30/17
 */

package bantam.interp;

import bantam.ast.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

/**
 * Generate the builtin classes (Object String Sys TextIO) of Bantam Java
 */
public class BuiltInMemberGenerator {

    /**
     * The interpreter visitor that interprets Bantam Java
     */
    private InterpreterVisitor interpreterVisitor;

    /**
     * Create a new builtin memeber generator
     * @param visitor the interpreter visitor
     */
    public BuiltInMemberGenerator(InterpreterVisitor visitor){
        this.interpreterVisitor = visitor;
    }

    /**
     * Generate the builtin methods of String
     * @param methods a hashmap of methods
     * @param fields a hashmap of fields
     */
    public void genStringMembers(HashMap<String,MethodBody> methods, HashMap<String,Object> fields){
        fields.put("length", 0);
        fields.put("*str","");
        methods.put("length", actualParams -> fields.get("length"));
        methods.put("equals", new MethodBody() {
            /**
             * Implement the equals method of String
             * @param actualParams the string to compare with
             * @return a boolean indicating if strings are equal
             */
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                if(objectData == null){
                    return false;
                }
                return fields.get("*str").equals(objectData.getField("*str",false));
            }
        });
        methods.put("toString", new MethodBody() {
            /**
             * Implement the equals method of String
             * @param actualParams the parameters of the method
             * @return the string itself
             */
            @Override
            public Object execute(ExprList actualParams) {
                return interpreterVisitor.visit(new ConstStringExpr(-1, (String)fields.get("*str"))) ;
            }
        });
        methods.put("substring", new MethodBody() {
            /**
             * Implement the substring method of String
             * @param actualParams the parameters of the method
             * @return the substring between the two indices
             */
            @Override
            public Object execute(ExprList actualParams) {
                Integer startI = (Integer) actualParams.get(0).accept(interpreterVisitor);
                Integer endI = (Integer) actualParams.get(1).accept(interpreterVisitor);
                String s = ((String)fields.get("*str")).substring(startI, endI);
                return interpreterVisitor.visit(new ConstStringExpr(-1, s)) ;
            }
        });
        methods.put("concat", new MethodBody() {
            /**
             * Implement the concat method of String
             * @param actualParams the parameters of the method
             * @return the concatenated string
             */
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String s = ((String)fields.get("*str"));
                String concatString = (String)objectData.getField("*str", false);
                return interpreterVisitor.visit(new ConstStringExpr(-1, s + concatString)) ;
            }
        });
    }

    /**
     * Generate the builtin methods of TextIO
     * @param methods a hashmap of methods
     * @param fields a hashmap of fields
     * @param thisObject the current object
     */
    public void genTextIOMembers(HashMap<String,MethodBody> methods, HashMap<String,Object> fields, ObjectData thisObject){
        fields.put("*outputStream", System.out);
        Scanner stdIn = new Scanner(System.in);
        fields.put("*inputStream", stdIn);

        methods.put("readStdin", new MethodBody() {
            /**
             * Implement the readStdin method of TextIO set to read from standard input
             * @param actualParams the parameters of the method
             * @return null
             */
            @Override
            public Object execute(ExprList actualParams) {
                if(!fields.get("*inputStream").equals(stdIn)) {
                    ((Scanner) fields.get("*inputStream")).close();
                }
                fields.put("*inputStream", stdIn);
                return null;
            }
        });
        methods.put("readFile", new MethodBody() {
            /**
             * Implement the readFile method of TextIO that set to read from standard input
             * @param actualParams the parameters of the method
             * @return null
             */
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String fileName = (String)objectData.getField("*str", false);
                try {
                    if(!fields.get("*inputStream").equals(stdIn)) {
                        ((Scanner) fields.get("*inputStream")).close();
                    }
                    fields.put("*inputStream", new Scanner(new FileInputStream(fileName)));
                }
                catch (FileNotFoundException e){
                    System.err.println("Cannot read from " + fileName + ". File not found.");
                    System.exit(-1);
                }
                return null;
            }
        });
        methods.put("writeStdout", new MethodBody() {
            /**
             * Implement the writeStdout method of TextIO that set to write to standard output
             * @param actualParams the parameters of the method
             * @return null
             */
            @Override
            public Object execute(ExprList actualParams) {
                PrintStream outputStream = (PrintStream) fields.get("*outputStream");
                if (!(outputStream.equals(System.out) || outputStream.equals(System.err))){
                    outputStream.close();
                }
                fields.put("*outputStream", System.out);
                return null;
            }
        });
        methods.put("writeStderr", new MethodBody() {
            /**
             * Implement the writeStderr method of TextIO that set to write to standard error
             * @param actualParams the parameters of the method
             * @return null
             */
            @Override
            public Object execute(ExprList actualParams) {
                PrintStream outputStream = (PrintStream) fields.get("*outputStream");
                if (!(outputStream.equals(System.out) || outputStream.equals(System.err))){
                    outputStream.close();
                }
                fields.put("*outputStream", System.err);
                return null;
            }
        });
        methods.put("writeFile", new MethodBody() {
            /**
             * Implement the writeFile method of TextIO that set to write to specified file
             * @param actualParams the parameters of the method
             * @return null
             */
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String fileName = (String)objectData.getField("*str", false);
                try {
                    PrintStream outputStream = (PrintStream) fields.get("*outputStream");
                    if (!(outputStream.equals(System.out) || outputStream.equals(System.err))){
                        outputStream.close();
                    }
                    fields.put("*outputStream", new PrintStream(fileName));
                }
                catch (FileNotFoundException e){
                    System.err.println("Cannot write to " + fileName + ". File not found.");
                    System.exit(-1);
                }
                return null;
            }
        });
        methods.put("getString", new MethodBody() {
            /**
             * Implement the getString method of TextIO that read next string
             * @param actualParams the parameters of the method
             * @return the next string
             */
            @Override
            public Object execute(ExprList actualParams) {
                String s = ((Scanner)fields.get("*inputStream")).nextLine();
                return interpreterVisitor.visit(new ConstStringExpr(-1, s)) ;
            }
        });
        methods.put("getInt", new MethodBody() {
            /**
             * Implement the getInt method of TextIO that read next int
             * @param actualParams the parameters of the method
             * @return the next int
             */
            @Override
            public Object execute(ExprList actualParams) {
                try {
                    return Integer.parseInt(((Scanner) fields.get("*inputStream")).nextLine());
                }
                catch (NumberFormatException e){
                    return 0;
                }
            }
        });
        methods.put("putString", new MethodBody() {
            /**
             * Implement the putString method of TextIO that write specified string
             * @param actualParams the input string to be written
             * @return the current object
             */
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String string = "null";
                if(objectData != null){
                    string = (String)objectData.getField("*str", false);
                }
                ((PrintStream)fields.get("*outputStream")).print(string);
                return thisObject;
            }
        });
        methods.put("putInt", new MethodBody() {
            /**
             * Implement the putInt method of TextIO that write specified int
             * @param actualParams the input int to be written
             * @return the current object
             */
            @Override
            public Object execute(ExprList actualParams) {
                Integer integer = (Integer) actualParams.get(0).accept(interpreterVisitor);
                ((PrintStream)fields.get("*outputStream")).print(integer);
                return thisObject;
            }
        });
    }

    /**
     * Generate the builtin methods of Sys
     * @param methods a hashmap of methods
     */
    public void genSysMembers(HashMap<String,MethodBody> methods) {
        methods.put("exit", new MethodBody() {
            /**
             * Implement the exit method of Sys that exit program with specified status
             * @param actualParams the numerical status
             * @return null
             */
            @Override
            public Object execute(ExprList actualParams) {
                Integer integer = (Integer) actualParams.get(0).accept(interpreterVisitor);
                System.exit(integer);
                return null;
            }
        });
        methods.put("time", new MethodBody() {
            /**
             * Implement the time method of Sys that return UTC time
             * @param actualParams the parameters of the method
             * @return the UTC time in seconds
             */
            @Override
            public Object execute(ExprList actualParams) {
                return (int)(System.currentTimeMillis()/1000);
            }
        });
        methods.put("random", new MethodBody() {
            /**
             * Implement the random method of Sys that return a random int
             * @param actualParams the parameters of the method
             * @return a random int
             */
            @Override
            public Object execute(ExprList actualParams) {
                return (new Random()).nextInt(Integer.MAX_VALUE) ;
            }
        });
    }

    /**
     * Generate the builtin methods of Object
     * @param methods a hashmap of methods
     * @param thisObject the object
     */
    public void genObjectMembers(HashMap<String,MethodBody> methods, ObjectData thisObject) {
        methods.put("toString", new MethodBody() {
            /**
             * Implements the toString method of Object that return a string
             * representation of object
             * @param actualParams the parameters of the method
             * @return the string representation of object
             */
            @Override
            public Object execute(ExprList actualParams) {
                String data = thisObject.toString();
                String toString = thisObject.getType() + data.substring(data.indexOf("@"));
                ObjectData toStringObject =
                        (ObjectData) interpreterVisitor.visit(new ConstStringExpr(-1,toString));
                return toStringObject;
            }
        });
        methods.put("equals", new MethodBody() {
            /**
             * Implements the equals method of Object that test if objects are equal
             * @param actualParams the input object to compare with
             * @return a boolean indicating if two objects are equal
             */
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData otherObject = (ObjectData)actualParams.get(0).accept(interpreterVisitor);
                return thisObject.equals(otherObject);
            }
        });
        methods.put("clone", new MethodBody() {
            /**
             * Implements the clone method of Object that copy an object
             * @param actualParams the parameters of the method
             * @return the cloned object
             */
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData newObject = (ObjectData)(new NewExpr(-1,thisObject.getType()))
                        .accept(interpreterVisitor);
                thisObject.copyFields(newObject);
                return newObject;
            }
        });

    }

    public void genArrays(HashMap<String,MethodBody> methods, ObjectArrayData thisObject){
        //TODO: should it actually have these methods? if so parser does not allow it
        this.genObjectMembers(methods, thisObject);
        methods.put("clone", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                String length = Integer.toString(thisObject.getLength());
                ObjectArrayData newObject = (ObjectArrayData)(new NewArrayExpr(-1,
                        thisObject.getType(), new ConstIntExpr(-1, length)))
                        .accept(interpreterVisitor);
                thisObject.copyFields(newObject);
                newObject.setArray(thisObject.getArray().clone());
                return newObject;
            }
        });

    }

    public void genExceptionMembers(HashMap<String,MethodBody> methods,
                                    HashMap<String,Object> fields, ObjectData thisObject) {
        BantamException e = new BantamException(thisObject.getType(),"",thisObject);
        fields.put("*e",e);
        fields.put("message",null);
        methods.put("getMessage", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                return fields.get("message");
            }
        });
        methods.put("setMessage", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData object = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String newMessage = (String) object.getField("*str", false);
                fields.put("message",new ConstStringExpr(-1,newMessage).accept(interpreterVisitor));
                e.setMessage(newMessage);
                return null;
            }
        });
    }
}
