package com.flavia.engine;

import com.flavia.domain.model.Card;
import com.flavia.player.Player;

public sealed interface TurnEvent {
    record CardDrawn(Player player, Card card) implements TurnEvent {}
    record PlayerStayed(Player player) implements TurnEvent {}
    record PlayerBusted(Player player, Card card) implements TurnEvent {}
    record PlayerFrozen(Player target) implements TurnEvent {}
    record SecondChanceConsumed(Player player, Card card) implements TurnEvent {}
    record ActionCardPlayed(Player actor, Card card, Player target) implements TurnEvent {}
    record DeckEmpty() implements TurnEvent {}
}
