package com.flavia.player;

import com.flavia.domain.enums.PlayerAction;
import com.flavia.domain.model.TurnInfo;

/**
 * Player interface: defines ONLY the decision logic.
 *
 * Decision:
 * - Engine (Dealer) asks the Player: "HIT or STAY?"
 * - Player receives a snapshot (TurnInfo).
 * - No engine methods here -> Player cannot manipulate the flow.
 */
public interface Player {

    PlayerAction decide(TurnInfo turnInfo);

    String getName();

    default String chooseTarget(TargetInfo info) {
        return info.eligibleTargetNames().getFirst();
    }

}
