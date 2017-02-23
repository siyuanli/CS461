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

    /**
     * Helper method to get the root of a parse tree of the given string
     * @param program the string to be parsed
     * @return the root of the parse tree
     * @throws Exception if something bad happened while parsing
     */
    private Symbol getParseTreeRoot(String program) throws Exception{
        Parser parser = new Parser(new Lexer(new StringReader(program)));
        Symbol result = parser.parse();
        assertEquals(0, parser.getErrorHandler().getErrorList().size());
        assertNotNull(result);
        return result;
    }

    /**
     * Helper method to get the body of a specific class from a java program
     * @param classIndex the index of the class in the program
     * @param program the string containing the java program
     * @return the list of classes
     * @throws Exception if something bad happened while parsing
     */
    private MemberList getClassBody(int classIndex, String program) throws Exception{
        Symbol result = this.getParseTreeRoot(program);
        ClassList classes = ((Program) result.value).getClassList();
        Class_ mainClass = (Class_) classes.get(classIndex);
        return mainClass.getMemberList();
    }

    /**
     * Gets the body of a specific method, given a java program,
     * the index of the class in the program, and the index of the method in the class
     * @param classIndex the index of the class
     * @param memberIndex the index of the member
     * @param program the string containing the Java program
     * @return the body of the method
     * @throws Exception if something bad happened while parsing
     */
    private StmtList getMethodBody(int classIndex, int memberIndex, String program) throws Exception{
        MemberList memberList = this.getClassBody(classIndex, program);
        Method method = (Method)memberList.get(memberIndex);
        return method.getStmtList();
    }

    /**
     * Returns the expression of the expression statement at the given index in the
     * statement list
     * @param stmtList the statement list
     * @param index the index of the ExprStmt
     * @return the Expression
     */
    private Expr getExpr(StmtList stmtList, int index) {
        return ((ExprStmt)stmtList.get(index)).getExpr();
    }

    /**
     * tests whether a class is empty and has the given name
     * @param mainClass the class
     * @param className the name of the class
     */
    private void testClass(Class_ mainClass, String className ){
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

    private void fieldTest(String type, String name, Boolean hasAssignment, Field field){
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

    private void formalListTest(String[][] formalProperties, FormalList formalList){
        assertEquals(formalProperties.length, formalList.getSize());
        for (int i =0; i< formalList.getSize(); i++){
            Formal formal = (Formal) formalList.get(i);
            assertEquals(formalProperties[i][0], formal.getType());
            assertEquals(formalProperties[i][1], formal.getName());
            i++;
        }
    }

    private void methodTest(String returnType, String name, String[][] params, int stmtListSize, Method method ){
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

        MemberList memberList = this.getClassBody(0, program);
        String[][] noParams = {};
        assertEquals(2, memberList.getSize());
        this.fieldTest("int", "y", true, (Field)memberList.get(0));
        this.methodTest("int", "method1", noParams, 0, (Method)memberList.get(1));
    }

    /** tests the case of one item in Class */
    @Test
    public void singleItemMemberList() throws Exception{
        String program = " class Main{" +
                "int y = 5;" +
                "}";

        MemberList memberList = this.getClassBody(0, program);
        assertEquals(1, memberList.getSize());
        this.fieldTest("int", "y", true, (Field)memberList.get(0));
    }

    @Test
    public void noArrayDeclStmt() throws Exception{
        String program = " class Main{" +
                " void method () { boolean flag = true; }" +
                "}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(1, stmtList.getSize());
        DeclStmt statement = (DeclStmt) stmtList.get(0);
        assertEquals("boolean", statement.getType());
        assertEquals("flag", statement.getName());
        assertNotNull(statement.getInit());

    }

    @Test
    public void arrayDeclStmt() throws Exception{
        String program = "class Main{int method () { boolean[] flag = true; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(1, stmtList.getSize());
        DeclStmt statement = (DeclStmt) stmtList.get(0);
        assertEquals("boolean[]", statement.getType());
        assertEquals("flag", statement.getName());
        assertNotNull(statement.getInit());
    }


    @Test
    public void ifWithElse() throws Exception{
        String program = "class Main{int method () { if(true) break; else return; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(1, stmtList.getSize());
        IfStmt statement = (IfStmt) stmtList.get(0);
        assertEquals("true", ((ConstBooleanExpr) statement.getPredExpr()).getConstant());
        assert statement.getThenStmt() instanceof BreakStmt;
        assert statement.getElseStmt() instanceof ReturnStmt;
    }

    @Test
    public void ifNoElse() throws Exception{
        String program = "class Main{int method () { if(true) break; return; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(2, stmtList.getSize());
        IfStmt statement = (IfStmt) stmtList.get(0);
        assertEquals("true", ((ConstBooleanExpr) statement.getPredExpr()).getConstant());
        assert statement.getThenStmt() instanceof BreakStmt;
        assert stmtList.get(1) instanceof ReturnStmt;
    }

    @Test
    public void whileAndBreakStatement() throws Exception{
        String program = "class Main{int method () { while(true) break; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(1, stmtList.getSize());
        WhileStmt statement = (WhileStmt) stmtList.get(0);
        assertEquals("true", ((ConstBooleanExpr) statement.getPredExpr()).getConstant());
        assert statement.getBodyStmt() instanceof BreakStmt;
    }

    @Test
    public void forStatement() throws Exception{
        String[] strings = {"for(;;){}","for(a;;){}","for(;a;){}","for(a;a;){}"
                            ,"for(;;a){}","for(a;;a){}","for(;a;a){}","for(a;a;a){}"};
        for(int i = 0; i< 8;i++) {
            String program = "class Main{int method () {"+strings[i]+"}}";
            StmtList stmtList = this.getMethodBody(0, 0, program);
            assertEquals(1, stmtList.getSize());
            ForStmt statement = (ForStmt) stmtList.get(0);
            if(i%2==0){
                assertNull(statement.getInitExpr());
            }
            else{
                assertEquals("a",((VarExpr)statement.getInitExpr()).getName());
            }
            if(i/2%2==0){
                assertNull(statement.getPredExpr());
            }
            else{
                assertEquals("a",((VarExpr)statement.getPredExpr()).getName());
            }
            if(i<4){
                assertNull(statement.getUpdateExpr());
            }
            else{
                assertEquals("a",((VarExpr)statement.getUpdateExpr()).getName());
            }
        }
    }

    /**
     * tests the legal usage of return statements
     * @throws Exception if the test failed
     */
    @Test
    public void returnStatement() throws Exception{
        String program = "class Main{int method () { return; return 7; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(2, stmtList.getSize());
        assert stmtList.get(0) instanceof ReturnStmt;
        ReturnStmt stmt2 = (ReturnStmt) stmtList.get(1);
        assertEquals("7", ((ConstIntExpr) stmt2.getExpr()).getConstant());
    }

    /**
     * tests empty block statements
     * @throws Exception if the test failed
     */
    @Test
    public void emptyBlockStatement() throws Exception{
        String program = "class Main{int method () { {} }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(1, stmtList.getSize());
        BlockStmt stmt = (BlockStmt) stmtList.get(0);
        assertEquals(0,stmt.getStmtList().getSize());
    }

    /**
     * tests legal assignment statements
     * @throws Exception if the test failed
     */
    @Test
    public void assignExpr() throws Exception{
        String program = "class Main{int method () { this.a = 4; a[3]=4; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(2, stmtList.getSize());
        AssignExpr stmt1 = (AssignExpr) this.getExpr(stmtList, 0);
        ArrayAssignExpr stmt2 = (ArrayAssignExpr) this.getExpr(stmtList, 1);
        assertEquals("a", stmt1.getName());
        assertEquals("4", ((ConstIntExpr)stmt1.getExpr()).getConstant());
        assertEquals("this", stmt1.getRefName());
        assertEquals("a", stmt2.getName());
        assertEquals("4", ((ConstIntExpr)stmt2.getExpr()).getConstant());
        assertEquals("3", ((ConstIntExpr)stmt2.getIndex()).getConstant());
    }

    /**
     * tests legal usage of dispatch
     * @throws Exception if the test failed
     */
    @Test
    public void dispatchExpr() throws Exception{
        String program = "class Main{int method () { a.b().c(7); d.e(x,y); }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(2, stmtList.getSize());
        DispatchExpr expr1 = (DispatchExpr) this.getExpr(stmtList, 0);
        DispatchExpr expr2 = (DispatchExpr) this.getExpr(stmtList, 1);
        assertEquals("c",expr1.getMethodName());
        assertEquals(1,expr1.getActualList().getSize());
        assertEquals("7",((ConstIntExpr)expr1.getActualList().get(0)).getConstant());
        //a.b()
        DispatchExpr expr3 = (DispatchExpr) expr1.getRefExpr();
        assertEquals("b",expr3.getMethodName());
        assertEquals(0,expr3.getActualList().getSize());
        assertEquals("a",((VarExpr)expr3.getRefExpr()).getName());
        assertEquals("e",expr2.getMethodName());
        assertEquals(2,expr2.getActualList().getSize());
        assertEquals("d",((VarExpr)expr2.getRefExpr()).getName());
    }

    /**
     * tests legal usage of new
     * @throws Exception if the tests fail
     */
    @Test
    public void newExpr() throws Exception{
        String program = "class Main{int method () { new a(); new a[3]; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(2, stmtList.getSize());
        NewExpr expr1 = (NewExpr) this.getExpr(stmtList, 0);
        NewArrayExpr expr2 = (NewArrayExpr) this.getExpr(stmtList, 1);
        assertEquals("a", expr1.getType());
        assertEquals("a", expr2.getType());
        assertEquals("3", ((ConstIntExpr) expr2.getSize()).getConstant());
    }

    /**
     * tests legal instance of usage
     * @throws Exception if the tests fail
     */
    @Test
    public void instanceOfTest() throws Exception{
        String program = "class Main{ " +
                            "int method () { " +
                                "x instanceof int; " +
                                "y instanceof int[]; } " +
                         "}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        InstanceofExpr expr1 = (InstanceofExpr)this.getExpr(stmtList, 0);
        InstanceofExpr expr2 = (InstanceofExpr)this.getExpr(stmtList, 1);
        assertEquals("x", ((VarExpr)expr1.getExpr()).getName());
        assertEquals("int", expr1.getType());
        assertEquals("y", ((VarExpr)expr2.getExpr()).getName());
        assertEquals("int[]", expr2.getType());
    }

    /**
     * test legal casting statements
     * @throws Exception if the tests failed
     */
    @Test
    public void castTest() throws Exception{
        String program = "class Main{ " +
                "int method () { " +
                    "(int) (num); " +
                    "(int[]) (num); } " +
                "}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        CastExpr expr1 = (CastExpr)this.getExpr(stmtList, 0);
        CastExpr expr2 = (CastExpr)this.getExpr(stmtList, 1);
        assertEquals("int", expr1.getType());
        assertEquals("num", ((VarExpr)expr1.getExpr()).getName());
        assertEquals("int[]", expr2.getType());
        assertEquals("num", ((VarExpr)expr2.getExpr()).getName());

    }

    /**
     * tests legal data types
     * @throws Exception if the tests fail
     */
    @Test
    public void dataTypesTest() throws Exception{
        String program = "class Main{ " +
                "int method () { " +
                    "50; " +
                    "true; " +
                    "\"hi\"; } " +
                "}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        ConstIntExpr expr1 = (ConstIntExpr)this.getExpr(stmtList, 0);
        ConstBooleanExpr expr2 = (ConstBooleanExpr)this.getExpr(stmtList, 1);
        ConstStringExpr expr3 = (ConstStringExpr)this.getExpr(stmtList, 2);
        assertEquals("50",  expr1.getConstant());
        assertEquals("true",  expr2.getConstant());
        assertEquals("\"hi\"",  expr3.getConstant());
    }

    /**
     * tests legal arithmetic operations
     * @throws Exception if the test fail
     */
    @Test
    public void arithmeticCompTest() throws  Exception {
        String program = "class Main{int method () { " +
                "a + b;" +
                "c - d;" +
                "e * f;" +
                "g / h;" +
                "i % j;" +
                "}}";

        StmtList stmtList = this.getMethodBody(0, 0, program);
        BinaryExpr plus = (BinaryExpr)this.getExpr(stmtList, 0);
        assert plus instanceof BinaryArithPlusExpr;

        BinaryExpr minus = (BinaryExpr)this.getExpr(stmtList, 1);
        assert minus instanceof BinaryArithMinusExpr;

        BinaryExpr times = (BinaryExpr)this.getExpr(stmtList, 2);
        assert times instanceof BinaryArithTimesExpr;

        BinaryExpr divide = (BinaryExpr)this.getExpr(stmtList, 3);
        assert divide instanceof BinaryArithDivideExpr;

        BinaryExpr modulus = (BinaryExpr)this.getExpr(stmtList, 4);
        assert modulus instanceof BinaryArithModulusExpr;
    }

    /**
     * tests legal binary comparison operators
     * @throws Exception if the tests fail
     */
    @Test
    public void binaryCompTest() throws  Exception{
        String program = "class Main{int method () { " +
                "a == b;" +
                "c != d;" +
                "e < f;" +
                "g <= h;" +
                "i > j;" +
                "k >= l;" +
                "}}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        BinaryExpr eq = (BinaryExpr)this.getExpr(stmtList, 0);
        assert eq instanceof BinaryCompEqExpr;

        BinaryExpr ne = (BinaryExpr)this.getExpr(stmtList, 1);
        assert ne instanceof BinaryCompNeExpr;

        BinaryExpr lt = (BinaryExpr)this.getExpr(stmtList, 2);
        assert lt instanceof BinaryCompLtExpr;

        BinaryExpr leq = (BinaryExpr)this.getExpr(stmtList, 3);
        assert leq instanceof BinaryCompLeqExpr;

        BinaryExpr gt = (BinaryExpr)this.getExpr(stmtList, 4);
        assert gt instanceof BinaryCompGtExpr;

        BinaryExpr geq = (BinaryExpr)this.getExpr(stmtList, 5);
        assert geq instanceof BinaryCompGeqExpr;
    }

    /**
     * tests legal binary logic operators
     * @throws Exception if the tests fail
     */
    @Test
    public void binaryLogicTest() throws  Exception{
        String program = "class Main{int method () { " +
                "a || b;" +
                "c && d;" +
                "}}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        BinaryExpr or = (BinaryExpr)this.getExpr(stmtList, 0);
        assert or instanceof BinaryLogicOrExpr;

        BinaryExpr and = (BinaryExpr)this.getExpr(stmtList, 1);
        assert and instanceof BinaryLogicAndExpr;
    }

    /**
     * Tests legal unary operators
     * @throws Exception if the tests fail
     */
    @Test
    public void unaryOperatorsTest() throws Exception{
        String program = "class Main{int method () { " +
                "z++;" +
                "++z;" +
                "!z;" +
                "-z;" +
                "z--;" +
                "--z;" +
                "}}";
        StmtList stmtList = this.getMethodBody(0, 0, program);

        UnaryExpr postIncr = (UnaryExpr)this.getExpr(stmtList, 0);
        assert postIncr instanceof UnaryIncrExpr;
        assertEquals(true, postIncr.isPostfix());

        UnaryExpr preIncr = (UnaryExpr)this.getExpr(stmtList, 1);
        assert preIncr instanceof UnaryIncrExpr;
        assertEquals(false, preIncr.isPostfix());

        UnaryExpr not = (UnaryExpr)this.getExpr(stmtList, 2);
        assert not instanceof UnaryNotExpr;

        UnaryExpr unaryMinus = (UnaryExpr)this.getExpr(stmtList, 3);
        assert unaryMinus instanceof UnaryNegExpr;

        UnaryExpr postDecr = (UnaryExpr)this.getExpr(stmtList, 4);
        assert postDecr instanceof UnaryDecrExpr;
        assertEquals(true, postDecr.isPostfix());

        UnaryExpr preDecr = (UnaryExpr)this.getExpr(stmtList, 5);
        assert preDecr instanceof UnaryDecrExpr;
        assertEquals(false, preDecr.isPostfix());
    }

    /**
     * Tests if the name and if the var has a reference matches the properties of the
     * VarExpr
     * @param name the name the VarExpr should have
     * @param hasReference if the VarExpr should have a reference
     * @param varExpr the VarExpr being tested
     */
    private void varExprTest(String name, Boolean hasReference, VarExpr varExpr){
        assertEquals(name, varExpr.getName());
        if (hasReference){
            assertNotNull(varExpr.getRef());
        }
        else{
            assertNull(varExpr.getRef());
        }
    }

    /**
     * tests if the name, index, and if the array has a reference matches the ArrayExpr
     * @param name the name the ArrayExpr should have
     * @param hasReference if the ArrayExpr should have a reference
     * @param index the index the ArrayExpr should have
     * @param arrayExpr the ArrayExpr being tested
     */
    private void arrayExprTest(String name, Boolean hasReference, int index, ArrayExpr arrayExpr){
        assertEquals(name, arrayExpr.getName());
        if (hasReference){
            assertNotNull(arrayExpr.getRef());
        }
        else{
            assertNull(arrayExpr.getRef());
        }
        assertEquals(index, ((ConstIntExpr)arrayExpr.getIndex()).getIntConstant());
    }


    /**
     * test VarExpr and ArrayExprs
     * @throws Exception if the test fails
     */
    @Test
    public void varExprTest() throws Exception {
        String program = "class Main{ int method(){" +
                "a;" +
                "this.b;" +
                "this.d[2];" +
                "c[1];" +
                "}}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        this.varExprTest("a", false, (VarExpr)this.getExpr(stmtList, 0));
        this.varExprTest("b", true, (VarExpr)this.getExpr(stmtList, 1));
        this.arrayExprTest("d", true, 2, (ArrayExpr)this.getExpr(stmtList,2));
        this.arrayExprTest("c", false, 1, (ArrayExpr) this.getExpr(stmtList,3));


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

    /**
     * tests the legality of a given program
     * @param str the program
     * @return if the program is legal
     * @throws Exception if the parser crashed
     */
    private boolean badTest(String str) throws Exception {
        try {
            Parser parser = new Parser(new Lexer(new StringReader(str)));
            parser.parse();
        }
        catch (RuntimeException e){
            return true;
        }
        return false;
    }

    /**
     * tests if the given body of the class is legal
     * @param str the body of the class
     * @return if the test fails
     * @throws Exception if the test fails or the parser crashed
     */
    private boolean badTestClass(String str) throws Exception{
        return badTest("class Main{"+str+"}");
    }

    /**
     * tests if the given body of the method is legal
     * @param str the body of the method
     * @return if the test fails
     * @throws Exception the test fails or the parser crashed
     */
    private boolean badTestMethod(String str) throws Exception{
        return badTestClass("void main(){"+str+"}");
    }

    /**
     * test incorrect syntax for a class
     * @throws Exception if the test fails or the parser crashed
     */
    @Test
    public void classErrors() throws Exception {
        assert badTest("");
        assert badTest("class Main{");
        assert badTest("class Main{ class Test{}}");
        assert badTest("interface X{}");
    }

    /**
     * tests incorrect syntax for a method
     * @throws Exception if the test fails or the parser crashed
     */
    @Test
    public void methodErrors() throws Exception{
        assert badTestClass("int (int x, int y){x = 4;}"); //missing id
        assert badTestClass("int (int x, int y){ "); //missing closing {
        assert badTestClass("int (int x int y){x = 4;}"); //missing comma in params
    }

    /**
     * tests incorrect syntax for a field
     * @throws Exception if the test fails or the parser crashed
     */
    @Test
    public void fieldError() throws Exception{
        assert badTestClass("int ;"); //missing id
        assert badTestClass("[] x ;"); //missing id
        assert badTestClass("int[] x"); //missing ;
        assert badTestClass("int[ x;"); //missing closing ]
        assert badTestClass("int[] x =;"); //missing assignment statement
        assert badTestClass("int[] x = class Main{};"); //missing assignment statement
    }

    /**
     * tests detection of missing semicolon
     * @throws Exception if the tests fails or the parser crashed
     */
    @Test
    public void missingSemicolon() throws Exception {
        assert badTestMethod("x + 6");
    }

    /**
     * Tests incorrect declaration statement syntax
     * @throws Exception if the test fails
     */
    @Test
    public void declStmtError() throws Exception{
        assert badTestMethod("int x = ;");
        assert badTestMethod("int[ x = 10;");
        assert badTestMethod("10 = int x;");
        assert badTestMethod("int x = 3 53 5;");
        assert badTestMethod("int 10 = 3;");
        assert badTestMethod("10 int = 2;");
        assert badTestMethod("int] x = 4;");
        assert badTestMethod("int y = 5");
        assert badTestMethod("int z  7;");
        assert badTestMethod("int p = class Main{};");
    }

    /**
     * tests incorrect if statement syntax
     * @throws Exception if the test fails or the parser crashes
     */
    @Test
    public void ifStatementError() throws Exception{
        assert badTestMethod("if (x) 3");
        assert badTestMethod("if (x 3;");
        assert badTestMethod("if x (x) {3;}");
        assert badTestMethod("if (x) 2 {3;}");
        assert badTestMethod("Gif (x)  {3;}");
        assert badTestMethod("if if (x)  {3;}");
        assert badTestMethod("if (x) 3; else ");
        assert badTestMethod("if (x) else {x;}");
        assert badTestMethod("(x) 3; else {x;} ");
    }

    /**
     * Tests invalid while loop syntax
     * @throws Exception if the test fails or the parser crashes
     */
    @Test
    public void whileError() throws Exception{
        assert badTestMethod("while (x) ");
        assert badTestMethod("x while (x) {}");
        assert badTestMethod("while (break;) {}");
        assert badTestMethod( "while (x {}");
        assert badTestMethod( "whi (x) {}");
    }

    /**
     * Tests invalid for loop syntax
     * @throws Exception if the test fails or the parser crashes
     */
    @Test
    public void forError() throws Exception{
        assert badTestMethod("for (;; {x;}");
        assert badTestMethod("x for (;;) {x;}");
        assert badTestMethod("for (;) {x;}");
        assert badTestMethod("for (;;) x {x;}");
        assert badTestMethod("for  x (;;) {x;}");
        assert badTestMethod("for (;;;) {x;}");
        assert badTestMethod("for (;;) ");
        assert badTestMethod("fr (;;) {x;}");
        assert badTestMethod("for ( x= ; 3; 6) {x;};");
    }

    /**
     * Tests incorrect syntax of break statements
     * @throws Exception if the test fails or the parser crashes
     */
    @Test
    public void breakError() throws Exception{
        assert badTestMethod("break");
        assert badTestMethod("hi break");
        assert badTestMethod("break kirqlh;");
    }

    /**
     * Tests incorrect syntax of block statements
     * @throws Exception if the test fails or the parser crashes
     */
    @Test
    public void blockError() throws Exception{
        assert badTestMethod("{id anotherId id3;}");
        assert badTestMethod("{int methodInside(){}}");
        assert badTestMethod("{");
        assert badTestMethod("}{");
    }

    /**
     * Tests incorrect syntax of expressions.
     * @throws Exception if the test fails or the parser crashes
     */
    @Test
    public void exprError() throws Exception{
        assert badTestMethod("{(class Main{})}");
        assert badTestMethod("{(break;)}");
        assert badTestMethod("{(()}");
        assert badTestMethod("{(return x;)}");
    }



    /**
     * Tests some expressions with lexErrors
     * @throws Exception if the test fails
     */
    @Test
    public void lexError() throws Exception {
        assert badTest("class Main{~~~~~}");
        assert badTest("class _Main{}");
        assert badTest("class 3Main{}");
        assert badTest("class Main{String x = \"asdf;lhwlwer;");
        assert badTest("class/*12345M Main {}");
        assert badTest("class Main{String x =\"123\\4\"}");
        assert badTest("class Main{int x = 111111111111111111111111111111111111111111;}");
        String s = "class Main{String x = \"L";
        for(int i = 0;i <50000;i++){
            s = s +"O";
        }
        s=s+"L\";}";
        assert badTest(s);
    }

    /**
     * Tests some incorrect assign expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badAssign() throws Exception {
        assert badTestMethod("a=;");
        assert badTestMethod("3=4;");
        assert badTestMethod("a.b.c=4;");
        assert badTestMethod("=4;");
        assert badTestMethod("a()=5;");
        assert badTestMethod("a[]=4;");
        assert badTestMethod("a[3]=a[];");
    }

    /**
     * Tests some incorrect dispatch expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badDispatch() throws Exception {
        assert badTestMethod("a.();");
        assert badTestMethod("a.b(.c);");
        assert badTestMethod("a(,);");
        assert badTestMethod("a.b(a,);");
        assert badTestMethod("c.(a,b;");
        assert badTestMethod("c(.a,b);");
    }

    /**
     * Tests some incorrect new expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badNew() throws Exception {
        assert badTestMethod("new id (;");
        assert badTestMethod("new id(3);");
        assert badTestMethod("new id[];");
        assert badTestMethod("new id[(;");
        assert badTestMethod("new thing id (;");
        assert badTestMethod("new id.();");
        assert badTestMethod("new thing[3][4];");
    }

    /**
     * Tests some incorrect instanceof expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badInstanceOf() throws Exception {
        assert badTestMethod("x instanceof ;");
        assert badTestMethod("x instanceof instanceof y;");
        assert badTestMethod("x instanceof 4;");
        assert badTestMethod("x instanceof thing[;");
        assert badTestMethod("x instancof thing;");
    }

    /**
     * Tests some incorrect casting expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badCast() throws Exception {
        assert badTestMethod("x=int) (thing);");
        assert badTestMethod("x= (int[) (thing);");
        assert badTestMethod("x=(int] (thing);");
        assert badTestMethod("x=(int) 5;");
        assert badTestMethod("x=(int) thing);");
        assert badTestMethod("x=(int[]) (thing[);");
    }

    /**
     * Tests some incorrect arithmetic expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badArithExpr() throws Exception {
        String[] ops = {"+","-","*","/","%"};
        for(int i = 0;i<5;i++){
            assert badTestMethod("class"+ops[i]+"4;");
            assert badTestMethod("4"+ops[i]+";");
        }
    }

    /**
     * Tests some incorrect binary comparison expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badBinaryComp() throws Exception {
        String[] ops = {"==","!=","<",">","<=",">="};
        for(int i = 0;i<6;i++){
            assert badTestMethod("class"+ops[i]+"4;");
            assert badTestMethod(ops[i]+"4;");
        }
    }

    /**
     * Tests some incorrect binary logic expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badBinaryLogic() throws Exception {
        assert badTestMethod("3||;");
        assert badTestMethod("3|| 4 &&;");
        assert badTestMethod("3&&if;");
    }

    /**
     * Tests some incorrect unary minus expressions
     * @throws Exception if the test fails
     */
    @Test
    public void unaryMinus() throws Exception {
        assert badTestMethod("x-;");
        assert badTestMethod("-class;");
    }

    /**
     * Tests some incorrect not expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badNot() throws Exception {
        assert badTestMethod("x!;");
        assert badTestMethod("!if;");
    }

    /**
     * Tests some incorrect Incr and Decr expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badIncrDecr() throws Exception {
        assert badTestMethod("int x = --;");
        assert badTestMethod("--;");
        assert badTestMethod("y----------------------x;");
        assert badTestMethod("int x = ++;");
        assert badTestMethod("++;");
        assert badTestMethod("y++++++++++++++++++++++x;");
    }

    /**
     * Tests some incorrect VarExpr expressions
     * @throws Exception if the test fails
     */
    @Test
    public void badVarExpr() throws Exception {
        assert badTestMethod("a.b.c.d.e.g;");
        assert badTestMethod("x[] = 3;");
        assert badTestMethod("[3];");
        assert badTestMethod("[3]x;");
        assert badTestMethod("3.3;");
        assert badTestMethod("x[3;");
    }






}