package com.flavia.engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScoreBoard {

    private final Map<Player, Integer> totalScores = new HashMap<>();
    private final int targetScore;
    private final ConsoleGameView view;

    public ScoreBoard(List<Player> players, int targetScore) {
        this.targetScore = targetScore;
        this.view = new ConsoleGameView();
        for (Player p : players) {
            totalScores.put(p, 0);
        }
    }

    public Optional<Player> scoreRound(List<Player> players, RoundState state) {
        view.displayScoringHeader();

        for (Player player : players) {
            List<Card> hand = state.hand(player);

            // If busted, 0 points for this round
            int roundPoints = (state.status(player) == PlayerStatus.BUSTED)
                    ? 0
                    : calculatePoints(hand);

            int currentTotal = totalScores.getOrDefault(player, 0);
            int newTotal = currentTotal + roundPoints;
            totalScores.put(player, newTotal);

            view.displayScoring(player, roundPoints, newTotal, state.status(player) == PlayerStatus.BUSTED);

            // Check for win condition
            if (newTotal >= targetScore) {
                view.displayWinner(player);
                return Optional.of(player);
            }
        }

        view.displayRoundEnd();
        return Optional.empty();
    }

    private int calculatePoints(List<Card> hand) {
        int points = 0;
        for (Card card : hand) {
            // Only number cards count towards score
            if (card.type() == CardType.NUMBER) points += card.value();
        }
        return points;
    }
}
