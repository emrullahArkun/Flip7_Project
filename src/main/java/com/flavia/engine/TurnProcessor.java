package com.flavia.engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.Deck;
import com.flavia.domain.model.TurnInfo;
import com.flavia.exceptions.DeckEmptyException;
import com.flavia.player.Player;
import com.flavia.player.TargetInfo;
import com.flavia.rules.DefaultProbabilityCalculator;
import com.flavia.rules.SuccessProbabilityCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TurnProcessor {

    private static final int FLIP_THREE_COUNT = 3;

    private final Deck deck;
    private final List<Player> players;
    private final RoundState state;
    private final SuccessProbabilityCalculator probabilityCalculator;
    private final Map<CardType, CardEffect> cardEffects;

    public TurnProcessor(Deck deck, List<Player> players, RoundState state) {
        this(deck, players, state, new DefaultProbabilityCalculator());
    }

    public TurnProcessor(Deck deck, List<Player> players, RoundState state, SuccessProbabilityCalculator probabilityCalculator) {
        this.deck = deck;
        this.players = players;
        this.state = state;
        this.probabilityCalculator = probabilityCalculator;
        this.cardEffects = Map.of(
                CardType.FREEZE, new FreezeEffect(),
                CardType.FLIP_THREE, new FlipThreeEffect(),
                CardType.SECOND_CHANCE, new SecondChanceEffect()
        );
    }

    public TurnResult processTurn(Player player) {
        List<TurnEvent> events = new ArrayList<>();

        // Skip if player cannot act (e.g. BUSTED, STAYED, FROZEN)
        if (!state.status(player).canAct()) return new TurnResult(events);

        List<Card> hand = state.hand(player);

        // Calculate probability of drawing a safe card
        double successProb = probabilityCalculator.calculateSuccessProbability(
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
            state.setStatus(player, PlayerStatus.STAYED);
            events.add(new TurnEvent.PlayerStayed(player));
            return new TurnResult(events);
        }

        // Player chose to HIT
        drawAndResolve(player, events);
        return new TurnResult(events);
    }

    private void drawAndResolve(Player player, List<TurnEvent> events) {
        if (!state.status(player).canAct()) return;

        List<Card> hand = state.hand(player);
        Card drawn;
        try {
            drawn = deck.draw();
        } catch (DeckEmptyException e) {
            events.add(new TurnEvent.DeckEmpty());
            return;
        }
        events.add(new TurnEvent.CardDrawn(player, drawn));

        if (drawn.type() == CardType.NUMBER) {
            boolean isDuplicate = hasNumberValue(hand, drawn.value());

            if (isDuplicate) {
                // Check for Second Chance card to save the player
                if (consumeSecondChanceIfAvailable(hand)) {
                    events.add(new TurnEvent.SecondChanceConsumed(player, drawn));
                    deck.discardAll(List.of(drawn));
                } else {
                    hand.add(drawn); // Add to hand to show the duplicate
                    state.setStatus(player, PlayerStatus.BUSTED);
                    events.add(new TurnEvent.PlayerBusted(player, drawn));
                }
                return;
            }

            hand.add(drawn);
            return;
        }

        // Handle Action Cards
        hand.add(drawn);
        applyActionCard(player, drawn, events);
    }

    private void applyActionCard(Player actor, Card actionCard, List<TurnEvent> events) {
        CardEffect effect = cardEffects.get(actionCard.type());
        if (effect != null) {
            effect.apply(this, actor, actionCard, events);
        }
    }

    // Made public for CardEffect implementations
    public void forceDraw(Player target, List<TurnEvent> events) {
        for (int i = 0; i < FLIP_THREE_COUNT; i++) {
            if (!state.status(target).canAct()) return;
            drawAndResolve(target, events);
        }
    }

    // Made public for CardEffect implementations
    public Optional<Player> selectTarget(Player actor, CardType actionType) {
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

    // Made public for CardEffect implementations
    public void setStatus(Player player, PlayerStatus status) {
        state.setStatus(player, status);
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
