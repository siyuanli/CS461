Bantam Java Compiler Project
Extended Language Version 1.3
  Changes from 1.2:
    * Added installation package using Ant (rather than make)
    * Added JVM target for generating Java Virtual Machine Code,
      which can be converted by Jasmin into class files
    * Added an optimizer component for optimizing programs
      (the bantam.opt/ and bantam.cfg/ packages contain this code)
    * Added an interpreter component for interpreting programs
    * String.equals now takes an Object as a parameter
      (as it does in Java)
    * Added Object.toString, Object.equals, Sys.time, and 
      Sys.random built-in methods
    * Symbol table is no longer flushed after semantic analysis
      and can be used by code generator
    * Fixed other minor bugs in solution, API, and manual
  Changes from 1.1:
    * Made String.equals parameter type Object rather than String to
      conform with Java's definition of String.equals
    * Added an extended language (includes arrays, for loops, etc.)
    * Fully implemented bantam.lexer in JavaCC version of reference compiler
      (lexical errors caught in bantam.lexer rather than in bantam.parser -- thanks
       to David Furcy for identifying this and providing solution code)
    * Fixed off by one conformance testing bug in reference compiler
    * Fixed difference in language between Bantam Java and Java:
      Bantam Java now does not allow redeclaration of local variables
      with overlapping scopes
    * Fixed other minor bugs in solution, API, and manual

Contents:
  src - contains source code for the bantam compiler
        (contents shown below)
  api - contains the documentation (html) of the API
  bin - contains scripts for running the compiler and the
        documentation generator
  lib - contains library code needed by the compiler and other tools
  man - contains a man page for the bantam compiler
  tools - contains auxiliary tools for the bantam compiler
	  (unlike tarball version, this already contains auxiliary
	  tools: jlex, java cup, and javacc)
  tests - contains bantam test programs
  build.xml - an ant XML file for building the source 
              (using build.xml src/) and generating the 
	      documentation (using build.xml in bin/)

api contents:
  html/ (after building): contains html code compiled from
    javadoc, which describes the bantam compiler API
  build.xml: an ant XML file for building the API via ant

src contents: 
  src contains bantam and bantam-jj.  bantam uses JLex and 
    Java Cup while bantam-jj uses JavaCC.  You will want to
    implement the compiler from one of these directories 
    (but probably not both).

  bantam - contains the bantam compiler for the base language (uses JLex/Java Cup):
           build.xml - an ant XML for compiling the source
	   bantam.Main.java - the main program for the compiler
	   bantam.lexer - a package containing the bantam.lexer:
	           bantam.lexer.jlex - bantam.lexer specification (incomplete)
		   Token.java - token passed from bantam.lexer to bantam.parser
	           *.ref - reference class files (contain working code)
	   bantam.parser - a package containing the bantam.parser:
	            bantam.parser.cup - bantam.parser specification (incomplete)
		    TokenIds.java - generated class containing token ids
	            *.ref - reference class files (contain working code)
	   bantam.semant - a package containing the semantic analyzer
                    SemanticAnalyzer.java - semantic analyzer (incomplete)
	            *.ref - reference class files (contain working code)
	   bantam.interp - a package containing the interpreter
                    Interpreter.java - interpreter (incomplete)
	            *.ref - reference class files (contain working code)
	   bantam.opt - a package containing the optimizer
                    Optimizer.java - optimizer (incomplete)
	            *.ref - reference class files (contain working code)
           bantam.codegenjvm - a package containing the code generator
		     	 CodeGenerator.java - JVM code generator (incomplete)
			 *.ref - reference class files (contain working code)
           bantam.codegenmips - a package containing the code generator
		     	 CodeGenerator.java - MIPS code generator (incomplete)
		         MipsSupport.java - a support file that provides 
                           architectural support for the Mips ISA
			 *.ref - reference class files (contain working code)
           bantam.codegenx86 - a package containing the code generator
		     	CodeGenerator.java - Mips code generator (incomplete)
		        X86Support.java - a support file that provides 
                          architectural support for the x86 ISA
			*.ref - reference class files (contain working code)
           bantam.ast - a package containing the AST classes
                 *.java - each class in this directory represents a node in the
		   AST (illustrated in the ASTNode class)
	   bantam.cfg - a package containing the control flow graph (CFG) classes
                 (needed for optimization)
	         *.java - each class in this directory represents a node in 
                 the CFG
           bantam.util - a package containing some utilties
	          SymbolTable.java - a class representing a scoped symbol table
		    (maps Strings to Objects, which may represent type, location, etc.)
		  Location.java - a class representing a variable location
		    in memory or a register
                  ErrorHandler.java - a class for handling error reporting and exiting
		  ClassTreeNode.java - class for generating node the class hierarchy tree
           bantam.visitor - a package containing bantam.visitor classes
	             Visitor.java - abstract bantam.visitor class that other visitors extend
		     PrintVisitor.java - a subclass of Visitor that prints the AST
  bantam-jj - contains the bantam compiler for the base language (uses JavaCC):
           build.xml - an ant XML for compiling the source
	   bantam.Main.java - the main program for the compiler
	   bantam.lexer - a package containing the bantam.lexer:
	           bantam.lexer.jj - bantam.lexer specification (incomplete)
	           *.ref - reference class files (contain working code)
	   bantam.parser - a package containing the bantam.parser:
	            bantam.parser.jj - bantam.parser specification (incomplete)
	            *.ref - reference class files (contain working code)
	   bantam.semant - same as above
	   bantam.interp - same as above
           bantam.opt - same as above
           bantam.codegenjvm - same as above
           bantam.codegenmips - same as above
           bantam.codegenx86 - same as above
           bantam.ast - same as above
	   bantam.cfg - same as above
           bantam.util - same as above
           bantam.visitor - same as above

bin contents:
  bantamc - shell script for running compiler (added after source is built)
  bantamc-ref - shell script for running reference JLex/Java Cup compiler
  bantamc-jj-ref - shell script for running reference JavaCC compiler
  => src build.xml file expects shell scripts for running JLex, Java Cup, 
     and JavaCC to reside in here

lib contents:
  bantamc-ref.jar - jar file containing reference JLex/Java Cup compiler
  bantamc-jj-ref.jar - jar file containing reference JavaCC compiler
  exceptions.s - SPIM trap handler file, must use this file to run
    assembly files compiled by the bantam compiler  
  runtime.s - x86 runtime system (32-bit), must link with this file
    (e.g., with gcc) to run assembly files compiled by the bantam compiler
  TextIO.class - part of JVM runtime system (handles TextIO builtin class),
    need to include in the class path when running classes compiled by the
    bantam compiler
  Sys.class - part of JVM runtime system (handles Sys builtin class)
    need to include in the class path when running classes compiled by the
    bantam compiler

man contents:
  man1/bantamc.1 - man page for the bantam compiler

tools contents:
  (unlike tarball version, this already contains auxiliary tools: 
   jlex, java cup, and javacc)
  lib - contains the library code needed for building the reference 
	compiler and also the runtime system code for the MIPS/SPIM, 
	x86/Linux, and JVM targets
  jlex - contains JLex bantam.lexer generator tool
         (http://www.cs.princeton.edu/~appel/modern/java/JLex/)
  javacup - contains Java Cup bantam.parser generator tool
	    (http://www2.cs.tum.edu/projects/cup/)
  javacc - contains the JavaCC bantam.lexer and bantam.parser generator tool
           (https://javacc.dev.java.net/)

tests contents:
  *.btm - Several bantam programs for testing out the bantam compiler
  build.xml - an ant XML for building the bantam programs using the
              built compiler or a reference compiler

To build the source code for the compiler, the documentation, and the
the Bantam test programs (i.e., compile them with the compiler built by 
making the source code) simply type 'ant all' in the root directory.  
Note: if the compiler isn't working then this will fail when compiling
the test programs.  However, the (incomplete) compiler and documentation
should build properly.

Note: the SPIM simulator is not included in this installation package
although the runtime library (rootdir/lib/exceptions.s) is.  To use 
SPIM you will need to install SPIM and configure it to use the provided
runtime library file.
 
To learn more about the Bantam Java language or compiler, read the Bantam Compiler
Project Lab manual available at http://www.bantamjava.com/.

Have fun!!!
