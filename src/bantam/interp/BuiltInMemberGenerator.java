package bantam.interp;

import bantam.ast.ConstStringExpr;
import bantam.ast.ExprList;
import bantam.ast.NewExpr;

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
                if(objectData == null){
                    return false;
                }
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

    public void genTextIOMembers(HashMap<String,MethodBody> methods, HashMap<String,Object> fields, ObjectData thisObject){
        fields.put("*outputStream", System.out);
        Scanner stdIn = new Scanner(System.in);
        fields.put("*inputStream", stdIn);

        methods.put("readStdin", new MethodBody() {
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
                    return Integer.parseInt(((Scanner) fields.get("*inputStream")).nextLine());
                }
                catch (NumberFormatException e){
                    return 0;
                }
            }
        });
        methods.put("putString", new MethodBody() {
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
            @Override
            public Object execute(ExprList actualParams) {
                Integer integer = (Integer) actualParams.get(0).accept(interpreterVisitor);
                ((PrintStream)fields.get("*outputStream")).print(integer);
                return thisObject;
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

    public void genObjectMembers(HashMap<String,MethodBody> methods, ObjectData thisObject) {
        methods.put("toString", new MethodBody() {
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
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData otherObject = (ObjectData)actualParams.get(0).accept(interpreterVisitor);
                return thisObject.equals(otherObject);
            }
        });
        methods.put("clone", new MethodBody() {
            @Override
            public Object execute(ExprList actualParams) {
                ObjectData newObject = (ObjectData)(new NewExpr(-1,thisObject.getType()))
                        .accept(interpreterVisitor);
                thisObject.copyFields(newObject);
                return newObject;
            }
        });


    }
}
