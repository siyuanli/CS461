package bantam.interp;

import bantam.ast.ConstStringExpr;
import bantam.ast.ExprList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by joseph on 4/23/17.
 */
public class BuiltInMemberGenerator {

    private InterpreterVisitor interpreterVisitor;

    public BuiltInMemberGenerator(InterpreterVisitor visitor){
        this.interpreterVisitor = visitor;
    }

    public void genStringMembers(HashMap<String,MethodBody> methods, HashMap<String,Object> fields){
        fields.put("length", 0);
        fields.put("*str","");
        methods.put("length", actualParams -> fields.get("length"));
        methods.put("equals", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                return fields.get("*str").equals(objectData.getField("*str",false));
            }
        });
        methods.put("toString", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                return interpreterVisitor.visit(new ConstStringExpr(-1, (String)fields.get("*str"))) ;
            }
        });
        methods.put("substring", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                Integer startI = (Integer) actualParams.get(0).accept(interpreterVisitor);
                Integer endI = (Integer) actualParams.get(1).accept(interpreterVisitor);
                String s = ((String)fields.get("*str")).substring(startI, endI);
                return interpreterVisitor.visit(new ConstStringExpr(-1, s)) ;
            }
        });
        methods.put("concat", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String s = ((String)fields.get("*str"));
                String concatString = (String)objectData.getField("*str", false);
                return interpreterVisitor.visit(new ConstStringExpr(-1, s + concatString)) ;
            }
        });
    }

    public void genTextIOMembers(HashMap<String,MethodBody> methods, HashMap<String,Object> fields){
        /*fields.put("readFD", 0);
        fields.put("writeFD", 1);*/
        //TODO:Check if this is correct
        fields.put("*outputStream", System.out);
        fields.put("*inputStream", new Scanner(System.in));

        methods.put("readStdin", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ((Scanner)fields.get("*inputStream")).close();
                fields.put("*inputStream", new Scanner(System.in));
                return null;
            }
        });
        methods.put("readFile", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String fileName = (String)objectData.getField("*str", false);
                try {
                    ((Scanner)fields.get("*inputStream")).close();
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
            @Override
            public Object execute(ExprList actualParams) {
                String s = ((Scanner)fields.get("*inputStream")).nextLine();
                return interpreterVisitor.visit(new ConstStringExpr(-1, s)) ;
            }
        });
        methods.put("getInt", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                try {
                    return ((Scanner) fields.get("*inputStream")).nextInt();
                }
                catch (InputMismatchException e){
                    return 0;
                }
            }
        });
        methods.put("putString", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData objectData = (ObjectData) actualParams.get(0).accept(interpreterVisitor);
                String string = (String)objectData.getField("*str", false);
                ((PrintStream)fields.get("*outputStream")).print(string);
                return fields.get("*this");
            }
        });
        methods.put("putInt", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                Integer integer = (Integer) actualParams.get(0).accept(interpreterVisitor);
                ((PrintStream)fields.get("*outputStream")).print(integer);
                return fields.get("*this");
            }
        });
    }

    public void genSysMembers(HashMap<String,MethodBody> methods) {
        methods.put("exit", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                Integer integer = (Integer) actualParams.get(0).accept(interpreterVisitor);
                System.exit(integer);
                return null;
            }
        });
        methods.put("time", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                return (int)(System.currentTimeMillis()/1000);
            }
        });
        methods.put("random", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                return (new Random()).nextInt(Integer.MAX_VALUE) ;
            }
        });
    }

    public void genObjectMembers(HashMap<String,MethodBody> methods, HashMap<String,Object> fields) {

    }
}
