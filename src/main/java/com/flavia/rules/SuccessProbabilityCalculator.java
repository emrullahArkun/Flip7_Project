package com.flavia.rules;

import com.flavia.domain.model.Card;
import java.util.List;

public interface SuccessProbabilityCalculator {
    double calculateSuccessProbability(List<Card> playerHand, List<Card> remainingDeck);
}
