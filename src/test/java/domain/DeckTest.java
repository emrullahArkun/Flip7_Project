package domain;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.Deck;
import com.flavia.exceptions.DeckEmptyException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    // Tests if drawing from a preset deck returns cards in order and updates size
    @Test
    void draw_fromPresetDeck_returnsCardsInOrder_andDecreasesSize() throws DeckEmptyException {
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

    // Tests if the view of the draw pile is unmodifiable
    @Test
    void viewDrawPile_isUnmodifiable() {
        Deck deck = new Deck(List.of(new Card(7, CardType.NUMBER)));

        List<Card> view = deck.viewDrawPile();
        assertThrows(UnsupportedOperationException.class, () -> view.add(new Card(1, CardType.NUMBER)));
    }

    // Tests if the deck refills from discard pile when draw pile is empty
    @Test
    void draw_whenDrawPileEmpty_butDiscardHasCards_refillsAndDrawsOneOfThoseCards() throws DeckEmptyException {
        Deck deck = new Deck(List.of()); // empty draw pile
        Card c1 = new Card(3, CardType.NUMBER);
        Card c2 = new Card(0, CardType.FREEZE);

        deck.discardAll(List.of(c1, c2));

        Card drawn = deck.draw();

        assertTrue(Set.of(c1, c2).contains(drawn));
        assertEquals(1, deck.drawPileSize());
    }

    // Tests if drawing from an empty deck (both piles) throws an exception
    @Test
    void draw_whenNoCardsAnywhere_throws() {
        Deck deck = new Deck(List.of());
        assertThrows(DeckEmptyException.class, deck::draw);
    }

    // Tests if discarding an empty list works without error
    @Test
    void discardAll_acceptsEmptyList_andDoesNotThrow() {
        Deck deck = new Deck(List.of(new Card(5, CardType.NUMBER)));
        assertDoesNotThrow(() -> deck.discardAll(List.of()));
    }
}
