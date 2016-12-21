package anaphora.evaluator;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import anaphora.domain.AnaphorContext;
import org.junit.Test;

import anaphora.helper.MARSHelper;
import anaphora.resolver.MARSResolver;
import edu.stanford.nlp.trees.Tree;

public class IndicatorWordsEvaluatorTest {
    BasicEvaluator evaluator = new IndicatorWordsEvaluator();

    @Test
    public void testEvaluate() {
        String sentence = "To develop a new algorithm, you should first design it";
        Tree sentenceTree = MARSResolver.PARSER.parse(sentence);
//        sentenceTree.pennPrint();
        Tree anaphor = Tree.valueOf("(PRP it)");
        List<Tree> candidates = MARSHelper.getNPs(0, Collections.singletonList(sentenceTree), anaphor);
        candidates.forEach(Tree::pennPrint);
        AnaphorContext context = new AnaphorContext(anaphor, Collections.singletonList(sentenceTree), candidates);
        int[] resultScores = {1};

        evaluator.evaluate(context);
//        System.out.println(Arrays.toString(evaluator.getLastScores()));
        assertTrue(Arrays.equals(resultScores, evaluator.getLastScores()));
    }
}
