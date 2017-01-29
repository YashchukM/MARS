package anaphora.evaluator;

import static anaphora.helper.MARSHelper.matchedTrees;

import java.util.HashSet;
import java.util.Set;

import anaphora.domain.AnaphorInfo;
import edu.stanford.nlp.trees.Tree;

public class ImmediateReferenceEvaluator extends BasicEvaluator {
    public static final int MARK = 2;
    public static final String PATTERN = getPattern();

    @Override
    public int[] evaluate(AnaphorInfo anaphorInfo) {
        Set<Tree> marked = new HashSet<>();

        marked.addAll(matchedTrees(
                anaphorInfo.getAnaphorSentence(), PATTERN, STANDARD_MATCH_NAME,
                tree -> tree.equals(anaphorInfo.getAnaphor())
        ));

        return giveScores(anaphorInfo.getCandidates(), marked, MARK);
    }

    private static String getPattern() {
        String anaphoraVP = String.format(
                "(VP < (VB [$. (NP < PRP=%s) | $. (S < (NP < PRP=%s))]))",
                STANDARD_MATCH_NAME, STANDARD_MATCH_NAME
        );
        return String.format(
                "NP !< PRP > (VP [$. (CC $. %s) | $. (/,/ $. %s)])",
                anaphoraVP, anaphoraVP
        );
    }
}
