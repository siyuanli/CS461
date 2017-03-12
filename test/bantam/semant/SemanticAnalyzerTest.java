/*
 * File: StringConstantsVisitor.java
 * CS461 Project 3
 * Author: Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 3/11/17
 */
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
 * Author: djskrien, Phoebe Hughes, Siyuan Li, Joseph Malionek
 * Date: 3/11/17
 */
public class SemanticAnalyzerTest
{

    /**
     * Helper method which puts the given class body in a Main class with a main method
     * @param body The body of the class
     * @return The complete string of the class
     */
    private String createClass(String body){
        return "class Main { " + body + " void main(){} }";
    }

    /**
     * Helper method which creates a Main class with a main method and inserts the given
     * method body into the body of a second method.
     * @param methodBody The body of the method which is to be inserted in the method
     * @return the complete string of the resulting class
     */
    private String createMethod(String methodBody){
        return this.createClass("void test(){" + methodBody + "}");
    }

    /**
     * Helper method which creates a class with the given fields and a method containing
     * the given method body.
     * @param fieldDecs The field declarations
     * @param methodBody The body of the method
     * @return The complete string of the resulting class
     */
    private String createFieldsAndMethod(String fieldDecs, String methodBody){
        return this.createClass(fieldDecs+" void test(){\n "+methodBody+" }");
    }

    /**
     * Tests whether or not the given program is a semantically correct Bantam Java program
     * @param programString The string containing the program
     * @throws Exception Throws an exception if the String is not a semantically
     * correct Bantam Java program
     */
    private void testValidProgram(String programString) throws  Exception{
        assertFalse(testProgram(programString, "null"));
    }

    /**
     * Tests whether or not the given program is not a semantically correct Bantam Java program
     * @param programString The string containing the program
     * @throws Exception Throws an exception if the String is a semantically correct
     * Bantam Java program or if the string is not a Bantam Java program
     */
    private void testInvalidProgram(String programString) throws Exception{
        assertTrue(testProgram(programString,
                "Bantam semantic analyzer found errors."));
    }

    /**
     * Compiles the program and checks to see if the given program throws an exception
     * with the given message.
     * @param programString The program to be compiled
     * @param expectedMessage The expected error message
     * @return Whether or not an ex
     * @throws Exception
     */
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
                "class Bar extends int { } ");

        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Bar extends boolean { } ");

        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Bar extends null { } ");

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
        this.testInvalidProgram("class Main{ void main(){} } " +
                "class Main2 extends Main{ void main(int num){}}");
        this.testInvalidProgram("class Main{ void main(){} void test(int num){} } " +
                "class Main2 extends Main{ void test(boolean bool){}}");


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
        this.testValidProgram("class Main{ void main(){} } " +
                "class Main2 extends Main{ int main(){return 5;}}");


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
        this.testInvalidProgram(this.createMethod("Foo x = null; "));
        this.testInvalidProgram(this.createMethod("void x = null; "));
        this.testInvalidProgram(this.createMethod("int x = true; "));
        this.testInvalidProgram(this.createMethod("int[] x = 5; "));
        this.testInvalidProgram(this.createMethod("int int = 5; "));
        this.testInvalidProgram(this.createMethod("int[] null = null; "));
        this.testInvalidProgram(this.createMethod("int x = 5; String x = \"test\";"));
        this.testInvalidProgram(this.createClass("void test(int x){ int x = 5; }"));

        this.testValidProgram(this.createMethod("int x = 6;"));
        this.testValidProgram(this.createMethod("int[] y = new int[7];"));
        this.testValidProgram(this.createFieldsAndMethod("String x;", "int x = 0;"));
        this.testValidProgram(this.createFieldsAndMethod("int[] x;", "int[] x = null;"));
    }

    @Test
    public void testScopesWithinMethods() throws Exception{
        this.testInvalidProgram(this.createMethod(
                "int x = 6; " +
                "while(true){ boolean x = true; } "));

        this.testInvalidProgram(this.createMethod(
                "int x = 6; " +
                "if(true){ boolean x = true; } "));

        this.testInvalidProgram(this.createMethod(
                "int x = 6; " +
                "for(;;){ boolean x = true; } "));

        this.testInvalidProgram(this.createMethod(
                "int x = 6; " +
                "{ boolean x = true; } "));

        this.testValidProgram(this.createMethod(
                "int x = 6; " +
                "while(true){ int y = x; } "));

        this.testValidProgram(this.createMethod(
                "int x = 6; " +
                "if(true){ int y = x; } "));

        this.testValidProgram(this.createMethod(
                "int x = 6; " +
                "for(;;){ int y = x; } "));

        this.testValidProgram(this.createMethod(
                "int x = 6; " +
                "{ int y = x; } "));


        this.testValidProgram(this.createFieldsAndMethod("int x = 6; ",
                "while(true){ int y = this.x; }"));

        this.testValidProgram(this.createFieldsAndMethod("int x = 9;",
                "{{if(true){ int y = this.x; } }}"));

        this.testValidProgram(this.createFieldsAndMethod("int x = 6; ",
                "{for(;;){ int y = this.x; } }"));

        this.testValidProgram(this.createFieldsAndMethod("int x = 6; ",
                "{{ int y = this.x; }} "));
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
                        "x = true; a = false; z = \"ahhhh\"; " +
                        "x = null; z = null;"));
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
        this.testValidProgram(this.createMethod(
                "int[] x = new int[10]; " +
                        "String[] str = new String[10]; " +
                        "boolean[] b = new boolean[10]; " +
                        "x[3] = 12; str[5] = \"Hi Dale\"; b[15] = false; " +
                        "int num = x[5]; String string = str[90]; "));
        this.testValidProgram(
                "class Test{ int[] a = new int[10]; }" +
                "class Main extends Test{ boolean[] b = new boolean[10]; " +
                "void main(){ super.a[5] = 55; this.b[2] = true; }}");
        this.testInvalidProgram(this.createMethod(
                "int[] x = new int[10]; " +
                        "String[] str = new String[10]; " +
                        "boolean[] b = new boolean[10]; " +
                        "x[3] = true; str[5] = 55; b[15] = \"Hi Dale\"; " +
                        "arr[10] = false; " +
                        "x[true] = 3; x[\"Hi Dale\"] = 3; " +
                        "x.length = 90; x.length = true; x.length = \"Hi Dale\"; "));
        this.testInvalidProgram(
                "class Test{ int[] a = new int[10]; }" +
                        "class Main extends Test{ boolean[] b = new boolean[10]; " +
                        "void main(){ this.a[5] = 55; super.b[2] = true; }}");
        this.testInvalidProgram(
                "class Test{  }" +
                        "class Main extends Test{ " +
                        "int[] a = new int[10]; boolean[] b = new boolean[10]; " +
                        "void main(){ super.a[5] = 55; this.b[2] = true; }}");
    }

    @Test
    public void testDispatchExpr() throws Exception{
        this.testValidProgram(
                "class Hello {" +
                "String foo(int num1, String str){return str;} " +
                "void bar(){} }" +

                "class Main { " +
                "int x = 456; " +
                 "int method1(int num, boolean flag){return 10;}" +
                 "void main(){} } " +

                "class Test extends Main {" +
                "void methodCall(){" +
                "method1(3333, true);" +
                "super.method1(52, true); " +
                "this.method1(68, false);" +
                "new Hello().bar();" +
                "new Hello().foo(23445,\"Hi Dale!\");  }}" );
        this.testValidProgram("class Main{" +
                "int thing(Object obj, int num, boolean bool, boolean bool2){" +
                "return 4; }" +
                "void main(){" +
                "int variable = this.thing(new Object(), -2340981, true, false);" +
                "this.thing(\"Hi Dale\", variable,false,true); }" +
                "}");
        this.testInvalidProgram("class Main{" +
                "int thing(Object obj, int num, boolean bool, boolean bool2){" +
                "return 4; }" +
                "void main(){" +
                "thing1(new Object(), -2340981, true, false);}" +
                "}");
        this.testInvalidProgram("class Main extends B{" +
                "int thing(Object obj, int num, boolean bool, boolean bool2){" +
                "return 4; }" +
                "void main(){}}" +
                "class B {" +
                "void public(){thing(\"Hi Dale\",5,true,false);}}");
        this.testInvalidProgram("class Main{" +
                "int thing(Object obj, int num, boolean bool, boolean bool2){" +
                "return 4; }" +
                "void main(){" +
                "new HELL().thing(\"HidnDale\",5,false,true);}}");
        this.testInvalidProgram("class Main{" +
                "void main(){}" +
                "} class Main2 extends Main{" +
                "void test(){super.gazorpazorp();}}");
        this.testInvalidProgram("class Main{" +
                "int thing(Object obj, int num, boolean bool, boolean bool2){" +
                "return 4; }" +
                "void main(){" +
                "thing(new Object(), -2340981, true);}" +
                "}");
        this.testInvalidProgram("class Main{" +
                "int thing(Object obj, int num, boolean bool, boolean bool2){" +
                "return 4; }" +
                "void main(){" +
                "thing(new Object(), -2340981, true);}" +
                "}");
        this.testInvalidProgram("class Main { void main(){}" +
                "void test(int num, boolean bool, String str){}" +
                "void test2(){ test(true,5,\"Hey Dale\");}}");
        this.testInvalidProgram("class Main { void main(){}" +
                "void test(int num, boolean bool, String str){}" +
                "void test2(){ test(5,true);}}");
        this.testInvalidProgram("class Main { void main(){}" +
                "void test(int num, boolean bool, String str){}" +
                "void test2(){ test(5,true,\"Hey Dale\",5,5,5,5,5);}}");
        this.testInvalidProgram("class Main{" +
                "void main(){}" +
                "} class Main2 extends Main{" +
                "void test(){super.test();}}");
        this.testInvalidProgram("class Main {void main(){this.blah();} }");
    }

    @Test
    public void testNewExpr() throws Exception{
        this.testInvalidProgram(this.createMethod("new Baz();"));
        this.testInvalidProgram(this.createMethod("new null();"));
        this.testInvalidProgram(this.createMethod("new int[true];"));
        this.testInvalidProgram(this.createMethod("new Foo[7];"));
        this.testInvalidProgram("class Main{ void main(){}}" +
                "  class Child extends Main{}" +
                "  class Grandchild extends Child{}" +
                "  class Test { Grandchild down = new Main(); }");

        this.testInvalidProgram("class Main{ void main(){}}" +
                "  class Child extends Main{}" +
                "  class GrandchildA extends Child{}" +
                "  class GrandchildB extends Child{}" +
                "  class Test { GrandchildA sibling = new GrandchildB(); }");

        this.testValidProgram("class Main{ void main(){}} " +
                            "  class Test{ Main x = new Main(); }");
        this.testValidProgram("class Main{ void main(){}}" +
                            "  class Child extends Main{}" +
                            "  class Grandchild extends Child{}" +
                            "  class Test { Main up = new Grandchild(); }");

        this.testValidProgram(this.createMethod("new String();"));
        this.testValidProgram(this.createMethod("new int[7];"));
    }

    @Test
    public void testInstanceofExpr() throws Exception{
        String classes = "class Foo {}" +
                        " class Bar extends Foo{} " +
                        " class Baz extends Bar{} " +
                        " class Boz extends Bar{} ";

        this.testInvalidProgram( classes + this.createMethod(
                "Foo f = new Foo(); "+
                "if (f instanceof String ) {} "
        ));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz b = new Boz(); "+
                "if (b instanceof Baz ) {} "
        ));


        this.testInvalidProgram( classes + this.createMethod(
                "if (z instanceof Baz ) {} "
        ));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz b = new Boz();" +
                "if (b instanceof NotAClass ) {} "
        ));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz b = new Boz(); "+
                "if (b instanceof int ) {} "
        ));

        this.testInvalidProgram( classes + this.createMethod(
                "if (5 instanceof Foo ) {} "));

        this.testInvalidProgram( classes + this.createMethod(
                "if (new Foo[5] instanceof String[] ) {} "));

        this.testInvalidProgram( classes + this.createMethod(
                "if (new Foo() instanceof Bar[] ) {} "));

        this.testInvalidProgram( classes + this.createMethod(
                "if (new Foo[8] instanceof Bar ) {} "));


        this.testValidProgram( classes + this.createMethod(
                "if (new Foo() instanceof Foo ) {} "));

        this.testValidProgram( classes + this.createMethod(
                "if (new Baz() instanceof Foo ) {} "));

        this.testValidProgram( classes + this.createMethod(
                "if (new Foo() instanceof Bar ) {} "));


        this.testValidProgram( classes + this.createMethod(
                "if (new Foo[2] instanceof Bar[] ) {} "));

        this.testValidProgram( classes + this.createMethod(
                "if (new Bar[5] instanceof Foo[] ) {} "));


    }

    @Test
    public void testCastExpr() throws Exception{
        String classes = "class Foo {}" +
                " class Bar extends Foo{} " +
                " class Baz extends Bar{} " +
                " class Boz extends Bar{} ";

        this.testInvalidProgram( classes + this.createMethod(
                "Bar b = (Cheese)(new Bar());"));

        this.testInvalidProgram( classes + this.createMethod(
                "int b = (int)(new Bar());"));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz b = (Boz)(new Main());"));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz b = (Boz)(5);"));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz b = (Boz)(new Baz());"));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz b = (Boz)(new Boz[6]);"));

        this.testInvalidProgram( classes + this.createMethod(
                "Boz[] b = (Boz[])(new Boz());"));

        this.testInvalidProgram( classes + this.createMethod(
                "String b = (String)(new Foo());"));


        this.testValidProgram( classes + this.createMethod(
                "Boz b = (Boz)(new Foo());"));
        this.testValidProgram( classes + this.createMethod(
                "Foo b = (Foo)(new Bar());"));

        this.testValidProgram( classes + this.createMethod(
                "Boz[] b = (Boz[])(new Foo[5]);"));
        this.testValidProgram( classes + this.createMethod(
                "Foo[] b = (Foo[])(new Bar[5]);"));
        this.testValidProgram( classes + this.createMethod(
                "Foo b = (Foo)(new Foo());"));

    }

    @Test
    public void testBinaryArithExpr() throws Exception{
        this.testInvalidProgram(this.createMethod("int x = true + true;"));
        this.testInvalidProgram(this.createMethod("boolean x = true + true;"));
        this.testInvalidProgram(this.createMethod("String x = \" hi \" + \" hi \";"));
        this.testInvalidProgram(this.createMethod("int x = true + 5;"));
        this.testInvalidProgram(this.createMethod("int x = 5 + true;"));
        this.testInvalidProgram(this.createMethod("int x = true - true;"));
        this.testInvalidProgram(this.createMethod("int x = true - 5;"));
        this.testInvalidProgram(this.createMethod("int x = 5 - true;"));
        this.testInvalidProgram(this.createMethod("int x = true * true;"));
        this.testInvalidProgram(this.createMethod("int x = true * 5;"));
        this.testInvalidProgram(this.createMethod("int x = 5 * true;"));
        this.testInvalidProgram(this.createMethod("int x = true / true;"));
        this.testInvalidProgram(this.createMethod("int x = true / 5;"));
        this.testInvalidProgram(this.createMethod("int x = 5 / true;"));
        this.testInvalidProgram(this.createMethod("int x = true % true;"));
        this.testInvalidProgram(this.createMethod("int x = true % 5;"));
        this.testInvalidProgram(this.createMethod("int x = 5 % true;"));

        this.testValidProgram(this.createMethod("int x = 5 + 5; "));
        this.testValidProgram(this.createMethod("int x = 5 - 5; "));
        this.testValidProgram(this.createMethod("int x = 57 * 25; "));
        this.testValidProgram(this.createMethod("int x = 57 / 25; "));
        this.testValidProgram(this.createMethod("int x = 57 % 25; "));
    }

    @Test
    public void testBinaryCompExpr() throws Exception{
        String classes = "class Foo {}" +
                " class Bar extends Foo{} " +
                " class Baz extends Bar{} " +
                " class Boz extends Bar{} ";

        this.testInvalidProgram(this.createMethod("boolean x = true < true;"));
        this.testInvalidProgram(this.createMethod("boolean x = 8 < true;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" < 5;"));

        this.testInvalidProgram(this.createMethod("boolean x = true > true;"));
        this.testInvalidProgram(this.createMethod("boolean x = 5 > false;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" > 5;"));


        this.testInvalidProgram(this.createMethod("boolean x = true <= true;"));
        this.testInvalidProgram(this.createMethod("boolean x = 5 <= true;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" <= 5;"));

        this.testInvalidProgram(this.createMethod("boolean x = true >= true;"));
        this.testInvalidProgram(this.createMethod("boolean x = 5 >= true;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" >= 5;"));

        this.testInvalidProgram(this.createMethod("int x = true == true;"));
        this.testInvalidProgram(this.createMethod("boolean x = true == 5;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" == 5;"));

        this.testInvalidProgram(this.createMethod("int x = 5 == 5;"));
        this.testInvalidProgram(this.createMethod("boolean x = true == 5;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" == 5;"));

        this.testInvalidProgram(classes + this.createMethod("boolean x = new Main() == new Foo();"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = new Foo() == new Main();"));
        this.testInvalidProgram(classes + this.createMethod("Main x = new Main() == new Main();"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = new Foo() == 6;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = 7 == new Foo();"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = new Boz() == new Baz();"));

        this.testInvalidProgram(classes + this.createMethod("boolean x = null == 5;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = 5 == null;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = null == true;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = false == null;"));

        this.testInvalidProgram(this.createMethod("int x = true != true;"));
        this.testInvalidProgram(this.createMethod("boolean x = true != 5;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" != 5;"));

        this.testInvalidProgram(this.createMethod("int x = 5 != 5;"));
        this.testInvalidProgram(this.createMethod("boolean x = true != 5;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" != 5;"));

        this.testInvalidProgram(classes + this.createMethod("boolean x = null != 5;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = 5 != null;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = null != true;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = false != null;"));

        this.testInvalidProgram(classes + this.createMethod("boolean x = new Main() != new Foo();"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = new Foo() != new Main();"));
        this.testInvalidProgram(classes + this.createMethod("Main x = new Main() != new Main();"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = new Foo() != 6;"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = 7 != new Foo();"));
        this.testInvalidProgram(classes + this.createMethod("boolean x = new Boz() != new Baz();"));

        this.testValidProgram(this.createMethod("boolean x = 7 < 3;"));
        this.testValidProgram(this.createMethod("boolean x = 7 <= 3;"));
        this.testValidProgram(this.createMethod("boolean x = 7 > 3;"));
        this.testValidProgram(this.createMethod("boolean x = 7 >= 3;"));
        this.testValidProgram(this.createMethod("boolean x = true == true;"));
        this.testValidProgram(this.createMethod("boolean x = 5 == 7;"));
        this.testValidProgram(classes + this.createMethod(
                "boolean x = new Bar() == new Bar();"));
        this.testValidProgram(classes + this.createMethod(
                "boolean x = new Bar() == new Foo();"));
        this.testValidProgram(classes + this.createMethod(
                "boolean x = new Foo() == new Bar();"));
        this.testValidProgram(classes + this.createMethod(
                "boolean x = null == new Baz();"));
        this.testValidProgram(classes + this.createMethod(
                "boolean x = new Bar() == null;"));
        this.testValidProgram(classes + this.createMethod("boolean x = null == null;"));
        this.testValidProgram(this.createMethod("boolean x = true != true;"));
        this.testValidProgram(this.createMethod("boolean x = 5 != 7;"));
        this.testValidProgram(this.createMethod("boolean x = 5 != 7;"));
        this.testValidProgram(classes + this.createMethod("boolean x = null != new Baz();"));
        this.testValidProgram(classes + this.createMethod("boolean x = new Bar() != null;"));
        this.testValidProgram(classes + this.createMethod("boolean x = null != null;"));
    }

    @Test
    public void testBinaryLogicExpr() throws Exception{
        this.testInvalidProgram(this.createMethod("boolean x = 6 || false;"));
        this.testInvalidProgram(this.createMethod("boolean x = true || 9;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" || 9;"));
        this.testInvalidProgram(this.createMethod("boolean x = true || new String();"));
        this.testInvalidProgram(this.createMethod("boolean x = new String() || false;"));
        this.testInvalidProgram(this.createMethod("boolean x = null || false;"));
        this.testInvalidProgram(this.createMethod("boolean x = null || null;"));
        this.testInvalidProgram(this.createMethod("boolean x = new String() || null;"));
        this.testInvalidProgram(this.createMethod("boolean x = false || null;"));
        this.testInvalidProgram(this.createMethod("int x = false || true;"));
        this.testInvalidProgram(this.createMethod("int x = 7 || 3;"));


        this.testInvalidProgram(this.createMethod("boolean x = 6 && false;"));
        this.testInvalidProgram(this.createMethod("boolean x = true && 9;"));
        this.testInvalidProgram(this.createMethod("boolean x = \"hi\" && 9;"));
        this.testInvalidProgram(this.createMethod("boolean x = true && new String();"));
        this.testInvalidProgram(this.createMethod("boolean x = new String() && false;"));
        this.testInvalidProgram(this.createMethod("boolean x = null && false;"));
        this.testInvalidProgram(this.createMethod("boolean x = new String() && null;"));
        this.testInvalidProgram(this.createMethod("boolean x = null && null;"));
        this.testInvalidProgram(this.createMethod("boolean x = false && null;"));
        this.testInvalidProgram(this.createMethod("int x = false && true;"));
        this.testInvalidProgram(this.createMethod("int x = 9 && 12;"));

        this.testValidProgram(this.createMethod("boolean x = true || false;"));
        this.testValidProgram(this.createMethod("boolean x = false && false;"));
        this.testValidProgram(this.createMethod(
                "boolean x = false && false && true || false;"));
    }

    @Test
    public void testUnaryNegExpr() throws Exception{
        this.testValidProgram(this.createMethod("int x = -0;"));
        this.testValidProgram(this.createMethod("boolean x = -3<=-4;"));
        this.testValidProgram(this.createMethod("int x = - - - - -3;"));
        this.testValidProgram(this.createClass("int thing(){return 4;} " +
                "void asdf(){int x = -(-this.thing());}"));
        this.testInvalidProgram(this.createMethod("int x = -true;"));
        this.testInvalidProgram(this.createMethod("int x = -\"hi\";"));
        this.testInvalidProgram(this.createMethod("int x = -(new String());"));
        this.testInvalidProgram(this.createMethod("int x = -null;"));
        this.testInvalidProgram(this.createMethod("boolean x = -8;"));

        this.testInvalidProgram(this.createMethod("int x =-x;"));
        this.testInvalidProgram(this.createMethod("boolean x = -true;"));
        this.testInvalidProgram(this.createMethod("boolean null = -null;"));
        this.testInvalidProgram(this.createClass("boolean thing(){return true;} " +
                "void asdf(){int x = -(-this.thing());}"));
        this.testValidProgram(this.createMethod("int x = -5;"));
        this.testValidProgram(this.createMethod("int x = -102004;"));
        this.testValidProgram(this.createMethod("int x = -(-(-(-(-102004))));"));
    }

    @Test
    public void testUnaryNotExpr() throws Exception{
        this.testValidProgram(this.createMethod("boolean x = !!!!!!!!!!!!true;"));
        this.testValidProgram(this.createMethod("boolean x = true; x = !x;"));
        this.testValidProgram(this.createClass("boolean thing(int x){return true;}" +
                "boolean test(){boolean x = !this.thing(4); return !x;}"));
        this.testValidProgram(this.createMethod("if(!(true==!true)){}"));

        this.testInvalidProgram(this.createMethod("int x = !3;"));
        this.testInvalidProgram(this.createMethod("boolean x = !3;"));
        this.testInvalidProgram(this.createMethod("boolean thing = !thing;"));
        this.testInvalidProgram(this.createMethod("boolean x = true; int y = 4; y = !x;"));
    }

    @Test
    public void testIncrDecrExpr() throws Exception{
        this.testValidProgram(this.createMethod("int x = 0; x++;"));
        this.testValidProgram(this.createMethod("int x = 0; x=x--;"));
        this.testValidProgram(this.createMethod("int x = 0; x=x-++x;"));
        this.testValidProgram(this.createMethod("int[] x = new int[4]; x[4]++;"));
        this.testValidProgram(this.createMethod("int[] x = new int[6]; --x[4];"));
        this.testValidProgram(this.createMethod("int x = 2514523; x = x+++ ++x;"));
        this.testValidProgram(this.createMethod("int x = 0; x = -x++;"));
        this.testValidProgram(this.createMethod("int[] x=new int[4]; x[4]= x[3] + --x[3];"));

        this.testInvalidProgram(this.createMethod("boolean x = false; x++;"));
        this.testInvalidProgram(this.createMethod("Object x = null; --x;"));
        this.testInvalidProgram(this.createMethod("int x = 0; null++;"));

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
        this.testValidProgram(this.createMethod("int[] x = new int[4]; int thing= x.length;"));
        this.testValidProgram(this.createMethod("Object[] x = new Object[4]; int thing= x.length;"));
        this.testValidProgram(this.createFieldsAndMethod("int length = 4; int thing = this.length;",""));
        this.testValidProgram(this.createFieldsAndMethod("int length = 4; int thing = length;",""));

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