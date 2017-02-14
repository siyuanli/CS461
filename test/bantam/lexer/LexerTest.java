/*
 * Project 1: Build Lexer
 * File: LexerTest.java
 * Author: djskrien, Phoebe Hughes, Joseph Malionek, Siyuan Li
 * Date: Feb 14, 2017
 */

package bantam.lexer;

import java_cup.runtime.Symbol;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.StringReader;
import static org.junit.Assert.assertEquals;

public class LexerTest
{
    @BeforeClass
    public static void begin() {
        System.out.println("begin");
    }

    /**
     * Match the first token in the string with the given id
     * @param str the string to be tokenized
     * @param id the token id to be matched
     * @throws Exception throws an exception if the two do not match
     */

    public void checkToken(String str, String id) throws Exception {
        Lexer lexer = new Lexer(new StringReader(str));
        Symbol token = lexer.next_token();
        String s = ((Token)token.value).getName();
        assertEquals(id,s);
    }

    @Test
    public void whiteSpaceToken() throws Exception {
        checkToken("    \t\n\f\t\r", "EOF");
    }

    @Test
    public void commentToken() throws Exception {
        checkToken("/*class \n while*/   // */ if // new \n if", "IF");
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
    public void dotToken() throws Exception {
        checkToken(" . ", "DOT");
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
        checkToken(" <= ", "LEQ");
    }

    @Test
    public void geqToken() throws Exception {
        checkToken(" >= ", "GEQ");
    }

    @Test
    public void assignToken() throws Exception {
        checkToken(" = ", "ASSIGN");
    }

    @Test
    public void eqToken() throws Exception {
        checkToken(" == ", "EQ");
    }

    @Test
    public void neToken() throws Exception {
        checkToken(" != ", "NE");
    }


    @Test
    public void andToken() throws Exception {
        checkToken(" && ", "AND");
    }


    @Test
    public void orToken() throws Exception {
        checkToken(" || ", "OR");
    }


    @Test
    public void notToken() throws Exception {
        checkToken(" ! ", "NOT");
    }


    @Test
    public void basicStringToken() throws Exception {
        checkToken(" \"hi\" ", "STRING_CONST");
    }

    @Test
    public void stringToken() throws Exception {
        checkToken(" \" hi 890#$^&*^$  \\n  \\t \\f \\r \\\\  \\\" \" ",
                "STRING_CONST");
    }

    @Test
    public void unterminatedCommentToken() throws Exception {
        checkToken("/* sdjkwelk/////  *****\nsdllkjsdf  ** ///\\//","LEX_ERROR");
    }

    @Test
    public void unClosedStringToken() throws Exception {
        checkToken(" \"skdjfs\t\f  ", "LEX_ERROR");
    }

    @Test
    public void multilineStringToken() throws Exception {
        checkToken(" \"skdjfs\t\f\n sdfkjs sldksf \\\" \"  ", "LEX_ERROR");
    }

    @Test
    public void illegalItemsInStringToken() throws Exception {
        checkToken(" \" \\0 \" ", "LEX_ERROR");
    }


    @Test
    public void illegalSymbolToken() throws Exception {
        checkToken("??@#$^&$#", "LEX_ERROR");
        //a bell
        checkToken(Character.toString( (char) 7), "LEX_ERROR");
    }

    @Test
    public void illegalIdentifierToken() throws Exception {
        checkToken("_aaaa", "LEX_ERROR");
        checkToken("22aaaa", "LEX_ERROR");
    }

    @Test
    public void legalIdentifierToken() throws Exception {
        checkToken("public", "ID");
        checkToken("int", "ID");
        checkToken("a_23", "ID");
        checkToken("A_20B", "ID");
    }

    @Test
    public void longStringToken() throws Exception {
        String str = "\"A";
        for (int i = 0; i < 5010; i++){
            str += "A";
        }
        checkToken(str + "H!!!!\"", "LEX_ERROR");
    }

    @Test
    public void longIntToken() throws Exception {
        checkToken("99999999999999", "LEX_ERROR");
    }

    @Test
    public void legalIntToken() throws Exception {
        checkToken("012999", "INT_CONST");
    }

    @Test
    public void booleanToken() throws Exception {
        checkToken(" true ", "BOOLEAN_CONST");
        checkToken(" false ", "BOOLEAN_CONST");
    }

    @Test
    public void testTokenSequences() throws Exception{
        Lexer lexer = new Lexer(new StringReader(".5E-3"));
        String[] ids = {"DOT","LEX_ERROR","MINUS","INT_CONST"};
        for(int i = 0;i<4;i++){
            Symbol token = lexer.next_token();
            String s = ((Token)token.value).getName();
            assertEquals(ids[i],s);
        }
    }

    @Test
    public void EOFToken() throws Exception {
        checkToken("","EOF");
    }


}