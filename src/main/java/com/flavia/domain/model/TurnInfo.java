package com.flavia.domain.model;

import java.util.List;

/**
 * Snapshot of the current situation for a player's decision.
 */
public record TurnInfo(
        List<Card> myCards,
        int currentPoints,
        double successProbability,
        int cardsRemainingInDeck,
        List<String> securedPlayerNames
) {}
