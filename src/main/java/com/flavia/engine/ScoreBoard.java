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

    public ScoreBoard(List<Player> players, int targetScore) {
        this.targetScore = targetScore;
        for (Player p : players) {
            totalScores.put(p, 0);
        }
    }

    public Optional<Player> scoreRound(List<Player> players, RoundState state) {
        System.out.println("\n=== RUNDENABRECHNUNG ===");

        for (Player player : players) {
            List<Card> hand = state.hand(player);

            int roundPoints = (state.status(player) == PlayerStatus.BUSTED)
                    ? 0
                    : calculatePoints(hand);

            if (roundPoints == 0 && state.status(player) == PlayerStatus.BUSTED) {
                System.out.println(player.getName() + ": BUST (0 Points)");
            } else {
                System.out.println(player.getName() + ": " + roundPoints + " Points");
            }

            int newTotal = totalScores.get(player) + roundPoints;
            totalScores.put(player, newTotal);

            System.out.println("   -> Neuer Gesamtstand: " + newTotal);

            if (newTotal >= targetScore) {
                System.out.println("\n*** GAME OVER! " + player.getName() + " HAS WON! ***");
                System.out.println("========================\n");
                return Optional.of(player);
            }
        }

        System.out.println("========================\n");
        return Optional.empty();
    }

    private int calculatePoints(List<Card> hand) {
        int points = 0;
        for (Card card : hand) {
            if (card.type() == CardType.NUMBER) points += card.value();
        }
        return points;
    }
}
