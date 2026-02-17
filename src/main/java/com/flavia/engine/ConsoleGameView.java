package com.flavia.engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.player.Player;

import java.util.List;
import java.util.Objects;

public class ConsoleGameView {

    public void displayTurnEvents(List<TurnEvent> events) {
        for (TurnEvent event : events) {
            if (event instanceof TurnEvent.CardDrawn(Player player, Card card)) {
                System.out.println("-> " + player.getName() + " draws: " + card);
            } else if (event instanceof TurnEvent.PlayerStayed(Player player)) {
                System.out.println("-> " + player.getName() + " stays.");
            } else if (event instanceof TurnEvent.PlayerBusted e) {
                System.out.println("!!! " + e.player().getName() + " BUSTED! (Duplicate) !!!");
            } else if (event instanceof TurnEvent.PlayerFrozen(Player target)) {
                System.out.println("FREEZE: " + target.getName() + " must stop immediately.");
            } else if (event instanceof TurnEvent.SecondChanceConsumed e) {
                System.out.println("Second Chance used: Duplicate " + e.card().value() + " ignored.");
            } else if (event instanceof TurnEvent.ActionCardPlayed e) {
                if (Objects.requireNonNull(e.card().type()) == CardType.FLIP_THREE) {
                    System.out.println("FLIP_THREE: " + e.target().getName() + " draws 3 cards.");
                }
            } else if (event instanceof TurnEvent.DeckEmpty) {
                System.out.println("Deck is empty! Cannot draw more cards.");
            }
        }
    }

    public void displayRoundStart() {
        System.out.println("\n--- NEW ROUND STARTING ---");
    }

    public void displayRoundEnd() {
        System.out.println("========================\n");
    }

    public void displayScoring(Player player, int points, int totalPoints, boolean busted) {
        if (busted) {
            System.out.println(player.getName() + ": BUST (0 Points)");
        } else {
            System.out.println(player.getName() + ": " + points + " Points");
        }
        System.out.println("   -> New Total: " + totalPoints);
    }

    public void displayWinner(Player winner) {
        System.out.println("\n*** GAME OVER! " + winner.getName() + " HAS WON! ***");
        System.out.println("========================\n");
    }

    public void displayScoringHeader() {
        System.out.println("\n=== ROUND SCORING ===");
    }
}
