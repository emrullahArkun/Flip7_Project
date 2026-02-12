package com.flavia;

import com.flavia.domain.model.Deck;
import com.flavia.engine.GameEngine;
import com.flavia.player.ConsolePlayer;
import com.flavia.player.Player;
import com.flavia.player.SimpleBotPlayer;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1) Spieler erstellen
        List<Player> players = new ArrayList<>();
        players.add(new ConsolePlayer("Human"));
        players.add(new SimpleBotPlayer("Test-Bot", 15));

        // 2) Engine starten
        GameEngine game = new GameEngine(players, new Deck(),200);

        // 3) Spiel-Loop (Runden)
        while (true) {
            var winner = game.playRound();
            System.out.println("Round end");

            if (winner.isPresent()) {
                System.out.println("Winner: " + winner.get().getName());
                break;
            }

            // Optional: Pause für Lesbarkeit
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
