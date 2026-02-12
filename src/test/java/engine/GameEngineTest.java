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

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    @Test
    void freeze_stopsTargetImmediately_andTargetIsNotAskedToDecide() {
        // Deck: actor draws FREEZE first.
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FREEZE)
        ));

        ScriptedPlayer actor = new ScriptedPlayer("A", "B",
                PlayerAction.HIT, PlayerAction.STAY
        );
        ProbeStayPlayer target = new ProbeStayPlayer("B");

        GameEngine engine = new GameEngine(List.of(actor, target), deck, 9999);

        engine.playRound();

        // Target should be frozen before their turn; decide() must not be called.
        assertEquals(0, target.decideCalls);
    }

    @Test
    void flipThree_forcesTargetToDrawThree_andTargetSeesThoseCardsOnItsTurn() {
        // Deck order:
        // A draws FLIP_THREE, then B gets 1,2,3 as forced draws.
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

        GameEngine engine = new GameEngine(List.of(actor, target), deck, 9999);

        engine.playRound();

        assertNotNull(target.lastInfo);
        assertEquals(List.of(
                new Card(1, CardType.NUMBER),
                new Card(2, CardType.NUMBER),
                new Card(3, CardType.NUMBER)
        ), target.lastInfo.myCards());
    }

    @Test
    void deck_recyclesDiscardAcrossRounds_withoutCrashing() {
        // Only 2 cards exist -> after round 1 draw pile is empty, discard has 2 cards.
        // Round 2 must refill from discard and still allow two draws.
        Deck deck = new Deck(List.of(
                new Card(4, CardType.NUMBER),
                new Card(9, CardType.NUMBER)
        ));

        // Each player: HIT once, then STAY (repeat for 2 rounds => 4 actions).
        ScriptedPlayer p1 = new ScriptedPlayer("P1", "P2",
                PlayerAction.HIT, PlayerAction.STAY, PlayerAction.HIT, PlayerAction.STAY
        );
        ScriptedPlayer p2 = new ScriptedPlayer("P2", "P1",
                PlayerAction.HIT, PlayerAction.STAY, PlayerAction.HIT, PlayerAction.STAY
        );

        GameEngine engine = new GameEngine(List.of(p1, p2), deck, 9999);

        assertDoesNotThrow(engine::playRound);
        assertEquals(0, deck.drawPileSize()); // both cards drawn in round 1

        assertDoesNotThrow(engine::playRound);
        assertEquals(0, deck.drawPileSize()); // recycled cards drawn in round 2
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
