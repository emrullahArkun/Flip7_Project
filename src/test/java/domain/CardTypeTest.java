package domain;

import com.flavia.domain.enums.CardType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTypeTest {

    @Test
    void isNumber_returnsTrueOnlyForNumberType() {
        assertTrue(CardType.NUMBER.isNumber());
        assertFalse(CardType.FREEZE.isNumber());
        assertFalse(CardType.FLIP_THREE.isNumber());
        assertFalse(CardType.SECOND_CHANCE.isNumber());
    }

    @Test
    void isAction_returnsTrueForNonNumberTypes() {
        assertFalse(CardType.NUMBER.isAction());
        assertTrue(CardType.FREEZE.isAction());
        assertTrue(CardType.FLIP_THREE.isAction());
        assertTrue(CardType.SECOND_CHANCE.isAction());
    }
}
