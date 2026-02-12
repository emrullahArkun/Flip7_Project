package com.flavia.player;

import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;

import java.util.Scanner;

public class ConsolePlayer implements Player {

    private final String name;
    private final Scanner scanner;

    public ConsolePlayer(String name, Scanner scanner) {
        this.name = name;
        this.scanner = scanner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayerAction decide(TurnInfo info) {
        // Display current turn information to the user
        System.out.println("\n--- " + name + "'s Turn ---");
        System.out.println("Your Cards: " + info.myCards());
        System.out.println("Current Points: " + info.currentPoints());
        System.out.printf("Success Probability: %.1f%%%n", info.successProbability() * 100);
        System.out.println("Cards remaining in Deck: " + info.cardsRemainingInDeck());

        if (!info.securedPlayerNames().isEmpty()) {
            System.out.println("Secured Players: " + info.securedPlayerNames());
        }

        // Loop until valid input is received
        while (true) {
            System.out.print("Decision: (h)it or (s)tay? ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("h") || input.equals("hit")) {
                return PlayerAction.HIT;
            }
            if (input.equals("s") || input.equals("stay")) {
                return PlayerAction.STAY;
            }

            System.out.println("Please enter 'h'/'hit' or 's'/'stay'.");
        }
    }

    @Override
    public String chooseTarget(TargetInfo info) {
        // Display available targets for the action card
        var targets = info.eligibleTargetNames();
        System.out.println("\nAction Card: " + info.actionCard() + " (from " + info.actorName() + ")");
        for (int i = 0; i < targets.size(); i++) {
            System.out.println((i + 1) + ") " + targets.get(i));
        }

        // Loop until valid target selection
        while (true) {
            System.out.print("Choose target player (1-" + targets.size() + "): ");
            String input = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < targets.size()) return targets.get(idx);
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid. Please enter a number.");
        }
    }

}
