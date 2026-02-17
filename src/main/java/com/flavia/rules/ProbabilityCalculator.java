package com.flavia.rules;

import com.flavia.domain.model.Card;

import java.util.List;

/**
 * @deprecated Use {@link DefaultProbabilityCalculator} instead.
 */
@Deprecated
public class ProbabilityCalculator {

    private ProbabilityCalculator() {}

    /**
     * @return Success probability (0.0 to 1.0) that the next HIT will NOT bust.
     */
    public static double calculateSuccessProbability(List<Card> playerHand, List<Card> remainingDeck) {
        return new DefaultProbabilityCalculator().calculateSuccessProbability(playerHand, remainingDeck);
    }
}
