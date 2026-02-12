package player;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;
import com.flavia.player.Player;
import com.flavia.player.TargetInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void defaultChooseTarget_returnsFirstEligibleTarget() {
        Player player = new Player() {
            @Override
            public PlayerAction decide(TurnInfo turnInfo) {
                return PlayerAction.STAY;
            }

            @Override
            public String getName() {
                return "TestPlayer";
            }
        };

        TargetInfo info = new TargetInfo(CardType.FREEZE, "TestPlayer", List.of("Target1", "Target2"));
        assertEquals("Target1", player.chooseTarget(info));
    }
}
