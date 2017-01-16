package anaphora.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static anaphora.evaluator.BasicEvaluator.STANDARD_MATCH_NAME;
import static anaphora.evaluator.ImmediateReferenceEvaluator.PATTERN;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import anaphora.domain.AnaphorInfo;
import anaphora.helper.MARSHelper;
import anaphora.resolver.MARSResolver;
import edu.stanford.nlp.trees.Tree;

public class ImmediateReferenceEvaluatorTest {
    private BasicEvaluator evaluator = new ImmediateReferenceEvaluator();

    @Test
    public void testEvaluate() {
        String sentence = "To print the paper, you can stand the printer up or lay it flat.";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);
        Tree anaphor = Tree.valueOf("(PRP it)");
        List<Tree> candidates = MARSHelper.getNPs(0, Collections.singletonList(sentenceTree), anaphor);
        AnaphorInfo anaphorInfo =
                new AnaphorInfo(anaphor, sentenceTree, Collections.singletonList(sentenceTree), candidates);
        int[] resultScores = {0, 2};

        evaluator.evaluate(anaphorInfo);

        assertTrue(Arrays.equals(resultScores, evaluator.getLastScores()));
    }

    @Test
    public void testPattern() {
        String sentence = "Unwrap the paper, form it and align it, then load it into the drawer.";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);

        Tree anaphor = Tree.valueOf("(PRP it)");
        Tree result = Tree.valueOf("(NP (DT the) (NN paper))");

        List<Tree> matchedTrees = MARSHelper.matchedTrees(
                sentenceTree, PATTERN, STANDARD_MATCH_NAME, anaphor
        );

        assertEquals(1, matchedTrees.size());
        assertEquals(result, matchedTrees.get(0));
    }
}
