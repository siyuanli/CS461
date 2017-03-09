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
        return "class Main { " + body + " void main(){} }";
    }

    private String createMethod(String methodBody){
        return this.createClass("void test(){" + methodBody + "}");
    }

    private String createFieldsAndMethod(String fieldDecs, String methodBody){
        return this.createClass(fieldDecs+" void test(){\n "+methodBody+" }");
    }

    private void testValidProgram(String programString) throws  Exception{
        assertFalse(testProgram(programString, "null"));
    }

    private void testInvalidProgram(String programString) throws Exception{
        assertTrue(testProgram(programString,
                "Bantam semantic analyzer found errors."));
    }

    private boolean testProgram(String programString, String expectedMessage) throws Exception{
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
                for (ErrorHandler.Error err : analyzer.getErrorHandler().getErrorList()) {
                    System.out.println(err);
                }
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
    public void testInvalidMainMainClass() throws Exception {
        this.testInvalidProgram("class Main {  }");
        this.testInvalidProgram("class Main{ int main(){ return 5;} }");
    }

    @Test
    public void testIllegalInheritance() throws Exception{
        this.testInvalidProgram("class Main extends Test{ void main(){} } " +
                "class Test extends Bar {} " +
                "class Bar extends Main {}");

        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Bar extends Foo {} ");

        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Bar extends Sys {} ");

        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Bar extends String {} ");

        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Bar extends TextIO { } ");
    }

    @Test
    public void testInvalidClassNames() throws Exception{
        this.testInvalidProgram("class Main{ void main(){} }" +
                "class void{}" +
                "class int{}");

        this.testInvalidProgram("class Main{ void main(){} }" +
                "class Foo{ void test(){}} " +
                "class Foo{} ");

    }

    @Test
    public void testValidClasses() throws Exception{
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
        this.testValidProgram(
                this.createMethod("if(3==3){int x = 3;} "));
        this.testValidProgram(
                this.createMethod("if(3==4){int x = 3;} else {int y = 6;}"));
        this.testInvalidProgram(
                this.createMethod("if(\"hi\"){ int x = 3;} "));
        this.testInvalidProgram(
                this.createMethod("if(11111){ int x = 3;} "));
        this.testInvalidProgram(
                this.createMethod("if(x = 5){ int x = 3;} "));
        this.testInvalidProgram(
                this.createMethod("if(1 == 1){ x = 3;} "));
    }

    @Test
    public void testWhileStmt() throws Exception{
        //this.testValidProgram( this.createMethod("int x = 0; while(true){ x = x + 2; }"));

        System.out.println(this.createMethod("int x = 6; while(x > 2){ x--; }"));
        this.testValidProgram( this.createMethod("int x = 6; while(x > 2){}"));
        /*this.testInvalidProgram( this.createMethod(
                ""));
        this.testInvalidProgram( this.createMethod(
                ""));
        this.testInvalidProgram( this.createMethod(
                ""));*/
    }

    @Test
    public void testForStmt() throws Exception{

        this.testValidProgram( this.createMethod(""));
        this.testValidProgram( this.createMethod(""));
        this.testInvalidProgram( this.createMethod(""));
        this.testInvalidProgram( this.createMethod(""));
        this.testInvalidProgram( this.createMethod(""));
    }

    @Test
    public void testBreakStmt() throws Exception{

        this.testValidProgram( this.createMethod(""));
        this.testValidProgram( this.createMethod(""));
        this.testInvalidProgram( this.createMethod(""));
        this.testInvalidProgram( this.createMethod(""));
        this.testInvalidProgram( this.createMethod(""));
    }

    @Test
    public void testArrayExpr() throws Exception{
        this.testValidProgram(this.createMethod("int[] x = new int[3];" +
                "x[3]=4;"));
        this.testValidProgram(this.createMethod("Object[] x = new Object[3+4];" +
                "x[3]=null;"));
        this.testValidProgram(this.createMethod("Object[] x = new Object[3+4];" +
                "x[3]=new Object();"));
        this.testValidProgram(this.createFieldsAndMethod("int[] x;",
                "this.x = new int[4];" + "int x = this.x[4];"));
        this.testValidProgram("class Main { void main(){}}" +
                "class Foo { int[] x; }" +
                "class Bar extends Foo { void test(){ int z = super.x[1]; } } ");
    }




}