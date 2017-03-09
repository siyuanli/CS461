package bantam.semant;

import bantam.ast.Program;
import bantam.lexer.Lexer;
import org.junit.Test;
import bantam.parser.Parser;
import bantam.util.ErrorHandler;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * File: SemanticAnalyzerTest.java
 * Author: djskrien
 * Date: 2/13/17
 */
public class SemanticAnalyzerTest
{

    private String createClass(String body){
        String program = "class Main { void main(){} " + body + "}";
        return program;
    }

    private String createMethod(String methodBody){
        String program = this.createClass("void test(){" + methodBody + "}");
        return program;
    }

    private String createFieldsAndMethod(String fieldDecs, String methodBody){
        String program = this.createClass(fieldDecs+"\n"+this.createMethod(methodBody));
        return program;
    }

    private void testValidProgram(String programString) throws  Exception{
        assertFalse(testProgram(programString, "null"));
    }

    private void testInvalidProgram(String programString) throws Exception{
        assertTrue(testProgram(programString, "Bantam semantic analyzer found errors."));
    }

    private boolean testProgram(String programString, String expectedMessage) throws  Exception{
        boolean thrown = false;
        Parser parser = new Parser(new Lexer(new StringReader(programString)));
        Program program = (Program) parser.parse().value;
        SemanticAnalyzer analyzer = new SemanticAnalyzer(program, false);
        try {
            analyzer.analyze();
        } catch (RuntimeException e) {
            thrown = true;
            try {
                assertEquals(expectedMessage, e.getMessage());
            } catch (AssertionError assertE) {
                for (ErrorHandler.Error err : analyzer.getErrorHandler().getErrorList()) {
                    System.out.println(err);
                }
                throw assertE;
            }
        }
        return thrown;
    }

    @Test
    public void testMainMainClass() throws Exception {
        this.testInvalidProgram("class Main {  }");
        this.testInvalidProgram("class Main{ int main(){ return 5;} }");
        this.testValidProgram("class Main{ void main(){}}");
        this.testValidProgram("class Test{ void main(){} }" +
                "class Main extends Test{ }");
    }

    @Test
    public void testDeclStmt() throws Exception{
        String program = this.createMethod("int x = \"lkesfjkw\"; ");
        this.testInvalidProgram(program);
    }

    @Test
    public void testIfStmt() throws Exception{
        String program = this.createMethod("if() ");
        this.testInvalidProgram(program);
    }


    @Test
    public void testArrayExpr() throws Exception{
        this.testValidProgram(this.createMethod("int[] x = new int[3];" +
                "x[3]=4;"));
        this.testValidProgram(this.createMethod("Object[] x = new Object[3+4];" +
                "x[3]=null;"));
        this.testValidProgram(this.createMethod("Object[] x = new Object[3+4];" +
                "x[3]=new Object();"));
        this.testValidProgram(this.createFieldsAndMethod("int x[];",
                "this.x = new int[4];" + "int x = this.x[4];"));
    }




}