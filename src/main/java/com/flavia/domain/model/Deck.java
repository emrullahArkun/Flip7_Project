package com.flavia.domain.model;

import com.flavia.domain.enums.CardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages draw and discard piles.
 */
public class Deck {

    private final List<Card> drawPile;
    private final List<Card> discardPile;

    public Deck() {
        this.drawPile = initializeCards();
        this.discardPile = new ArrayList<>();
        shuffle();
    }

    public Deck(List<Card> presetDrawPile) {
        this.drawPile = new ArrayList<>(presetDrawPile);
        this.discardPile = new ArrayList<>();
    }

    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    /**
     * Draws the top card. Refills from discard pile if empty.
     */
    public Card draw() {
        if (drawPile.isEmpty()) {
            refillFromDiscard();
        }
        if (drawPile.isEmpty()) {
            throw new IllegalStateException("No cards left in the deck!");
        }
        return drawPile.removeFirst();
    }

    public void discardAll(List<Card> cards) {
        discardPile.addAll(cards);
    }

    public int drawPileSize() {
        return drawPile.size();
    }

    /**
     * Returns an unmodifiable view of the draw pile for probability calculations.
     */
    public List<Card> viewDrawPile() {
        return Collections.unmodifiableList(drawPile);
    }

    private void refillFromDiscard() {
        if (discardPile.isEmpty()) {
            return;
        }
        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }

    /**
     * Initializes the deck:
     * - Values 0-11: value+1 copies
     * - Value 12: 12 copies
     * - 3 copies of each special card
     */
    private List<Card> initializeCards() {
        List<Card> cardList = new ArrayList<>();

        for (int value = 0; value <= 12; value++) {
            int count = (value == 12) ? 12 : value + 1;
            for (int i = 0; i < count; i++) {
                cardList.add(new Card(value, CardType.NUMBER));
            }
        }

        addSpecialCards(cardList, CardType.FREEZE);
        addSpecialCards(cardList, CardType.FLIP_THREE);
        addSpecialCards(cardList, CardType.SECOND_CHANCE);

        return cardList;
    }

    private void addSpecialCards(List<Card> list, CardType type) {
        for (int i = 0; i < 3; i++) {
            list.add(new Card(0, type));
        }
    }
}
