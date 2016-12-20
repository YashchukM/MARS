package anaphora.evaluator;


import static anaphora.helper.MARSHelper.getBaseVBForm;
import static anaphora.helper.MARSHelper.matchedTrees;
import static anaphora.helper.MARSHelper.wordOf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.trees.Tree;

public class IndicatorWordsEvaluator extends BasicEvaluator {
    public static final int MARK = 1;
    public static final String PATTERN = String.format("NP > VP $- @VB=%s", STANDARD_MATCH_NAME);

    public static final Set<String> INDICATOR_WORDS = new HashSet<>(
            Arrays.asList(
                    "discuss", "present", "illustrate", "identify", "summarize", "examine",
                    "describe", "define", "show", "check", "develop", "review", "report",
                    "outline", "consider", "investigate", "explore", "assess", "analyse",
                    "synthesize", "study", "survey", "deal", "cover"
            )
    );

    @Override
    public int[] evaluate(List<Tree> candidateSentences, List<Tree> candidates, Tree anaphor) {
        Set<Tree> marked = new HashSet<>();

        for (Tree candidateSentence : candidateSentences) {
            marked.addAll(
                    matchedTrees(
                            candidateSentence, PATTERN, STANDARD_MATCH_NAME,
                            tree -> INDICATOR_WORDS.contains(getBaseVBForm(wordOf(tree)))
                    )
            );
        }

        return giveScores(candidates, marked, MARK);
    }
}
