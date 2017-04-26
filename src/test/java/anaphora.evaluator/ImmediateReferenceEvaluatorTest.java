package anaphora.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static anaphora.evaluator.BasicEvaluator.STANDARD_MATCH_NAME;
import static anaphora.evaluator.ImmediateReferenceEvaluator.PATTERN;

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

public class ImmediateReferenceEvaluatorTest {
    private BasicEvaluator evaluator;
    private String sentence;
    private AnaphorInfo anaphorInfo;

    @Before
    public void init() throws IOException {
        evaluator = new ImmediateReferenceEvaluator();
        sentence = "To print the paper, you can stand the printer up or lay it flat.";
        try (Reader reader = new StringReader(sentence)) {
            anaphorInfo = new AnaphorIterable(reader).iterator().next();
        }
    }

    @Test
    public void testEvaluate() {
        int[] resultScores = {0, 2};

        evaluator.evaluate(anaphorInfo);

        assertTrue(Arrays.equals(resultScores, evaluator.getLastScores()));
    }

    @Test
    public void testPattern() {
        Tree sentenceTree = anaphorInfo.getAnaphorSentence();
        sentenceTree.pennPrint();
        Tree anaphor = anaphorInfo.getAnaphor();
        Tree result = Tree.valueOf("(NP (DT the) (NN printer))");

        List<Tree> matchedTrees = MARSHelper.matchedTrees(
                sentenceTree, PATTERN, STANDARD_MATCH_NAME, anaphor
        );

        assertEquals(1, matchedTrees.size());
        assertEquals(result, matchedTrees.get(0));
    }
}
