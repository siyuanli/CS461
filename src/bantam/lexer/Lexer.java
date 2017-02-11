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
/* import Symbol class, which represents the symbols that are passed
   from the bantam.lexer to the bantam.parser.  Each symbol consists of an ID
   and a token value, which is defined in Token.java */
import java_cup.runtime.Symbol;


public class Lexer implements java_cup.runtime.Scanner {
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
    /** maximum string size allowed */
    private final int MAX_STRING_SIZE = 5000;
    /** boolean indicating whether debugging is enabled */
    private boolean debug = false;
    /** boolean indicating whether we're lexing multiple files or a single file */
    private boolean multipleFiles = false;
    /** array that holds the names of each file we're lexing 
      * (used only when multipleFiles is true)
      * */
    private String[] filenames;
    /** array that holds the reader for each file we're lexing 
      * (used only when multipleFiles is true)
      * */
    private java.io.BufferedReader[] fileReaders;
    /** current file number used to index filenames and fileReaders
      * (used only when multipleFiles is true)
      * */
    private int fileCnt = 0;
    /** Lexer constructor - defined in JLex specification file
      * Needed to handle lexing multiple files
      * @param filenames list of filename strings
      * @param debug boolean indicating whether debugging is enabled
      * */
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
	if (filenames.length == 0)
	    throw new RuntimeException("Must specify at least one filename to scan");
	// must initialize readers for each file (BufferedReader)
	fileReaders = new java.io.BufferedReader[filenames.length];
	for (int i = 0; i < filenames.length; i++) {
	    // try...catch checks if file is found
	    try {
		// create the ith file reader
		fileReaders[i] = new java.io.BufferedReader(new java.io.FileReader(filenames[i]));
	    }
	    catch(java.io.FileNotFoundException e) {
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
    /** holds the current string constant
      * note: we use StringBuffer so that appending does not require constructing a new object 
      * */
    private StringBuffer currStringConst;
    /** getter method for accessing the current line number
      * @return current line number
      * */
    public int getCurrLineNum() {
	return yyline;
    }
    /** getter method for accessing the current file name
      * @return current filename string
      * */
    public String getCurrFilename() {
	return filenames[fileCnt];
    }
    /** print tokens - used primarily for debugging the bantam.lexer
      * */
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
		System.out.println((Token)symbol.value);
		// if we've reached the EOF (EOF only returned for the last
		// file) then we break out of loop
		if (symbol.sym == TokenIds.EOF)
		    break;
	    }
	}
	catch (java.io.IOException e) {
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

	public Lexer (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	public Lexer (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Lexer () {
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
	private final int yy_state_dtrans[] = {
		0
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
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
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
				++yyline;
			}
			if ('\r' == yy_buffer[i]) {
				++yyline;
				yy_last_was_cr=true;
			} else yy_last_was_cr=false;
		}
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
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
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NO_ANCHOR,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_END,
		/* 36 */ YY_NOT_ACCEPT,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NO_ANCHOR,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NO_ANCHOR,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NO_ANCHOR,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_END,
		/* 54 */ YY_NOT_ACCEPT,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NOT_ACCEPT,
		/* 58 */ YY_NO_ANCHOR,
		/* 59 */ YY_NOT_ACCEPT,
		/* 60 */ YY_NO_ANCHOR,
		/* 61 */ YY_NOT_ACCEPT,
		/* 62 */ YY_NO_ANCHOR,
		/* 63 */ YY_NO_ANCHOR,
		/* 64 */ YY_NO_ANCHOR,
		/* 65 */ YY_NO_ANCHOR,
		/* 66 */ YY_NO_ANCHOR,
		/* 67 */ YY_NO_ANCHOR,
		/* 68 */ YY_NO_ANCHOR,
		/* 69 */ YY_NO_ANCHOR,
		/* 70 */ YY_NO_ANCHOR,
		/* 71 */ YY_NO_ANCHOR,
		/* 72 */ YY_NO_ANCHOR,
		/* 73 */ YY_NO_ANCHOR,
		/* 74 */ YY_NO_ANCHOR,
		/* 75 */ YY_NO_ANCHOR,
		/* 76 */ YY_NO_ANCHOR,
		/* 77 */ YY_NO_ANCHOR,
		/* 78 */ YY_NO_ANCHOR,
		/* 79 */ YY_NO_ANCHOR,
		/* 80 */ YY_NO_ANCHOR,
		/* 81 */ YY_NO_ANCHOR,
		/* 82 */ YY_NO_ANCHOR,
		/* 83 */ YY_NO_ANCHOR,
		/* 84 */ YY_NO_ANCHOR,
		/* 85 */ YY_NO_ANCHOR,
		/* 86 */ YY_NO_ANCHOR,
		/* 87 */ YY_NO_ANCHOR,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NO_ANCHOR,
		/* 91 */ YY_NO_ANCHOR,
		/* 92 */ YY_NO_ANCHOR,
		/* 93 */ YY_NO_ANCHOR,
		/* 94 */ YY_NO_ANCHOR,
		/* 95 */ YY_NO_ANCHOR,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NO_ANCHOR,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NO_ANCHOR,
		/* 100 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,130,
"47:9,6,1,47,6,4,47:18,6,40,46,47:2,36,41,47,28,29,3,35,26,34,27,2,43:10,47," +
"25,37,39,38,47:2,44:26,30,5,31,47,45,47,9,23,7,15,11,19,44,21,17,44,24,8,44" +
",14,18,44:2,20,10,13,22,44,16,12,44:2,32,42,33,47:2,0,48")[0];

	private int yy_rmap[] = unpackFromString(1,101,
"0,1,2,1:2,3,1:9,4,5,1,6,7,8,9,10,11,12,13,14,1:9,11,1,14:2,1,14:2,1,14:6,15" +
",16,17,11,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39" +
",40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,14,60,61,62,63")[0];

	private int yy_nxt[][] = unpackFromString(64,49,
"1,50,2,3,4:2,50,5,96:3,80,96,81,69,96,98,52,96,70,99,96:2,100,96,6,7,8,9,10" +
",11,12,13,14,15,16,17,18,19,20,21,51,55,22,96,4,23,4,1,-1:51,24,25,-1:52,96" +
",82,96:16,-1:18,96:3,-1:37,27,-1:49,28,-1:52,29,-1:48,30,-1:48,31,-1:48,32," +
"-1:52,22,-1:6,35,36:2,53,54,36:40,37,36,35,-1:2,24:2,-1,24:43,-1:2,25:2,57," +
"25:44,-1:8,96:18,-1:18,96:3,-1:4,50,-1:4,50,-1:83,33,-1:14,96:7,85,96:4,26," +
"96:5,-1:18,96:3,-1:5,59:2,-1,36,59:7,36:2,59:4,36,59:26,36,59,-1:43,34,-1:1" +
"3,96:9,38,96:8,-1:18,96:3,-1:4,25,40,25:45,-1:8,96:13,39,96:4,-1:18,96:3,-1" +
":5,59:3,61,59:40,43,59,-1:8,96:4,41,96:13,-1:18,96:3,-1:5,59:2,-1,59:43,-1:" +
"8,96:4,42,96:13,-1:18,96:3,-1:10,96:3,44,96:14,-1:18,96:3,-1:10,96:4,45,96:" +
"13,-1:18,96:3,-1:10,96:17,46,-1:18,96:3,-1:10,96:7,47,96:10,-1:18,96:3,-1:1" +
"0,96:3,48,96:14,-1:18,96:3,-1:10,96:12,49,96:5,-1:18,96:3,-1:10,96:4,56,96:" +
"13,-1:18,96:3,-1:10,96:2,86,96:8,58,96:6,-1:18,96:3,-1:10,96:3,60,96:14,-1:" +
"18,96:3,-1:10,96:15,62,96:2,-1:18,96:3,-1:10,96:3,63,96:14,-1:18,96:3,-1:10" +
",96,64,96:16,-1:18,96:3,-1:10,96:3,62,96:14,-1:18,96:3,-1:10,96:2,65,96:15," +
"-1:18,96:3,-1:10,96:13,66,96:4,-1:18,96:3,-1:10,96:8,67,96:9,-1:18,96:3,-1:" +
"10,96:11,68,96:6,-1:18,96:3,-1:10,96,71,96:3,83,96:12,-1:18,96:3,-1:10,96:1" +
"3,72,96:4,-1:18,96:3,-1:10,96:2,73,96:15,-1:18,96:3,-1:10,96:6,88,96:11,-1:" +
"18,96:3,-1:10,96:10,74,96:7,-1:18,96:3,-1:10,96:3,89,96:14,-1:18,96:3,-1:10" +
",96,75,96:16,-1:18,96:3,-1:10,96:4,76,96:13,-1:18,96:3,-1:10,96:4,91,96:13," +
"-1:18,96:3,-1:10,96:6,92,96:11,-1:18,96:3,-1:10,96:15,77,96:2,-1:18,96:3,-1" +
":10,96:7,78,96:10,-1:18,96:3,-1:10,96:2,93,96:15,-1:18,96:3,-1:10,96:7,94,9" +
"6:10,-1:18,96:3,-1:10,95,96:17,-1:18,96:3,-1:10,96:4,79,96:13,-1:18,96:3,-1" +
":10,96:6,90,96:11,-1:18,96:3,-1:10,96:14,84,96:3,-1:18,96:3,-1:10,96:4,97,9" +
"6:13,-1:18,96:3,-1:10,96:13,87,96:4,-1:18,96:3,-1:3");

	public java_cup.runtime.Symbol next_token ()
		throws java.io.IOException {
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
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

    /* code below is executed when the end-of-file is reached */
    switch(yy_lexical_state) {
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
					case 0:
						{/*This is the regex for white spaces*/}
					case -2:
						break;
					case 1:
						
					case -3:
						break;
					case 2:
						{ return new Symbol(TokenIds.DIVIDE,
						    new Token("DIVIDE", yyline)); }
					case -4:
						break;
					case 3:
						{ return new Symbol(TokenIds.TIMES,
						    new Token("TIMES", yyline)); }
					case -5:
						break;
					case 4:
						{ return new Symbol(TokenIds.LEX_ERROR,
                                               new Token("LEX_ERROR", yytext(), yyline)); }
					case -6:
						break;
					case 5:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -7:
						break;
					case 6:
						{ return new Symbol(TokenIds.SEMI,
						    new Token("SEMI", yyline)); }
					case -8:
						break;
					case 7:
						{ return new Symbol(TokenIds.COMMA,
						    new Token("COMMA", yyline)); }
					case -9:
						break;
					case 8:
						{ return new Symbol(TokenIds.DOT,
						    new Token("DOT", yyline)); }
					case -10:
						break;
					case 9:
						{ return new Symbol(TokenIds.LPAREN,
						    new Token("LPAREN", yyline)); }
					case -11:
						break;
					case 10:
						{ return new Symbol(TokenIds.RPAREN,
						    new Token("RPAREN", yyline)); }
					case -12:
						break;
					case 11:
						{ return new Symbol(TokenIds.LSQBRACE,
						    new Token("LSQBRACE", yyline)); }
					case -13:
						break;
					case 12:
						{ return new Symbol(TokenIds.RSQBRACE,
						    new Token("RSQBRACE", yyline)); }
					case -14:
						break;
					case 13:
						{ return new Symbol(TokenIds.LBRACE,
						    new Token("LBRACE", yyline)); }
					case -15:
						break;
					case 14:
						{ return new Symbol(TokenIds.RBRACE,
						    new Token("RBRACE", yyline)); }
					case -16:
						break;
					case 15:
						{ return new Symbol(TokenIds.MINUS,
						    new Token("MINUS", yyline)); }
					case -17:
						break;
					case 16:
						{ return new Symbol(TokenIds.PLUS,
						    new Token("PLUS", yyline)); }
					case -18:
						break;
					case 17:
						{ return new Symbol(TokenIds.MODULUS,
						    new Token("MODULUS", yyline)); }
					case -19:
						break;
					case 18:
						{ return new Symbol(TokenIds.LT,
						    new Token("LT", yyline)); }
					case -20:
						break;
					case 19:
						{ return new Symbol(TokenIds.GT,
						    new Token("GT", yyline)); }
					case -21:
						break;
					case 20:
						{ return new Symbol(TokenIds.ASSIGN,
						    new Token("ASSIGN", yyline)); }
					case -22:
						break;
					case 21:
						{ return new Symbol(TokenIds.NOT,
						    new Token("NOT", yyline)); }
					case -23:
						break;
					case 22:
						{ if (Integer.parseInt(yytext()) <= Math.pow(2, 31)){
                                                return new Symbol(TokenIds.INT_CONST,
                                                  new Token("INT_CONST", yytext(), yyline));
                                             }
                                             else{
                                                return new Symbol(TokenIds.LEX_ERROR,
                                                  new Token("LEX_ERROR", yyline));
                                             }
                                           }
					case -24:
						break;
					case 23:
						{ throw new RuntimeException("Unmatched lexeme " +
                            yytext() + " at line " + yyline); }
					case -25:
						break;
					case 24:
						{/*Regex for inline comments*/}
					case -26:
						break;
					case 25:
						{ return new Symbol(TokenIds.LEX_ERROR,
						                       new Token("LEX_ERROR", yytext(), yyline)); }
					case -27:
						break;
					case 26:
						{ return new Symbol(TokenIds.IF,
						    new Token("IF", yyline)); }
					case -28:
						break;
					case 27:
						{ return new Symbol(TokenIds.DECR,
						    new Token("DECR", yyline)); }
					case -29:
						break;
					case 28:
						{ return new Symbol(TokenIds.INCR,
						    new Token("INCR", yyline)); }
					case -30:
						break;
					case 29:
						{ return new Symbol(TokenIds.LEQ,
						    new Token("LEQ", yyline)); }
					case -31:
						break;
					case 30:
						{ return new Symbol(TokenIds.GEQ,
						    new Token("GEQ", yyline)); }
					case -32:
						break;
					case 31:
						{ return new Symbol(TokenIds.EQ,
						    new Token("EQ", yyline)); }
					case -33:
						break;
					case 32:
						{ return new Symbol(TokenIds.NE,
						    new Token("NE", yyline)); }
					case -34:
						break;
					case 33:
						{ return new Symbol(TokenIds.AND,
						    new Token("AND", yyline)); }
					case -35:
						break;
					case 34:
						{ return new Symbol(TokenIds.OR,
						    new Token("OR", yyline)); }
					case -36:
						break;
					case 35:
						{ /*unterminated string*/
                                             return new Symbol(TokenIds.LEX_ERROR,
                                               new Token("LEX_ERROR",yytext(), yyline)); }
					case -37:
						break;
					case 37:
						{ if (yytext().length() <= 5000){
                                                return new Symbol(TokenIds.STRING_CONST,
                                                  new Token("STRING_CONST", yytext(), yyline));
                                             }
                                             else{
                                                return new Symbol(TokenIds.LEX_ERROR,
                                                  new Token("LEX_ERROR",yytext(), yyline));
                                             }
                                           }
					case -38:
						break;
					case 38:
						{ return new Symbol(TokenIds.NEW,
						    new Token("NEW", yyline)); }
					case -39:
						break;
					case 39:
						{ return new Symbol(TokenIds.FOR,
						    new Token("FOR", yyline)); }
					case -40:
						break;
					case 40:
						{/*Regex for block comments*/}
					case -41:
						break;
					case 41:
						{ return new Symbol(TokenIds.ELSE,
						    new Token("ELSE", yyline)); }
					case -42:
						break;
					case 42:
						{ return new Symbol(TokenIds.BOOLEAN_CONST,
						                       new Token("BOOLEAN_CONST", yytext(), yyline)); }
					case -43:
						break;
					case 43:
						{ /*illegal escape*/
                                             return new Symbol(TokenIds.LEX_ERROR,
                                               new Token("LEX_ERROR",yytext(), yyline)); }
					case -44:
						break;
					case 44:
						{ return new Symbol(TokenIds.CLASS,
						    new Token("CLASS", yyline)); }
					case -45:
						break;
					case 45:
						{ return new Symbol(TokenIds.WHILE,
						    new Token("WHILE", yyline)); }
					case -46:
						break;
					case 46:
						{ return new Symbol(TokenIds.BREAK,
						    new Token("BREAK", yyline)); }
					case -47:
						break;
					case 47:
						{ return new Symbol(TokenIds.RETURN,
						    new Token("RETURN", yyline)); }
					case -48:
						break;
					case 48:
						{ return new Symbol(TokenIds.EXTENDS,
						    new Token("EXTENDS", yyline)); }
					case -49:
						break;
					case 49:
						{ return new Symbol(TokenIds.INSTANCEOF,
						    new Token("INSTANCEOF", yyline)); }
					case -50:
						break;
					case 50:
						{/*This is the regex for white spaces*/}
					case -51:
						break;
					case 51:
						{ return new Symbol(TokenIds.LEX_ERROR,
                                               new Token("LEX_ERROR", yytext(), yyline)); }
					case -52:
						break;
					case 52:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -53:
						break;
					case 53:
						{ /*unterminated string*/
                                             return new Symbol(TokenIds.LEX_ERROR,
                                               new Token("LEX_ERROR",yytext(), yyline)); }
					case -54:
						break;
					case 55:
						{ return new Symbol(TokenIds.LEX_ERROR,
                                               new Token("LEX_ERROR", yytext(), yyline)); }
					case -55:
						break;
					case 56:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -56:
						break;
					case 58:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -57:
						break;
					case 60:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -58:
						break;
					case 62:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -59:
						break;
					case 63:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -60:
						break;
					case 64:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -61:
						break;
					case 65:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -62:
						break;
					case 66:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -63:
						break;
					case 67:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -64:
						break;
					case 68:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -65:
						break;
					case 69:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -66:
						break;
					case 70:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -67:
						break;
					case 71:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -68:
						break;
					case 72:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -69:
						break;
					case 73:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -70:
						break;
					case 74:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -71:
						break;
					case 75:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -72:
						break;
					case 76:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -73:
						break;
					case 77:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -74:
						break;
					case 78:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -75:
						break;
					case 79:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -76:
						break;
					case 80:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -77:
						break;
					case 81:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -78:
						break;
					case 82:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -79:
						break;
					case 83:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -80:
						break;
					case 84:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -81:
						break;
					case 85:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -82:
						break;
					case 86:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -83:
						break;
					case 87:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -84:
						break;
					case 88:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -85:
						break;
					case 89:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -86:
						break;
					case 90:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -87:
						break;
					case 91:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -88:
						break;
					case 92:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -89:
						break;
					case 93:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -90:
						break;
					case 94:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -91:
						break;
					case 95:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -92:
						break;
					case 96:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -93:
						break;
					case 97:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -94:
						break;
					case 98:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -95:
						break;
					case 99:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -96:
						break;
					case 100:
						{ return new Symbol(TokenIds.ID,
                                               new Token("ID", yytext(), yyline)); }
					case -97:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
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
