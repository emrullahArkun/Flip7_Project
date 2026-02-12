package com.flavia.engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.Deck;
import com.flavia.domain.model.TurnInfo;
import com.flavia.player.Player;
import com.flavia.player.TargetInfo;
import com.flavia.rules.ProbabilityCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TurnProcessor {

    private final Deck deck;
    private final List<Player> players;
    private final RoundState state;

    public TurnProcessor(Deck deck, List<Player> players, RoundState state) {
        this.deck = deck;
        this.players = players;
        this.state = state;
    }

    public void processTurn(Player player) {
        // Skip if player cannot act (e.g. BUSTED, STAYED, FROZEN)
        if (!state.status(player).canAct()) return;

        List<Card> hand = state.hand(player);

        // Calculate probability of drawing a safe card
        double successProb = ProbabilityCalculator.calculateSuccessProbability(
                hand,
                deck.viewDrawPile()
        );

        // Prepare info for player decision
        TurnInfo info = new TurnInfo(
                List.copyOf(hand),
                calculatePoints(hand),
                successProb,
                deck.drawPileSize(),
                state.securedPlayerNames(players)
        );

        PlayerAction action = player.decide(info);

        if (action == PlayerAction.STAY) {
            System.out.println("-> " + player.getName() + " stays.");
            state.setStatus(player, PlayerStatus.STAYED);
            return;
        }

        // Player chose to HIT
        drawAndResolve(player);
    }

    private void drawAndResolve(Player player) {
        if (!state.status(player).canAct()) return;

        List<Card> hand = state.hand(player);
        Card drawn = deck.draw();
        System.out.println("-> " + player.getName() + " draws: " + drawn);

        if (drawn.type() == CardType.NUMBER) {
            boolean isDuplicate = hasNumberValue(hand, drawn.value());

            if (isDuplicate) {
                // Check for Second Chance card to save the player
                if (consumeSecondChanceIfAvailable(hand)) {
                    System.out.println("Second Chance used: Duplicate " + drawn.value() + " ignored.");
                    deck.discardAll(List.of(drawn));
                } else {
                    hand.add(drawn); // Add to hand to show the duplicate
                    System.out.println("!!! " + player.getName() + " BUSTED! (Duplicate) !!!");
                    state.setStatus(player, PlayerStatus.BUSTED);
                }
                return;
            }

            hand.add(drawn);
            return;
        }

        // Handle Action Cards
        hand.add(drawn);
        applyActionCard(player, drawn);
    }

    private void applyActionCard(Player actor, Card actionCard) {
        switch (actionCard.type()) {

            case SECOND_CHANCE -> System.out.println("   (Second Chance acquired)");

            case FREEZE -> {
                Optional<Player> targetOpt = selectTarget(actor, CardType.FREEZE);
                if (targetOpt.isEmpty()) {
                    System.out.println("   (FREEZE no effect: no active target)");
                    return;
                }
                Player target = targetOpt.get();
                System.out.println("FREEZE: " + target.getName() + " must stop immediately.");
                state.setStatus(target, PlayerStatus.FROZEN);
            }

            case FLIP_THREE -> {
                Optional<Player> targetOpt = selectTarget(actor, CardType.FLIP_THREE);
                if (targetOpt.isEmpty()) {
                    System.out.println("   (FLIP_THREE no effect: no active target)");
                    return;
                }
                Player target = targetOpt.get();
                System.out.println("FLIP_THREE: " + target.getName() + " draws 3 cards.");
                forceDraw(target);
            }

            default -> { }
        }
    }

    private void forceDraw(Player target) {
        for (int i = 0; i < 3; i++) {
            if (!state.status(target).canAct()) return;
            drawAndResolve(target);
        }
    }

    private Optional<Player> selectTarget(Player actor, CardType actionType) {
        // Find eligible targets (other active players)
        List<Player> eligible = new ArrayList<>();
        for (Player p : players) {
            if (p == actor) continue;
            if (state.status(p).canAct()) eligible.add(p);
        }
        if (eligible.isEmpty()) return Optional.empty();

        // Ask actor to choose a target
        List<String> names = eligible.stream().map(Player::getName).toList();
        String chosenName = actor.chooseTarget(new TargetInfo(actionType, actor.getName(), names));

        for (Player p : eligible) {
            if (p.getName().equals(chosenName)) return Optional.of(p);
        }
        // Default to first eligible if choice is invalid
        return Optional.of(eligible.getFirst());
    }

    private boolean consumeSecondChanceIfAvailable(List<Card> hand) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).type() == CardType.SECOND_CHANCE) {
                Card consumed = hand.remove(i);
                deck.discardAll(List.of(consumed));
                return true;
            }
        }
        return false;
    }

    private boolean hasNumberValue(List<Card> hand, int value) {
        for (Card c : hand) {
            if (c.type() == CardType.NUMBER && c.value() == value) return true;
        }
        return false;
    }

    private int calculatePoints(List<Card> hand) {
        return hand.stream()
                .filter(card -> card.type() == CardType.NUMBER)
                .mapToInt(Card::value)
                .sum();
    }
}
