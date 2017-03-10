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
                System.out.println(expectedMessage +"   ;  "+ e.getMessage());
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


        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Bar extends Foo { } " +
                "class Foo extends Bar {} " +
                "class Test extends Foo {} ");
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
                "class Main extends Test{ }" +
                "class Foo extends Test{} " +
                "class Bar {} " +
                "class Baz extends Foo{} ");
    }

    @Test
    public void testFieldDeclaration() throws Exception{
        this.testInvalidProgram(this.createFieldsAndMethod( "int x = \"hi\";", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int x = true;", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int null;", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int void;", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "void x;", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int x; int x; ", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int[] x; String x; ", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int[] x = new int[\"hi\"]; ", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int[] x = new int[true];", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int[] x = new String[6];", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "Foo j;", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "Foo j = new Foo();", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int x = y; int y = 5;", ""));

        this.testValidProgram(this.createFieldsAndMethod( "int x = 7; String[] y = new String[5];", ""));
        this.testValidProgram(this.createFieldsAndMethod( "int[] x = new int[6]; int y = 5;", ""));
        this.testValidProgram(this.createFieldsAndMethod( "int[] x;", ""));
        this.testValidProgram(this.createFieldsAndMethod( "int j;", ""));
    }

    @Test
    public void testFieldMethodInheritance() throws Exception{
        this.testValidProgram("class Main { " +
                                "int x = 5; " +
                                "int testMethod(int num){}" +
                                "void main(){} } " +
                            " class Test extends Main {" +
                                "int method(){ " +
                                    "super.main(); " +
                                    "testMethod(x); " +
                                    "return super.x; }}" +
                            " class Child extends Test{" +
                                "int y = super.x; " +
                                "int z = x;" +
                                "int j = super.testMethod(y);" +
                                "int e = testMethod(y); }" );

        this.testValidProgram(this.createFieldsAndMethod("int x;", "int j = this.x; int k = x;"));
        //this.testInvalidProgram(this.createFieldsAndMethod("int x;", "int j = this.x; int k = x;"));
    }

    @Test
    public void testDeclStmt() throws Exception{
        String program = this.createMethod("int x = \"lkesfjkw\"; ");
        this.testInvalidProgram(program);
    }

    @Test
    public void testIfStmt() throws Exception{
        this.testValidProgram(this.createMethod(
                "if(3==3){int x = 3;} "));
        this.testValidProgram(this.createMethod(
                "if(3==4){int x = 3;} else {int y = 6;}"));
        this.testInvalidProgram(this.createMethod(
                "if(\"Hi Dale\"){ int x = 3;} "));
        this.testInvalidProgram(this.createMethod(
                "if(11111){ int x = 3;} "));
        this.testInvalidProgram(this.createMethod(
                "if(x = 5){ int x = 3;} "));
        this.testInvalidProgram(this.createMethod(
                "if(1 == 1){ x = 3;} "));
    }

    @Test
    public void testWhileStmt() throws Exception{
        this.testValidProgram( this.createMethod(
                "int x = 0; while(true){ x = x + 2; }"));
        this.testValidProgram( this.createMethod(
                "int x = 6; while(x > 2){ x = x - 2;}"));
        this.testInvalidProgram( this.createMethod(
                "int x = 0; while(55){ x = x + 2; }"));
        this.testInvalidProgram( this.createMethod(
                "int x = 0; while(\"Hi Dale\"){ x = x + 2; }"));
        this.testInvalidProgram( this.createMethod(
                "int x = 0; while(x = 5){ x = x + 2; }"));
    }

    @Test
    public void testForStmt() throws Exception{
        this.testValidProgram( this.createMethod(
                "int i = 0; for(i = 0;i<5;){i = i+3;}"));
        this.testValidProgram( this.createMethod(
                "int i = 0; for(567;true;i){i = i+3;}"));
        this.testInvalidProgram( this.createMethod(
                "int i = 0; for(567;i=4;i){i = i+3;}"));
        this.testInvalidProgram( this.createMethod(
                "int i = 0; for(i = 0;567;){i = i+3;}"));
        this.testInvalidProgram( this.createMethod(
                "int i = 0; for(i = 0;\"Hi Dale\";){i = i+3;}"));
    }

    @Test
    public void testBreakStmt() throws Exception{
        this.testValidProgram( this.createMethod(
                "while(true){ break; }"));
        this.testValidProgram( this.createMethod(
                "int i = 0; for(i = 0;;){break;}"));
        this.testInvalidProgram( this.createMethod(
                "if(5==5){break;}"));
        this.testInvalidProgram( this.createMethod(
                "String x = \"Hi Dale\"; break;"));
    }

    @Test
    public void testAssignExpr() throws Exception{
        this.testValidProgram(this.createMethod(
                "int x=null; int y=123; String a=\"aww\"; boolean z=true;"));
        this.testValidProgram(this.createMethod(
                ""));
    }

    @Test
    public void testArrayAssignExpr() throws Exception{

    }

    @Test
    public void testDispatchExpr() throws Exception{

    }

    @Test
    public void testNewExpr() throws Exception{

    }

    @Test
    public void testInstanceofExpr() throws Exception{

    }

    @Test
    public void testCastExpr() throws Exception{

    }

    @Test
    public void testBinaryArithExpr() throws Exception{

    }

    @Test
    public void testBinaryCompExpr() throws Exception{

    }

    @Test
    public void testBinaryLogicExpr() throws Exception{

    }

    @Test
    public void testUnaryNegExpr() throws Exception{

    }

    @Test
    public void testUnaryNotExpr() throws Exception{

    }

    @Test
    public void testIncrDecrExpr() throws Exception{

    }

    @Test
    public void testVarExpr() throws Exception{

    }

    @Test
    public void testArrayExpr() throws Exception{
        this.testValidProgram(this.createMethod(
                "int[] x = new int[3]; x[3]=4;"));
        this.testValidProgram(this.createMethod(
                "Object[] x = new Object[3+4]; x[3]=null;"));
        this.testValidProgram(this.createMethod(
                "Object[] x = new Object[3+4]; x[3]=new Object();"));
        this.testValidProgram(this.createFieldsAndMethod("int[] x;",
                "this.x = new int[4];" + "int x = this.x[4];"));
        this.testValidProgram("class Main { void main(){}}" +
                "class Foo { int[] x; }" +
                "class Bar extends Foo { void test(){ int z = super.x[1]; } } ");
        this.testInvalidProgram("class Main { void main(){" +
                "int[] array = new int[4];" +
                "boolean thing = array[3]; } }");
        this.testInvalidProgram("class Main { void main(){" +
                "int[] array = new int[4];" +
                "int thing = array[true]; } }");
        this.testInvalidProgram("class Main { void main(){" +
                "int[] array = new int[4];" +
                "int thing = this.array[4]; } }");
    }


}