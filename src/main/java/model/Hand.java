package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand.java - Represents a single hand of cards for a player
 * แทนมือไพ่ 1 ชุดของผู้เล่น (รองรับการแยกไพ่ Split)
 */
public class Hand {
    private final List<Card> cards;
    private int bet;  // เงินเดิมพันของมือนี้
    private boolean isDoubled;  // ระบุว่าได้ทำการ Double แล้วหรือไม่
    private boolean isSplit;  // ระบุว่าเกิดจากการ Split หรือไม่
    private boolean isInsurance;  // ระบุว่าซื้อ Insurance หรือไม่
    private boolean isStood;  // ระบุว่าเลือก Stand แล้วหรือยัง

    /**
     * Constructor for Hand with an initial bet.
     * คอนสตรัคเตอร์สร้างมือไพ่พร้อมกำหนดเงินเดิมพันเริ่มต้น
     */
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

    /**
     * Double the current bet and mark this hand as doubled.
     * เพิ่มเดิมพันเป็น 2 เท่าสำหรับกฎ Double Down
     */
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
        // นับแต้มรวมโดยตั้งค่า Ace ทุกใบให้มีค่าสูงสุดเป็น 11 ก่อน
        for (Card c : cards) {
            total += c.getValue();
            if (c.getRank() == Card.Rank.ACE) aces++;
        }
        // If total > 21 and we have Aces, change Ace from 11 to 1
        // หากแต้มเกิน 21 และในมือมี Ace (ที่นับเป็น 11), ให้ลดค่าของ Ace ลงเหลือ 1 คะแนนทีละใบ (ลบออกทีละ 10 แต้ม)
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    /**
     * Check if this hand is a natural Blackjack
     * ตรวจสอบว่าเป็น Blackjack ธรรมชาติหรือไม่
     */
    public boolean isBlackjack() {
        // Blackjack ธรรมชาติเกิดจากการได้ 21 แต้มด้วยไพ่ 2 ใบแรกเท่านั้น และต้องไม่ใช่มือที่เกิดจากการ Split
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
     * Check if hand is soft (contains Ace counted as 11 without busting)
     * ตรวจสอบว่าเป็น Soft hand หรือไม่ (มี Ace และนับเป็น 11 แล้วไม่เกิน 21)
     */
    public boolean isSoft() {
        int total = 0;
        int aces = 0;

        for (Card c : cards) {
            total += c.getValue();  // Ace นับเป็น 11 ก่อน
            if (c.getRank() == Card.Rank.ACE) {
                aces++;
            }
        }

        // คล้ายกับ getTotal() เพื่อรองรับกรณีที่ผู้เล่นมี Ace หลายใบ (เช่น A, A, 5)
        // เราต้องตรวจสอบว่าหลังจากหักลบแต้มของ Ace บางใบที่จำเป็นต้องเป็น 1 แล้ว
        // ยังคงมี Ace หลงเหลืออยู่อย่างน้อย 1 ใบที่ยังนับแต้มเป็น 11 คะแนนได้โดยแต้มรวมไม่เกิน 21
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        // Soft hand = มี Ace และการนับ Ace เป็น 11 ไม่ทำให้แต้มเกิน 21
        // (กล่าวคือ มี Ace อย่างน้อย 1 ใบในมือที่ยังมีค่าเป็น 11 แต้มอยู่)
        return aces > 0;
    }

    /**
     * Get string representation of hand for display
     * แสดงมือไพ่ในรูปแบบข้อความ
     */
    public String displayHand() {
        StringBuilder sb = new StringBuilder();
        // รวมสัญลักษณ์ไพ่แต่ละใบ (เช่น "A♥ 10♦ ")
        for (Card c : cards) {
            sb.append(c.getShortName()).append(" ");
        }
        sb.append("= ").append(getTotal());
        // เพิ่มสถานะพิเศษเพื่อเป็นข้อมูลในการเล่น
        if (isDoubled) sb.append(" (Doubled)");
        if (isSplit) sb.append(" (Split)");
        if (isStood) sb.append(" (Stood)");
        return sb.toString();
    }
}