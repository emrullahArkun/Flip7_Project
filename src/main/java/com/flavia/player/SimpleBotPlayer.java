package com.flavia.player;

import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;

/**
 * Einfacher Bot: zieht bis zu einem Punktelimit, sonst bleibt er stehen.
 *
 * Entscheidung:
 * - Bot-Logik gehört in player/, nicht in Main oder Engine.
 * - Engine bleibt generisch: sie kennt nur das Player-Interface.
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
        if (info.currentPoints() < hitUntilPoints) {
            System.out.println(name + " zieht eine Karte.");
            return PlayerAction.HIT;
        }
        System.out.println(name + " bleibt stehen.");
        return PlayerAction.STAY;
    }
}
