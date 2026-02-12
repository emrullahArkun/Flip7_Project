package com.flavia.player;


import com.flavia.domain.enums.CardType;

import java.util.List;

/** Kontext für Zielauswahl bei Aktionskarten. */
public record TargetInfo(
        CardType actionCard,
        String actorName,
        List<String> eligibleTargetNames
) {}
