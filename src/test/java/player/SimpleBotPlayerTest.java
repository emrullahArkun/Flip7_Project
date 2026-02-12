package player;

import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;
import com.flavia.player.SimpleBotPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleBotPlayerTest {

    @Test
    void decide_hits_whenPointsBelowLimit() {
        SimpleBotPlayer bot = new SimpleBotPlayer("Bot", 50);
        
        // 40 points < 50 limit -> HIT
        TurnInfo info = new TurnInfo(List.of(), 40, 0.5, 10, List.of());
        
        assertEquals(PlayerAction.HIT, bot.decide(info));
    }

    @Test
    void decide_stays_whenPointsAtOrAboveLimit() {
        SimpleBotPlayer bot = new SimpleBotPlayer("Bot", 50);
        
        // 50 points >= 50 limit -> STAY
        TurnInfo info = new TurnInfo(List.of(), 50, 0.5, 10, List.of());
        assertEquals(PlayerAction.STAY, bot.decide(info));

        // 60 points > 50 limit -> STAY
        TurnInfo info2 = new TurnInfo(List.of(), 60, 0.5, 10, List.of());
        assertEquals(PlayerAction.STAY, bot.decide(info2));
    }

    @Test
    void getName_returnsCorrectName() {
        SimpleBotPlayer bot = new SimpleBotPlayer("MyBot", 10);
        assertEquals("MyBot", bot.getName());
    }
}
