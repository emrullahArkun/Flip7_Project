package engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.Deck;
import com.flavia.domain.model.TurnInfo;
import com.flavia.engine.PlayerStatus;
import com.flavia.engine.RoundState;
import com.flavia.engine.TurnProcessor;
import com.flavia.engine.TurnResult;
import com.flavia.engine.TurnEvent;
import com.flavia.player.Player;
import com.flavia.player.TargetInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnProcessorTest {

    // Tests if Second Chance card is consumed to ignore a duplicate, keeping player active
    @Test
    void secondChance_isConsumed_andDuplicateIsIgnored_statusStaysActive() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.SECOND_CHANCE),
                new Card(5, CardType.NUMBER),
                new Card(5, CardType.NUMBER) // duplicate
        ));

        Player p1 = new HitPlayer("P1");

        RoundState state = new RoundState();
        List<Player> players = List.of(p1);
        state.initRound(players);

        TurnProcessor tp = new TurnProcessor(deck, players, state);

        // Draw SECOND_CHANCE
        tp.processTurn(p1);
        assertEquals(PlayerStatus.ACTIVE, state.status(p1));
        assertTrue(state.hand(p1).stream().anyMatch(c -> c.type() == CardType.SECOND_CHANCE));

        // Draw NUMBER 5
        tp.processTurn(p1);
        assertTrue(state.hand(p1).stream().anyMatch(c -> c.type() == CardType.NUMBER && c.value() == 5));

        // Draw duplicate NUMBER 5 -> should consume Second Chance and ignore the duplicate
        TurnResult result = tp.processTurn(p1);

        assertEquals(PlayerStatus.ACTIVE, state.status(p1));

        // Verify event
        assertTrue(result.events().stream().anyMatch(e -> e instanceof TurnEvent.SecondChanceConsumed));

        // Second Chance should be removed from hand after consumption
        assertFalse(state.hand(p1).stream().anyMatch(c -> c.type() == CardType.SECOND_CHANCE));

        // Only one "5" should remain in hand (duplicate ignored)
        long fives = state.hand(p1).stream()
                .filter(c -> c.type() == CardType.NUMBER && c.value() == 5)
                .count();
        assertEquals(1, fives);
    }

    // Tests if drawing a duplicate without a Second Chance card busts the player
    @Test
    void duplicateWithoutSecondChance_bustsPlayer() {
        Deck deck = new Deck(List.of(
                new Card(5, CardType.NUMBER),
                new Card(5, CardType.NUMBER)
        ));

        Player p1 = new HitPlayer("P1");

        RoundState state = new RoundState();
        List<Player> players = List.of(p1);
        state.initRound(players);

        TurnProcessor tp = new TurnProcessor(deck, players, state);

        tp.processTurn(p1); // first 5
        assertEquals(PlayerStatus.ACTIVE, state.status(p1));

        TurnResult result = tp.processTurn(p1); // duplicate 5 -> bust
        assertEquals(PlayerStatus.BUSTED, state.status(p1));
        assertTrue(result.events().stream().anyMatch(e -> e instanceof TurnEvent.PlayerBusted));
    }

    // Tests if FREEZE card sets target to FROZEN and prevents them from acting
    @Test
    void freeze_setsTargetToFrozen_andTargetDoesNotGetTurn() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FREEZE) // actor draws FREEZE
        ));

        Player actor = new TargetingHitPlayer("A", "B");
        ProbePlayer target = new ProbePlayer("B");

        RoundState state = new RoundState();
        List<Player> players = List.of(actor, target);
        state.initRound(players);

        TurnProcessor tp = new TurnProcessor(deck, players, state);

        TurnResult result = tp.processTurn(actor);

        assertEquals(PlayerStatus.FROZEN, state.status(target));
        assertTrue(result.events().stream().anyMatch(e -> e instanceof TurnEvent.PlayerFrozen));

        // Frozen players must not act; processTurn should return early without calling decide().
        tp.processTurn(target);
        assertEquals(0, target.decideCalls);
    }

    // Tests if FLIP_THREE forces the target to draw three cards in order
    @Test
    void flipThree_forcesTargetToDrawThreeCards_inOrder() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FLIP_THREE), // actor draws FLIP_THREE
                new Card(1, CardType.NUMBER),
                new Card(2, CardType.NUMBER),
                new Card(3, CardType.NUMBER)
        ));

        Player actor = new TargetingHitPlayer("A", "B");
        Player target = new HitPlayer("B"); // decide() won't be called during forced draws

        RoundState state = new RoundState();
        List<Player> players = List.of(actor, target);
        state.initRound(players);

        TurnProcessor tp = new TurnProcessor(deck, players, state);

        TurnResult result = tp.processTurn(actor);

        assertEquals(3, state.hand(target).stream().filter(c -> c.type() == CardType.NUMBER).count());
        assertEquals(List.of(
                new Card(1, CardType.NUMBER),
                new Card(2, CardType.NUMBER),
                new Card(3, CardType.NUMBER)
        ), state.hand(target));
        
        assertTrue(result.events().stream().anyMatch(e -> e instanceof TurnEvent.ActionCardPlayed));
    }

    // Tests if FREEZE card targets self when no other players are active
    @Test
    void freeze_targetsSelf_ifNoOtherTargetsAreActive() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FREEZE)
        ));

        Player actor = new HitPlayer("A");
        // B is already busted, so not a valid target
        Player target = new HitPlayer("B");

        RoundState state = new RoundState();
        List<Player> players = List.of(actor, target);
        state.initRound(players);
        state.setStatus(target, PlayerStatus.BUSTED);

        TurnProcessor tp = new TurnProcessor(deck, players, state);

        tp.processTurn(actor);

        // Actor should be FROZEN because they are the only active player
        assertEquals(PlayerStatus.FROZEN, state.status(actor));
        assertEquals(PlayerStatus.BUSTED, state.status(target));
    }

    // Tests if target selection defaults to first eligible player if invalid name chosen
    @Test
    void selectTarget_defaultsToFirstEligible_ifPlayerChoosesInvalidName() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FREEZE)
        ));

        // Actor chooses "INVALID_NAME", should default to "B"
        Player actor = new TargetingHitPlayer("A", "INVALID_NAME");
        Player target = new HitPlayer("B");

        RoundState state = new RoundState();
        List<Player> players = List.of(actor, target);
        state.initRound(players);

        TurnProcessor tp = new TurnProcessor(deck, players, state);

        tp.processTurn(actor);

        assertEquals(PlayerStatus.FROZEN, state.status(target));
    }

    // Tests if FLIP_THREE stops drawing if the target busts midway
    @Test
    void flipThree_stopsDrawing_ifTargetBustsMidway() {
        Deck deck = new Deck(List.of(
                new Card(0, CardType.FLIP_THREE),
                new Card(5, CardType.NUMBER),
                new Card(5, CardType.NUMBER), // Duplicate -> Bust
                new Card(1, CardType.NUMBER)  // Should NOT be drawn
        ));

        Player actor = new TargetingHitPlayer("A", "B");
        Player target = new HitPlayer("B");

        RoundState state = new RoundState();
        List<Player> players = List.of(actor, target);
        state.initRound(players);

        TurnProcessor tp = new TurnProcessor(deck, players, state);

        tp.processTurn(actor);

        assertEquals(PlayerStatus.BUSTED, state.status(target));
        // Should have 2 cards (5 and 5), not 3
        assertEquals(2, state.hand(target).size());
    }

    // --- Test helpers ---

    private static class HitPlayer implements Player {
        private final String name;

        HitPlayer(String name) { this.name = name; }

        @Override public PlayerAction decide(TurnInfo turnInfo) { return PlayerAction.HIT; }
        @Override public String getName() { return name; }
    }

    private static class TargetingHitPlayer extends HitPlayer {
        private final String targetName;

        TargetingHitPlayer(String name, String targetName) {
            super(name);
            this.targetName = targetName;
        }

        @Override
        public String chooseTarget(TargetInfo info) {
            return targetName;
        }
    }

    private static class ProbePlayer implements Player {
        private final String name;
        int decideCalls = 0;

        ProbePlayer(String name) { this.name = name; }

        @Override
        public PlayerAction decide(TurnInfo turnInfo) {
            decideCalls++;
            return PlayerAction.STAY;
        }

        @Override
        public String getName() { return name; }
    }
}
