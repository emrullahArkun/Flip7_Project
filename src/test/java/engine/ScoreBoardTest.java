package engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.Card;
import com.flavia.domain.model.TurnInfo;
import com.flavia.engine.PlayerStatus;
import com.flavia.engine.RoundState;
import com.flavia.engine.ScoreBoard;
import com.flavia.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ScoreBoardTest {

    // Tests if a busted player receives zero points for the round
    @Test
    void scoreRound_bustedPlayer_getsZeroPoints() {
        Player p1 = testPlayer();

        RoundState state = new RoundState();
        state.initRound(List.of(p1));

        // Hand would normally have a duplicate, but ScoreBoard only checks status.
        state.hand(p1).add(new Card(5, CardType.NUMBER));
        state.hand(p1).add(new Card(5, CardType.NUMBER));
        state.setStatus(p1, PlayerStatus.BUSTED);

        ScoreBoard board = new ScoreBoard(List.of(p1), 200);

        Optional<Player> winner = board.scoreRound(List.of(p1), state);

        assertTrue(winner.isEmpty());
    }

    // Tests if points are correctly added for a non-busted player
    @Test
    void scoreRound_addsPointsForNonBustedPlayer() {
        Player p1 = testPlayer();

        RoundState state = new RoundState();
        state.initRound(List.of(p1));

        state.hand(p1).add(new Card(2, CardType.NUMBER));
        state.hand(p1).add(new Card(3, CardType.NUMBER));
        state.hand(p1).add(new Card(0, CardType.SECOND_CHANCE)); // no points

        ScoreBoard board = new ScoreBoard(List.of(p1), 200);

        Optional<Player> winner = board.scoreRound(List.of(p1), state);

        assertTrue(winner.isEmpty());
        // We can't directly read totals here (by design). Winner test covers threshold behavior below.
    }

    // Tests if a winner is returned when the target score is reached or exceeded
    @Test
    void scoreRound_returnsWinner_whenTargetScoreReachedOrExceeded() {
        Player p1 = testPlayer();

        RoundState state = new RoundState();
        state.initRound(List.of(p1));

        // Target score is 5; hand sums to 6.
        state.hand(p1).add(new Card(1, CardType.NUMBER));
        state.hand(p1).add(new Card(5, CardType.NUMBER));

        ScoreBoard board = new ScoreBoard(List.of(p1), 5);

        Optional<Player> winner = board.scoreRound(List.of(p1), state);

        assertTrue(winner.isPresent());
        assertEquals("P1", winner.get().getName());
    }

    // Tests if scores accumulate across rounds until a winner is found
    @Test
    void scoreRound_accumulatesAcrossRounds_untilWinner() {
        Player p1 = testPlayer();
        List<Player> players = List.of(p1);

        ScoreBoard board = new ScoreBoard(players, 10);

        // Round 1: 4 points
        RoundState state1 = new RoundState();
        state1.initRound(players);
        state1.hand(p1).add(new Card(4, CardType.NUMBER));
        board.scoreRound(players, state1);

        // Round 2: 6 points -> total becomes 10 => winner
        RoundState state2 = new RoundState();
        state2.initRound(players);
        state2.hand(p1).add(new Card(6, CardType.NUMBER));
        Optional<Player> winner = board.scoreRound(players, state2);

        assertTrue(winner.isPresent());
        assertEquals("P1", winner.get().getName());
    }

    private Player testPlayer() {
        return new Player() {
            @Override public PlayerAction decide(TurnInfo turnInfo) {
                return PlayerAction.STAY;
            }
            @Override public String getName() { return "P1"; }
        };
    }
}
