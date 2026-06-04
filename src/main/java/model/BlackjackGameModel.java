package model;

import java.util.*;
import javax.swing.Timer;

/**
 * BlackjackGameModel.java - Core game logic and state management
 *
 * Bug fixes in this version:
 *   1. startNewRound()  – removed duplicate currentPlayerIdx reset that made
 *      the skip-dealer while-loop a no-op.
 *   2. nextPlayer()     – when a split player still has a second hand and we
 *      have just wrapped around to the dealer, currentPlayerIdx was left at
 *      dealerIndex.  The next processAction()/processBotTurn() call then
 *      operated on the dealer's hand instead of the player's split hand.
 *      Fix: restore currentPlayerIdx = startIdx before returning.
 */
public class BlackjackGameModel {
    private final List<Player> players;
    private int dealerIndex;
    private final Deck deck;
    private int currentPlayerIdx;
    private boolean roundInProgress;
    private boolean waitingForHuman;
    private boolean gameOver;
    private Player winner;
    private int roundNumber;
    private final int[] roundWins;
    private final List<String> roundHistory;
    private boolean paused;
    private boolean dealerRevealed;
    private boolean allPlayersFinished;
    private int pendingHumanBet;

    private Runnable uiUpdater;

    public BlackjackGameModel() {
        players = new ArrayList<>();
        players.add(new Player("You",       1000, true));
        players.add(new Player("Bot Alpha", 1000, false));
        players.add(new Player("Bot Beta",  1000, false));
        players.add(new Player("Bot Gamma", 1000, false));
        dealerIndex = 0;
        deck = new Deck(4);
        roundNumber = 0;
        roundWins = new int[players.size()];
        roundHistory = new ArrayList<>();
        roundInProgress = false;
        waitingForHuman = false;
        gameOver = false;
        paused = false;
        dealerRevealed = false;
        allPlayersFinished = false;
    }

    public void setUIUpdater(Runnable updater) { this.uiUpdater = updater; }

    private void notifyUI() {
        while (roundHistory.size() > 500) roundHistory.remove(0);
        if (uiUpdater != null) uiUpdater.run();
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public List<Player> getPlayers()        { return players; }
    public int  getDealerIndex()            { return dealerIndex; }
    public boolean isPaused()               { return paused; }
    public void setPaused(boolean paused)   { this.paused = paused; notifyUI(); }
    public boolean isGameOver()             { return gameOver; }
    public Player  getWinner()              { return winner; }
    public int  getRoundNumber()            { return roundNumber; }
    public List<String> getRoundHistory()   { return roundHistory; }
    public boolean isRoundInProgress()      { return roundInProgress; }
    public Player  getCurrentDealer()       { return players.get(dealerIndex); }
    public boolean isDealerRevealed()       { return dealerRevealed; }

    public Player getCurrentPlayer() {
        if (!roundInProgress) return null;
        return players.get(currentPlayerIdx);
    }

    public void setHumanBet(int amount) {
        Player human = players.get(0);
        if (amount > 0 && amount <= human.getBalance()) {
            pendingHumanBet = amount;
            roundHistory.add(human.getName() + " prepares bet $" + amount);
        }
    }

    // ── Round lifecycle ────────────────────────────────────────────────────────

    public void startNewRound() {
        if (gameOver || paused) return;
        roundInProgress = true;
        roundNumber++;
        dealerRevealed = false;
        allPlayersFinished = false;

        for (Player p : players) p.resetHands();

        // Place human bet (only when human is not the dealer)
        if (dealerIndex != 0 && pendingHumanBet > 0) {
            players.get(0).placeBet(pendingHumanBet);
        }

        // Place bot bets
        for (int i = 0; i < players.size(); i++) {
            if (i != dealerIndex && players.get(i).isActive()) {
                Player p = players.get(i);
                if (!p.isHuman()) {
                    int betAmount = (int)(p.getBalance() * 0.05);
                    if (betAmount < 5)   betAmount = 5;
                    if (betAmount > 500) betAmount = 500;
                    p.placeBet(betAmount);
                    roundHistory.add(p.getName() + " bets $" + betAmount);
                }
            }
        }

        // Deal 2 cards to every active player
        for (Player p : players) {
            if (p.isActive()) {
                Hand hand = p.getCurrentHand();
                hand.addCard(deck.drawCard());
                hand.addCard(deck.drawCard());
            }
        }

        // ── FIX #1 ────────────────────────────────────────────────────────────
        // Original code had a dead-code while-loop followed by a second reset
        // that wiped the loop's effect.  Replaced with a single clean loop that
        // skips both the dealer and inactive players.
        currentPlayerIdx = (dealerIndex + 1) % players.size();
        while (currentPlayerIdx != dealerIndex
                && !players.get(currentPlayerIdx).isActive()) {
            currentPlayerIdx = (currentPlayerIdx + 1) % players.size();
        }
        // If we somehow wrapped all the way to the dealer everyone else is out;
        // finishRound will handle game-over detection.
        // ─────────────────────────────────────────────────────────────────────

        waitingForHuman = players.get(currentPlayerIdx).isHuman();

        roundHistory.add("=== Round " + roundNumber + " started ===");
        roundHistory.add("Dealer: " + getCurrentDealer().getName());
        roundHistory.add("Dealer shows: " +
                getCurrentDealer().getCurrentHand().getCards().get(0).getShortName());

        notifyUI();

        if (!waitingForHuman && roundInProgress && !paused && !gameOver) {
            Timer timer = new Timer(500, e -> processBotTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    private boolean isCurrentPlayerDone() {
        Player p = players.get(currentPlayerIdx);
        if (!p.isActive()) return true;
        Hand hand = p.getCurrentHand();
        return hand.isBust() || hand.isStood();
    }

    public void processAction(String action) {
        if (paused || gameOver || !roundInProgress) return;

        Player player = players.get(currentPlayerIdx);
        Hand hand = player.getCurrentHand();

        if (isCurrentPlayerDone()) { nextPlayer(); return; }

        boolean turnEnded = false;

        switch (action) {
            case "Hit":
                hand.addCard(deck.drawCard());
                roundHistory.add(player.getName() + " hits -> " + hand.displayHand());
                if (hand.isBust()) {
                    roundHistory.add(player.getName() + " busts!");
                    turnEnded = true;
                }
                break;

            case "Stand":
                hand.setStood(true);
                roundHistory.add(player.getName() + " stands at " + hand.getTotal());
                turnEnded = true;
                break;

            case "Double":
                if (hand.getCards().size() == 2 && player.getBalance() >= hand.getBet()) {
                    player.deductBalance(hand.getBet());
                    hand.doubleBet();
                    hand.addCard(deck.drawCard());
                    roundHistory.add(player.getName() + " doubles! New bet: $" + hand.getBet()
                            + ", Hand: " + hand.displayHand());
                    turnEnded = true;
                    if (hand.isBust()) roundHistory.add(player.getName() + " busts!");
                } else {
                    roundHistory.add(player.getName() + " cannot double!");
                }
                break;

            case "Insurance":
                if (!dealerRevealed &&
                        getCurrentDealer().getCurrentHand().getCards().get(0).getRank() == Card.Rank.ACE) {
                    double cost = hand.getBet() / 2.0;
                    if (player.getBalance() >= cost) {
                        player.deductBalance(cost);
                        hand.setInsurance(true);
                        roundHistory.add(player.getName() + " bought insurance for $" + cost);
                    }
                }
                break;

            case "Split":
                List<Card> cards = hand.getCards();
                if (cards.size() == 2
                        && cards.get(0).getValue() == cards.get(1).getValue()
                        && player.getBalance() >= hand.getBet()) {
                    player.deductBalance(hand.getBet());
                    Hand newHand = new Hand(hand.getBet());
                    Card secondCard = cards.remove(1);
                    newHand.addCard(secondCard);
                    hand.addCard(deck.drawCard());
                    newHand.addCard(deck.drawCard());
                    hand.setSplit(true);
                    newHand.setSplit(true);
                    player.addSplitHand(newHand);
                    roundHistory.add(player.getName() + " splits! Now has "
                            + player.getHands().size() + " hands");
                }
                break;
        }

        notifyUI();

        if (turnEnded) {
            nextPlayer();
        } else if (!waitingForHuman && roundInProgress && !paused && !gameOver) {
            Timer timer = new Timer(500, e -> processBotTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void processBotTurn() {
        if (paused || gameOver || !roundInProgress || waitingForHuman) return;

        Player bot  = players.get(currentPlayerIdx);
        Hand   hand = bot.getCurrentHand();

        if (hand.isBust() || hand.isStood()) { nextPlayer(); return; }

        int    dealerUp = getCurrentDealer().getCurrentHand().getCards().get(0).getValue();
        boolean isSoft  = BasicStrategy.isSoftHand(hand);
        boolean canDouble = (hand.getCards().size() == 2 && bot.getBalance() >= hand.getBet());
        boolean canSplit  = (hand.getCards().size() == 2
                && hand.getCards().get(0).getValue() == hand.getCards().get(1).getValue()
                && bot.getBalance() >= hand.getBet());
        int pairVal = hand.getCards().size() == 2 ? hand.getCards().get(0).getValue() : 0;

        String action = BasicStrategy.getAction(
                hand.getTotal(), dealerUp, isSoft, canDouble, canSplit, pairVal);

        roundHistory.add(bot.getName() + " (bot) chooses: " + action
                + " (Hand: " + hand.displayHand() + ", Dealer shows: " + dealerUp + ")");

        switch (action) {
            case "H": processAction("Hit");    break;
            case "S": processAction("Stand");  break;
            case "D": processAction("Double"); break;
            case "P": processAction("Split");  break;
            default:  processAction("Stand");
        }
    }

    private void nextPlayer() {
        int startIdx = currentPlayerIdx;

        do {
            currentPlayerIdx = (currentPlayerIdx + 1) % players.size();

            if (currentPlayerIdx == dealerIndex) {
                // Check whether the previous player still has a split hand to play
                Player cur = players.get(startIdx);
                if (cur.hasMoreHands()) {
                    // ── FIX #2 ────────────────────────────────────────────────
                    // Restore the index to the same player so the next
                    // processAction() / processBotTurn() call targets their
                    // split hand and not the dealer's hand.
                    currentPlayerIdx = startIdx;
                    // ─────────────────────────────────────────────────────────
                    cur.nextHand();
                    waitingForHuman = cur.isHuman();
                    notifyUI();
                    if (!waitingForHuman && roundInProgress && !paused && !gameOver) {
                        Timer timer = new Timer(500, e -> processBotTurn());
                        timer.setRepeats(false);
                        timer.start();
                    }
                    return;
                }
                allPlayersFinished = true;
                roundInProgress = false;
                finishRound();
                return;
            }

        } while (!players.get(currentPlayerIdx).isActive()
                || isPlayerFinished(players.get(currentPlayerIdx)));

        waitingForHuman = players.get(currentPlayerIdx).isHuman();
        roundHistory.add("--- Now playing: " + players.get(currentPlayerIdx).getName() + " ---");
        notifyUI();

        if (!waitingForHuman && roundInProgress && !paused && !gameOver) {
            Timer timer = new Timer(500, e -> processBotTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    private boolean isPlayerFinished(Player p) {
        for (Hand h : p.getHands()) {
            if (!h.isBust() && !h.isStood()) return false;
        }
        return true;
    }

    // ── Round settlement ───────────────────────────────────────────────────────

    private void finishRound() {
        Player dealer     = getCurrentDealer();
        Hand   dealerHand = dealer.getCurrentHand();

        dealerRevealed = true;

        roundHistory.add("");
        roundHistory.add("=== Dealer's turn ===");
        roundHistory.add("Dealer (" + dealer.getName() + ") reveals: " + dealerHand.displayHand());

        while (dealerHand.getTotal() < 17) {
            Card newCard = deck.drawCard();
            dealerHand.addCard(newCard);
            roundHistory.add("Dealer hits: " + newCard.getShortName()
                    + " -> Total " + dealerHand.getTotal());
        }
        roundHistory.add(dealerHand.isBust()
                ? "Dealer busts!"
                : "Dealer stands at " + dealerHand.getTotal());

        roundHistory.add("");
        roundHistory.add("=== Settling bets ===");

        for (int i = 0; i < players.size(); i++) {
            if (i == dealerIndex || !players.get(i).isActive()) continue;
            Player p = players.get(i);
            for (Hand hand : p.getHands()) {
                int payout;
                int originalBet = hand.getBet();

                if (hand.isBust()) {
                    payout = 0;
                    roundHistory.add(p.getName() + " lost $" + originalBet + " (bust)");
                } else if (dealerHand.isBust()) {
                    payout = hand.getBet() * 2;
                    roundHistory.add(p.getName() + " wins $" + hand.getBet() + "! (Dealer bust)");
                } else {
                    int pTotal = hand.getTotal(), dTotal = dealerHand.getTotal();
                    if (pTotal > dTotal) {
                        payout = hand.getBet() * 2;
                        roundHistory.add(p.getName() + " wins $" + hand.getBet()
                                + "! (" + pTotal + " vs " + dTotal + ")");
                    } else if (pTotal == dTotal) {
                        payout = hand.getBet();
                        roundHistory.add(p.getName() + " pushes – gets $" + originalBet + " back");
                    } else {
                        payout = 0;
                        roundHistory.add(p.getName() + " loses $" + originalBet
                                + " (" + pTotal + " vs " + dTotal + ")");
                    }
                }

                if (hand.isInsurance()) {
                    if (dealerHand.isBlackjack()) {
                        int ins = originalBet;
                        payout += ins;
                        roundHistory.add(p.getName() + " insurance pays $" + ins);
                    } else {
                        roundHistory.add(p.getName() + " loses insurance bet");
                    }
                }

                p.addBalance(payout);
                dealer.addBalance(originalBet - payout);
                if (payout > hand.getBet()) roundWins[i]++;
            }
        }

        roundHistory.add("");
        roundHistory.add("Dealer (" + dealer.getName() + ") balance: $"
                + String.format("%.2f", dealer.getBalance()));
        roundHistory.add("Round " + roundNumber + " finished.");
        roundHistory.add("-----------------------------------");

        // Eliminate bankrupt players
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getBalance() <= 0 && players.get(i).isActive()) {
                players.get(i).setActive(false);
                roundHistory.add(players.get(i).getName() + " is bankrupt and eliminated!");
            }
        }

        // Check for game over
        int    activeCount = 0;
        Player lastMan     = null;
        for (Player p : players) {
            if (p.isActive()) { activeCount++; lastMan = p; }
        }
        if (activeCount <= 1) {
            gameOver = true;
            winner   = lastMan;
            roundHistory.add("");
            roundHistory.add("========== GAME OVER! ==========");
            roundHistory.add("Winner: " + winner.getName());
            roundHistory.add("=================================");
            notifyUI();
            return;
        }

        // Rotate dealer
        do {
            dealerIndex = (dealerIndex + 1) % players.size();
        } while (!players.get(dealerIndex).isActive());

        notifyUI();
    }

    // ── Query helpers ──────────────────────────────────────────────────────────

    public boolean areAllPlayersFinished() { return allPlayersFinished; }

    public boolean isHumanTurn() {
        return roundInProgress && waitingForHuman && !paused && !gameOver && !allPlayersFinished;
    }

    public boolean canBuyInsurance() {
        if (!isHumanTurn()) return false;
        Player dealer = getCurrentDealer();
        if (dealerRevealed || dealer.getCurrentHand().getCards().isEmpty()) return false;
        return dealer.getCurrentHand().getCards().get(0).getRank() == Card.Rank.ACE;
    }

    public boolean canSplit() {
        if (!isHumanTurn()) return false;
        Player p    = players.get(currentPlayerIdx);
        Hand   hand = p.getCurrentHand();
        if (hand.getCards().size() != 2) return false;
        if (hand.getCards().get(0).getValue() != hand.getCards().get(1).getValue()) return false;
        return p.getBalance() >= hand.getBet();
    }

    public boolean canDouble() {
        if (!isHumanTurn()) return false;
        Player p    = players.get(currentPlayerIdx);
        Hand   hand = p.getCurrentHand();
        if (hand.getCards().size() != 2 || hand.isDoubled()) return false;
        return p.getBalance() >= hand.getBet();
    }
}