package com.flavia.engine;

import com.flavia.domain.model.Deck;
import com.flavia.player.Player;

import java.util.List;
import java.util.Optional;

public class GameEngine {

    private final Deck deck;
    private final List<Player> players;

    private final RoundState roundState = new RoundState();
    private final TurnProcessor turnProcessor;
    private final ScoreBoard scoreBoard;

    public GameEngine(List<Player> players) {
        this(players, new Deck(), 200);
    }

    public GameEngine(List<Player> players, Deck deck, int targetScore) {
        this.deck = deck;
        this.players = List.copyOf(players);
        this.turnProcessor = new TurnProcessor(this.deck, this.players, roundState);
        this.scoreBoard = new ScoreBoard(this.players, targetScore);
    }

    public Optional<Player> playRound() {
        System.out.println("\n--- NEW ROUND STARTING ---");
        roundState.initRound(players);

        while (roundState.hasActivePlayers(players)) {
            for (Player player : players) {
                turnProcessor.processTurn(player);
            }
        }

        Optional<Player> winner = scoreBoard.scoreRound(players, roundState);

        // Collect cards + discard (to keep deck cyclic)
        deck.discardAll(roundState.collectAllPlayedCards(players));

        return winner;
    }
}
