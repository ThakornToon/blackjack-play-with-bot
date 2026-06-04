# ♠Blackjack - Dealer Rotation

> Multiplayer Blackjack game with dealer rotation, bot opponents using optimal Basic Strategy, and full casino rules.

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Swing](https://img.shields.io/badge/GUI-Swing-orange)

## Features

- **4 players** – You + 3 AI bots (Alpha, Beta, Gamma)
- **Dealer rotates** after every round – fair gameplay
- **Bots use professional Basic Strategy** – same as casino pros
- **Full Blackjack rules**:
    - Hit, Stand, Double, Split, Insurance
    - Dealer stands on soft 17
    - Blackjack pays 1:1 (standard for this variant)
    - Insurance pays 2:1
- **Bankroll management** – Players eliminated when balance reaches $0
- **Pause/Resume** anytime
- **Persistent game log** – history up to 500 rounds

## How to Play

1. **Place your bet** – Enter amount and click "CONFIRM BET & START ROUND"
2. **Play your hand** – Choose Hit, Stand, Double, Split, or Insurance
3. **Watch bots play** – Bots auto-play using optimal strategy
4. **Dealer reveals** – After all players finish, dealer hits until 17+
5. **Settle bets** – Winners get paid, losers lose their bet
6. **Dealer rotates** – Next round, a different player becomes dealer

## Controls

| Button | Action |
|--------|--------|
| **HIT** | Take another card |
| **STAND** | Keep current hand |
| **DOUBLE** | Double bet, take exactly one card |
| **SPLIT** | Split pairs into two hands |
| **INSURANCE** | Buy insurance when dealer shows Ace |
| **PAUSE/RESUME** | Stop or resume game flow |
| **RULES** | Show help dialog |
| **EXIT** | Return to main menu |

## Bot Strategy

Bots follow **optimal Basic Strategy** (same as professional Blackjack):

| Your Hand | Dealer 2-6 | Dealer 7-Ace |
|-----------|------------|--------------|
| 17+       | Stand      | Stand        |
| 13-16     | Stand      | Hit          |
| 12        | Stand (4-6) | Hit          |
| 11        | Double     | Double/Hit   |
| Aces/8s   | Split      | Split        |
| 10s/5s    | Never split | Never split |

## Winning Conditions

- Game ends when **only one player has money left**
- Winner declared with final balance summary
- Eliminated players can spectate remaining bot matches

## Project Structure
```
src/
├── Main.java # Entry point
├── model/
│ ├── BlackjackGameModel.java # Core game logic
│ ├── BasicStrategy.java # Optimal strategy for bots
│ ├── Player.java # Player state (balance, hands)
│ ├── Hand.java # Individual hand logic
│ ├── Card.java # Card representation
│ └── Deck.java # Shoe with multiple decks
└── view/
  ├── BlackjackGUI.java # Main JFrame (CardLayout)
  ├── GamePanel.java # Game screen with controls
  ├── MenuPanel.java # Main menu
  └── HelpPanel.java # Rules dialog
```