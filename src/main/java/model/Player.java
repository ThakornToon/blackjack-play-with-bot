package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Player.java - Represents a player (human or bot) with balance and hands
 * แทนผู้เล่น (มนุษย์หรือบอท) มีเงินคงเหลือและมือไพ่ (รองรับ Split)
 */
public class Player {
    private final String name;
    private double balance;
    private final boolean isHuman;
    private boolean isActive;  // ยังไม่ถูกคัดออก (เงินไม่หมด)
    private final List<Hand> hands;  // รองรับการ Split: สามารถมีหลายมือได้
    private int currentHandIndex;  // มือที่กำลังเล่นอยู่ในรอบนี้

    /**
     * Constructor to initialize a Player with name, starting balance, and type.
     * คอนสตรัคเตอร์สำหรับสร้างวัตถุผู้เล่น ระบุชื่อ เงินเริ่มต้น และประเภท (มนุษย์หรือบอท)
     */
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
        // ตรวจสอบว่าเดิมพันถูกต้อง และยอดเงินมีเพียงพอที่จะวางเดิมพัน
        if (amount <= balance && amount > 0) {
            balance -= amount;
            getCurrentHand().setBet(amount);
            return true;
        }
        return false;
    }

    /**
     * Returns the hand that the player is currently playing in this round.
     * ดึงมือไพ่ใบปัจจุบันที่ผู้เล่นกำลังเล่นอยู่ (รองรับกรณีมีหลายมือจากการ Split)
     */
    public Hand getCurrentHand() {
        // ป้องกันดัชนีเกินขอบเขตโครงสร้าง List
        if (currentHandIndex >= hands.size()) {
            currentHandIndex = hands.size() - 1;
        }
        return hands.get(currentHandIndex);
    }

    /**
     * Returns the index of the hand currently being played.
     */
    public int getCurrentHandIndex() {
        return currentHandIndex;
    }

    public List<Hand> getHands() { return hands; }

    /**
     * Switch turn to the next hand (used when split hands are played sequentially).
     * ขยับการเล่นไปมือถัดไปของผู้เล่นคนนี้ (ใช้สำหรับการเล่นทีละมือหลังการ Split)
     */
    public void nextHand() {
        currentHandIndex++;
    }

    /**
     * Checks if the player has more hands left to play in this round.
     */
    public boolean hasMoreHands() {
        return currentHandIndex < hands.size() - 1;
    }

    /**
     * Adds a new hand created during a Split action.
     */
    public void addSplitHand(Hand newHand) {
        hands.add(newHand);
    }

    // Getters and Setters
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public boolean isHuman() { return isHuman; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    /**
     * Deduct money from balance (used when placing bet or double)
     */
    public boolean deductBalance(double amount) {
        // ตรวจสอบยอดเงินก่อนหักบัญชีจริง (เช่น เมื่อผู้เล่นกด Double Down หรือแยกไพ่ Split)
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