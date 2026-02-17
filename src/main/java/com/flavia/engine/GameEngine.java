package com.flavia.engine;

import com.flavia.domain.model.Deck;
import com.flavia.player.Player;

import java.util.List;
import java.util.Optional;

public class GameEngine {

    private final Deck deck;
    private final List<Player> players;

    // Tracks current round status (active players, pot, etc)
    private final RoundState roundState = new RoundState();
    private final TurnProcessor turnProcessor;
    private final ScoreBoard scoreBoard;
    private final ConsoleGameView view;

    public GameEngine(List<Player> players) {
        // Default game with standard deck and target score 200
        this(players, new Deck(), 200);
    }

    public GameEngine(List<Player> players, Deck deck, int targetScore) {
        if (players.size() < 3 || players.size() > 18) {
            throw new IllegalArgumentException("Number of players must be between 3 and 18. Given: " + players.size());
        }

        this.deck = deck;
        this.players = List.copyOf(players);
        this.turnProcessor = new TurnProcessor(this.deck, this.players, roundState);
        this.scoreBoard = new ScoreBoard(this.players, targetScore);
        this.view = new ConsoleGameView();
    }

    public Optional<Player> playRound() {
        view.displayRoundStart();
        // Reset round state for new round
        roundState.initRound(players);

        // Continue until all players fold or bust
        while (roundState.hasActivePlayers(players)) {
            for (Player player : players) {
                TurnResult result = turnProcessor.processTurn(player);
                view.displayTurnEvents(result.events());
            }
        }

        // Calculate scores and check for game winner
        Optional<Player> winner = scoreBoard.scoreRound(players, roundState);

        // Collect cards + discard (to keep deck cyclic)
        deck.discardAll(roundState.collectAllPlayedCards(players));

        return winner;
    }
}
