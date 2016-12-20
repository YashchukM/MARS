package anaphora.evaluator;

import edu.stanford.nlp.trees.Tree;

import java.util.List;

/**
 * Basic interface for all rule evaluators, defined in MARS algorithm (Indicator words, Definiteness, etc).
 */
public interface Evaluator {
    int[] evaluate(List<Tree> candidateSentences, List<Tree> candidates, Tree anaphor);
}
