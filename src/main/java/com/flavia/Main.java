package com.flavia;

import com.flavia.domain.model.Deck;
import com.flavia.engine.GameEngine;
import com.flavia.player.ConsolePlayer;
import com.flavia.player.Player;
import com.flavia.player.SimpleBotPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try (Scanner sharedScanner = new Scanner(System.in)) {

            List<Player> players = new ArrayList<>();
            players.add(new ConsolePlayer("Human", sharedScanner));
            players.add(new ConsolePlayer("Animal", sharedScanner));
            players.add(new SimpleBotPlayer("Test-Bot", 15));

            GameEngine game = new GameEngine(players, new Deck(), 200);

            System.out.println("Welcome to the Game! First to 200 points wins.\n");

            while (true) {
                var winner = game.playRound();

                if (winner.isPresent()) {
                    System.out.println("\n################################");
                    System.out.println("   WINNER: " + winner.get().getName());
                    System.out.println("################################");
                    break;
                }

                System.out.println("--- Round over. Starting next round... ---");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}