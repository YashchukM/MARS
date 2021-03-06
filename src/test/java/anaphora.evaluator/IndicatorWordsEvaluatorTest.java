package anaphora.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static anaphora.evaluator.BasicEvaluator.STANDARD_MATCH_NAME;
import static anaphora.evaluator.IndicatorWordsEvaluator.INDICATOR_WORDS;
import static anaphora.evaluator.IndicatorWordsEvaluator.PATTERN;
import static anaphora.helper.MARSHelper.getBaseVBForm;
import static anaphora.helper.MARSHelper.wordOf;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import anaphora.domain.AnaphorInfo;
import anaphora.helper.AnaphorIterable;
import anaphora.helper.MARSHelper;
import edu.stanford.nlp.trees.Tree;

public class IndicatorWordsEvaluatorTest {
    private BasicEvaluator evaluator;
    private String sentence;
    private AnaphorInfo anaphorInfo;

    @Before
    public void init() throws IOException {
        evaluator = new IndicatorWordsEvaluator();
        sentence = "To develop a new algorithm, you should first design it";
        try (Reader reader = new StringReader(sentence)) {
            anaphorInfo = new AnaphorIterable(reader).iterator().next();
        }
    }

    @Test
    public void testEvaluate() {
        int[] resultScores = {1};

        evaluator.evaluate(anaphorInfo);

        assertTrue(Arrays.equals(resultScores, evaluator.getLastScores()));
    }

    @Test
    public void testPattern() {
        Tree sentenceTree = anaphorInfo.getAnaphorSentence();
        Tree result = Tree.valueOf("(NP (DT a) (JJ new) (NN algorithm))");

        List<Tree> matchedTrees = MARSHelper.matchedTrees(
                sentenceTree, PATTERN, STANDARD_MATCH_NAME,
                tree -> INDICATOR_WORDS.contains(getBaseVBForm(wordOf(tree)))
        );

        assertEquals(1, matchedTrees.size());
        assertEquals(result, matchedTrees.get(0));
    }
}
