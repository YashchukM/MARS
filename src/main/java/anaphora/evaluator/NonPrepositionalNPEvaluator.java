package anaphora.evaluator;

import static anaphora.helper.MARSHelper.matchedTrees;

import java.util.HashSet;
import java.util.Set;

import anaphora.domain.AnaphorInfo;
import edu.stanford.nlp.trees.Tree;

public class NonPrepositionalNPEvaluator extends BasicEvaluator {
    public static final int MARK = -1;
    public static final String PATTERN = getPattern();

    @Override
    public int[] evaluate(AnaphorInfo anaphorInfo) {
        Set<Tree> marked = new HashSet<>();

        for (Tree sentence: anaphorInfo.getCandidateSentences()) {
            marked.addAll(matchedTrees(sentence, PATTERN));
        }

        return giveScores(anaphorInfo.getCandidates(), marked, MARK);
    }

    private static String getPattern() {
        return "NP [>> PP & !< @PRP]";
    }
}
