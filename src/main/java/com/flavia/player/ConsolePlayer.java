package com.flavia.player;

import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;

import java.util.Scanner;

public class ConsolePlayer implements Player {

    private final String name;
    private final Scanner scanner;

    public ConsolePlayer(String name) {
        this.name = name;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayerAction decide(TurnInfo info) {
        System.out.println("\n--- " + name + " ist am Zug ---");
        System.out.println("Deine Karten: " + info.myCards());
        System.out.println("Aktuelle Punkte: " + info.currentPoints());
        System.out.printf("Erfolgswahrscheinlichkeit: %.1f%%%n", info.successProbability() * 100);
        System.out.println("Karten im Deck: " + info.cardsRemainingInDeck());

        if (!info.securedPlayerNames().isEmpty()) {
            System.out.println("Aktive Spieler: " + info.securedPlayerNames());
        }

        while (true) {
            System.out.print("Entscheidung: (h)it oder (s)tay? ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("h") || input.equals("hit")) {
                return PlayerAction.HIT;
            }
            if (input.equals("s") || input.equals("stay")) {
                return PlayerAction.STAY;
            }

            System.out.println("Bitte 'h'/'hit' oder 's'/'stay' eingeben.");
        }
    }

    @Override
    public String chooseTarget(TargetInfo info) {
        var targets = info.eligibleTargetNames();
        System.out.println("\nAktionskarte: " + info.actionCard() + " (von " + info.actorName() + ")");
        for (int i = 0; i < targets.size(); i++) {
            System.out.println((i + 1) + ") " + targets.get(i));
        }

        while (true) {
            System.out.print("Wähle Zielspieler (1-" + targets.size() + "): ");
            String input = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < targets.size()) return targets.get(idx);
            } catch (NumberFormatException ignored) {}
            System.out.println("Ungültig. Bitte Zahl eingeben.");
        }
    }

}
