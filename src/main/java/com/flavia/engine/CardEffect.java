package com.flavia.engine;

import com.flavia.domain.model.Card;
import com.flavia.player.Player;

import java.util.List;

public interface CardEffect {
    void apply(TurnProcessor processor, Player actor, Card card, List<TurnEvent> events);
}
