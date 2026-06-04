package model;

import java.util.*;
import javax.swing.Timer;

/**
 * BlackjackGameModel.java - Core game logic and state management
 * คลาสหลักที่ควบคุมตรรกะของเกม Blackjack, จัดการสถานะผู้เล่น, ลำดับตาเล่น และการคำนวณผลลัพธ์
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │                    GAME LOOP                                 │
 * │                                                              │
 * │  startNewRound() ──► วางเดิมพัน ──► แจกไพ่ ──► เล่น turn         │
 * │       ↑                                      │               │
 * │       │                                      ↓               │
 * │       │                              isHumanTurn()?          │
 * │       │                             /            \           │
 * │       │                          Human          Bot          │
 * │       │                           │              │           │
 * │       │                    processAction()  processBotTurn() │
 * │       │                           │              │           │
 * │       │                           └──────┬───────┘           │
 * │       │                                  ↓                   │
 * │       │                             nextPlayer() ─────┐      │
 * │       │                                  │            │      │
 * │       │                          ผู้เล่นทั้งหมดเล่นเสร็จ?    │      │
 * │       │                             /        \        │      │
 * │       │                          Yes          No ─────┘      │
 * │       │                            │                         │
 * │       │                      finishRound()                   │
 * │       │                            │                         │
 * │       │                      ตรวจสอบ gameOver?               │
 * │       │                         /        \                   │
 * │       │                       No        Yes                  │
 * │       │                       │           │                  │
 * │       └───────────────────────┘          END                 │
 * │                                                              │
 * └──────────────────────────────────────────────────────────────┘
 *
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                        MODEL                                    │
 * │                   (BlackjackGameModel)                          │
 * │  ┌─────────────────────────────────────────────────────────┐    │
 * │  │  • เก็บสถานะเกม (waitingForHuman, roundInProgress)       │    │
 * │  │  • จัดการ logic การเล่น (processAction, nextPlayer)       │    │
 * │  │  • เก็บข้อมูลผู้เล่น, ไพ่, เงิน                                 │    │
 * │  │  • ไม่รู้ว่า GUI คืออะไร!                                    │    │
 * │  └─────────────────────────────────────────────────────────┘    │
 * │                              ↑                                  │
 * │                              │ (เรียก uiUpdater.run()/notifyUI)  │
 * │                              ↓                                  │
 * │                         GUI (GamePanel)                         │
 * │  ┌─────────────────────────────────────────────────────────┐    │
 * │  │  • อ่านค่า waitingForHuman จาก Model                      │    │
 * │  │  • ถ้า waitingForHuman = true → เปิดปุ่ม, แสดงข้อความ        │    │
 * │  │  • รอผู้เล่นกดปุ่ม                                           │    │
 * │  │  • เมื่อกดปุ่ม → เรียก model.processAction()                 │    │
 * │  └─────────────────────────────────────────────────────────┘    │
 * └─────────────────────────────────────────────────────────────────┘
 */
public class BlackjackGameModel {
    private final List<Player> players;         // รายชื่อผู้เล่นทั้งหมดในเกม (มนุษย์ 1 คน + บอท 3 ตัว)
    private int dealerIndex;                    // ตำแหน่งดัชนีของดีลเลอร์ในรอบปัจจุบัน (มีการวนตำแหน่ง)
    private final Deck deck;                    // สำรับไพ่ใน Shoe (ปกติมี 4 สำรับ)
    private int currentPlayerIdx;               // ดัชนีของผู้เล่นที่กำลังเล่นตาปัจจุบัน
    private boolean roundInProgress;            // สถานะระบุว่ารอบการเล่นกำลังดำเนินอยู่หรือไม่
    private boolean waitingForHuman;            // ระบุว่าเกมกำลังรอรับคำสั่งจากผู้เล่นที่เป็นมนุษย์หรือไม่
    private boolean gameOver;                   // สถานะจบทัวร์นาเมนต์ (เมื่อเหลือผู้เล่นไม่ถึง 2 คน)
    private Player winner;                      // วัตถุผู้เล่นที่เป็นผู้ชนะคนสุดท้าย
    private int roundNumber;                    // เลขจำนวนรอบที่เล่นไปแล้ว
    private final List<String> roundHistory;    // ประวัติบันทึกเหตุการณ์ต่างๆ ในเกม (Game Log)
    private boolean paused;                     // ระบุว่าเกมถูกหยุดชั่วคราวหรือไม่
    private boolean dealerRevealed;             // ระบุว่าดีลเลอร์ได้เปิดเผยไพ่ใบที่สองที่ซ่อนไว้แล้วหรือไม่
    private boolean allPlayersFinished;         // ระบุว่าผู้เล่นปกติทุกคนเล่นเสร็จสิ้นแล้วและพร้อมให้ดีลเลอร์เล่นต่อ
    private int pendingHumanBet;                // เงินเดิมพันที่ผู้เล่นเตรียมการไว้สำหรับการเล่นรอบถัดไป

    private Runnable uiUpdater;                 // คอลแบ็คเมธอดเพื่อสั่งอัปเดตหน้าต่าง GUI เมื่อโมเดลเปลี่ยนสถานะ

    /**
     * Constructor - Initializing game state, players (human + 3 bots), and card shoe
     * คอนสตรัคเตอร์เริ่มต้นสถานะเกม ผู้เล่นทั้ง 4 และสร้างสำรับไพ่รองเท้า
     */
    public BlackjackGameModel() {
        players = new ArrayList<>();
        players.add(new Player("You",       1000, true));
        players.add(new Player("Bot Alpha", 1000, false));
        players.add(new Player("Bot Beta",  1000, false));
        players.add(new Player("Bot Gamma", 1000, false));
        dealerIndex = 0;
        deck = new Deck(4);
        roundNumber = 0;
        roundHistory = new ArrayList<>();
        roundInProgress = false;
        waitingForHuman = false;
        gameOver = false;
        paused = false;
        dealerRevealed = false;
        allPlayersFinished = false;
    }

    /**
     * Registers a callback interface to update the UI on model state modifications.
     * ตั้งค่า Runnable สำหรับเรียกอัปเดตหน้า GUI เมื่อมีการเปลี่ยนแปลงค่าต่างๆ ใน Model
     */
    public void setUIUpdater(Runnable updater) {
        // เป็นแค่ Runnable ไม่รู้ว่าเป็นอะไร
        this.uiUpdater = updater;
    }

    /**
     * Helper to run UI refresh and keep the memory usage bound by capping history size.
     * แจ้งเตือนหน้าต่าง GUI ให้วาดหน้าจอใหม่ พร้อมป้องกันปัญหาหน่วยความจำล้นโดยควบคุมประวัติไม่ให้เกิน 500 บันทึก
     */
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

    /**
     * Returns the player whose turn it currently is.
     * คืนค่าผู้เล่นที่มีสิทธิ์ตัดสินใจในเทิร์นปัจจุบัน
     */
    public Player getCurrentPlayer() {
        if (!roundInProgress) return null;
        return players.get(currentPlayerIdx);
    }

    /**
     * Stores the bet amount selected by the human player before starting a round.
     * บันทึกการเดิมพันที่เตรียมไว้สำหรับมนุษย์เพื่อนำไปวางเดิมพันจริงตอนเริ่มรอบใหม่
     */
    public void setHumanBet(int amount) {
        Player human = players.get(0);
        if (amount > 0 && amount <= human.getBalance()) {
            pendingHumanBet = amount;
            roundHistory.add(human.getName() + " prepares bet $" + amount);
        }
    }

    // ── Round lifecycle ────────────────────────────────────────────────────────

    /**
     * Starts a new round: resets hands, places bets, deals cards, and triggers bot/human turns.
     * เริ่มต้นรอบเกมใหม่: ล้างไพ่เก่าในมือ, วางเดิมพันสำหรับทุกคน, แจกไพ่เริ่มต้น 2 ใบ และหาผู้เล่นคนแรกที่จะได้เล่น
     */
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

        currentPlayerIdx = (dealerIndex + 1) % players.size();
        while (currentPlayerIdx != dealerIndex
                && !players.get(currentPlayerIdx).isActive()) {
            currentPlayerIdx = (currentPlayerIdx + 1) % players.size();
        }

        // คนแรกที่เจอ active คือผู้เล่นไหม
        waitingForHuman = players.get(currentPlayerIdx).isHuman();

        roundHistory.add("=== Round " + roundNumber + " started ===");
        roundHistory.add("Dealer: " + getCurrentDealer().getName());
        roundHistory.add("Dealer shows: " +
                getCurrentDealer().getCurrentHand().getCards().get(0).getShortName());

        // แจ้งเตือนหน้าต่าง GUI ให้วาดหน้าจอใหม่
        notifyUI();

        // ถ้าเป็น turn ของบอทให้หน่วงเวลาคิด
        if (!waitingForHuman && roundInProgress && !paused && !gameOver) {
            roundHistory.add("--- Now playing: " + players.get(currentPlayerIdx).getName() + " ---");
            Timer timer = new Timer(500, e -> processBotTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * Determines whether the current player's hand is finished (either busted or chose to stand).
     * ตรวจสอบว่ามือของเทิร์นปัจจุบันเล่นเสร็จสิ้นแล้วหรือไม่ (แต้มเกินเจ๊งไปแล้ว หรือกดยืนยันแต้มแล้ว)
     */
    private boolean isCurrentPlayerDone() {
        Player p = players.get(currentPlayerIdx);
        if (!p.isActive()) return true;
        Hand hand = p.getCurrentHand();
        return hand.isBust() || hand.isStood();
    }

    /**
     * Core handler for game actions: routes and processes decisions like Hit, Stand, Double, Split, or Insurance.
     * รับคำสั่งตัดตัดสินใจของผู้เล่น (Hit, Stand, Double, Insurance, Split) และประมวลผลการคำนวณตามกติกาจริง
     */
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
                    
                    // กฎคาสิโนจริงสำหรับ Split Aces:
                    // เมื่อทำการแยก Ace, ผู้เล่นจะได้รับไพ่เพิ่มเพียงมือละใบเดียวเท่านั้น และทั้งสองมือจะถูกบังคับให้ Stand (ยืนแต้ม) ทันที
                    if (secondCard.getRank() == Card.Rank.ACE) {
                        hand.setStood(true);
                        newHand.setStood(true);
                        roundHistory.add(player.getName() + " splits Aces! Each Ace receives exactly one card and stands automatically.");
                    } else {
                        roundHistory.add(player.getName() + " splits! Now has "
                                + player.getHands().size() + " hands");
                    }
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

    /**
     * Executes the bot decision making: queries BasicStrategy and performs the recommended actions.
     * เรียกใช้งานปัญญาประดิษฐ์ของบอท โดยสืบค้นแผนการเล่นจากตารางที่ดีที่สุด (Basic Strategy) แล้วสั่งทำงานคำสั่งโดยอัตโนมัติ
     */
    public void processBotTurn() {
        if (paused || gameOver || !roundInProgress || waitingForHuman) return;

        Player bot = players.get(currentPlayerIdx);
        Hand hand = bot.getCurrentHand();

        if (hand.isBust() || hand.isStood()) { nextPlayer(); return; }

        int dealerUp = getCurrentDealer().getCurrentHand().getCards().get(0).getValue();
        boolean canDouble = (hand.getCards().size() == 2 && bot.getBalance() >= hand.getBet());
        boolean canSplit = (hand.getCards().size() == 2
                && hand.getCards().get(0).getValue() == hand.getCards().get(1).getValue()
                && bot.getBalance() >= hand.getBet());
        int pairVal = hand.getCards().size() == 2 ? hand.getCards().get(0).getValue() : 0;

        String action = BasicStrategy.getAction(
                hand,
                dealerUp,
                canDouble,
                canSplit,
                pairVal);

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

    /**
     * Manages rotation of turns among players. Handles playing split hands consecutively.
     * จัดการสลับตาการเล่นไปยังผู้เล่นคนถัดไป และดูแลการเล่นมือแยก (Split Hands) ทีละมือของผู้เล่นแต่ละคนให้ครบถ้วนก่อนส่งตาต่อ
     */
    private void nextPlayer() {
        // ดึงอ้างอิงผู้เล่นปัจจุบันขึ้นมาตรวจสอบก่อนสลับไปยังผู้เล่นถัดไป
        Player cur = players.get(currentPlayerIdx);

        // Check whether the previous player still has a split hand to play
        // ตรวจสอบว่าผู้เล่นคนเดิมยังมีมือไพ่ที่แยก (Split Hand) ที่ยังเล่นไม่เสร็จในรอบนี้หรือไม่
        if (cur.isActive() && cur.hasMoreHands()) {
            // หากมีมือที่เหลือ ให้ขยับไปยังมือถัดไปของผู้เล่นคนเดิม และอนุญาตให้เล่นมือถัดไปต่อทันที
            cur.nextHand();
            waitingForHuman = cur.isHuman();
            
            // แสดงประวัติว่าสลับการควบคุมไปยัง Split Hand ถัดไปของผู้เล่นคนเดิม
            roundHistory.add("--- " + cur.getName() + " plays next split hand (#" + (cur.getCurrentHandIndex() + 1) + ") ---");

            // ถ้าเป็นตาผู้เล่น GUI จะจัดการให้
            notifyUI();
            
            // หากเป็นตาของบอท ให้เริ่มจับเวลาถัดไปของบอท
            if (!waitingForHuman && roundInProgress && !paused && !gameOver) {
                Timer timer = new Timer(500, e -> processBotTurn());
                timer.setRepeats(false);
                timer.start();
            }
            return;
        }

        // วนลูปเพื่อหาผู้เล่นที่ยังมีสิทธิ์เล่นคนถัดไป (ข้ามคนหมดตัว หรือคนที่เล่นเสร็จแล้ว)
        do {
            currentPlayerIdx = (currentPlayerIdx + 1) % players.size();

            // หากวนกลับมาครบรอบถึงตัวดีลเลอร์ หมายความว่าผู้เล่นปกติเล่นเสร็จสิ้นหมดแล้ว
            if (currentPlayerIdx == dealerIndex) {
                allPlayersFinished = true;
                roundInProgress = false;
                finishRound();  // จบรอบและทำการตัดสินรางวัล
                return;
            }

        } while (!players.get(currentPlayerIdx).isActive()
                || isPlayerFinished(players.get(currentPlayerIdx)));

        // ตั้งค่าว่าตาถัดไปเป็นของมนุษย์หรือไม่
        waitingForHuman = players.get(currentPlayerIdx).isHuman();
        roundHistory.add("--- Now playing: " + players.get(currentPlayerIdx).getName() + " ---");
        notifyUI();

        // เริ่มจับเวลาการเดินเกมของบอทกรณีสลับไปเป็นรอบของบอท
        if (!waitingForHuman && roundInProgress && !paused && !gameOver) {
            Timer timer = new Timer(500, e -> processBotTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * Checks if all hands belonging to a specific player are completed.
     * ตรวจสอบว่ามือทุกมือของผู้เล่นคนนั้นๆ ถือว่าเล่นเสร็จสิ้นแล้วหรือยัง (ใช้ตอนสลับรอบตาเล่นหลัก)
     */
    private boolean isPlayerFinished(Player p) {
        for (Hand h : p.getHands()) {
            if (!h.isBust() && !h.isStood()) return false;
        }
        return true;
    }

    // ── Round settlement ───────────────────────────────────────────────────────

    /**
     * Concludes the round: Dealer plays to soft-17 rules, bets are settled, bankruptcies checked, and dealer rotated.
     * จบรอบหลัก: ดีลเลอร์หงายไพ่และจั่วตามกฎของคาสิโนจนถึงแต้ม 17 ขึ้นไป, ตัดสินแพ้ชนะของผู้เล่นทั้งหมด, คัดคนเงินหมดตัวออก, วนตำแหน่งดีลเลอร์ใหม่
     */
    private void finishRound() {
        Player dealer = getCurrentDealer();
        Hand dealerHand = dealer.getCurrentHand();

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
                double payout;
                double originalBet = hand.getBet();

                if (hand.isBust()) {
                    payout = 0;
                    roundHistory.add(p.getName() + " lost $" + (int)originalBet + " (bust)");
                } else if (dealerHand.isBust()) {
                    payout = hand.getBet() * 2;
                    roundHistory.add(p.getName() + " wins $" + hand.getBet() + "! (Dealer bust)");
                } else if (hand.isBlackjack() && !dealerHand.isBlackjack()) {
                    // กฎคาสิโนจริง: Blackjack ชนะแต้ม 21 ที่จั่วหลายใบ
                    payout = hand.getBet() * 2;
                    roundHistory.add(p.getName() + " wins $" + hand.getBet() + " with Blackjack! 🃏");
                } else if (dealerHand.isBlackjack() && !hand.isBlackjack()) {
                    // กฎคาสิโนจริง: Dealer Blackjack ชนะแต้ม 21 ที่จั่วหลายใบของผู้เล่น
                    payout = 0;
                    roundHistory.add(p.getName() + " loses $" + (int)originalBet + " to Dealer's Blackjack! 🃏");
                } else if (hand.isBlackjack() && dealerHand.isBlackjack()) {
                    // ทั้งคู่ได้ Blackjack ถือว่าเสมอ (Push)
                    payout = hand.getBet();
                    roundHistory.add(p.getName() + " pushes (both have Blackjack) 🃏");
                } else {
                    int pTotal = hand.getTotal(), dTotal = dealerHand.getTotal();
                    if (pTotal > dTotal) {
                        payout = hand.getBet() * 2;
                        roundHistory.add(p.getName() + " wins $" + hand.getBet()
                                + "! (" + pTotal + " vs " + dTotal + ")");
                    } else if (pTotal == dTotal) {
                        payout = hand.getBet();
                        roundHistory.add(p.getName() + " pushes – gets $" + (int)originalBet + " back");
                    } else {
                        payout = 0;
                        roundHistory.add(p.getName() + " loses $" + (int)originalBet
                                + " (" + pTotal + " vs " + dTotal + ")");
                    }
                }

                // กฎคาสิโนจริงสำหรับ Insurance:
                // จ่าย 2:1 สำหรับเงินประกัน (ซึ่งมีค่าเท่ากับครึ่งหนึ่งของเงินเดิมพันหลัก หรือ cost = originalBet / 2.0)
                // ดังนั้น หากชนะ ประกันจะคืนเงินรางวัล 2 เท่าของเงินประกัน (มีค่าเท่ากับ originalBet) บวกกับเงินค่าประกันคืนมา (cost)
                // ยอดเงินคืนรวมทั้งหมดสำหรับเงินส่วนประกันนี้จึงเป็น cost * 3 = originalBet * 1.5 ซึ่งทำให้คืนทุน (Break Even) พอดี
                if (hand.isInsurance()) {
                    if (dealerHand.isBlackjack()) {
                        double insPayout = originalBet * 1.5;
                        payout += insPayout;
                        roundHistory.add(p.getName() + " insurance pays $" + (originalBet * 1.0) + " (breaks even)");
                    } else {
                        roundHistory.add(p.getName() + " loses insurance bet");
                    }
                }

                p.addBalance(payout);
                dealer.addBalance(originalBet - payout);
            }
        }

        roundHistory.add("");
        roundHistory.add("Dealer (" + dealer.getName() + ") balance: $"
                + String.format("%.2f", dealer.getBalance()));
        roundHistory.add("Round " + roundNumber + " finished.");
        roundHistory.add("-----------------------------------");

        // Eliminate bankrupt players
        // คัดแยกผู้เล่นที่สูญเสียเงินในธนาคารจนเหลือศูนย์ออกจากเกม (isActive = false)
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getBalance() <= 0 && players.get(i).isActive()) {
                players.get(i).setActive(false);
                roundHistory.add(players.get(i).getName() + " is bankrupt and eliminated!");
            }
        }

        // Check for game over
        // ตรวจสอบว่าเหลือผู้เล่นที่เหลือเงินใช้งานอยู่อีกหรือไม่เพื่อจบทัวร์นาเมนต์
        int activeCount = 0;
        Player lastMan = null;
        for (Player p : players) {
            if (p.isActive()) { activeCount++; lastMan = p; }
        }
        if (activeCount <= 1) {
            gameOver = true;
            winner = lastMan;
            roundHistory.add("");
            roundHistory.add("========== GAME OVER! ==========");
            roundHistory.add("Winner: " + winner.getName());
            roundHistory.add("=================================");
            notifyUI();
            return;
        }

        // Rotate dealer
        // สลับหมุนเวียนตำแหน่งดีลเลอร์ไปยังผู้เล่นที่ยังไม่ตกรอบคนถัดไป
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
        Player p = players.get(currentPlayerIdx);
        Hand hand = p.getCurrentHand();
        if (hand.getCards().size() != 2) return false;
        if (hand.getCards().get(0).getValue() != hand.getCards().get(1).getValue()) return false;
        return p.getBalance() >= hand.getBet();
    }

    public boolean canDouble() {
        if (!isHumanTurn()) return false;
        Player p = players.get(currentPlayerIdx);
        Hand hand = p.getCurrentHand();
        if (hand.getCards().size() != 2 || hand.isDoubled()) return false;
        return p.getBalance() >= hand.getBet();
    }
}