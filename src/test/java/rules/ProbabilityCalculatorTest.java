package rules;


import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.rules.DefaultProbabilityCalculator;
import com.flavia.rules.SuccessProbabilityCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProbabilityCalculatorTest {

    private final SuccessProbabilityCalculator calculator = new DefaultProbabilityCalculator();

    // Tests if success probability is 0.0 when the deck is empty
    @Test
    void successProbability_emptyDeck_isZero() {
        double p = calculator.calculateSuccessProbability(
                List.of(new Card(5, CardType.NUMBER)),
                List.of()
        );

        assertEquals(0.0, p);
    }

    // Tests if success probability is 1.0 when a Second Chance card is held
    @Test
    void successProbability_secondChanceInHand_isAlwaysOne() {
        double p = calculator.calculateSuccessProbability(
                List.of(new Card(0, CardType.SECOND_CHANCE), new Card(5, CardType.NUMBER)),
                List.of(new Card(5, CardType.NUMBER), new Card(5, CardType.NUMBER))
        );

        assertEquals(1.0, p);
    }

    // Tests if only duplicate number cards in the deck are counted as bust risks
    @Test
    void successProbability_countsOnlyDuplicateNumberCards_asBustRisk() {
        // Hand has {5,7}. Deck has two 5s => 2 "bad" cards out of 4 total.
        double p = calculator.calculateSuccessProbability(
                List.of(new Card(5, CardType.NUMBER), new Card(7, CardType.NUMBER)),
                List.of(
                        new Card(5, CardType.NUMBER),
                        new Card(5, CardType.NUMBER),
                        new Card(8, CardType.NUMBER),
                        new Card(0, CardType.FREEZE) // specials are safe draws
                )
        );

        assertEquals(0.5, p);
    }

    // Tests if success probability is 1.0 when no duplicates are possible
    @Test
    void successProbability_noDuplicatesPossible_isOne() {
        double p = calculator.calculateSuccessProbability(
                List.of(new Card(2, CardType.NUMBER)),
                List.of(
                        new Card(3, CardType.NUMBER),
                        new Card(4, CardType.NUMBER),
                        new Card(0, CardType.FLIP_THREE)
                )
        );

        assertEquals(1.0, p);
    }
}
