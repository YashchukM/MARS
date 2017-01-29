package anaphora.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static anaphora.evaluator.NonPrepositionalNPEvaluator.PATTERN;

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

public class NonPrepositionalNPEvaluatorTest {
    private BasicEvaluator evaluator;
    private String sentence;
    private AnaphorInfo anaphorInfo;

    @Before
    public void init() throws IOException {
        evaluator = new NonPrepositionalNPEvaluator();
        sentence = "Insert the cassette into the VCR, making sure it is suitable for the length of recording";
        try (Reader reader = new StringReader(sentence)) {
            anaphorInfo = new AnaphorIterable(reader).iterator().next();
        }
    }

    @Test
    public void testEvaluate() {
        int[] resultScores = {0, -1};

        evaluator.evaluate(anaphorInfo);

        assertTrue(Arrays.equals(resultScores, evaluator.getLastScores()));
    }

    @Test
    public void testPattern() {
        Tree sentenceTree = anaphorInfo.getAnaphorSentence();
        Tree result = Tree.valueOf("(NP (DT the) (NNP VCR))");

        List<Tree> matchedTrees = MARSHelper.matchedTrees(sentenceTree, PATTERN);

        assertEquals(result, matchedTrees.get(0));
    }
}
