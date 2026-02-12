package com.flavia.player;

import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;

/**
 * Simple Bot: Hits until a point limit is reached, then stays.
 *
 * Decision:
 * - Bot logic belongs in the player package, not in Main or Engine.
 * - The engine remains generic: it only knows the Player interface.
 */
public class SimpleBotPlayer implements Player {

    private final String name;
    private final int hitUntilPoints;

    public SimpleBotPlayer(String name, int hitUntilPoints) {
        this.name = name;
        this.hitUntilPoints = hitUntilPoints;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayerAction decide(TurnInfo info) {
        // Simple strategy: hit until the score limit is reached
        if (info.currentPoints() < hitUntilPoints) {
            System.out.println(name + " hits.");
            return PlayerAction.HIT;
        }
        System.out.println(name + " stays.");
        return PlayerAction.STAY;
    }
}
