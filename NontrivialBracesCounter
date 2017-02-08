import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Name: NontrivialBracesCounter.java
 * Class: CS461
 * Project: 0
 * Authors: Charlie Beck, Ana Sofia Solis Canales, Siyuan Li, Luis Henriquez-Perez
 */
public class NontrivialBracesCounter {

    /**
     * Regex definition of the start of a comment
     */
    private final String COMMENTSTARTREGEX = "//.*";

    /**
     * Regex definition of the end of a comment
     */
    private final String COMMENTENDREGEX = "(?s)/\\*.*?\\*/";

    /**
     * Regex definition of a String
     */
    private final String STRINGREGEX = "(\"(?:\\\\[^\"]|\\\\\"|.)*?\")";

    /**
     * Regex definition of a character
     */
    private final String CHARREGEX = "(\'(?:\\\\[^\']|\\\\\'|.)*?\')";

    /**
     * Regex definition for everything else but left braces
     */
    private final String NONLBRACKREGEX = "[^{]";

    /**
     * empty constructor
     */
    public NontrivialBracesCounter() {

    }

    /**
     * Returns the number of non trivial left braces in a java program.
     *
     * @param program in string format to analyze
     * @return int number of non-trivial left braces
     */
    public int getNumNontrivialLeftBraces(String program) {

        //Creates a regex with all the things to remove from the program.
        String removalRegex = COMMENTSTARTREGEX + "|" + STRINGREGEX + "|" +
                COMMENTENDREGEX + "|"+ CHARREGEX + "|" + NONLBRACKREGEX;

        //After removal, get the length of the remaining string as
        //the number of non-trivial left braces.
        return (program.replaceAll(removalRegex, "").length());

    }


    /**
     * Test the counter by passing a file and analysing it.
     *
     * @param fileName of the file to analyze
     * @throws FileNotFoundException if the file is non-existent.
     */
    public void TestNoTrivialBracesCounter (String fileName) throws FileNotFoundException{

        String delimiter = "--------------------------------------------------";
        System.out.println(delimiter);

        try{
            File file = new File(fileName);
            String contents = new Scanner(file).useDelimiter("\\Z").next();
            System.out.println("Number of NontrivialBraces in " + file.getName() + ": "
                    + getNumNontrivialLeftBraces(contents));

        } catch (FileNotFoundException e) {
            System.err.println("Caught FileNotFoundException: " + e.getMessage());
        }

    }
    /**
     * Tests the NontrivialBracesCounter class.
     *
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args)  throws FileNotFoundException {

        NontrivialBracesCounter ntbc = new NontrivialBracesCounter();

        //Original number of left braces, found by using intelliJ: 16
        //After editing the file to contain trivial left braces: 23
        //ntbc.TestNoTrivialBracesCounter("src/MidiPlayer.java");
        ntbc.TestNoTrivialBracesCounter("src/MidiPlayerWTrivalLB.java");

    }
}
