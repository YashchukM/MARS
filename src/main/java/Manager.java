import java.io.IOException;

import edu.stanford.nlp.process.Morphology;


public class Manager {
    public static final String DEFAULT_WORDNET_PATH = "D:\\JavaLibs\\Wordnet";

    public static void main(String[] args) throws IOException {
        testDictionary();
    }

    public static void testDictionary() throws IOException {
        String word = Morphology.lemmaStatic("studying", "VB", true);
        System.out.println("word = " + word);
    }

}
