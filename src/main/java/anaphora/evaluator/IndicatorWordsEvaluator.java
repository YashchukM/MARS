package anaphora.evaluator;


import anaphora.domain.AnaphorContext;
import edu.stanford.nlp.trees.Tree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static anaphora.helper.MARSHelper.*;

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
    public int[] evaluate(AnaphorContext anaphorContext) {
        Set<Tree> marked = new HashSet<>();

        for (Tree candidateSentence : anaphorContext.getCandidateSentences()) {
            marked.addAll(
                    matchedTrees(
                            candidateSentence, PATTERN, STANDARD_MATCH_NAME,
                            tree -> INDICATOR_WORDS.contains(getBaseVBForm(wordOf(tree)))
                    )
            );
        }

        return giveScores(anaphorContext.getCandidates(), marked, MARK);
    }
}
