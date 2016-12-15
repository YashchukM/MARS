package anaphora.evaluator;


import anaphora.finder.MARSResolver;
import anaphora.helper.MARSHelper;
import edu.stanford.nlp.trees.Tree;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MARSEvaluatorTest {
    MARSEvaluator evaluator;

    @Before
    public void init() {
        evaluator = new MARSEvaluator();
    }

    @Test
    public void testEvalImmediateReference() {
//        String sentence = "To turn on the printer, press the Power button and hold it down for a moment.";
        String sentence = "To print the paper, you can stand the printer up or lay it down.";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);

        List<Tree> pronounPhrases = MARSHelper.getPronounPhrases(sentenceTree);
        Tree anaphor = pronounPhrases.get(0);

        List<Tree> candidates = MARSHelper.getNPs(0, Collections.singletonList(sentenceTree), anaphor);
        candidates.forEach(Tree::pennPrint);

        evaluator.initScores(candidates.size());
        evaluator.evalImmediateReference(
                candidates,
                anaphor,
                sentenceTree);

        System.out.println(Arrays.toString(evaluator.getGeneralScores()));

        sentenceTree.pennPrint();
    }
}
