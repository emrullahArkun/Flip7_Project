package com.flavia.engine;

import com.flavia.domain.model.Card;
import com.flavia.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundState {

    // Tracks cards held by each player in the current round
    private final Map<Player, List<Card>> hands = new HashMap<>();
    // Tracks current status (ACTIVE, BUSTED, STAYED, etc.)
    private final Map<Player, PlayerStatus> status = new HashMap<>();

    public void initRound(List<Player> players) {
        // Reset state for all players at start of round
        for (Player p : players) {
            status.put(p, PlayerStatus.ACTIVE);
            hands.put(p, new ArrayList<>());
        }
    }

    public List<Card> hand(Player player) {
        return hands.get(player);
    }

    public PlayerStatus status(Player player) {
        return status.get(player);
    }

    public void setStatus(Player player, PlayerStatus newStatus) {
        status.put(player, newStatus);
    }

    public boolean hasActivePlayers(List<Player> players) {
        // Check if any player can still make a move
        for (Player p : players) {
            if (status(p).canAct()) return true;
        }
        return false;
    }

    public List<String> securedPlayerNames(List<Player> players) {
        // Returns names of players who have secured their points (Stayed or Frozen)
        List<String> secured = new ArrayList<>();
        for (Player p : players) {
            PlayerStatus s = status(p);
            if (s == PlayerStatus.STAYED || s == PlayerStatus.FROZEN) {
                secured.add(p.getName());
            }
        }
        return secured;
    }

    public List<Card> collectAllPlayedCards(List<Player> players) {
        // Gather all cards from hands to return to deck/discard
        List<Card> all = new ArrayList<>();
        for (Player p : players) {
            all.addAll(hand(p));
            hand(p).clear();
        }
        return all;
    }
}
