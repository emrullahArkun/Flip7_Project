# Flip 7 – Software-Architektur

---

## 1. Ziele & Kontext

**Ziele**
- Klare Trennung: **Daten (Domain)**, **Ablauf/Regeln (Engine)**, **Entscheidungen (Player)**
- Gute Testbarkeit durch **deterministisches Deck** (Deck-Injection) und **kleine Bausteine**
- Erweiterbarkeit: neue Bots/Aktionskarten ohne großen Umbau

**Kontext**
- Standalone-Konsolenspiel ohne externe Systeme (keine DB, kein Netzwerk)

---

## 2. Randbedingungen

| Constraint | Wert |
|---|---|
| Java | 21 |
| Build | Maven |
| Tests | JUnit 5, optional JaCoCo Coverage |
| UI | Konsole (stdout / stdin) |

---

## 3. Kontextabgrenzung

**Systemgrenze:** Flip-7 Spielkern + Konsolen-Player.  
**Außerhalb:** keine Persistenz, kein Multiplayer über Netzwerk, keine GUI.

---

## 4. Lösungsstrategie

- **Orchestrator-Pattern:** `GameEngine` hält den Runden-Loop minimal und delegiert.
- **State-Object:** `RoundState` kapselt Handkarten & Spielerstatus der aktuellen Runde.
- **Use-Case/Turn-Logik:** `TurnProcessor` verarbeitet genau einen Zug inkl. Karten-Effekte.
- **Scoring separat:** `ScoreBoard` verwaltet Gesamtscore und Winner-Check.
- **Port/Adapter:** `Player` ist das Interface (Port), konkrete Spieler sind Adapter (Konsole/Bot).

---

## 5. Bausteinsicht

### 5.1 Paketstruktur (Level 1–2)
```
src/main/java/com/flavia/
  Main.java
  domain/
    enums/ (CardType, PlayerAction)
    model/ (Card, Deck, TurnInfo)
  engine/ (GameEngine, TurnProcessor, RoundState, ScoreBoard, PlayerStatus)
  player/ (Player, ConsolePlayer, SimpleBotPlayer, TargetInfo)
  rules/  (ProbabilityCalculator)
```

### 5.2 Verantwortlichkeiten (Tabelle)

| Baustein | Dateien (Beispiele) | Verantwortung |
|---|---|---|
| `domain.enums` | `CardType`, `PlayerAction` | zentrale Spiel-Typen |
| `domain.model` | `Card`, `Deck`, `TurnInfo` | Daten + Stapelverwaltung (ziehen/ablegen/refill) + Snapshot |
| `rules` | `ProbabilityCalculator` | reine Berechnung (read-only), keine Seiteneffekte |
| `player` | `Player`, `ConsolePlayer`, `SimpleBotPlayer`, `TargetInfo` | Entscheidung **HIT/STAY** + Zielwahl für Aktionskarten |
| `engine` | `GameEngine`, `TurnProcessor`, `RoundState`, `ScoreBoard`, `PlayerStatus` | Dealer/Orchestrierung, Status, Effekte, Punkte |

### 5.3 Zentrale Klassen
- **`GameEngine`**: startet Runde, iteriert Spielerzüge, ruft Scoring, discardet am Ende.
- **`TurnProcessor`**: baut `TurnInfo`, ruft `Player.decide`, zieht Karte, löst Effekte (Freeze/FlipThree/SecondChance) auf.
- **`RoundState`**: pro Runde Hands + PlayerStatus (ACTIVE/STAYED/FROZEN/BUSTED).
- **`ScoreBoard`**: Gesamtscore über Runden, prüft `targetScore`.
- **`Deck`**: Draw-Pile + Discard-Pile, recycled Discard wenn Draw leer ist.

---

## 6. Laufzeitsicht (Runtime View)

### 6.1 Ablauf „Eine Runde“
1. `GameEngine.playRound()` → `RoundState.initRound(players)`
2. Solange aktive Spieler existieren:
   - `TurnProcessor.processTurn(player)`
3. `ScoreBoard.scoreRound(...)` berechnet Rundenpunkte & Winner
4. `RoundState.collectAllPlayedCards(...)` → `Deck.discardAll(...)`

### 6.2 Ablauf „Ein Zug (HIT)“
1. `TurnProcessor` erstellt `TurnInfo` (Snapshot) und fragt `Player.decide(...)`
2. Bei **HIT**:
   - `Deck.draw()`
   - **NUMBER**:
     - Duplikat? → falls `SECOND_CHANCE` vorhanden: konsumieren & Duplikat ignorieren, sonst `BUSTED`
   - **ACTION**:
     - `FREEZE`: wählt Ziel → Zielstatus `FROZEN`
     - `FLIP_THREE`: Ziel zieht 3 Karten (über `forceDraw`)
     - `SECOND_CHANCE`: bleibt als Karte in Hand, wird beim Duplikat verbraucht

> Hinweis: `TurnInfo` ist als **read-only Snapshot** gedacht. In Code sollte dafür eine **Kopie** der Hand genutzt werden (z.B. `List.copyOf(hand)`), damit spätere Hand-Änderungen den Snapshot nicht verändern.

---

## 7. Verteilungssicht

- Lokale JVM-Ausführung (Konsole)
- Keine externen Services

---

## 8. Querschnittliche Konzepte

- **Snapshot/Immutability:** `TurnInfo` nur lesen; Player kann Engine nicht direkt steuern.
- **Deterministische Tests:** `Deck` kann von außen injiziert werden.
- **Statusmodell:** `PlayerStatus` verhindert Züge nach STAY/FREEZE/BUST.

---

## 9. Wichtige Architekturentscheidungen

| Entscheidung | Begründung |
|---|---|
| `Player` als Interface (Port) | ermöglicht Bot/Console austauschbar ohne Engine-Änderung |
| Deck-Injection in `GameEngine` | reproduzierbare Tests (kein Shuffle-Random) |
| Engine in 4 Bausteine splitten | kleineres, verständlicheres `GameEngine` |

---

## 10. Qualitätsanforderungen

- **Testbarkeit:** deterministische Szenarien (Preset Deck, Scripted Players)
- **Wartbarkeit:** klare Zuständigkeiten pro Paket/Klasse
- **Erweiterbarkeit:** neue Aktionskarten/Bots lokal implementierbar

---

## 11. Risiken & Technische Schulden

- Snapshot-Bug, wenn `TurnInfo.myCards` nur View auf `hand` ist (nach Rundenende wird Hand geleert).

---

## 12. Glossar

| Begriff | Bedeutung |
|---|---|
| Dealer/Engine | steuert Ablauf & Regeln |
| Draw Pile | Ziehstapel |
| Discard Pile | Ablagestapel |
| Action Card | FREEZE / FLIP_THREE / SECOND_CHANCE |
| Bust | Duplikat-Zahlenkarte ohne Schutz |

---
