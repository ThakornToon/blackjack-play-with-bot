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
    private int numDecks;

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
        for (int d = 0; d < numDecks; d++) {
            for (Card.Suit suit : Card.Suit.values()) {
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
        Collections.shuffle(cards);
    }

    /**
     * Draw a card from the shoe (if empty, reinitialize)
     * จั่วไพ่ 1 ใบ (ถ้าไพ่หมด ให้สร้างสำรับใหม่)
     */
    public Card drawCard() {
        if (cards.isEmpty()) {
            init();
        }
        return cards.remove(0);
    }

    /**
     * Get number of cards remaining
     * จำนวนไพ่ที่เหลือในรองเท้า
     */
    public int remainingCards() {
        return cards.size();
    }
}