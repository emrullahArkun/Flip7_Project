package com.flavia.player;

import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;

/**
 * Player interface: defines ONLY the decision logic.
 *
 * Decision:
 * - Engine asks: "HIT or STAY?"
 * - Player gets snapshot (TurnInfo).
 * - No engine manipulation allowed.
 */
public interface Player {

    PlayerAction decide(TurnInfo turnInfo);

    String getName();

    // Default implementation picks the first available target
    default String chooseTarget(TargetInfo info) {
        return info.eligibleTargetNames().getFirst();
    }

}
