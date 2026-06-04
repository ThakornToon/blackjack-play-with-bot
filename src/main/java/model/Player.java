package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Player.java - Represents a player (human or bot) with balance and hands
 * แทนผู้เล่น (มนุษย์หรือบอท) มีเงินคงเหลือและมือไพ่ (รองรับ Split)
 */
public class Player {
    private String name;
    private double balance;
    private boolean isHuman;
    private boolean isActive;      // ยังไม่ถูกคัดออก (เงินไม่หมด)
    private List<Hand> hands;      // รองรับการ Split: สามารถมีหลายมือได้
    private int currentHandIndex;  // มือที่กำลังเล่นอยู่ในรอบนี้

    public Player(String name, double initialBalance, boolean isHuman) {
        this.name = name;
        this.balance = initialBalance;
        this.isHuman = isHuman;
        this.isActive = true;
        this.hands = new ArrayList<>();
        resetHands();
    }

    /**
     * Reset hands for a new round
     * รีเซ็ตมือทั้งหมดสำหรับรอบใหม่
     */
    public void resetHands() {
        hands.clear();
        hands.add(new Hand(0));  // เริ่มต้นด้วยมือเดียว ยังไม่วางเดิมพัน
        currentHandIndex = 0;
    }

    /**
     * Place a bet on the current hand
     * วางเดิมพันบนมือปัจจุบัน
     * @return true if successful, false if insufficient balance
     */
    public boolean placeBet(int amount) {
        if (amount <= balance && amount > 0) {
            balance -= amount;
            getCurrentHand().setBet(amount);
            return true;
        }
        return false;
    }

    /**
     * Add winnings to balance (e.g., after round settlement)
     * เพิ่มเงินรางวัลเข้าสู่ยอดคงเหลือ
     */
    public void addWinnings(int amount) {
        balance += amount;
    }

    public Hand getCurrentHand() {
        if (currentHandIndex >= hands.size()) {
            currentHandIndex = hands.size() - 1;
        }
        return hands.get(currentHandIndex);
    }

    public List<Hand> getHands() { return hands; }

    public void nextHand() {
        currentHandIndex++;
    }

    public void resetHandIndex() {
        currentHandIndex = 0;
    }

    public boolean hasMoreHands() {
        return currentHandIndex < hands.size() - 1;
    }

    public void addSplitHand(Hand newHand) {
        hands.add(newHand);
    }

    public int getCurrentHandIndex() { return currentHandIndex; }

    // Getters and Setters
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public boolean isHuman() { return isHuman; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    /**
     * Deduct money from balance (used when placing bet or double)
     */
    public boolean deductBalance(double amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Add money to balance (winning)
     */
    public void addBalance(double amount) {
        balance += amount;
    }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", balance) + ")";
    }
}