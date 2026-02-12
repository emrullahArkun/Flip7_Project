package player;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;
import com.flavia.player.ConsolePlayer;
import com.flavia.player.TargetInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsolePlayerTest {

    @Test
    void decide_returnsHit_onInputH() {
        Scanner scanner = createScanner("h\n");
        ConsolePlayer player = new ConsolePlayer("User", scanner);

        TurnInfo info = new TurnInfo(List.of(), 10, 0.5, 10, List.of());
        assertEquals(PlayerAction.HIT, player.decide(info));
    }

    @Test
    void decide_returnsStay_onInputS() {
        Scanner scanner = createScanner("s\n");
        ConsolePlayer player = new ConsolePlayer("User", scanner);

        TurnInfo info = new TurnInfo(List.of(), 10, 0.5, 10, List.of());
        assertEquals(PlayerAction.STAY, player.decide(info));
    }

    @Test
    void decide_loopsUntilValidInput() {
        // First "x" (invalid), then "stay" (valid)
        Scanner scanner = createScanner("x\nstay\n");
        ConsolePlayer player = new ConsolePlayer("User", scanner);

        TurnInfo info = new TurnInfo(List.of(), 10, 0.5, 10, List.of());
        assertEquals(PlayerAction.STAY, player.decide(info));
    }

    @Test
    void chooseTarget_returnsCorrectName_onValidIndex() {
        // Input "2" selects the second target ("Bob")
        Scanner scanner = createScanner("2\n");
        ConsolePlayer player = new ConsolePlayer("User", scanner);

        TargetInfo info = new TargetInfo(CardType.FREEZE, "User", List.of("Alice", "Bob", "Charlie"));
        assertEquals("Bob", player.chooseTarget(info));
    }

    @Test
    void chooseTarget_loopsUntilValidNumber() {
        // "abc" (invalid), "0" (invalid index), "4" (out of bounds), "1" (valid -> Alice)
        Scanner scanner = createScanner("abc\n0\n4\n1\n");
        ConsolePlayer player = new ConsolePlayer("User", scanner);

        TargetInfo info = new TargetInfo(CardType.FREEZE, "User", List.of("Alice", "Bob"));
        assertEquals("Alice", player.chooseTarget(info));
    }

    private Scanner createScanner(String data) {
        return new Scanner(new ByteArrayInputStream(data.getBytes()));
    }
}