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
     * @param hand มือไพ่ของผู้เล่น (ใช้ hand.isSoft() และ hand.getTotal())
     * @param dealerUpCardValue ค่าไพ่ใบแรกของดีลเลอร์ (2-11, Ace=11)
     * @param canDouble true ถ้ายังสามารถ Double ได้ (มือมี 2 ใบแรก)
     * @param canSplit true ถ้าไพ่สองใบแรกเป็นคู่กัน
     * @param pairRankValue ถ้าเป็นคู่ ค่าของไพ่คู่นั้น
     * @return "H" (Hit), "S" (Stand), "D" (Double if possible else Hit), "P" (Split)
     */
    public static String getAction(Hand hand, int dealerUpCardValue,
                                   boolean canDouble, boolean canSplit, int pairRankValue) {

        int playerTotal = hand.getTotal();
        boolean isSoft = hand.isSoft();  // ใช้ method ของ Hand โดยตรง
        int d = dealerUpCardValue;

        // ========== PAIR SPLITTING STRATEGY ==========
        // กลยุทธ์การแยกไพ่คู่เมื่อไพ่ 2 ใบแรกมีค่าเท่ากัน
        if (canSplit && pairRankValue > 0) {
            // Always split Aces and 8s
            // แยก Ace และ 8 เสมอ: การแยกคู่ Ace เพิ่มโอกาสได้ 21 สองชุด ส่วนคู่ 8 (แต้มรวม 16 เป็นจุดวิกฤต) แยกเพื่อเลี่ยงความเสี่ยง
            if (pairRankValue == 11) return "P";  // Ace
            if (pairRankValue == 8) return "P";
            
            // Never split 10s or 5s
            // ห้ามแยกคู่ 10 (เพราะ 20 แต้มดีมากอยู่แล้ว) หรือคู่ 5 (แต้มรวม 10 เหมาะกับการ Double มากกว่า)
            if (pairRankValue == 10) return "S";
            if (pairRankValue == 5) {
                if (d >= 2 && d <= 9 && canDouble) return "D";
                else return "H";
            }
            
            // 9s: split except against 7,10,11
            // แยกคู่ 9 ยกเว้นเมื่อเจอไพ่ดีลเลอร์ 7 (เพราะดีลเลอร์มีแนวโน้มได้ 17 และเราได้ 18 ซึ่งชนะอยู่แล้ว), 10, หรือ Ace
            if (pairRankValue == 9) {
                if (d == 7 || d == 10 || d == 11) return "S";
                else return "P";
            }
            
            // 7s: split if dealer 2-7
            // แยกคู่ 7 หากไพ่ดีลเลอร์ใบแรกคือ 2 ถึง 7
            if (pairRankValue == 7) {
                if (d >= 2 && d <= 7) return "P";
                else return "H";
            }
            
            // 6s: split if dealer 2-6
            // แยกคู่ 6 หากไพ่ดีลเลอร์ใบแรกคือ 2 ถึง 6
            if (pairRankValue == 6) {
                if (d >= 2 && d <= 6) return "P";
                else return "H";
            }
            
            // 4s: split only vs 5 or 6
            // แยกคู่ 4 เฉพาะเมื่อเจอไพ่ดีลเลอร์ 5 หรือ 6 เท่านั้น
            if (pairRankValue == 4) {
                if (d == 5 || d == 6) return "P";
                else return "H";
            }
            
            // 3s and 2s: split if dealer 2-7
            // แยกคู่ 3 และคู่ 2 หากไพ่ดีลเลอร์ใบแรกคือ 2 ถึง 7
            if (pairRankValue == 3 || pairRankValue == 2) {
                if (d >= 2 && d <= 7) return "P";
                else return "H";
            }
        }

        // ========== SOFT HANDS (มี Ace นับเป็น 11) ==========
        // ไพ่ Soft Hand คือมี Ace 1 ใบที่นับแต้มเป็น 11 ได้โดยไม่เกิน 21 แต้ม
        if (isSoft) {
            if (playerTotal >= 19) return "S";  // 19+ always stand (แต้มสูงพอแล้ว ยืนเสมอ)
            
            if (playerTotal == 18) {
                // Soft 18: ยืนเมื่อเจอดีลเลอร์ 2, 7, 8; เพิ่มเดิมพัน (Double) เมื่อเจอดีลเลอร์ 3-6; จั่ว (Hit) เมื่อเจอแต้มสูง 9, 10, A
                if (d == 2 || d == 7 || d == 8) return "S";
                if (d >= 3 && d <= 6 && canDouble) return "D";
                if (d == 9 || d == 10 || d == 11) return "H";
                return "S";
            }
            
            if (playerTotal == 17) {
                // Soft 17: เพิ่มเดิมพันเมื่อเจอดีลเลอร์ 3-6 มิฉะนั้นให้จั่ว
                if (d >= 3 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            
            if (playerTotal >= 15 && playerTotal <= 16) {
                // Soft 15-16: เพิ่มเดิมพันเมื่อเจอดีลเลอร์ 4-6 มิฉะนั้นให้จั่ว
                if (d >= 4 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            
            if (playerTotal == 14) {
                // Soft 14: เพิ่มเดิมพันเมื่อเจอดีลเลอร์ 5-6 มิฉะนั้นให้จั่ว
                if (d >= 5 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            
            if (playerTotal == 13) {
                // Soft 13: เพิ่มเดิมพันเมื่อเจอดีลเลอร์ 5-6 มิฉะนั้นให้จั่ว
                if (d >= 5 && d <= 6 && canDouble) return "D";
                else return "H";
            }
            return "H";
        }

        // ========== HARD HANDS (ไม่มี Ace หรือ Ace นับเป็น 1) ==========
        // ไพ่ Hard Hand คือไพ่ไม่มี Ace หรือ Ace นับแต้มเป็น 1 คะแนนเท่านั้น
        if (playerTotal >= 17) return "S"; // Hard 17+: ยืนเสมอเพราะโอกาสจั่วเพิ่มแล้วเกิน 21 สูงมาก
        
        if (playerTotal >= 13 && playerTotal <= 16) {
            // Hard 13-16: ยืนเมื่อดีลเลอร์มีโอกาสแต้มเกิน (ไพ่ใบแรกดีลเลอร์คือ 2-6) มิฉะนั้นจั่วเพิ่ม
            if (d >= 2 && d <= 6) return "S";
            else return "H";
        }
        
        if (playerTotal == 12) {
            // Hard 12: ยืนเมื่อเจอดีลเลอร์ 4-6 (ดีลเลอร์เสี่ยงเจ๊ง) จั่วเมื่อเจอดีลเลอร์ 2, 3 หรือ 7 ขึ้นไป
            if (d >= 4 && d <= 6) return "S";
            else return "H";
        }
        
        if (playerTotal == 11) {
            // Hard 11: ดับเบิ้ลเดิมพันเสมอเพื่อโอกาสได้ 21 แต้มสูง
            if (canDouble) return "D";
            else return "H";
        }
        
        if (playerTotal == 10) {
            // Hard 10: ดับเบิ้ลเดิมพันเมื่อเจอดีลเลอร์ 2-9
            if (d >= 2 && d <= 9 && canDouble) return "D";
            else return "H";
        }
        
        if (playerTotal == 9) {
            // Hard 9: ดับเบิ้ลเดิมพันเฉพาะเมื่อเจอดีลเลอร์ 3-6 เท่านั้น
            if (d >= 3 && d <= 6 && canDouble) return "D";
            else return "H";
        }
        
        // ต่ำกว่า 9 แต้มลงไป: จั่วเพิ่มเสมอ
        return "H";
    }
}