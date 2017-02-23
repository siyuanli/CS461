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

    /** tests the case of one item in Class */
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

    @Test
    public void returnStatement() throws Exception{
        String program = "class Main{int method () { return; return 7; }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(2, stmtList.getSize());
        assert stmtList.get(0) instanceof ReturnStmt;
        ReturnStmt stmt2 = (ReturnStmt) stmtList.get(1);
        assertEquals("7", ((ConstIntExpr) stmt2.getExpr()).getConstant());
    }

    @Test
    public void emptyBlockStatment() throws Exception{
        String program = "class Main{int method () { {} }}";
        StmtList stmtList = this.getMethodBody(0, 0, program);
        assertEquals(1, stmtList.getSize());
        BlockStmt stmt = (BlockStmt) stmtList.get(0);
        assertEquals(0,stmt.getStmtList().getSize());
    }

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

    @Test
    public void instanceOfArray() throws Exception{
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

    @Test
    public void castArray() throws Exception{
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

    @Test
    public void dataTypes() throws Exception{
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

    private Expr getExpr(StmtList stmtList, int index) {
        return ((ExprStmt)stmtList.get(index)).getExpr();
    }

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

    public void varExprTest(String name, Boolean hasReference, VarExpr varExpr){
        assertEquals(name, varExpr.getName());
        if (hasReference){
            assertNotNull(varExpr.getRef());
        }
        else{
            assertNull(varExpr.getRef());
        }
    }

    public void arrayExprTest(String name, Boolean hasReference, int index, ArrayExpr arrayExpr){
        assertEquals(name, arrayExpr.getName());
        if (hasReference){
            assertNotNull(arrayExpr.getRef());
        }
        else{
            assertNull(arrayExpr.getRef());
        }
        assertEquals(index, ((ConstIntExpr)arrayExpr.getIndex()).getIntConstant());
    }


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

    @Test
    public void emptyFile() throws Exception {
        Parser parser = new Parser(new Lexer(new StringReader("")));
        thrown.expect(RuntimeException.class);
        parser.parse();
    }

}