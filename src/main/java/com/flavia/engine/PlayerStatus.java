package com.flavia.engine;

/**
 * Status of a player within ONE round.
 *
 * Decision:
 * - ACTIVE: Player may act
 * - STAYED: Player voluntarily stopped
 * - BUSTED: Player is out (duplicate)
 * - FROZEN: Player was stopped immediately by FREEZE (like STAYED, but reason matters)
 */
public enum PlayerStatus {
    ACTIVE,
    STAYED,
    BUSTED,
    FROZEN;

    public boolean isFinished() {
        return this != ACTIVE;
    }

    public boolean canAct() {
        return this == ACTIVE;
    }
}
