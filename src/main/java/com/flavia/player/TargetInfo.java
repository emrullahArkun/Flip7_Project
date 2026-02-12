package com.flavia.player;


import com.flavia.domain.enums.CardType;

import java.util.List;

/** Context for target selection when playing action cards. */
public record TargetInfo(
        CardType actionCard,
        String actorName,
        List<String> eligibleTargetNames
) {}
