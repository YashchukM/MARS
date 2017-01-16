package anaphora.evaluator;

import anaphora.domain.AnaphorInfo;

/**
 * Basic interface for all rule evaluators, defined in MARS algorithm (Indicator words, Definiteness, etc).
 */
public interface Evaluator {
    int[] evaluate(AnaphorInfo anaphorInfo);
}
