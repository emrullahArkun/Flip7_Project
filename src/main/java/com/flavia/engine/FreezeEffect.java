package com.flavia.engine;

import com.flavia.domain.enums.CardType;
import com.flavia.domain.model.Card;
import com.flavia.player.Player;

import java.util.List;
import java.util.Optional;

public class FreezeEffect implements CardEffect {
    @Override
    public void apply(TurnProcessor processor, Player actor, Card card, List<TurnEvent> events) {
        Optional<Player> targetOpt = processor.selectTarget(actor, CardType.FREEZE);
        if (targetOpt.isEmpty()) {
            // Fallback: if no other active players, target self
            targetOpt = Optional.of(actor);
        }
        Player target = targetOpt.get();
        processor.setStatus(target, PlayerStatus.FROZEN);
        events.add(new TurnEvent.PlayerFrozen(target));
    }
}
