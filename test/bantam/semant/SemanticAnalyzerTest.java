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
            System.out.println(expectedMessage +"   ;  "+ e.getMessage());
            for (ErrorHandler.Error err : analyzer.getErrorHandler().getErrorList()) {
                System.out.println(err);
            }
            assertEquals(expectedMessage, e.getMessage());
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
        this.testInvalidProgram(this.createFieldsAndMethod( "Foo j;", ""));
        this.testInvalidProgram(this.createFieldsAndMethod( "int x = y; int y = 5;", ""));

        this.testValidProgram(this.createFieldsAndMethod( "int x = 7; String[] y = new String[5];", ""));
        this.testValidProgram(this.createFieldsAndMethod( "int[] x = new int[6]; int y = 5;", ""));
        this.testValidProgram(this.createFieldsAndMethod( "int[] x;", ""));
        this.testValidProgram(this.createFieldsAndMethod( "int j;", ""));
        this.testValidProgram(this.createFieldsAndMethod( "int j;", " String j=null;"));
    }

    @Test
    public void testFieldMethodInheritance() throws Exception{
        this.testInvalidProgram(this.createFieldsAndMethod("int x;", "int j = this.x; int k = this.j; "));
        this.testInvalidProgram(this.createFieldsAndMethod("int x;", "int j = super.x;"));
        this.testInvalidProgram(this.createFieldsAndMethod("int x;", "int j = super.hi();"));
        this.testInvalidProgram(this.createClass("int m1(){ return 5; } void m2(){ int j = super.m1(); } "));

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
                                "int a = super.x; " +
                                "int b = this.x;" +
                                "int c = x;" +
                                "int d = super.testMethod(a);" +
                                "int e = testMethod(b); " +
                                "int f = this.testMethod(c); }" );

        this.testValidProgram("class Main { " +
                                    "int[] x; " +
                                    "String[] testMethod(int num){}" +
                                    "void main(){} } " +
                            " class Child extends Main{" +
                                    "int a = super.x[1]; " +
                                    "int[] b = super.x; " +
                                    "int c = this.x[5];" +
                                    "int[] d = this.x;" +
                                    "int e = x[9];" +
                                    "int[] f = x;" +
                                    "String[] g = super.testMethod(a);" +
                                    "String[] h = testMethod(a); " +
                                    "String[] i = this.testMethod(a); }" );

        this.testValidProgram("class Main { int x = 0; void main(){} }" +
                "class Test extends Main{ void test(){ boolean x = true;} } " );

        this.testValidProgram(this.createFieldsAndMethod("int x;", "int j = this.x; int k = x;"));
        this.testValidProgram(this.createClass("int m1(){ return 5;} void m2(){ int j = this.m1(); }"));

    }

    @Test
    public void testMethods() throws  Exception{
        this.testInvalidProgram(this.createClass("void null(){}"));
        this.testInvalidProgram(this.createClass("void int(){}"));
        this.testInvalidProgram(this.createClass("Foo test(){ }"));
        this.testInvalidProgram(this.createClass("Foo[] test(){ }"));
        this.testInvalidProgram(this.createClass("int test(){ return true; }"));
        this.testInvalidProgram(this.createClass("boolean test(){ " +
                "if (true) { return true; } else {return false;} }"));
        this.testInvalidProgram(this.createClass("void test(){ return 5; }"));
        this.testInvalidProgram(this.createClass("void[] test(){ return; }"));
        this.testInvalidProgram(this.createClass("int test(){ return null; }"));
        this.testInvalidProgram(this.createClass("boolean test(){ return null; }"));
        this.testInvalidProgram(this.createClass("void test(int x){} void test(){}"));
        this.testInvalidProgram(this.createClass("void test(int x, String[] y){}" +
                                                " void test(String[] y, int x){}"));
        this.testInvalidProgram(this.createClass("void test(int[] x, String x){} "));
        this.testInvalidProgram(this.createClass("int test(){ return 5;} void test(){}"));
        this.testInvalidProgram(this.createClass("void test(void x){}"));
        this.testInvalidProgram(this.createClass("void test(Foo[] x){}"));
        this.testInvalidProgram(this.createClass("void test(int void){}"));
        this.testInvalidProgram(this.createClass("void test(int[] null){}"));
        this.testInvalidProgram(this.createClass("void test(){}  " +
                " void test2(){ int n = test(); }"));

        this.testValidProgram("class Main { int x = 5;  void x(int x){} void main(){} }");
        this.testValidProgram(this.createClass("void test(int x, int c, String[] s){}"));
        this.testValidProgram(this.createClass("String[] test(int[] c, String[] s){}"));
        this.testValidProgram(this.createClass("void test(){} "));
        this.testValidProgram(this.createClass("void test(){ return;} "));
        this.testValidProgram(this.createClass("int baz(){ return 3032;} "));
        this.testValidProgram(this.createClass("boolean test(){ " +
                "if (true) { return true; } return true;} "));
        this.testValidProgram("class Main{ void main(){} }" +
                "class Test extends Main { void main(){} }");
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
                "int x = 0; String a = null; boolean z = true;" +
                        "x = 5; a = \"ahhhh\"; z = false;"));
        this.testValidProgram("class Test{ int a = 0; }" +
                "class Main extends Test{ int b = 1;  void main(){ " +
                "int a = 0; super.a = 55; this.b = 66; }}");
        this.testInvalidProgram(this.createMethod(
                "int x = 0; String a = null; boolean z = true;" +
                        "x = true; a = false; z = \"ahhhh\";"));
        this.testInvalidProgram(this.createMethod(
                "hi = 123; bye = false; this.x = true; super.y = 0;"));
        this.testInvalidProgram("class Test{ int a = 0; }" +
                "class Main extends Test{ int b = 1;  void main(){ " +
                "int a = 0; this.a = 55; super.b = 66; }}");
        this.testInvalidProgram("class Test{ }" +
                "class Main extends Test{ int a = 0; void main(){ int b = 1; " +
                " super.a = 55; this.b = 66; }}");

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
        this.testValidProgram(this.createMethod("int x = 0; x++;"));
        System.out.println(this.createMethod("int x = 0; x=x--;"));
        this.testValidProgram(this.createMethod("int x = 0; x=x--;"));
        this.testValidProgram(this.createMethod("int x = 0; x=x-++x;"));

    }

    @Test
    public void testVarExpr() throws Exception{
        this.testValidProgram(this.createMethod(
                "int x = 4;"));
        this.testValidProgram(this.createMethod(
                "int x = 4; int y = x; x = -y+x; x++;"));
        this.testValidProgram(this.createMethod(
                "boolean x = false;"));
        this.testValidProgram(this.createFieldsAndMethod("int[] x;",
                "this.x=null;"));
        this.testValidProgram("class Main { void main(){}}" +
                "class Foo { int x; }" +
                "class Bar extends Foo { void test(){ super.x = super.x; } } ");
        this.testValidProgram("class Main { void main(){}}" +
                "class Foo { int x; }" +
                "class Bar extends Foo { void test(){ super.x = x; } } ");
        this.testValidProgram("class Main { void main(){}}" +
                "class Foo { int y; int x; }" +
                "class Bar extends Foo { void test(){ this.y = super.x; } } ");
        this.testValidProgram(this.createMethod("if(true) int x =3; " +
                "else int x = 4; " +
                "int x = 5;"));
        this.testInvalidProgram(this.createMethod(
                "int x = null;"));
        this.testInvalidProgram(this.createMethod(
                "boolean null = false;"));
        this.testInvalidProgram("class Main { void main(){}}" +
                "class Foo { int x; }" +
                "class Bar extends Foo { void test(){ boolean y = super.x; } } ");
        this.testInvalidProgram(this.createMethod("int x = 4; if(1==3){{{{int x = 4;}}}}"));
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
        this.testInvalidProgram(this.createMethod(
                "int[] array = new int[4];" +
                "boolean thing = array[3];"));
        this.testInvalidProgram(this.createMethod(
                "int[] array = new int[4];" +
                "int thing = array[true];"));
        this.testInvalidProgram("class Main { void main(){" +
                "int[] array = new int[4];" +
                "int thing = this.array[4]; } }");
        this.testInvalidProgram("class Main { void main(){" +
                "int[] array = new int[4];" +
                "int thing = super.array[4]; } }");
        this.testInvalidProgram("class Main { void main(){" +
                "int[] array = new int[4];" +
                "boolean thing = array[4]; } }");
    }


}