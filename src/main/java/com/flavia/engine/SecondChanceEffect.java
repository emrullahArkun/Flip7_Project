package com.flavia.engine;

import com.flavia.domain.model.Card;
import com.flavia.player.Player;

import java.util.List;

public class SecondChanceEffect implements CardEffect {
    @Override
    public void apply(TurnProcessor processor, Player actor, Card card, List<TurnEvent> events) {
        // No immediate effect, just acquired
    }
}
