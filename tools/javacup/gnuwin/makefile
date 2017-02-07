SIMPLEDIR=SIMPLE~1
# SIMPLEDIR=simple_calc
RM=del
# RM=rm
JAVA=java
JAVAOPTS=
JAVAC=javac
JAVACOPTS=-verbose


test: java_cup\\bantam.Main.class java_cup\\$(SIMPLEDIR)\\bantam.Main.class
	echo "1*-2+2;" | $(JAVA) $(JAVAOPTS) java_cup.simple_calc.bantam.Main

	echo 'If the line above says "= 0" everything is OK.'
	

java_cup\\bantam.Main.class:
	$(JAVAC) $(JAVACOPTS) java_cup\\bantam.Main.java

java_cup\\$(SIMPLEDIR)\\bantam.Main.class:
	cd java_cup
	cd $(SIMPLEDIR)
	echo $(RM) bantam.parser.java
	echo $(RM) sym.java
	$(JAVA) $(JAVAOPTS) java_cup.bantam.Main < bantam.parser.cup
	$(JAVAC) $(JAVACOPTS) bantam.Main.java
