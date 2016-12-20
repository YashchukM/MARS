package anaphora.resolver;

import anaphora.evaluator.MARSEvaluator;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

import java.util.List;

import static anaphora.helper.MARSHelper.*;

public class MARSResolver {
    private static final String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
    public static final LexicalizedParser PARSER = LexicalizedParser.loadModel(PCG_MODEL);

    public static void resolve(String fileName) {
        List<Tree> allSentences = getParsedSentences(PARSER, fileName);
        MARSEvaluator evaluator = new MARSEvaluator();

        for (int i = 0; i < allSentences.size(); i++) {
            Tree sentence = allSentences.get(i);
            List<Tree> pronouns = getPronounPhrases(sentence);

            List<Tree> candidateSentences = getPrevThreeSentences(i, allSentences);
            if (!pronouns.isEmpty()) {
                System.out.println("Sentence: " + (i + 1));
            }
            for (Tree anaphor : pronouns) {
                List<Tree> listNP = getNPs(i, allSentences, anaphor);
                int[] scores = evaluator.evaluate(candidateSentences, listNP, anaphor, sentence);

                Tree antecedent = chooseAntecedent(listNP, scores);
                System.out.println(stringFormOf(anaphor) + " - " + stringFormOf(antecedent));
            }
        }
    }

    private static Tree chooseAntecedent(List<Tree> listNP, int[] scores) {
        int maxIndex = 0, maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] >= maxValue) {
                maxIndex = i;
                maxValue = scores[i];
            }
        }

        return listNP.get(maxIndex);
    }
}
