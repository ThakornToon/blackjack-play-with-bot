package view;

import model.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * GamePanel.java - Main game screen with controls and display
 *
 * Layout : BorderLayout
 * ┌─────────────────────────────────────────────┐
 * │                   NORTH                     │  ← Info Panel
 * ├──────────────────────────────┬──────────────┤
 * │                              │              │
 * │            CENTER            │     EAST     │  ← CENTER = ผู้เล่น
 * │         (Players Panel)      │  (Game Log)  │     EAST = Log
 * │                              │              │
 * ├──────────────────────────────┴──────────────┤
 * │                   SOUTH                     │  ← Bottom Container
 * └─────────────────────────────────────────────┘
 *
 *   betPanel   → แสดงเมื่อ human ยังเล่นอยู่ (วางเดิมพัน + เริ่มรอบ)
 *   nextRoundBtn → แสดงเฉพาะเมื่อ human ถูกคัดออก (ดูบอทเล่นต่อ ไม่ต้องวางเดิมพัน)
 *
 *                          ┌─────────────────┐
 *                          │     ผู้เล่นกดปุ่ม    │
 *                          └────────┬────────┘
 *                                   │
 *                                   ▼
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                           GamePanel                                 │
 * │  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────┐   │
 * │  │    ปุ่มต่างๆ    │───▶│ เรียก Model   │───▶│ model.processAction()│   │
 * │  │  (HIT, etc)  │    │              │    │                      │   │
 * │  └──────────────┘    └──────────────┘    └──────────┬───────────┘   │
 * │                                                     │               │
 * │                                                     ▼               │
 * │  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────┐   │
 * │  │  refresh()   │◀───│ UIUpdater    │◀───│   Model เปลี่ยนสถานะ   │   │
 * │  │  (อัปเดต UI)  │    │ (callback)   │    │                      │   │
 * │  └──────────────┘    └──────────────┘    └──────────────────────┘   │
 * │         ▲                                                           │
 * │         │                                                           │
 * │  ┌──────┴──────┐                                                    │
 * │  │ Timer200ms  │ (ทำงานตลอดเวลา)                                    │
 * │  └─────────────┘                                                    │
 * └─────────────────────────────────────────────────────────────────────┘
 */
public class GamePanel extends JPanel {
    private boolean endSummaryShown = false;
    private final BlackjackGameModel model;
    private final BlackjackGUI parent;

    private final JPanel playersPanel;
    private final JTextArea logArea;
    private final JButton hitBtn;
    private final JButton standBtn;
    private final JButton doubleBtn;
    private final JButton insuranceBtn;
    private final JButton splitBtn;
    private final JButton pauseBtn;
    private final JButton resumeBtn;
    private final JButton exitBtn;
    private final JButton ruleBtn;
    private final JButton nextRoundBtn;
    private final JLabel roundLabel;
    private final JLabel dealerLabel;
    private final JLabel turnLabel;
    private Timer autoRefreshTimer;

    // Bet panel components (kept as fields so refresh() can update their text)
    private final JPanel betPanel;
    private final JTextField betField;
    private final JButton confirmBetBtn;
    private final JLabel betLabel;

    private final JPanel actionPanel;
    private final PlayerUI[] playerUIs;

    public GamePanel(BlackjackGameModel model, BlackjackGUI parent) {
        this.model = model;
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(34, 139, 34));

        // ── Top info panel ─────────────────────────────────────────────────────
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBackground(new Color(0, 100, 0));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        roundLabel = new JLabel("Round: 0");
        roundLabel.setFont(new Font("Arial", Font.BOLD, 18));
        roundLabel.setForeground(Color.WHITE);

        dealerLabel = new JLabel("Dealer: ");
        dealerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dealerLabel.setForeground(Color.YELLOW);

        turnLabel = new JLabel("Status: Waiting for round start");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        turnLabel.setForeground(Color.CYAN);

        infoPanel.add(roundLabel);
        infoPanel.add(dealerLabel);
        infoPanel.add(turnLabel);
        add(infoPanel, BorderLayout.NORTH);

        // ── Center: players display ────────────────────────────────────────────
        playersPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        playersPanel.setBackground(new Color(34, 139, 34));
        playersPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.YELLOW, 2),
                "Players", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16), Color.YELLOW));

        // Initialize player UI panels once to prevent continuous component recreation
        // สร้างหน้าจอแสดงผลของผู้เล่นเตรียมไว้ครั้งเดียว เพื่อปรับปรุงประสิทธิภาพและเลี่ยงการวาด UI ซ้ำๆ
        playerUIs = new PlayerUI[model.getPlayers().size()];
        for (int i = 0; i < model.getPlayers().size(); i++) {
            Player p = model.getPlayers().get(i);
            playerUIs[i] = new PlayerUI(p);
            playersPanel.add(playerUIs[i].panel);
        }

        JScrollPane playerScroll = new JScrollPane(playersPanel);
        playerScroll.setBackground(new Color(34, 139, 34));
        playerScroll.setBorder(null);
        add(playerScroll, BorderLayout.CENTER);

        // ── Right: game log ────────────────────────────────────────────────────
        logArea = new JTextArea(20, 35);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setBackground(new Color(255, 255, 230));
        logArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.BLUE, 2),
                "Game Log", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.BLUE));
        add(logScroll, BorderLayout.EAST);

        // ── Bottom: Bet panel (shown when human is still active) ───────────────
        betPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        betPanel.setBackground(new Color(0, 100, 0));
        betPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        betLabel = new JLabel("Your Bet: $");
        betLabel.setFont(new Font("Arial", Font.BOLD, 20));
        betLabel.setForeground(Color.WHITE);

        betField = new JTextField(10);
        betField.setFont(new Font("Arial", Font.BOLD, 18));
        betField.setText("50");

        confirmBetBtn = new JButton("CONFIRM BET & START ROUND");
        confirmBetBtn.setFont(new Font("Arial", Font.BOLD, 16));
        confirmBetBtn.setBackground(new Color(255, 215, 0));
        confirmBetBtn.setForeground(Color.BLACK);
        confirmBetBtn.setFocusPainted(false);
        confirmBetBtn.addActionListener(e -> startRoundWithBet());

        betPanel.add(betLabel);
        betPanel.add(betField);
        betPanel.add(confirmBetBtn);

        // ── Bottom: Action buttons ─────────────────────────────────────────────
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setBackground(new Color(0, 100, 0));

        hitBtn        = createLargeButton("HIT",       new Color(50, 205, 50),   Color.WHITE);
        standBtn      = createLargeButton("STAND",     new Color(255, 69, 0),    Color.WHITE);
        doubleBtn     = createLargeButton("DOUBLE",    new Color(255, 215, 0),   Color.WHITE);
        insuranceBtn  = createLargeButton("INSURANCE", new Color(138, 43, 226),  Color.WHITE);
        splitBtn      = createLargeButton("SPLIT",     new Color(255, 105, 180), Color.WHITE);
        pauseBtn      = createLargeButton("PAUSE",     new Color(128, 128, 128), Color.WHITE);
        resumeBtn     = createLargeButton("RESUME",    new Color(34, 139, 34),   Color.WHITE);
        exitBtn       = createLargeButton("EXIT",      new Color(178, 34, 34),   Color.WHITE);
        ruleBtn       = createLargeButton("RULES",     new Color(70, 130, 180),  Color.WHITE);
        // nextRoundBtn is used ONLY when the human player is eliminated
        nextRoundBtn  = createLargeButton("▶ NEXT ROUND", new Color(255, 140, 0), Color.WHITE);

        hitBtn.addActionListener(e -> { model.processAction("Hit");       refresh(); });
        standBtn.addActionListener(e -> { model.processAction("Stand");   refresh(); });
        doubleBtn.addActionListener(e -> { model.processAction("Double"); refresh(); });
        insuranceBtn.addActionListener(e -> { model.processAction("Insurance"); refresh(); });
        splitBtn.addActionListener(e -> { model.processAction("Split");   refresh(); });
        pauseBtn.addActionListener(e -> { model.setPaused(true);  refresh(); });
        resumeBtn.addActionListener(e -> { model.setPaused(false); refresh(); });
        exitBtn.addActionListener(e -> {
            autoRefreshTimer.stop();
            parent.resetGame();
            parent.showMenu();
        });
        ruleBtn.addActionListener(e -> parent.showHelp());

        // nextRoundBtn: human is out, just start the next round with no bet
        nextRoundBtn.addActionListener(e -> {
            if (!model.isRoundInProgress() && !model.isGameOver()) {
                model.startNewRound();
                refresh();
            }
        });

        actionPanel.add(hitBtn);
        actionPanel.add(standBtn);
        actionPanel.add(doubleBtn);
        actionPanel.add(insuranceBtn);
        actionPanel.add(splitBtn);
        actionPanel.add(pauseBtn);
        actionPanel.add(resumeBtn);
        actionPanel.add(exitBtn);
        actionPanel.add(ruleBtn);
        actionPanel.add(nextRoundBtn);

        // Combine bet strip + action strip in one south container
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(betPanel,    BorderLayout.NORTH);
        bottomContainer.add(actionPanel, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        model.setUIUpdater(this::refresh);  // ส่งเมธอด refresh() ไปให้ Model

        // สร้าง Timer ที่ทำงานทุก 200 ms เรียก refresh()
        autoRefreshTimer = new Timer(200, e -> refresh());
        autoRefreshTimer.start();  // เริ่มทำงาน
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private JButton createLargeButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Called when human clicks "CONFIRM BET & START ROUND".
     * ถ้า human เป็น dealer รอบนี้ ก็ไม่ต้องวางเดิมพัน — เริ่มได้เลย
     */
    private void startRoundWithBet() {
        Player human = model.getPlayers().get(0);

        if (model.getDealerIndex() == 0) {
            // Human is the dealer this round — no bet required
            model.startNewRound();
            refresh();
            return;
        }

        try {
            int bet = Integer.parseInt(betField.getText().trim());
            if (bet > 0 && bet <= human.getBalance()) {
                model.setHumanBet(bet);
                model.startNewRound();
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid bet! You have $" + String.format("%.2f", human.getBalance()),
                        "Invalid Bet", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number!",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Main refresh ───────────────────────────────────────────────────────────
    /**
     * refresh() {
     *     │
     *     ├── 1. อัปเดตแถบข้อมูลบนสุด
     *     │       roundLabel.setText("Round: " + รอบที่)
     *     │       dealerLabel.setText("Dealer: " + ชื่อดีลเลอร์)
     *     │       turnLabel.setText(สถานะปัจจุบัน)
     *     │
     *     ├── 2. อัปเดตผู้เล่นแต่ละคน (Loop)
     *     │       for (ผู้เล่นทุกคน) {
     *     │           - เปลี่ยนชื่อ + สถานะ (👑 DEALER, 💀 OUT, ◀ PLAYING)
     *     │           - อัปเดตเงินคงเหลือ
     *     │           - แสดงมือไพ่ (ซ่อนไพ่ดีลเลอร์ใบที่ 2 ถ้าจำเป็น)
     *     │       }
     *     │
     *     ├── 3. อัปเดต Game Log
     *     │       logArea.setText(ประวัติทั้งหมด)
     *     │       เลื่อนลงไปล่าสุดอัตโนมัติ
     *     │
     *     ├── 4. เปิด/ปิดปุ่มต่างๆ
     *     │       hitBtn.setEnabled(isHumanTurn)
     *     │       doubleBtn.setEnabled(isHumanTurn && canDouble)
     *     │       splitBtn.setEnabled(isHumanTurn && canSplit)
     *     │       insuranceBtn.setEnabled(isHumanTurn && canBuyInsurance)
     *     │
     *     ├── 5. สลับการแสดง Bet Panel / Next Round Button
     *     │       if (ระหว่างรอบ และ มนุษย์ยังอยู่) → แสดง Bet Panel
     *     │       if (ระหว่างรอบ และ มนุษย์ตาย) → แสดง Next Round Button
     *     │
     *     └── 6. ตรวจสอบเกมจบ
     *             if (gameOver และ ยังไม่เคยแสดงสรุป) {
     *                 showEndSummary() → หยุด Timer → แสดงผลผู้ชนะ
     *             }
     * }
     */
    public void refresh() {
        // ── 1. Update roundLabel, dealerLabel, turnLabel ──────────────────────────────────
        roundLabel.setText("Round: " + model.getRoundNumber());
        if (model.getDealerIndex() >= 0 && model.getPlayers().size() > model.getDealerIndex()) {
            dealerLabel.setText("Dealer: " + model.getPlayers().get(model.getDealerIndex()).getName());
        }

        if (model.isGameOver()) {
            turnLabel.setText("GAME OVER – " + model.getWinner().getName() + " wins!");
        } else if (model.isRoundInProgress()) {
            if (model.isHumanTurn()) {
                turnLabel.setText("YOUR TURN!  " + model.getCurrentPlayer().getCurrentHand().displayHand());
            } else if (model.areAllPlayersFinished()) {
                turnLabel.setText("Round ending – Dealer's turn…");
            } else {
                turnLabel.setText("Bot turn: " + model.getCurrentPlayer().getName());
            }
        } else {
            boolean humanActive = model.getPlayers().get(0).isActive();
            turnLabel.setText(humanActive
                    ? "Place your bet and click CONFIRM BET & START ROUND"
                    : "You are out – click NEXT ROUND to watch the bots");
        }

        // ── 2. Players panel update ───────────────────────────────────────────────
        // อัปเดตข้อมูลของผู้เล่นแต่ละคนใน Swing component ที่สร้างเตรียมไว้แล้ว (หลีกเลี่ยงการสร้าง JPanel ใหม่ทุกๆ วินาที)
        for (int i = 0; i < model.getPlayers().size(); i++) {
            Player p = model.getPlayers().get(i);
            PlayerUI ui = playerUIs[i];

            // สร้างชื่อและสถานะของผู้เล่นเพื่อแสดงผล
            String title = p.getName();
            if (i == model.getDealerIndex()) title += " 👑 (DEALER)";
            if (!p.isActive())               title += " 💀 (OUT)";
            if (model.isRoundInProgress() && model.getCurrentPlayer() == p && !model.isGameOver())
                title += " ◀ PLAYING";

            ui.nameLabel.setText(title);
            ui.balanceLabel.setText(String.format("💰 $%.2f", p.getBalance()));

            StringBuilder sb = new StringBuilder();
            // วนลูปการแสดงผลแต่ละมือของผู้เล่นคนนั้นๆ (เช่น กรณีมีการ Split)
            for (Hand h : p.getHands()) {
                if (h.getBet() > 0) sb.append("Bet: $").append(h.getBet()).append("\n");
                
                // สำหรับไพ่ดีลเลอร์: หากยังไม่จบรอบการเล่นและดีลเลอร์ยังไม่เปิดไพ่ ให้ซ่อนไพ่ใบที่สองไว้
                if (i == model.getDealerIndex() && !model.isDealerRevealed()) {
                    if (h.getCards() != null && !h.getCards().isEmpty()) {
                        sb.append(h.getCards().get(0).getShortName()).append(", [Hidden]");
                    } else {
                        sb.append("No active hand");
                    }
                } else {
                    sb.append(h.displayHand());
                }
                sb.append("\n");
            }
            if (sb.length() == 0) sb.append("No active hand");
            ui.handArea.setText(sb.toString());
        }

        // ── 3. Game log ───────────────────────────────────────────────────────────
        StringBuilder logSb = new StringBuilder();
        for (String log : model.getRoundHistory()) logSb.append(log).append("\n");
        logArea.setText(logSb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());  // เลื่อน Scrollbar ของ Game Log ลงไปที่บรรทัดล่าสุดโดยอัตโนมัติ

        // ── 4. Action button enable/disable ───────────────────────────────────────
        boolean isHumanTurn = model.isHumanTurn();
        hitBtn.setEnabled(isHumanTurn);
        standBtn.setEnabled(isHumanTurn);
        doubleBtn.setEnabled(isHumanTurn && model.canDouble());
        insuranceBtn.setEnabled(isHumanTurn && model.canBuyInsurance());
        splitBtn.setEnabled(isHumanTurn && model.canSplit());
        pauseBtn.setEnabled(!model.isGameOver() && !model.isPaused());
        resumeBtn.setEnabled(model.isPaused());

        // ── 5. Bet panel vs Next-Round button (mutually exclusive) ────────────────
        boolean betweenRounds = !model.isRoundInProgress() && !model.isGameOver();
        boolean humanActive   = model.getPlayers().get(0).isActive();

        // betPanel: only when human is still playing (needs to place a bet)
        boolean showBetPanel = betweenRounds && humanActive;
        betPanel.setVisible(showBetPanel);

        if (showBetPanel) {
            boolean isHumanDealer = (model.getDealerIndex() == 0);
            if (isHumanDealer) {
                betLabel.setText("You are the Dealer this round! →");
                betField.setVisible(false);
                confirmBetBtn.setText("START ROUND (No Bet)");
            } else {
                betLabel.setText("Your Bet: $");
                betField.setVisible(true);
                confirmBetBtn.setText("CONFIRM BET & START ROUND");
            }
        }

        // nextRoundBtn: only when human is eliminated (spectator mode)
        boolean showNextRound = betweenRounds && !humanActive;
        nextRoundBtn.setVisible(showNextRound);
        nextRoundBtn.setEnabled(showNextRound);

        // ── 6. End summary ────────────────────────────────────────────────────────
        if (model.isGameOver() && !endSummaryShown) {
            endSummaryShown = true;
            showEndSummary();
        }
    }

    private void showEndSummary() {
        autoRefreshTimer.stop();
        Player winner = model.getWinner();
        StringBuilder msg = new StringBuilder();
        msg.append("═════════ GAME OVER ═════════\n\n");
        msg.append("🏆 WINNER: ").append(winner.getName()).append(" 🏆\n\n");
        msg.append("Final Balances:\n");
        msg.append("─────────────────\n");
        for (Player p : model.getPlayers()) {
            String status = p.isActive() ? "✓" : "✗";
            msg.append(String.format("%-12s: $%8.2f %s\n", p.getName(), p.getBalance(), status));
        }
        JOptionPane.showMessageDialog(this, msg.toString(), "Game Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Inner class to keep references to each player's Swing UI components.
     * This avoids reconstructing components on every timer tick / GUI refresh.
     * คลาสภายในช่วยเก็บส่วนประกอบ GUI ของผู้เล่น เพื่อไม่ต้องสร้างออบเจกต์ JPanel ใหม่ทุกๆ การอัปเดต
     */
    private static class PlayerUI {
        final JPanel panel;
        final JLabel nameLabel;
        final JLabel balanceLabel;
        final JTextArea handArea;

        PlayerUI(Player player) {
            panel = new JPanel(new BorderLayout(5, 5));
            panel.setBackground(new Color(255, 255, 240));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            nameLabel = new JLabel(player.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            nameLabel.setForeground(new Color(0, 100, 0));

            balanceLabel = new JLabel(String.format("💰 $%.2f", player.getBalance()));
            balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));

            handArea = new JTextArea(6, 14);
            handArea.setEditable(false);
            handArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            handArea.setBackground(new Color(255, 255, 240));

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(new Color(255, 255, 240));
            topPanel.add(nameLabel, BorderLayout.CENTER);
            topPanel.add(balanceLabel, BorderLayout.SOUTH);

            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(new JScrollPane(handArea), BorderLayout.CENTER);
        }
    }
}