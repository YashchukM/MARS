package anaphora.evaluator;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import anaphora.finder.MARSResolver;
import anaphora.helper.MARSHelper;
import edu.stanford.nlp.trees.Tree;

public class MARSEvaluatorTest {
    MARSEvaluator evaluator;

    @Before
    public void init() {
        evaluator = new MARSEvaluator();
    }

    @Test
    public void testEvalImmediateReference() {
        String sentence = "To print the paper, you can stand the printer up or lay it flat.";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);
        Tree anaphor = Tree.valueOf("(PRP it)");
        List<Tree> candidates = MARSHelper.getNPs(0, Collections.singletonList(sentenceTree), anaphor);
        int[] resultScores = {0, 2};

        evaluator.initScores(candidates.size());
        evaluator.evalImmediateReference(candidates, anaphor, sentenceTree);

        assertTrue(Arrays.equals(resultScores, evaluator.getGeneralScores()));
    }

    @Test
    public void testEvalImmediateReferencePattern() {
        String sentence = "Unwrap the paper, form it and align it, then load it into the drawer.";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);

        String anaphoraVP = "(VP < (VB [$. (NP < PRP=anaphor) | $. (S < (NP < PRP=anaphor))]))";
        String pattern = String.format("NP !< PRP > (VP [$. (CC $. %s) | $. (/,/ $. %s)])", anaphoraVP, anaphoraVP);

        Tree anaphor = Tree.valueOf("(PRP it)");
        Tree result = Tree.valueOf("(NP (DT the) (NN paper))");

        List<Tree> matchedTrees = MARSHelper.matchedTrees(sentenceTree, pattern, "anaphor", anaphor);
        assertEquals(1, matchedTrees.size());
        assertEquals(result, matchedTrees.get(0));
    }
}