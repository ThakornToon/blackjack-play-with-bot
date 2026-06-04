package view;

import javax.swing.*;
import java.awt.*;

/**
 * HelpDialog.java - Help popup dialog showing game rules
 * แสดงกฎและวิธีเล่นในรูปแบบ popup dialog (modal)
 *
 * Layout : BorderLayout
 * ┌─────────────────────────────────────────────────┐
 * │                     NORTH                       │
 * │                    (ไม่ได้ใช้)                     │
 * ├─────────────────────────────────────────────────┤
 * │                                                 │
 * │                    CENTER                       │
 * │              (JScrollPane + TextArea)           │
 * │                                                 │
 * ├─────────────────────────────────────────────────┤
 * │                     SOUTH                       │
 * │              (JPanel + Close Button)            │
 * └─────────────────────────────────────────────────┘
 *
 * การใช้งาน: HelpDialog dlg = new HelpDialog(parentFrame); dlg.setVisible(true);
 */
public class HelpDialog extends JDialog {

    public HelpDialog(JFrame parent) {
        super(parent, "How to Play – Blackjack", true); // modal
        setSize(760, 620);
        setLocationRelativeTo(parent);
        setResizable(true);

        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 3));

        // ── Text area ──────────────────────────────────────────────────────────
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBackground(new Color(255, 255, 240));
        textArea.setMargin(new Insets(20, 20, 20, 20));
        textArea.setText(
                        "╔══════════════════════════════════════════════════════════════════╗\n" +
                        "║                       BLACKJACK RULES                            ║\n" +
                        "╚══════════════════════════════════════════════════════════════════╝\n\n" +
                        "🎯 Goal: Beat the dealer without exceeding 21.\n\n" +
                        "┌─────────────┬────────────────────────────────────────────────────┐\n" +
                        "│ Card Values │                                                    │\n" +
                        "├─────────────┼────────────────────────────────────────────────────┤\n" +
                        "│ 2 - 10      │ Face value                                         │\n" +
                        "│ J, Q, K     │ 10 points                                          │\n" +
                        "│ Ace         │ 1 or 11 points (counts as 11 unless it would bust) │\n" +
                        "└─────────────┴────────────────────────────────────────────────────┘\n\n" +
                        "┌─────────────┬────────────────────────────────────────────────────┐\n" +
                        "│ Actions     │                                                    │\n" +
                        "├─────────────┼────────────────────────────────────────────────────┤\n" +
                        "│ HIT         │ Take another card                                  │\n" +
                        "│ STAND       │ Stop and keep your current hand                    │\n" +
                        "│ DOUBLE      │ Double your bet, take exactly one more card        │\n" +
                        "│ SPLIT       │ Split pairs into two hands (requires extra bet)    │\n" +
                        "│ INSURANCE   │ When dealer shows Ace, bet half your bet that      │\n" +
                        "│             │ dealer has Blackjack (pays 2:1)                    │\n" +
                        "└─────────────┴────────────────────────────────────────────────────┘\n\n" +
                        "┌─────────────┬────────────────────────────────────────────────────┐\n" +
                        "│ Game Flow   │                                                    │\n" +
                        "├─────────────┼────────────────────────────────────────────────────┤\n" +
                        "│ 1. Place bet│ Each round starts with you placing a bet           │\n" +
                        "│ 2. Deal     │ Everyone gets 2 cards. Dealer shows 1 card         │\n" +
                        "│ 3. Take turn│ Players take turns                                 │\n" +
                        "│ 4. Dealer   │ After all players, dealer must hit until 17+       │\n" +
                        "│ 5. Settle   │ Higher total wins, push on tie                     │\n" +
                        "│ 6. Rotate   │ Dealer rotates to next player each round           │\n" +
                        "└─────────────┴────────────────────────────────────────────────────┘\n\n" +
                        "🎮 This Game:\n" +
                        "  • 4 players: You + 3 bots\n" +
                        "  • Bots use optimal Basic Strategy (same as professional players)\n" +
                        "  • Dealer rotates after each round\n" +
                        "  • Game ends when only one player has money left\n" +
                        "  • You can Pause/Resume/Exit anytime\n\n" +
                        "✨ Tips:\n" +
                        "  • Always split Aces and 8s\n" +
                        "  • Never split 10s or 5s\n" +
                        "  • Stand on 17 or higher\n" +
                        "  • Double on 11 against dealer 2-10\n\n" +
                        "Good luck and have fun! 🍀"
        );

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        add(scrollPane, BorderLayout.CENTER);

        // ── Close button ───────────────────────────────────────────────────────
        JButton closeBtn = new JButton("✖  CLOSE");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        closeBtn.setBackground(new Color(70, 130, 180));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setOpaque(true);
        closeBtn.setContentAreaFilled(true);   // ให้ background เต็มพื้นที่ปุ่ม
        closeBtn.setBorderPainted(false);       // ไม่มีขอบ ทำให้ดูเรียบ
        closeBtn.setPreferredSize(new Dimension(160, 44)); // กำหนดขนาดปุ่มชัดเจน
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        // also close on Escape key
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(34, 139, 34));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}