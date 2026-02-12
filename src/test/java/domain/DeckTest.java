package domain;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.Deck;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void draw_fromPresetDeck_returnsCardsInOrder_andDecreasesSize() {
        // Use a preset deck to avoid shuffle randomness in tests.
        Deck deck = new Deck(List.of(
                new Card(1, CardType.NUMBER),
                new Card(2, CardType.NUMBER),
                new Card(0, CardType.SECOND_CHANCE)
        ));

        assertEquals(3, deck.drawPileSize());

        assertEquals(new Card(1, CardType.NUMBER), deck.draw());
        assertEquals(2, deck.drawPileSize());

        assertEquals(new Card(2, CardType.NUMBER), deck.draw());
        assertEquals(1, deck.drawPileSize());

        assertEquals(new Card(0, CardType.SECOND_CHANCE), deck.draw());
        assertEquals(0, deck.drawPileSize());
    }

    @Test
    void viewDrawPile_isUnmodifiable() {
        Deck deck = new Deck(List.of(new Card(7, CardType.NUMBER)));

        List<Card> view = deck.viewDrawPile();
        assertThrows(UnsupportedOperationException.class, () -> view.add(new Card(1, CardType.NUMBER)));
    }

    @Test
    void draw_whenDrawPileEmpty_butDiscardHasCards_refillsAndDrawsOneOfThoseCards() {
        Deck deck = new Deck(List.of()); // empty draw pile
        Card c1 = new Card(3, CardType.NUMBER);
        Card c2 = new Card(0, CardType.FREEZE);

        // Cards go to discard; Deck should recycle them when draw pile is empty.
        deck.discardAll(List.of(c1, c2));

        Card drawn = deck.draw();

        // Refill may shuffle, so we only check membership, not exact order.
        assertTrue(Set.of(c1, c2).contains(drawn));
        assertEquals(1, deck.drawPileSize()); // 2 recycled - 1 drawn = 1 remaining
    }

    @Test
    void draw_whenNoCardsAnywhere_throws() {
        Deck deck = new Deck(List.of());
        assertThrows(IllegalStateException.class, deck::draw);
    }

    @Test
    void discardAll_acceptsEmptyList_andDoesNotThrow() {
        Deck deck = new Deck(List.of(new Card(5, CardType.NUMBER)));
        assertDoesNotThrow(() -> deck.discardAll(List.of()));
    }
}
