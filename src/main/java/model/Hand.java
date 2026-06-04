package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand.java - Represents a single hand of cards for a player
 * แทนมือไพ่ 1 ชุดของผู้เล่น (รองรับการแยกไพ่ Split)
 */
public class Hand {
    private List<Card> cards;
    private int bet;           // เงินเดิมพันของมือนี้
    private boolean isDoubled; // ระบุว่าได้ทำการ Double แล้วหรือไม่
    private boolean isSplit;   // ระบุว่าเกิดจากการ Split หรือไม่
    private boolean isInsurance; // ระบุว่าซื้อ Insurance หรือไม่
    private boolean isStood;   // ระบุว่าเลือก Stand แล้วหรือยัง

    public Hand(int bet) {
        this.cards = new ArrayList<>();
        this.bet = bet;
        this.isDoubled = false;
        this.isSplit = false;
        this.isInsurance = false;
        this.isStood = false;
    }

    public void addCard(Card card) { cards.add(card); }
    public List<Card> getCards() { return cards; }
    public int getBet() { return bet; }

    /**
     * Set bet amount for this hand (used when placing initial bet)
     * กำหนดจำนวนเงินเดิมพันสำหรับมือนี้
     */
    public void setBet(int bet) {
        this.bet = bet;
    }

    public void doubleBet() {
        bet *= 2;
        isDoubled = true;
    }

    public boolean isDoubled() { return isDoubled; }
    public void setSplit(boolean split) { isSplit = split; }
    public boolean isSplit() { return isSplit; }
    public void setInsurance(boolean insurance) { isInsurance = insurance; }
    public boolean isInsurance() { return isInsurance; }
    public void setStood(boolean stood) { isStood = stood; }
    public boolean isStood() { return isStood; }

    /**
     * Calculate the best total value of the hand (Ace can be 1 or 11)
     * คำนวณคะแนนรวมที่ดีที่สุดของมือ (Ace นับเป็น 1 หรือ 11)
     */
    public int getTotal() {
        int total = 0;
        int aces = 0;
        for (Card c : cards) {
            total += c.getValue();
            if (c.getRank() == Card.Rank.ACE) aces++;
        }
        // If total > 21 and we have Aces, change Ace from 11 to 1
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    /**
     * Check if this hand is a natural Blackjack (Ace + 10-value card)
     * ตรวจสอบว่าเป็น Blackjack ธรรมชาติหรือไม่
     */
    public boolean isBlackjack() {
        return cards.size() == 2 && getTotal() == 21 && !isSplit;
    }

    /**
     * Check if hand is bust (over 21)
     * ตรวจสอบว่าแต้มเกิน 21 หรือไม่
     */
    public boolean isBust() {
        return getTotal() > 21;
    }
    /**
     * Check if hand is soft (contains Ace counted as 11)
     */
    public boolean isSoft() {
        int total = 0;
        int aces = 0;
        for (Card c : cards) { total += c.getValue(); if (c.getRank()==Card.Rank.ACE) aces++; }
        while (total > 21 && aces > 0) { total -= 10; aces--; }
        return aces > 0;
    }


    /**
     * Clear all cards and reset status (for new round)
     * ล้างไพ่ทั้งหมดและรีเซ็ตสถานะ (สำหรับรอบใหม่)
     */
    public void clear() {
        cards.clear();
        isDoubled = false;
        isSplit = false;
        isInsurance = false;
        isStood = false;
        bet = 0;
    }

    /**
     * Get string representation of hand for display
     * แสดงมือไพ่ในรูปแบบข้อความ
     */
    public String displayHand() {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            sb.append(c.getShortName()).append(" ");
        }
        sb.append("= ").append(getTotal());
        if (isDoubled) sb.append(" (Doubled)");
        if (isSplit) sb.append(" (Split)");
        if (isStood) sb.append(" (Stood)");
        return sb.toString();
    }
}