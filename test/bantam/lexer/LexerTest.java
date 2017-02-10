package bantam.lexer;

import com.sun.javafx.fxml.expression.Expression;
import java_cup.runtime.Symbol;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/*
 * File: LexerTest.java
 * Author: djskrien
 * Date: 1/8/17
 */
public class LexerTest
{
    @BeforeClass
    public static void begin() {
        System.out.println("begin");
    }

    public void checkToken(String str, String id) throws Exception {
        Lexer lexer = new Lexer(new StringReader(str));
        Symbol token = lexer.next_token();
        String s = ((Token)token.value).getName();
        assertEquals(id,s);
    }

    @Test
    public void whiteSpaceToken() throws Exception {
        checkToken("    \t\n\f\t", "EOF");
    }

    @Test
    public void commentToken() throws Exception {
        checkToken("/*class*/   // */ if // new \n class", "CLASS");
    }

    @Test
    public void classToken() throws Exception {
        checkToken(" class ", "CLASS");
    }

    @Test
    public void extendsToken() throws Exception {
        checkToken(" extends ", "EXTENDS");
    }

    @Test
    public void basicStringToken() throws Exception {
        checkToken(" \"hi\" ", "STRING_CONST");
    }

    @Test
    public void stringToken() throws Exception {
        checkToken(" \" hi 890#$^&*^$  \\n  \\t \\f \\\\  \\\" \" ", "STRING_CONST");
    }

    @Test
    public void EOFToken() throws Exception {
        Lexer lexer = new Lexer(new StringReader(""));
        Symbol token = lexer.next_token();
        String s = ((Token)token.value).getName();
        assertEquals("EOF",s);
    }
}