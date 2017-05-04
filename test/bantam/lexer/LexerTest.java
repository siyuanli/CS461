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

    /**
     * Checks if white space matches a token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void whiteSpaceToken() throws Exception {
        checkToken("    \t\n\f\t\r", "EOF");
    }

    /**
     * Checks if comments match a token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void commentToken() throws Exception {
        checkToken("/*class \n while*/   // */ if // new \n if", "IF");
        checkToken("/* *** class text ****/ if", "IF");
    }


    /**
     * Checks if "class" matches the class token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void classToken() throws Exception {
        checkToken(" class ", "CLASS");
    }

    /**
     * Checks if "extends" matches the extends token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void extendsToken() throws Exception {
        checkToken(" extends ", "EXTENDS");
    }


    /**
     * Checks if "new" matches the new token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void newToken() throws Exception {
        checkToken(" new ", "NEW");
    }

    /**
     * Checks if "instanceof" matches the instanceof token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void instanceofToken() throws Exception {
        checkToken(" instanceof ", "INSTANCEOF");
    }

    /**
     * Checks if "for" matches the for token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void forToken() throws Exception {
        checkToken(" for ", "FOR");
    }

    /**
     * Checks if "while" matches the while token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void whileToken() throws Exception {
        checkToken(" while ", "WHILE");
    }

    /**
     * Checks if "if" matches the if token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void ifToken() throws Exception {
        checkToken(" if ", "IF");
    }

    /**
     * Checks if "else" matches the else token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void elseToken() throws Exception {
        checkToken(" else ", "ELSE");
    }

    /**
     * Checks if "return" matches the return token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void returnToken() throws Exception {
        checkToken(" return ", "RETURN");
    }

    /**
     * Checks if "break" matches the break token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void breakToken() throws Exception {
        checkToken(" break ", "BREAK");
    }

    /**
     * Checks if "try" matches the try token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void tryToken() throws Exception {
        checkToken(" try ", "TRY");
    }

    /**
     * Checks if "catch" matches the catch token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void catchToken() throws Exception {
        checkToken(" catch ", "CATCH");
    }

    /**
     * Checks if "throw" matches the throw token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void throwToken() throws Exception {
        checkToken(" throw ", "THROW");
    }

    /**
     * Checks if a semicolon matches the semi token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void semiToken() throws Exception {
        checkToken(" ; ", "SEMI");
    }

    /**
     * Checks if a comma matches the comma token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void commmaToken() throws Exception {
        checkToken(" , ", "COMMA");
    }

    /**
     * Checks if a period matches the dot token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void dotToken() throws Exception {
        checkToken(" . ", "DOT");
    }

    /**
     * Checks if a left parenthesis  matches the lparen token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void lparenToken() throws Exception {
        checkToken(" ( ", "LPAREN");
    }

    /**
     * Checks if a right parenthesis matches the rparen token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void rparenToken() throws Exception {
        checkToken(" ) ", "RPAREN");
    }

    /**
     * Checks if a left square brace matches the lsqbrace token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void lsqbraceToken() throws Exception {
        checkToken(" [ ", "LSQBRACE");
    }

    /**
     * Checks if a right square brace matches the rsqbrace token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void rsqbraceToken() throws Exception {
        checkToken(" ] ", "RSQBRACE");
    }

    /**
     * Checks if a left curly brace matches the lbrace token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void lbraceToken() throws Exception {
        checkToken(" { ", "LBRACE");
    }

    /**
     * Checks if a right curly brace matches the rbrace token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void rbraceToken() throws Exception {
        checkToken(" } ", "RBRACE");
    }

    /**
     * Checks if the minus sign matches the minus token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void minusToken() throws Exception {
        checkToken(" - ", "MINUS");
    }

    /**
     * Checks if the plus sign matches the plus token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void plusToken() throws Exception {
        checkToken(" + ", "PLUS");
    }

    /**
     * Checks if the forward slash matches the divide token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void divideToken() throws Exception {
        checkToken(" / ", "DIVIDE");
    }

    /**
     * Checks if the star matches the times token
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void timesToken() throws Exception {
        checkToken(" * ", "TIMES");
    }

    /**
     * Checks to see if it catches the modulus token "%"
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void modulusToken() throws Exception {
        checkToken(" % ", "MODULUS");
    }

    /**
     * Checks to see if it catches the decrementer token "--"
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void decrToken() throws Exception {
        checkToken(" -- ", "DECR");
    }

    /**
     * Checks to see if it catches the incrementer token "++"
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void incrToken() throws Exception {
        checkToken(" ++ ", "INCR");
    }

    /**
     * Checks to see if it catches the less than token "<"
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void ltToken() throws Exception {
        checkToken(" < ", "LT");
    }

    /**
     * Checks to see if it catches the greater than token ">"
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void gtToken() throws Exception {
        checkToken(" > ", "GT");
    }

    /**
     * Checks to see if it catches the less than or equal to token "<="
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void leqToken() throws Exception {
        checkToken(" <= ", "LEQ");
    }

    /**
     * Checks to see if it catches the greater than or equals token ">="
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void geqToken() throws Exception {
        checkToken(" >= ", "GEQ");
    }

    /**
     * Checks to see if it catches the assignment token "="
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void assignToken() throws Exception {
        checkToken(" = ", "ASSIGN");
    }

    /**
     * Checks to see if it catches the "equals" token ==
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void eqToken() throws Exception {
        checkToken(" == ", "EQ");
    }

    /**
     * Checks to see if it catches the "not equals" token !=
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void neToken() throws Exception {
        checkToken(" != ", "NE");
    }

    /**
     * Checks to see if it catches the "not" token !
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void notToken() throws Exception {
        checkToken(" ! ", "NOT");
    }

    /**
     * Checks to see if it catches the "and" token &&
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void andToken() throws Exception {
        checkToken(" && ", "AND");
    }


    /**
     * Checks to see if it catches the "or" token ||
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void orToken() throws Exception {
        checkToken(" || ", "OR");
    }

    /**
     * Checks to see if it catches the boolean constants true and false
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void booleanToken() throws Exception {
        checkToken(" true ", "BOOLEAN_CONST");
        checkToken(" false ", "BOOLEAN_CONST");
    }

    /**
     * Checks to see if it catches integer constants which are too long
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void longIntToken() throws Exception {
        checkToken("99999999999999", "LEX_ERROR");
    }

    /**
     * Checks to see if it catches legal integer constants
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void legalIntToken() throws Exception {
        checkToken("012999", "INT_CONST");
    }

    /**
     * Checks to see if it catches various legal identifiers
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void legalIdentifierToken() throws Exception {
        checkToken("public", "ID");
        checkToken("int", "ID");
        checkToken("a_23", "ID");
        checkToken("A_20B", "ID");
    }

    /**
     * Checks to see if it catches illegal identifiers
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void illegalIdentifierToken() throws Exception {
        checkToken("_aaaa", "LEX_ERROR");
        checkToken("22aaaa", "LEX_ERROR");
    }

    /**
     * Checks to see if it catches basic string constants
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void basicStringToken() throws Exception {
        checkToken(" \"hi\" ", "STRING_CONST");
    }

    /**
     * Checks to see if it catches string constants with legal escape sequences
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void stringToken() throws Exception {
        checkToken(" \" hi 890#$^&*^$  \\n  \\t \\f \\r \\\\  \\\" \" ",
                "STRING_CONST");
    }


    /**
     * Checks to see if it catches string constants which are too long
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void longStringToken() throws Exception {
        String str = "\"A";
        for (int i = 0; i < 5010; i++){
            str += "A";
        }
        checkToken(str + "H!!!!\"", "LEX_ERROR");
    }

    /**
     * Checks to see if it catches unclosed string constants
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void unClosedStringToken() throws Exception {
        checkToken(" \"skdjfs\t\f  ", "LEX_ERROR");
    }

    /**
     * Checks to see if it catches multi-line quotes
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void multilineStringToken() throws Exception {
        checkToken(" \"skdjfs\t\f\n sdfkjs sldksf \\\" \"  ", "LEX_ERROR");
    }

    /**
     * Checks to see if it catches illegal escape sequences
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void illegalItemsInStringToken() throws Exception {
        checkToken(" \" \\0 \" ", "LEX_ERROR");
    }

    /**
     * Checks to see if it catches unterminated comments
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void unterminatedCommentToken() throws Exception {
        checkToken("/* sdjkwelk/////  *****\nsdllkjsdf  ** ///\\//","LEX_ERROR");
        checkToken("/* *** if **** ", "LEX_ERROR");
    }

    /**
     * Checks to see if it catches illegal characters
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void illegalSymbolToken() throws Exception {
        checkToken("??@#$^&$#", "LEX_ERROR");
        //a bell
        checkToken(Character.toString( (char) 7), "LEX_ERROR");
    }

    /**
     * Checks if it handles multiple tokens (in this case scientific notation) correctly
     * It leaves it up to the parser to check to see if the user has incorrectly used
     * decimals
     * @throws Exception throws an exception if the two do not match
     */
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

    /**
     * Checks if it handles the end of the file correctly
     * @throws Exception throws an exception if the two do not match
     */
    @Test
    public void EOFToken() throws Exception {
        checkToken("","EOF");
    }

}