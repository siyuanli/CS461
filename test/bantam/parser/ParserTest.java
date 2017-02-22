package bantam.parser;

import bantam.ast.*;
import java_cup.runtime.Symbol;
import bantam.lexer.Lexer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import bantam.util.ErrorHandler;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

/*
 * File: ParserTest.java
 * Author: djskrien
 * Date: 2/13/17
 */
public class ParserTest
{
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @BeforeClass
    public static void begin() {
        /* add here any initialization code for all test methods. For example,
         you might want to initialize some fields here. */
    }

    public Symbol getParseTreeRoot(String program) throws Exception{
        Parser parser = new Parser(new Lexer(new StringReader(program)));
        Symbol result = parser.parse();
        assertEquals(0, parser.getErrorHandler().getErrorList().size());
        assertNotNull(result);
        return result;
    }

    public MemberList getClassBody(int classIndex, String program) throws Exception{
        Symbol result = this.getParseTreeRoot(program);
        ClassList classes = ((Program) result.value).getClassList();
        Class_ mainClass = (Class_) classes.get(classIndex);
        return mainClass.getMemberList();
    }

    public StmtList getMethodBody(int classIndex, int memberIndex, String program) throws Exception{
        MemberList memberList = this.getClassBody(classIndex, program);
        Method method = (Method)memberList.get(memberIndex);
        return method.getStmtList();
    }

    public void testClass(Class_ mainClass, String className ){
        assertEquals(className, mainClass.getName());
        assertEquals(0, mainClass.getMemberList().getSize());
    }

    /** tests the case of a Main class with no members */
    @Test
    public void emptyMainClassTest() throws Exception {
        Symbol result = this.getParseTreeRoot("class Main { }");
        ClassList classes = ((Program) result.value).getClassList();
        assertEquals(1, classes.getSize());
        Class_ mainClass = (Class_) classes.get(0);
        this.testClass(mainClass, "Main");
    }

    /** tests the case of multiple classes */
    @Test
    public void multipleClassesTest() throws Exception{
        Symbol result = this.getParseTreeRoot("class Main {} class Main2 {}");
        ClassList classes = ((Program) result.value).getClassList();
        assertEquals(2, classes.getSize());
        Class_ mainClass = (Class_) classes.get(0);
        Class_ main2Class = (Class_) classes.get(1);
        this.testClass(mainClass, "Main");
        this.testClass(main2Class, "Main2");
    }

    /** tests the case of extending a class */
    @Test
    public void extendsTest() throws Exception{
        Symbol result = this.getParseTreeRoot("class Main extends Test{}");
        ClassList classes = ((Program) result.value).getClassList();
        assertEquals(1, classes.getSize());
        Class_ mainClass = (Class_) classes.get(0);
        assertEquals(mainClass.getParent(), "Test");
    }

    public void fieldTest(String type, String name, Boolean hasAssignment, Field field){
        assertEquals(type, field.getType());
        assertEquals(name, field.getName());
        if (hasAssignment) {
            assertNotNull(hasAssignment.toString(), field.getInit());
        }
        else{
            assertNull(hasAssignment.toString(), field.getInit());
        }

    }

    /** tests the case of a Main class with no members */
    @Test
    public void fieldsTest() throws Exception {
        String program = "class Main { " +
                " String x;" +
                " int y = 5;" +
                " Boolean[] z;" +
                " int[] a = b;" +
                "}";

        MemberList memberList = this.getClassBody(0, program);
        assertEquals(4, memberList.getSize());
        this.fieldTest("String", "x", false, (Field)memberList.get(0));
        this.fieldTest("int", "y", true, (Field)memberList.get(1));
        this.fieldTest("Boolean[]", "z", false, (Field)memberList.get(2));
        this.fieldTest("int[]", "a", true, (Field)memberList.get(3));
    }

    public void formalListTest(String[][] formalProperties, FormalList formalList){
        assertEquals(formalProperties.length, formalList.getSize());
        for (int i =0; i< formalList.getSize(); i++){
            Formal formal = (Formal) formalList.get(i);
            assertEquals(formalProperties[i][0], formal.getType());
            assertEquals(formalProperties[i][1], formal.getName());
            i++;
        }
    }

    public void methodTest(String returnType, String name, String[][] params, int stmtListSize, Method method ){
        assertEquals(returnType, method.getReturnType());
        assertEquals(name, method.getName());
        this.formalListTest(params, method.getFormalList());
        assertEquals(stmtListSize, method.getStmtList().getSize());

    }

    /** tests the case of a Method */
    @Test
    public void methodsTest() throws Exception{
        String program = " class Main{" +
                "int method1 () {}" +
                "int method2 () { int a = 0; }" +
                "int method3 (int z, int p) { int a = 0; int b = 5;}" +
                "int[] method4 () {}" +
                "int[] method5 () { int a = 0; }" +
                "int[] method6 (int a) { int a = 0; }" +
                "}";
        ;
        String[][] noParams = {};
        String[][] method3Params = {{"int", "z"},{"int", "p"}};
        String[][] method6Params = {{"int", "a"}};
        MemberList memberList = this.getClassBody(0, program);
        assertEquals(6, memberList.getSize());
        this.methodTest("int", "method1", noParams, 0, (Method)memberList.get(0));
        this.methodTest("int", "method2", noParams, 1, (Method)memberList.get(1));
        this.methodTest("int", "method3", method3Params, 2, (Method)memberList.get(2));
        this.methodTest("int[]", "method4", noParams, 0, (Method)memberList.get(3));
        this.methodTest("int[]", "method5", noParams, 1, (Method)memberList.get(4));
        this.methodTest("int[]", "method6", method6Params, 1, (Method)memberList.get(5));
    }


    /** test the case of both Method and Field  */
    @Test
    public void methodsFieldTest() throws Exception{
        String program = " class Main{" +
                "int y = 5;" +
                "int method1 () {}" +
                "}";
        ;
        MemberList memberList = this.getClassBody(0, program);
        String[][] noParams = {};
        assertEquals(2, memberList.getSize());
        this.fieldTest("int", "y", true, (Field)memberList.get(0));
        this.methodTest("int", "method1", noParams, 0, (Method)memberList.get(1));
    }

    /** tests the case of one thing in Class */
    @Test
    public void singleItemMemberList() throws Exception{
        String program = " class Main{" +
                "int y = 5;" +
                "}";
        ;
        MemberList memberList = this.getClassBody(0, program);
        assertEquals(1, memberList.getSize());
        this.fieldTest("int", "y", true, (Field)memberList.get(0));
    }



    /* INVALID CODE TESTS ----------------------------------------------------------*/
    /**
     * tests the case of a missing right brace at end of a class def
     * using an ExpectedException Rule
     */
    @Test
    public void unmatchedLeftBraceTest1() throws Exception {
        Parser parser = new Parser(new Lexer(new StringReader("class Main {  ")));
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Bantam parser found errors.");
        parser.parse();
    }

    /**
     * tests the case of a missing right brace at end of a class def.
     * This version is like unmatchedLeftBraceTest1 except that it
     * doesn't use an ExpectedException Rule and it also prints the error messages.
     */
    @Test
    public void unmatchedLeftBraceTest2() throws Exception {
        Parser parser = new Parser(new Lexer(new StringReader("class Main {  ")));
        boolean thrown = false;

        try {
            parser.parse();
        } catch (RuntimeException e) {
            thrown = true;
            assertEquals("Bantam parser found errors.", e.getMessage());
            for (ErrorHandler.Error err : parser.getErrorHandler().getErrorList()) {
                System.out.println(err);
            }
        }
        assertTrue(thrown);
    }

}