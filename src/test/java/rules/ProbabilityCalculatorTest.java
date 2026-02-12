package rules;


import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.rules.ProbabilityCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProbabilityCalculatorTest {

    @Test
    void successProbability_emptyDeck_isZero() {
        double p = ProbabilityCalculator.calculateSuccessProbability(
                List.of(new Card(5, CardType.NUMBER)),
                List.of()
        );

        assertEquals(0.0, p);
    }

    @Test
    void successProbability_secondChanceInHand_isAlwaysOne() {
        // With Second Chance available, the next duplicate can be ignored once.
        double p = ProbabilityCalculator.calculateSuccessProbability(
                List.of(new Card(0, CardType.SECOND_CHANCE), new Card(5, CardType.NUMBER)),
                List.of(new Card(5, CardType.NUMBER), new Card(5, CardType.NUMBER))
        );

        assertEquals(1.0, p);
    }

    @Test
    void successProbability_countsOnlyDuplicateNumberCards_asBustRisk() {
        // Hand has {5,7}. Deck has two 5s => 2 "bad" cards out of 4 total.
        double p = ProbabilityCalculator.calculateSuccessProbability(
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

    @Test
    void successProbability_noDuplicatesPossible_isOne() {
        double p = ProbabilityCalculator.calculateSuccessProbability(
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
