package com.flavia.engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.player.Player;

import java.util.List;
import java.util.Optional;

public class FlipThreeEffect implements CardEffect {
    @Override
    public void apply(TurnProcessor processor, Player actor, Card card, List<TurnEvent> events) {
        Optional<Player> targetOpt = processor.selectTarget(actor, CardType.FLIP_THREE);
        if (targetOpt.isEmpty()) {
            return;
        }
        Player target = targetOpt.get();
        events.add(new TurnEvent.ActionCardPlayed(actor, card, target));
        processor.forceDraw(target, events);
    }
}
