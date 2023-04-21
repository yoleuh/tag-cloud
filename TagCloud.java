import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Java program that counts word occurrences in a given input file and outputs
 * an HTML document with a table of the words and counts listed in alphabetical
 * order.
 *
 * @author brian
 *
 */
public final class TagCloud {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloud() {
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *                   the {@code String} from which to get the word or separator
     *                   string
     * @param position
     *                   the starting index
     * @param separators
     *                   the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures
     *
     *          <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     *          </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        char c = text.charAt(position);
        String x = "";
        int i = position + 1;
        x = x + c;
        if (!separators.contains(c)) {
            while (i < text.length() && !separators.contains(c)) {
                c = text.charAt(i);
                if (!separators.contains(c)) {
                    String y = "" + c;
                    x = x.concat(y);
                }
                i++;
            }
        } else {
            while (i < text.length() && separators.contains(c)) {
                c = text.charAt(i);
                if (separators.contains(c)) {
                    String y = "" + c;
                    x = x.concat(y);
                }
                i++;
            }
        }
        return x;
    }

    /**
     * Assign counts to each word in the map and create a queue of distinct
     * words from the text file
     *
     * @param wordCount
     *                  map of counts assigned to respective words
     * @param words
     *                  queue of new words
     * @param fileIn
     *                  input stream
     *
     * @ensures words contains all distinct words from the fileIn, count is
     *          assigned to words in wordCount
     *
     */
    public static void count(Map<String, Integer> wordCount,
            BufferedReader fileIn) {

        // create separator set
        String separatorsString = "_,./;'[]=-?!:()*&^%$#@0123456789\"`~ ";
        Set<Character> separators = new HashSet<Character>();
        int l = separatorsString.length();
        for (int i = 0; i < l; i++) {
            separators.add(separatorsString.charAt(i));
        }
        String line = "";
        while (line != null) {
            try {
                line = fileIn.readLine();
            } catch (IOException e) {
                System.err.println("unable to read file");
            }
            if (line != null) {
                line = line.toLowerCase();
                int i = 0;
                while (i < line.length()) {
                    // check if string is separator
                    String s = nextWordOrSeparator(line, i, separators);
                    if (separators.contains(s.charAt(0))) {
                        i++;
                    } else {
                        // word counter
                        if (wordCount.containsKey(s)) {
                            int count = wordCount.get(s) + 1;
                            wordCount.remove(s);
                            wordCount.put(s, count);
                        } else {
                            // if it is a new word, add into queue
                            wordCount.put(s, 1);
                        }
                        i = i + s.length();
                    }
                }
            }

        }
    }

    /**
     * Sorts the words into a map with font sizes
     *
     * @param wordCount
     *                  map of counts assigned to respective words
     * @param count
     *                  number of words to print
     * @param fileOut
     *                  output stream
     * @ensures well formatted html body of words in the correct font size
     */
    public static void fontSort(Map<String, Integer> wordCount, int count,
            PrintWriter fileOut) {

        Map<String, String> wordFont = new HashMap<>();

        // sort numerically
        Comparator<Map.Entry<String, Integer>> comparatorN = new numerical();
        ArrayList<Map.Entry<String, Integer>> temp = new ArrayList<Map.Entry<String, Integer>>(
                wordCount.entrySet());
        temp.sort(comparatorN);

        // add to map with font sizes
        int j = 0;
        int current = 48;
        while (temp.size() > 0 && j < count) {
            for (int i = 0; i < (count / 39) + 1; i++) {
                Map.Entry<String, Integer> t = temp.remove(0);
                wordFont.put(t.getKey(), "f" + current);
                wordCount.put(t.getKey(), t.getValue());
                j++;
            }
            current--;
        }

        // sort alphabetically
        Comparator<Map.Entry<String, String>> comparatorA = new alphabetical();
        ArrayList<Map.Entry<String, String>> temp2 = new ArrayList<Map.Entry<String, String>>(
                wordFont.entrySet());
        temp2.sort(comparatorA);

        // print to html
        while (temp2.size() > 0) {
            Map.Entry<String, String> e = temp2.remove(0);
            fileOut.println(
                    "<span style=\"cursor:default\" class=\"" + e.getValue()
                            + "\" title=\"count:" + wordCount.get(e.getKey())
                            + "\">" + e.getKey() + "</span>");
        }

    }

    /**
     * Compare two strings based on alphabetical order
     */
    private static class alphabetical
            implements Comparator<Map.Entry<String, String>> {
        @Override
        public int compare(Map.Entry<String, String> o1,
                Map.Entry<String, String> o2) {
            return o1.getKey().toLowerCase()
                    .compareTo(o2.getKey().toLowerCase());
        }
    }

    /**
     * Compare two strings based on numerical order
     */
    private static class numerical
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }

    }

    /**
     * output html header
     *
     * @param inputFile
     *                  name of input file
     * @param fileOut
     *                  output stream into html file
     * @param count
     *                  number of words
     *
     *
     * @ensures complete html header openings
     */
    public static void outputHeader(String inputFile, PrintWriter fileOut,
            int count) {

        fileOut.println("<html>");
        fileOut.println("<head>");
        fileOut.println(
                "<title>Top " + count + " words in " + inputFile + "</title>");
        fileOut.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">\r");
        fileOut.println(
                "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">\r");
        fileOut.println("</head>");
        fileOut.println("<body>");
        fileOut.println(
                "<h2>Top " + count + " words in " + inputFile + "</h2>");
        fileOut.println("<hr>");
        fileOut.println("<div class = \"cdiv\">");
        fileOut.println("<p class = \"cbox\">");
    }

    /**
     * output html footer
     *
     * @param inputFile
     *                  name of input file
     * @param fileOut
     *                  output stream into html file
     *
     * @ensures complete html footer closings
     */
    public static void outputClosing(String inputFile, PrintWriter fileOut) {
        fileOut.println("</p>");
        fileOut.println("</div>");
        fileOut.println("</body>");
        fileOut.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *             the command line arguments
     */
    public static void main(String[] args) {
        // open streams
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in));

        // input prompt
        String inputFile = "";
        BufferedReader fileIn = null;
        while (fileIn == null) {
            try {
                System.out.println("name of an input file: ");
                inputFile = input.readLine();
                fileIn = new BufferedReader(new FileReader(inputFile));
            } catch (IOException e) {
                System.err.println("invalid input file");
            }
        }

        // output prompt
        String outputFile = "";
        PrintWriter fileOut = null;
        while (fileOut == null) {
            try {
                System.out.println("name of an output file: ");
                outputFile = input.readLine();
                fileOut = new PrintWriter(new FileWriter(outputFile));
            } catch (IOException e) {
                System.err.println("invalid output file");
            }
        }

        // count prompt
        int count = 0;
        try {
            System.out.print(
                    "the number of words to be included in the generated tag cloud: ");
            count = Integer.parseInt(input.readLine());
        } catch (Exception e) {
            System.err.println("invalid number");
        }

        // close streams
        try {
            input.close();
        } catch (Exception e) {
            System.err.println("unable to close input");
        }

        // output header
        outputHeader(inputFile, fileOut, count);

        // count the number of occurrences for each word
        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        count(wordCount, fileIn);

        // sort and print body
        fontSort(wordCount, count, fileOut);

        // closing tags
        outputClosing(inputFile, fileOut);

        // close streams
        try {
            fileIn.close();
        } catch (IOException e) {
            System.err.println("unable to close file");
        }
        fileOut.close();
    }

}
