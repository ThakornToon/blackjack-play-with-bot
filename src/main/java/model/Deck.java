package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deck.java - Represents a shoe of multiple card decks
 * แทนสำรับไพ่ (Shoe) ที่ประกอบด้วยไพ่หลายสำรับ
 */
public class Deck {
    private List<Card> cards;
    private final int numDecks;

    /**
     * Constructor
     * @param numDecks จำนวนสำรับไพ่ที่ใช้ (ปกติ 4-6 สำรับ)
     */
    public Deck(int numDecks) {
        this.numDecks = numDecks;
        init();
    }

    /**
     * Initialize and shuffle the shoe
     * สร้างและสับไพ่ในรองเท้า
     */
    private void init() {
        cards = new ArrayList<>();
        // วนลูปตามจำนวนสำรับที่ต้องการ (เช่น 4 สำรับ)
        for (int d = 0; d < numDecks; d++) {
            // ดอกไพ่ทั้ง 4 ชนิด
            for (Card.Suit suit : Card.Suit.values()) {
                // แต้มไพ่ทั้ง 13 ลำดับ
                for (Card.Rank rank : Card.Rank.values()) {
                    cards.add(new Card(suit, rank));
                }
            }
        }
        shuffle();
    }

    /**
     * Shuffle the cards
     * สับไพ่
     */
    public void shuffle() {
        // ใช้ Collections.shuffle เพื่อสับไพ่ทั้งหมดในสำรับแบบสุ่ม
        Collections.shuffle(cards);
    }

    /**
     * Draw a card
     * จั่วไพ่ 1 ใบ
     */
    public Card drawCard() {
        // กฎการสับไพ่ใหม่เมื่อไพ่ใกล้หมด (Reshuffle early)
        // หากจำนวนไพ่เหลือน้อยกว่า 20% ของจำนวนไพ่ทั้งหมดใน Shoe (numDecks * 52)
        // จะทำการสร้างสำรับและสับไพ่ใหม่ทันที เพื่อป้องกันการนับไพ่ (Card Counting) ตามหลักคาสิโนจริง
        if (cards.size() < numDecks * 52 * 0.2) {
            init(); // reshuffle early
        }
        // ดึงไพ่ใบแรกสุด (ดัชนี 0) ออกจากสำรับและส่งคืนค่า
        return cards.remove(0);
    }
}