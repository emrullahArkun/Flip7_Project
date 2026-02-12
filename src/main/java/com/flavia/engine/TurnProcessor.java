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
        if (!state.status(player).canAct()) return;

        List<Card> hand = state.hand(player);

        double successProb = ProbabilityCalculator.calculateSuccessProbability(
                hand,
                deck.viewDrawPile()
        );

        TurnInfo info = new TurnInfo(
                List.copyOf(hand),
                calculatePoints(hand),
                successProb,
                deck.drawPileSize(),
                state.securedPlayerNames(players)
        );

        PlayerAction action = player.decide(info);

        if (action == PlayerAction.STAY) {
            System.out.println("-> " + player.getName() + " bleibt stehen.");
            state.setStatus(player, PlayerStatus.STAYED);
            return;
        }

        drawAndResolve(player);
    }

    private void drawAndResolve(Player player) {
        if (!state.status(player).canAct()) return;

        List<Card> hand = state.hand(player);
        Card drawn = deck.draw();
        System.out.println("-> " + player.getName() + " zieht: " + drawn);

        if (drawn.type() == CardType.NUMBER) {
            boolean isDuplicate = hasNumberValue(hand, drawn.value());

            if (isDuplicate) {
                if (consumeSecondChanceIfAvailable(hand)) {
                    System.out.println("   ✅ Second Chance verbraucht: Duplikat " + drawn.value() + " ignoriert.");
                    deck.discardAll(List.of(drawn));
                    return;
                } else {
                    hand.add(drawn); // optional sichtbar lassen
                    System.out.println("!!! " + player.getName() + " ist BUST! (Duplikat) !!!");
                    state.setStatus(player, PlayerStatus.BUSTED);
                    return;
                }
            }

            hand.add(drawn);
            return;
        }

        // ACTION
        hand.add(drawn);
        applyActionCard(player, drawn);
    }

    private void applyActionCard(Player actor, Card actionCard) {
        switch (actionCard.type()) {

            case SECOND_CHANCE -> System.out.println("   (Second Chance erhalten)");

            case FREEZE -> {
                Optional<Player> targetOpt = selectTarget(actor, CardType.FREEZE);
                if (targetOpt.isEmpty()) {
                    System.out.println("   (FREEZE ohne Effekt: kein aktives Ziel)");
                    return;
                }
                Player target = targetOpt.get();
                System.out.println("   ❄️ FREEZE: " + target.getName() + " muss sofort stoppen.");
                state.setStatus(target, PlayerStatus.FROZEN);
            }

            case FLIP_THREE -> {
                Optional<Player> targetOpt = selectTarget(actor, CardType.FLIP_THREE);
                if (targetOpt.isEmpty()) {
                    System.out.println("   (FLIP_THREE ohne Effekt: kein aktives Ziel)");
                    return;
                }
                Player target = targetOpt.get();
                System.out.println("   🎴 FLIP_THREE: " + target.getName() + " zieht 3 Karten.");
                forceDraw(target, 3);
            }

            default -> { }
        }
    }

    private void forceDraw(Player target, int amount) {
        for (int i = 0; i < amount; i++) {
            if (!state.status(target).canAct()) return;
            drawAndResolve(target);
        }
    }

    private Optional<Player> selectTarget(Player actor, CardType actionType) {
        List<Player> eligible = new ArrayList<>();
        for (Player p : players) {
            if (p == actor) continue;
            if (state.status(p).canAct()) eligible.add(p);
        }
        if (eligible.isEmpty()) return Optional.empty();

        List<String> names = eligible.stream().map(Player::getName).toList();
        String chosenName = actor.chooseTarget(new TargetInfo(actionType, actor.getName(), names));

        for (Player p : eligible) {
            if (p.getName().equals(chosenName)) return Optional.of(p);
        }
        return Optional.of(eligible.get(0));
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
        int points = 0;
        for (Card card : hand) {
            if (card.type() == CardType.NUMBER) points += card.value();
        }
        return points;
    }
}
