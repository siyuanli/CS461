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
    public void newToken() throws Exception {
        checkToken(" new ", "NEW");
    }

    @Test
    public void instanceofToken() throws Exception {
        checkToken(" instanceof ", "INSTANCEOF");
    }

    @Test
    public void forToken() throws Exception {
        checkToken(" for ", "FOR");
    }

    @Test
    public void whileToken() throws Exception {
        checkToken(" while ", "WHILE");
    }

    @Test
    public void ifToken() throws Exception {
        checkToken(" if ", "IF");
    }

    @Test
    public void elseToken() throws Exception {
        checkToken(" else ", "ELSE");
    }

    @Test
    public void returnToken() throws Exception {
        checkToken(" return ", "RETURN");
    }

    @Test
    public void breakToken() throws Exception {
        checkToken(" break ", "BREAK");
    }

    @Test
    public void semiToken() throws Exception {
        checkToken(" ; ", "SEMI");
    }

    @Test
    public void commmaToken() throws Exception {
        checkToken(" , ", "COMMA");
    }

    @Test
    public void lparenToken() throws Exception {
        checkToken(" ( ", "LPAREN");
    }

    @Test
    public void rparenToken() throws Exception {
        checkToken(" ) ", "RPAREN");
    }

    @Test
    public void lsqbraceToken() throws Exception {
        checkToken(" [ ", "LSQBRACE");
    }

    @Test
    public void rsqbraceToken() throws Exception {
        checkToken(" ] ", "RSQBRACE");
    }

    @Test
    public void lbraceToken() throws Exception {
        checkToken(" { ", "LBRACE");
    }

    @Test
    public void rbraceToken() throws Exception {
        checkToken(" } ", "RBRACE");
    }

    @Test
    public void minusToken() throws Exception {
        checkToken(" - ", "MINUS");
    }

    @Test
    public void plusToken() throws Exception {
        checkToken(" + ", "PLUS");
    }

    @Test
    public void divideToken() throws Exception {
        checkToken(" / ", "DIVIDE");
    }

    @Test
    public void timesToken() throws Exception {
        checkToken(" * ", "TIMES");
    }

    @Test
    public void modulusToken() throws Exception {
        checkToken(" % ", "MODULUS");
    }

    @Test
    public void decrToken() throws Exception {
        checkToken(" -- ", "DECR");
    }

    @Test
    public void incrToken() throws Exception {
        checkToken(" ++ ", "INCR");
    }

    @Test
    public void ltToken() throws Exception {
        checkToken(" < ", "LT");
    }

    @Test
    public void gtToken() throws Exception {
        checkToken(" > ", "GT");
    }

    @Test
    public void leqToken() throws Exception {
        checkToken(" <= ", "LEG");
    }

    @Test
    public void geqToken() throws Exception {
        checkToken(" >= ", "GEG");
    }

    @Test
    public void assignToken() throws Exception {
        checkToken(" = ", "ASSIGN");
    }

    @Test
    public void eqToken() throws Exception {
        checkToken(" == ", "EG");
    }

    @Test
    public void neToken() throws Exception {
        checkToken(" != ", "NE");
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