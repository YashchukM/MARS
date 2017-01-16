package anaphora.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static anaphora.evaluator.BasicEvaluator.STANDARD_MATCH_NAME;
import static anaphora.evaluator.IndicatorWordsEvaluator.INDICATOR_WORDS;
import static anaphora.evaluator.IndicatorWordsEvaluator.PATTERN;
import static anaphora.helper.MARSHelper.getBaseVBForm;
import static anaphora.helper.MARSHelper.wordOf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import anaphora.domain.AnaphorInfo;

import org.junit.Test;

import anaphora.helper.MARSHelper;
import anaphora.resolver.MARSResolver;
import edu.stanford.nlp.trees.Tree;

public class IndicatorWordsEvaluatorTest {
    private BasicEvaluator evaluator = new IndicatorWordsEvaluator();

    @Test
    public void testEvaluate() {
        String sentence = "To develop a new algorithm, you should first design it";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);
        Tree anaphor = Tree.valueOf("(PRP it)");
        List<Tree> candidates = MARSHelper.getNPs(0, Collections.singletonList(sentenceTree), anaphor);
        AnaphorInfo context = new AnaphorInfo(anaphor, Collections.singletonList(sentenceTree), candidates);
        int[] resultScores = {1};

        evaluator.evaluate(context);

        assertTrue(Arrays.equals(resultScores, evaluator.getLastScores()));
    }

    @Test
    public void testPattern() {
        String sentence = "To develop a new algorithm, you should first design it.";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);
        Tree result = Tree.valueOf("(NP (DT a) (JJ new) (NN algorithm))");

        List<Tree> matchedTrees = MARSHelper.matchedTrees(
                sentenceTree, PATTERN, STANDARD_MATCH_NAME,
                tree -> INDICATOR_WORDS.contains(getBaseVBForm(wordOf(tree)))
        );

        assertEquals(1, matchedTrees.size());
        assertEquals(result, matchedTrees.get(0));
    }
}
