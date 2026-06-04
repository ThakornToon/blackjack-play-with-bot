package model;

/**
 * Card.java - Represents a playing card with suit and rank
 * แทนไพ่ 1 ใบ มีดอกและเลข/หน้าไพ่
 */
public class Card {
    // Enum for card suits (ดอกไพ่)
    public enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }

    // Enum for card ranks (เลข/หน้าไพ่)
    public enum Rank { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }

    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Get the numerical value of the card for Blackjack
     * ค่าของไพ่ตามกติกา Blackjack (Ace = 11, face cards = 10)
     */
    public int getValue() {
        if (rank == Rank.ACE) return 11;
        if (rank.ordinal() >= Rank.TEN.ordinal()) return 10;
        return rank.ordinal() + 2;  // TWO=2, THREE=3, ...
    }

    /**
     * Get short name for display (e.g., "AH" for Ace of Hearts)
     * ชื่อย่อสำหรับแสดงผล เช่น "AH" แทน Ace of Hearts
     */
    public String getShortName() {
        String r;
        switch (rank) {
            case TEN: r = "10"; break;
            case JACK: r = "J"; break;
            case QUEEN: r = "Q"; break;
            case KING: r = "K"; break;
            case ACE: r = "A"; break;
            default: r = String.valueOf(getValue());
        }
        String s;
        switch (suit) {
            case HEARTS: s = "♥"; break;
            case DIAMONDS: s = "♦"; break;
            case CLUBS: s = "♣"; break;
            case SPADES: s = "♠"; break;
            default: s = "?";
        }
        return r + s;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}