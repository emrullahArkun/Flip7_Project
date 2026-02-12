package com.flavia.rules;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calculates the probability that the next HIT will NOT result in a bust.
 */
public class ProbabilityCalculator {

    private ProbabilityCalculator() {}

    /**
     * @return Success probability (0.0 to 1.0) that the next HIT will NOT bust.
     */
    public static double calculateSuccessProbability(List<Card> playerHand, List<Card> remainingDeck) {
        if (remainingDeck == null || remainingDeck.isEmpty()) {
            return 0.0;
        }

        // Second Chance guarantees the next draw won't bust.
        if (hasSecondChance(playerHand)) {
            return 1.0;
        }

        return 1.0 - getBustProbability(playerHand, remainingDeck);
    }

    private static boolean hasSecondChance(List<Card> hand) {
        for (Card c : hand) {
            if (c.type() == CardType.SECOND_CHANCE) {
                return true;
            }
        }
        return false;
    }

    private static double getBustProbability(List<Card> playerHand, List<Card> remainingDeck) {
        Set<Integer> ownedValues = new HashSet<>();
        for (Card c : playerHand) {
            if (c.type() == CardType.NUMBER) {
                ownedValues.add(c.value());
            }
        }

        int badCardsCount = 0;
        for (Card c : remainingDeck) {
            if (c.type() == CardType.NUMBER && ownedValues.contains(c.value())) {
                badCardsCount++;
            }
        }

        return (double) badCardsCount / remainingDeck.size();
    }
}
