package player;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;
import com.flavia.player.ConsolePlayer;
import com.flavia.player.TargetInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsolePlayerTest {

    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() {
        // No setup needed for System.in here, done per test
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
    }

    @Test
    void decide_returnsHit_onInputH() {
        provideInput("h\n");
        ConsolePlayer player = new ConsolePlayer("User");
        
        TurnInfo info = new TurnInfo(List.of(), 10, 0.5, 10, List.of());
        assertEquals(PlayerAction.HIT, player.decide(info));
    }

    @Test
    void decide_returnsStay_onInputS() {
        provideInput("s\n");
        ConsolePlayer player = new ConsolePlayer("User");
        
        TurnInfo info = new TurnInfo(List.of(), 10, 0.5, 10, List.of());
        assertEquals(PlayerAction.STAY, player.decide(info));
    }

    @Test
    void decide_loopsUntilValidInput() {
        // First "x" (invalid), then "stay" (valid)
        provideInput("x\nstay\n");
        ConsolePlayer player = new ConsolePlayer("User");
        
        TurnInfo info = new TurnInfo(List.of(), 10, 0.5, 10, List.of());
        assertEquals(PlayerAction.STAY, player.decide(info));
    }

    @Test
    void chooseTarget_returnsCorrectName_onValidIndex() {
        // Input "2" selects the second target ("Bob")
        provideInput("2\n");
        ConsolePlayer player = new ConsolePlayer("User");
        
        TargetInfo info = new TargetInfo(CardType.FREEZE, "User", List.of("Alice", "Bob", "Charlie"));
        assertEquals("Bob", player.chooseTarget(info));
    }

    @Test
    void chooseTarget_loopsUntilValidNumber() {
        // "abc" (invalid), "0" (invalid index), "4" (out of bounds), "1" (valid -> Alice)
        provideInput("abc\n0\n4\n1\n");
        ConsolePlayer player = new ConsolePlayer("User");
        
        TargetInfo info = new TargetInfo(CardType.FREEZE, "User", List.of("Alice", "Bob"));
        assertEquals("Alice", player.chooseTarget(info));
    }

    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }
}
