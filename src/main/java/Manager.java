import java.io.IOException;

import anaphora.finder.MARSResolver;
import anaphora.helper.MARSHelper;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;


public class Manager {
    private static final String DEFAULT_INPUT_PATH = "input.txt";

    public static void main(String[] args) throws IOException {
        testDictionary();
//        MARSResolver.resolve(DEFAULT_INPUT_PATH);
    }

    public static void testDictionary() throws IOException {
//        String parse  = "(ROOT (S (NP (DT The) (NN dog)) (VP (VBD ran) (PP (IN after) (NP (DT the) (JJ intruding) (JJR bigger) (NN dog))))))";
//        Tree tree = Tree.valueOf(parse);

        Tree t = MARSResolver.PARSER.parse("Organize apps in Launchpad.");
        System.out.println(t);
    }

}
