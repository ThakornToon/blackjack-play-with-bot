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

    /**
     * Constructor to initialize a Card object with a specific suit and rank.
     * คอนสตรัคเตอร์สำหรับสร้างวัตถุไพ่โดยระบุดอกและแต้ม/หน้าไพ่
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Get the numerical value of the card for Blackjack
     * ค่าของไพ่ตามกติกา Blackjack (Ace = 11, face cards = 10)
     */
    public int getValue() {
        // ในกติกา Blackjack, Ace จะเริ่มต้นด้วยค่าเป็น 11 แต้ม (และอาจลดเป็น 1 ในภายหลังหากแต้มเกิน)
        if (rank == Rank.ACE) return 11;
        
        // ไพ่หน้าคน (Jack, Queen, King) หรือไพ่ 10 มีค่าเท่ากับ 10 แต้ม
        // เราใช้ rank.ordinal() เพื่อเปรียบเทียบลำดับการประกาศใน Enum
        if (rank.ordinal() >= Rank.TEN.ordinal()) return 10;
        
        // สำหรับไพ่ตัวเลขอื่นๆ (2-9), ค่าจะตรงกับลำดับของมันบวกด้วย 2 (เช่น Rank.TWO มี ordinal เป็น 0 -> 0 + 2 = 2 แต้ม)
        return rank.ordinal() + 2;  // TWO=2, THREE=3, ...
    }

    /**
     * Get short name for display (e.g., "A♥" for Ace of Hearts)
     * ชื่อย่อสำหรับแสดงผล เช่น "A♥" แทน Ace of Hearts
     */
    public String getShortName() {
        String r;
        // แปลง Rank เป็นรหัสย่อสำหรับแสดงผลบนการ์ดใน GUI
        switch (rank) {
            case TEN: r = "10"; break;
            case JACK: r = "J"; break;
            case QUEEN: r = "Q"; break;
            case KING: r = "K"; break;
            case ACE: r = "A"; break;
            default: r = String.valueOf(getValue());
        }
        String s;
        // แปลง Suit หรือดอกไพ่เป็นสัญลักษณ์ Unicode (♥, ♦, ♣, ♠) เพื่อความสวยงามใน GUI
        switch (suit) {
            case HEARTS: s = "♥"; break;
            case DIAMONDS: s = "♦"; break;
            case CLUBS: s = "♣"; break;
            case SPADES: s = "♠"; break;
            default: s = "?";
        }
        return r + s;
    }

    /**
     * Getter for rank of the card.
     */
    public Rank getRank() { return rank; }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}