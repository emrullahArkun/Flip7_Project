package engine;


import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.TurnInfo;
import com.flavia.engine.PlayerStatus;
import com.flavia.engine.RoundState;
import com.flavia.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundStateTest {

    // Tests if initializing a round sets all players to ACTIVE and clears hands
    @Test
    void initRound_setsAllPlayersActive_andHandsEmpty() {
        Player p1 = testPlayer("P1");
        Player p2 = testPlayer("P2");

        RoundState state = new RoundState();
        state.initRound(List.of(p1, p2));

        assertEquals(PlayerStatus.ACTIVE, state.status(p1));
        assertEquals(PlayerStatus.ACTIVE, state.status(p2));

        assertTrue(state.hand(p1).isEmpty());
        assertTrue(state.hand(p2).isEmpty());
    }

    // Tests if securedPlayerNames returns only players who STAYED or are FROZEN
    @Test
    void securedPlayerNames_containsOnlyStayedAndFrozen() {
        Player p1 = testPlayer("P1");
        Player p2 = testPlayer("P2");
        Player p3 = testPlayer("P3");
        Player p4 = testPlayer("P4");

        RoundState state = new RoundState();
        List<Player> players = List.of(p1, p2, p3, p4);
        state.initRound(players);

        state.setStatus(p1, PlayerStatus.STAYED);
        state.setStatus(p2, PlayerStatus.FROZEN);
        state.setStatus(p3, PlayerStatus.ACTIVE);
        state.setStatus(p4, PlayerStatus.BUSTED);

        List<String> secured = state.securedPlayerNames(players);

        assertEquals(List.of("P1", "P2"), secured);
    }

    // Tests if collecting all played cards returns them and clears player hands
    @Test
    void collectAllPlayedCards_returnsAllCards_andClearsHands() {
        Player p1 = testPlayer("P1");
        Player p2 = testPlayer("P2");

        RoundState state = new RoundState();
        List<Player> players = List.of(p1, p2);
        state.initRound(players);

        state.hand(p1).add(new Card(1, CardType.NUMBER));
        state.hand(p1).add(new Card(0, CardType.SECOND_CHANCE));
        state.hand(p2).add(new Card(3, CardType.NUMBER));

        List<Card> collected = state.collectAllPlayedCards(players);

        assertEquals(3, collected.size());
        assertTrue(state.hand(p1).isEmpty());
        assertTrue(state.hand(p2).isEmpty());
    }

    // Tests if hasActivePlayers returns true when at least one player can act
    @Test
    void hasActivePlayers_returnsTrue_ifAtLeastOnePlayerCanAct() {
        Player p1 = testPlayer("P1");
        Player p2 = testPlayer("P2");
        RoundState state = new RoundState();
        state.initRound(List.of(p1, p2));

        state.setStatus(p1, PlayerStatus.STAYED);
        state.setStatus(p2, PlayerStatus.ACTIVE);

        assertTrue(state.hasActivePlayers(List.of(p1, p2)));
    }

    // Tests if hasActivePlayers returns false when no players can act
    @Test
    void hasActivePlayers_returnsFalse_ifAllPlayersCannotAct() {
        Player p1 = testPlayer("P1");
        Player p2 = testPlayer("P2");
        RoundState state = new RoundState();
        state.initRound(List.of(p1, p2));

        state.setStatus(p1, PlayerStatus.BUSTED);
        state.setStatus(p2, PlayerStatus.STAYED);

        assertFalse(state.hasActivePlayers(List.of(p1, p2)));
    }

    private Player testPlayer(String name) {
        return new Player() {
            @Override public PlayerAction decide(TurnInfo turnInfo) {
                return PlayerAction.STAY;
            }
            @Override public String getName() { return name; }
        };
    }
}
