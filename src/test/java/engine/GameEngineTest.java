package engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.Deck;
import com.flavia.domain.model.TurnInfo;
import com.flavia.engine.GameEngine;
import com.flavia.player.Player;
import com.flavia.player.TargetInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    // Tests if FREEZE card stops the target immediately without asking for decision
    @Test
    void freeze_stopsTargetImmediately_andTargetIsNotAskedToDecide() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FREEZE)
        ));

        ScriptedPlayer actor = new ScriptedPlayer("A", "B",
                PlayerAction.HIT, PlayerAction.STAY
        );
        ProbeStayPlayer target = new ProbeStayPlayer("B");

        // Dummy player to satisfy GameEngine(min 3 players)
        ProbeStayPlayer filler = new ProbeStayPlayer("C");

        GameEngine engine = new GameEngine(List.of(actor, target, filler), deck, 9999);

        engine.playRound();

        assertEquals(0, target.decideCalls);
    }

    // Tests if FLIP_THREE forces target to draw 3 cards and they appear in hand
    @Test
    void flipThree_forcesTargetToDrawThree_andTargetSeesThoseCardsOnItsTurn() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FLIP_THREE),
                new Card(1, CardType.NUMBER),
                new Card(2, CardType.NUMBER),
                new Card(3, CardType.NUMBER)
        ));

        ScriptedPlayer actor = new ScriptedPlayer("A", "B",
                PlayerAction.HIT, PlayerAction.STAY
        );
        ProbeStayPlayer target = new ProbeStayPlayer("B");

        // Dummy player to satisfy GameEngine(min 3 players)
        ProbeStayPlayer filler = new ProbeStayPlayer("C");

        GameEngine engine = new GameEngine(List.of(actor, target, filler), deck, 9999);

        engine.playRound();

        assertNotNull(target.lastInfo);
        assertEquals(List.of(
                new Card(1, CardType.NUMBER),
                new Card(2, CardType.NUMBER),
                new Card(3, CardType.NUMBER)
        ), target.lastInfo.myCards());
    }

    // Tests if deck recycles discarded cards across rounds correctly
    @Test
    void deck_recyclesDiscardAcrossRounds_withoutCrashing() {
        Deck deck = new Deck(List.of(
                new Card(4, CardType.NUMBER),
                new Card(9, CardType.NUMBER)
        ));

        ScriptedPlayer p1 = new ScriptedPlayer("P1", "P2",
                PlayerAction.HIT, PlayerAction.STAY, PlayerAction.HIT, PlayerAction.STAY
        );
        ScriptedPlayer p2 = new ScriptedPlayer("P2", "P1",
                PlayerAction.HIT, PlayerAction.STAY, PlayerAction.HIT, PlayerAction.STAY
        );

        // Dummy player: must NOT HIT, otherwise you'd need 3 draws with only 2 cards
        ProbeStayPlayer filler = new ProbeStayPlayer("P3");

        GameEngine engine = new GameEngine(List.of(p1, p2, filler), deck, 9999);

        assertDoesNotThrow(engine::playRound);
        assertEquals(0, deck.drawPileSize());

        assertDoesNotThrow(engine::playRound);
        assertEquals(0, deck.drawPileSize());
    }

    // Tests if playRound returns a winner when the score limit is reached
    @Test
    void playRound_returnsWinner_ifScoreLimitReached() {
        Deck deck = new Deck(List.of(
                new Card(10, CardType.NUMBER)
        ));

        ScriptedPlayer p1 = new ScriptedPlayer("P1", "P2",
                PlayerAction.HIT, PlayerAction.STAY
        );

        // Dummy players (STAY), just to satisfy >= 3 players rule
        ProbeStayPlayer filler1 = new ProbeStayPlayer("P2");
        ProbeStayPlayer filler2 = new ProbeStayPlayer("P3");

        // Target score 10, player draws 10 -> wins immediately
        GameEngine engine = new GameEngine(List.of(p1, filler1, filler2), deck, 10);

        Optional<Player> winner = engine.playRound();

        assertTrue(winner.isPresent());
        assertEquals("P1", winner.get().getName());
    }

    // Tests if default constructor creates an engine with default settings
    @Test
    void defaultConstructor_createsEngineWithDefaults() {
        ScriptedPlayer p1 = new ScriptedPlayer("P1", "P2", PlayerAction.STAY);

        // Dummy players (STAY) to satisfy >= 3 players rule
        ProbeStayPlayer filler1 = new ProbeStayPlayer("P2");
        ProbeStayPlayer filler2 = new ProbeStayPlayer("P3");

        GameEngine engine = new GameEngine(List.of(p1, filler1, filler2));

        assertDoesNotThrow(engine::playRound);
    }

    // --- Test helpers ---

    private static class ScriptedPlayer implements Player {
        private final String name;
        private final String targetName;
        private final Deque<PlayerAction> actions = new ArrayDeque<>();

        ScriptedPlayer(String name, String targetName, PlayerAction... actions) {
            this.name = name;
            this.targetName = targetName;
            for (PlayerAction a : actions) this.actions.addLast(a);
        }

        TurnInfo lastInfo = null;

        @Override
        public PlayerAction decide(TurnInfo turnInfo) {
            lastInfo = turnInfo;
            return actions.isEmpty() ? PlayerAction.STAY : actions.removeFirst();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String chooseTarget(TargetInfo info) {
            return targetName;
        }
    }

    private static class ProbeStayPlayer implements Player {
        private final String name;
        int decideCalls = 0;
        TurnInfo lastInfo = null;

        ProbeStayPlayer(String name) {
            this.name = name;
        }

        @Override
        public PlayerAction decide(TurnInfo turnInfo) {
            decideCalls++;
            lastInfo = turnInfo;
            return PlayerAction.STAY;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
