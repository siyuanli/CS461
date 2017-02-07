/* Bantam Java Compiler and Language Toolset.
   Copyright (C) 2009 by Marc Corliss (corliss@hws.edu) and 
                         David Furcy (furcyd@uwosh.edu) and
                         E Christopher Lewis (lewis@vmware.com).
   ALL RIGHTS RESERVED.
   The Bantam Java toolset is distributed under the following 
   conditions:
     You may make copies of the toolset for your own use and 
     modify those copies.
     All copies of the toolset must retain the author names and 
     copyright notice.
     You may not sell the toolset or distribute it in 
     conjunction with a commerical product or service without 
     the expressed written consent of the authors.
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS 
   OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
   PARTICULAR PURPOSE. 
*/
/* code below is copied to the file containing the bantam.lexer */
package bantam.lexer;

import bantam.parser.TokenIds;
import java_cup.runtime.Symbol;

/* import Symbol class, which represents the symbols that are passed
   from the bantam.lexer to the bantam.parser.  Each symbol consists of an ID
   and a token value, which is defined in Token.java */


public class Lexer implements java_cup.runtime.Scanner
{
    private final int YY_BUFFER_SIZE = 512;
    private final int YY_F = -1;
    private final int YY_NO_STATE = -1;
    private final int YY_NOT_ACCEPT = 0;
    private final int YY_START = 1;
    private final int YY_END = 2;
    private final int YY_NO_ANCHOR = 4;
    private final int YY_BOL = 128;
    private final int YY_EOF = 129;

    /* code below is copied to the class containing the bantam.lexer */
    /**
     * maximum string size allowed
     */
    private final int MAX_STRING_SIZE = 5000;
    /**
     * boolean indicating whether debugging is enabled
     */
    private boolean debug = false;
    /**
     * boolean indicating whether we're lexing multiple files or a single file
     */
    private boolean multipleFiles = false;
    /**
     * array that holds the names of each file we're lexing
     * (used only when multipleFiles is true)
     */
    private String[] filenames;
    /**
     * array that holds the reader for each file we're lexing
     * (used only when multipleFiles is true)
     */
    private java.io.BufferedReader[] fileReaders;
    /**
     * current file number used to index filenames and fileReaders
     * (used only when multipleFiles is true)
     */
    private int fileCnt = 0;

    /**
     * Lexer constructor - defined in JLex specification file
     * Needed to handle lexing multiple files
     *
     * @param filenames list of filename strings
     * @param debug     boolean indicating whether debugging is enabled
     */
    public Lexer(String[] filenames, boolean debug) {
        // call private constructor, which does some initialization
        this();
        this.debug = debug;
        // set the multipleFiles flag to true (provides compatibility
        // with the single file constructors)
        multipleFiles = true;
        // initialize filenames field to parameter filenames
        // used later for finding the name of the current file
        this.filenames = filenames;
        // check that there is at least one specified filename
        if (filenames.length == 0) {
            throw new RuntimeException("Must specify at least one filename to scan");
        }
        // must initialize readers for each file (BufferedReader)
        fileReaders = new java.io.BufferedReader[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            // try...catch checks if file is found
            try {
                // create the ith file reader
                fileReaders[i] = new java.io.BufferedReader(new java.io.FileReader
                        (filenames[i]));
            } catch (java.io.FileNotFoundException e) {
                // if file not found then report an error and exit
                System.err.println("Error: file '" + filenames[i] + "' not found");
                System.exit(1);
            }
        }
        // set yy_reader (a JLex variable) to the first file reader
        yy_reader = fileReaders[0];
        // set yyline to 1 (as opposed to 0)
        yyline = 1;
    }

    /**
     * holds the current string constant
     * note: we use StringBuffer so that appending does not require constructing a new
     * object
     */
    private StringBuffer currStringConst;

    /**
     * getter method for accessing the current line number
     *
     * @return current line number
     */
    public int getCurrLineNum() {
        return yyline;
    }

    /**
     * getter method for accessing the current file name
     *
     * @return current filename string
     */
    public String getCurrFilename() {
        return filenames[fileCnt];
    }

    /**
     * print tokens - used primarily for debugging the bantam.lexer
     */
    public void printTokens() throws java.io.IOException {
        // prevFileCnt is used to determine when the filename has changed
        // every time an EOF is encountered fileCnt is incremented
        // by testing fileCnt with prevFileCnt, we can determine when the
        // filename has changed and print the filename along with the tokens
        int prevFileCnt = -1;
        // try...catch needed since next_token() can throw an IOException
        try {
            // iterate through all tokens
            while (true) {
                // get the next token
                Symbol symbol = next_token();
                // check if file has changed
                if (prevFileCnt != fileCnt) {
                    // if it has then print out the new filename
                    System.out.println("# " + filenames[fileCnt]);
                    // update prevFileCnt
                    prevFileCnt = fileCnt;
                }
                // print out the token
                System.out.println((Token) symbol.value);
                // if we've reached the EOF (EOF only returned for the last
                // file) then we break out of loop
                if (symbol.sym == TokenIds.EOF) {
                    break;
                }
            }
        } catch (java.io.IOException e) {
            // if an IOException occurs then print error and exit
            System.err.println("Unexpected IO exception while scanning.");
            throw e;
        }
    }

    private java.io.BufferedReader yy_reader;
    private int yy_buffer_index;
    private int yy_buffer_read;
    private int yy_buffer_start;
    private int yy_buffer_end;
    private char yy_buffer[];
    private int yychar;
    private int yyline;
    private boolean yy_at_bol;
    private int yy_lexical_state;

    public Lexer(java.io.Reader reader) {
        this();
        if (null == reader) {
            throw (new Error("Error: Bad input stream initializer."));
        }
        yy_reader = new java.io.BufferedReader(reader);
    }

    public Lexer(java.io.InputStream instream) {
        this();
        if (null == instream) {
            throw (new Error("Error: Bad input stream initializer."));
        }
        yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
    }

    private Lexer() {
        yy_buffer = new char[YY_BUFFER_SIZE];
        yy_buffer_read = 0;
        yy_buffer_index = 0;
        yy_buffer_start = 0;
        yy_buffer_end = 0;
        yychar = 0;
        yyline = 0;
        yy_at_bol = true;
        yy_lexical_state = YYINITIAL;

        // set yyline to 1 (as opposed to 0)
        yyline = 1;
    }

    private boolean yy_eof_done = false;
    private final int YYINITIAL = 0;
    private final int yy_state_dtrans[] = {0};

    private void yybegin(int state) {
        yy_lexical_state = state;
    }

    private int yy_advance() throws java.io.IOException {
        int next_read;
        int i;
        int j;

        if (yy_buffer_index < yy_buffer_read) {
            return yy_buffer[yy_buffer_index++];
        }

        if (0 != yy_buffer_start) {
            i = yy_buffer_start;
            j = 0;
            while (i < yy_buffer_read) {
                yy_buffer[j] = yy_buffer[i];
                ++i;
                ++j;
            }
            yy_buffer_end = yy_buffer_end - yy_buffer_start;
            yy_buffer_start = 0;
            yy_buffer_read = j;
            yy_buffer_index = j;
            next_read = yy_reader.read(yy_buffer, yy_buffer_read, yy_buffer.length -
                    yy_buffer_read);
            if (-1 == next_read) {
                return YY_EOF;
            }
            yy_buffer_read = yy_buffer_read + next_read;
        }

        while (yy_buffer_index >= yy_buffer_read) {
            if (yy_buffer_index >= yy_buffer.length) {
                yy_buffer = yy_double(yy_buffer);
            }
            next_read = yy_reader.read(yy_buffer, yy_buffer_read, yy_buffer.length -
                    yy_buffer_read);
            if (-1 == next_read) {
                return YY_EOF;
            }
            yy_buffer_read = yy_buffer_read + next_read;
        }
        return yy_buffer[yy_buffer_index++];
    }

    private void yy_move_end() {
        if (yy_buffer_end > yy_buffer_start && '\n' == yy_buffer[yy_buffer_end - 1]) {
            yy_buffer_end--;
        }
        if (yy_buffer_end > yy_buffer_start && '\r' == yy_buffer[yy_buffer_end - 1]) {
            yy_buffer_end--;
        }
    }

    private boolean yy_last_was_cr = false;

    private void yy_mark_start() {
        int i;
        for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
            if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
                ++yyline;
            }
            if ('\r' == yy_buffer[i]) {
                ++yyline;
                yy_last_was_cr = true;
            }
            else {
                yy_last_was_cr = false;
            }
        }
        yychar = yychar + yy_buffer_index - yy_buffer_start;
        yy_buffer_start = yy_buffer_index;
    }

    private void yy_mark_end() {
        yy_buffer_end = yy_buffer_index;
    }

    private void yy_to_mark() {
        yy_buffer_index = yy_buffer_end;
        yy_at_bol = (yy_buffer_end > yy_buffer_start) && ('\r' ==
                yy_buffer[yy_buffer_end - 1] || '\n' == yy_buffer[yy_buffer_end - 1] ||
                2028/*LS*/ == yy_buffer[yy_buffer_end - 1] || 2029/*PS*/ ==
                yy_buffer[yy_buffer_end - 1]);
    }

    private java.lang.String yytext() {
        return (new java.lang.String(yy_buffer, yy_buffer_start, yy_buffer_end -
                yy_buffer_start));
    }

    private int yylength() {
        return yy_buffer_end - yy_buffer_start;
    }

    private char[] yy_double(char buf[]) {
        int i;
        char newbuf[];
        newbuf = new char[2 * buf.length];
        for (i = 0; i < buf.length; ++i) {
            newbuf[i] = buf[i];
        }
        return newbuf;
    }

    private final int YY_E_INTERNAL = 0;
    private final int YY_E_MATCH = 1;
    private java.lang.String yy_error_string[] = {"Error: Internal error.\n", "Error: " +
            "Unmatched input.\n"};

    private void yy_error(int code, boolean fatal) {
        java.lang.System.out.print(yy_error_string[code]);
        java.lang.System.out.flush();
        if (fatal) {
            throw new Error("Fatal Error.\n");
        }
    }

    private int[][] unpackFromString(int size1, int size2, String st) {
        int colonIndex = -1;
        String lengthString;
        int sequenceLength = 0;
        int sequenceInteger = 0;

        int commaIndex;
        String workString;

        int res[][] = new int[size1][size2];
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                if (sequenceLength != 0) {
                    res[i][j] = sequenceInteger;
                    sequenceLength--;
                    continue;
                }
                commaIndex = st.indexOf(',');
                workString = (commaIndex == -1) ? st : st.substring(0, commaIndex);
                st = st.substring(commaIndex + 1);
                colonIndex = workString.indexOf(':');
                if (colonIndex == -1) {
                    res[i][j] = Integer.parseInt(workString);
                    continue;
                }
                lengthString = workString.substring(colonIndex + 1);
                sequenceLength = Integer.parseInt(lengthString);
                workString = workString.substring(0, colonIndex);
                sequenceInteger = Integer.parseInt(workString);
                res[i][j] = sequenceInteger;
                sequenceLength--;
            }
        }
        return res;
    }

    private int yy_acpt[] = {
        /* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NOT_ACCEPT,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NOT_ACCEPT,
		/* 7 */ YY_NOT_ACCEPT};
    private int yy_cmap[] = unpackFromString(1, 130, "5:10,0,5:2,0,5:83,3,5,1,5:8,2," +
            "5:6,4,5:12,6:2")[0];

    private int yy_rmap[] = unpackFromString(1, 8, "0,1,2:2,3,2,4,5")[0];

    private int yy_nxt[][] = unpackFromString(6, 7, "-1,1,5:4,2,-1:2,4,-1:14,6,-1:7,7," +
            "-1:6,3,-1:2");

    public java_cup.runtime.Symbol next_token() throws java.io.IOException {
        int yy_lookahead;
        int yy_anchor = YY_NO_ANCHOR;
        int yy_state = yy_state_dtrans[yy_lexical_state];
        int yy_next_state = YY_NO_STATE;
        int yy_last_accept_state = YY_NO_STATE;
        boolean yy_initial = true;
        int yy_this_accept;

        yy_mark_start();
        yy_this_accept = yy_acpt[yy_state];
        if (YY_NOT_ACCEPT != yy_this_accept) {
            yy_last_accept_state = yy_state;
            yy_mark_end();
        }
        while (true) {
            if (yy_initial && yy_at_bol) {
                yy_lookahead = YY_BOL;
            }
            else {
                yy_lookahead = yy_advance();
            }
            yy_next_state = YY_F;
            yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
            if (YY_EOF == yy_lookahead && true == yy_initial) {

    /* code below is executed when the end-of-file is reached */
                switch (yy_lexical_state) {
                    case YYINITIAL:
                        // if in YYINITIAL when EOF occurs then no error
                        break;
                    // if defining other states then might want to add other cases here...
                }
                // if we reach here then we should either start lexing the next
                // file (if there are more files to lex) or return EOF (if we're
                // at the file)
                if (multipleFiles && fileCnt < fileReaders.length - 1) {
                    // more files to lex so update yy_reader and yyline and then continue
                    yy_reader = fileReaders[++fileCnt];
                    yyline = 1;
                    continue;
                }
                // if we reach here, then we're at the last file so we return EOF
                // to bantam.parser
                return new Symbol(TokenIds.EOF, new Token("EOF", yyline));
            }
            if (YY_F != yy_next_state) {
                yy_state = yy_next_state;
                yy_initial = false;
                yy_this_accept = yy_acpt[yy_state];
                if (YY_NOT_ACCEPT != yy_this_accept) {
                    yy_last_accept_state = yy_state;
                    yy_mark_end();
                }
            }
            else {
                if (YY_NO_STATE == yy_last_accept_state) {
                    throw (new Error("Lexical Error: Unmatched Input."));
                }
                else {
                    yy_anchor = yy_acpt[yy_last_accept_state];
                    if (0 != (YY_END & yy_anchor)) {
                        yy_move_end();
                    }
                    yy_to_mark();
                    switch (yy_last_accept_state) {
                        case 1: {
                            throw new RuntimeException("Unmatched lexeme " + yytext() + " at line " + yyline);
                        }
                        case -2:
                            break;
                        case 2:

                        case -3:
                            break;
                        case 3: {
                            return new Symbol(TokenIds.CLASS, new Token("CLASS", yyline));
                        }
                        case -4:
                            break;
                        case 5: {
                            throw new RuntimeException("Unmatched lexeme " + yytext() + " at line " + yyline);
                        }
                        case -5:
                            break;
                        default:
                            yy_error(YY_E_INTERNAL, false);
                        case -1:
                    }
                    yy_initial = true;
                    yy_state = yy_state_dtrans[yy_lexical_state];
                    yy_next_state = YY_NO_STATE;
                    yy_last_accept_state = YY_NO_STATE;
                    yy_mark_start();
                    yy_this_accept = yy_acpt[yy_state];
                    if (YY_NOT_ACCEPT != yy_this_accept) {
                        yy_last_accept_state = yy_state;
                        yy_mark_end();
                    }
                }
            }
        }
    }
}
