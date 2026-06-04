package model;

/**
 * BasicStrategy.java - Optimal Blackjack strategy for bots
 * กลยุทธ์ที่ดีที่สุดสำหรับบอท ตามหลัก Basic Strategy ของ Blackjack
 *
 * อ้างอิงจากตาราง Basic Strategy มาตรฐาน (เดลเลอร์ยืนที่ 17)
 */
public class BasicStrategy {

    /**
     * Get the optimal action based on player's hand and dealer's upcard
     * @param playerTotal แต้มรวมของผู้เล่น (ปรับ Ace แล้ว)
     * @param dealerUpCardValue ค่าไพ่ใบแรกของดีลเลอร์ (2-11, Ace=11)
     * @param isSoft true ถ้ามี Ace และนับเป็น 11 (Soft hand)
     * @param canDouble true ถ้ายังสามารถ Double ได้ (มือมี 2 ใบแรก)
     * @param canSplit true ถ้าไพ่สองใบแรกเป็นคู่กัน
     * @param pairRankValue ถ้าเป็นคู่ ค่าของไพ่คู่นั้น (เช่น 10, 11)
     * @return "H" (Hit), "S" (Stand), "D" (Double if possible else Hit), "P" (Split)
     */
    public static String getAction(int playerTotal, int dealerUpCardValue,
                                   boolean isSoft, boolean canDouble,
                                   boolean canSplit, int pairRankValue) {
        int d = dealerUpCardValue;

        // ========== PAIR SPLITTING STRATEGY ==========
        if (canSplit && pairRankValue > 0) {
            // Always split Aces and 8s
            if (pairRankValue == 11) return "P";  // Ace
            if (pairRankValue == 8) return "P";
            // Never split 10s or 5s
            if (pairRankValue == 10) return "S";
            if (pairRankValue == 5) {
                if (d >= 2 && d <= 9 && canDouble) return "D";
                else return "H";
            }
            // 9s: split except against 7,10,11
            if (pairRankValue == 9) {
                if (d == 7 || d == 10 || d == 11) return "S";
                else return "P";
            }
            // 7s: split if dealer 2-7
            if (pairRankValue == 7) {
                if (d >= 2 && d <= 7) return "P";
                else return "H";
            }
            // 6s: split if dealer 2-6
            if (pairRankValue == 6) {
                if (d >= 2 && d <= 6) return "P";
                else return "H";
            }
            // 4s: split only vs 5 or 6
            if (pairRankValue == 4) {
                if (d == 5 || d == 6) return "P";
                else return "H";
            }
            // 3s and 2s: split if dealer 2-7
            if (pairRankValue == 3 || pairRankValue == 2) {
                if (d >= 2 && d <= 7) return "P";
                else return "H";
            }
        }

        // ========== SOFT HANDS (มี Ace นับเป็น 11) ==========
        if (isSoft) {
            if (playerTotal >= 19) return "S";  // 19+ always stand
            if (playerTotal == 18) {
                if (d == 2 || d == 7 || d == 8) return "S";
                if (d >= 3 && d <= 6 && canDouble) return "D";
                if (d == 9 || d == 10 || d == 11) return "H";
                return "S";
            }
            if (playerTotal == 17) {
                if (d >= 3 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            if (playerTotal >= 15 && playerTotal <= 16) {
                if (d >= 4 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            if (playerTotal == 14) {
                if (d >= 5 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            if (playerTotal == 13) {
                if (d >= 5 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            return "H";
        }

        // ========== HARD HANDS (ไม่มี Ace หรือ Ace นับเป็น 1) ==========
        if (playerTotal >= 17) return "S";
        if (playerTotal >= 13 && playerTotal <= 16) {
            if (d >= 2 && d <= 6) return "S";
            else return "H";
        }
        if (playerTotal == 12) {
            if (d >= 4 && d <= 6) return "S";
            else return "H";
        }
        if (playerTotal == 11) {
            if (canDouble) return "D";
            else return "H";
        }
        if (playerTotal == 10) {
            if (d >= 2 && d <= 9 && canDouble) return "D";
            else return "H";
        }
        if (playerTotal == 9) {
            if (d >= 3 && d <= 6 && canDouble) return "D";
            else return "H";
        }
        return "H";
    }

    /**
     * Check if a hand is "soft" (has Ace counted as 11)
     * ตรวจสอบว่าเป็น Soft hand หรือไม่ (มี Ace และรวมแต้มยังไม่เกิน 21)
     */
    public static boolean isSoftHand(Hand hand) {
        int total = 0;
        boolean hasAce = false;
        for (Card c : hand.getCards()) {
            total += c.getValue();
            if (c.getRank() == Card.Rank.ACE) hasAce = true;
        }
        // If has Ace and total <= 21, it's soft (Ace counted as 11)
        return hasAce && total <= 21;
    }
}