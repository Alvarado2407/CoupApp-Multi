package juego_coup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements Serializable {
   private static final long serialVersionUID = 1L;
   public static final int TYPE_MESSAGE = 1;
   public static final int TYPE_GAME_STATE = 2;
   public static final int TYPE_GAME_OVER = 3;
   public static final int TYPE_CONNECT = 4;
   public static final int TYPE_DISCONNECT = 5;
   public static final int TYPE_PROMPT_ACTION = 10;
   public static final int TYPE_PROMPT_CHALLENGE = 11;
   public static final int TYPE_PROMPT_BLOCK = 12;
   public static final int TYPE_PROMPT_BLOCK_CHALLENGE = 13;
   public static final int TYPE_PROMPT_CARDS = 14;
   public static final int TYPE_PROMPT_INFLUENCE_LOSS = 15;
   public static final int TYPE_ACTION = 20;
   public static final int TYPE_CHALLENGE = 21;
   public static final int TYPE_BLOCK = 22;
   public static final int TYPE_PASS = 23;
   public static final int TYPE_CHOOSE_CARDS = 24;
   public static final int TYPE_CHALLENGE_BLOCK = 25;
   public static final int TYPE_CHOOSE_INFLUENCE = 26;
   public static final int ACTION_INCOME = 101;
   public static final int ACTION_FOREIGN_AID = 102;
   public static final int ACTION_COUP = 103;
   public static final int ACTION_TAX = 104;
   public static final int ACTION_ASSASSINATE = 105;
   public static final int ACTION_STEAL = 106;
   public static final int ACTION_EXCHANGE = 107;
   public static final int BLOCK_GENERAL = 200;
   private final int type;
   private final String message;
   private final int senderPlayerNum;
   private int targetPlayerNum;
   private int actionCode;
   private int choiceIndex;
   private int[] chosenIndices;
   private Influence[] cardPool;

   public Command(int type, String message) {
      this.type = type;
      this.message = message;
      this.senderPlayerNum = -1;
      this.targetPlayerNum = -1;
      this.actionCode = 0;
      this.cardPool = null;
      this.chosenIndices = null;
   }

   public Command(int type, int senderPlayerNum, int actionCode, int targetPlayerNum) {
      this.type = type;
      this.senderPlayerNum = senderPlayerNum;
      this.actionCode = actionCode;
      this.targetPlayerNum = targetPlayerNum;
      this.message = null;
      this.cardPool = null;
      this.chosenIndices = null;
   }

   public Command(int type, int senderPlayerNum) {
      this.type = type;
      this.senderPlayerNum = senderPlayerNum;
      this.message = null;
      this.actionCode = 0;
      this.targetPlayerNum = -1;
      this.cardPool = null;
      this.chosenIndices = null;
   }

   public Command(int type, int senderPlayerNum, int value) {
      this.type = type;
      this.senderPlayerNum = senderPlayerNum;
      this.message = null;
      this.targetPlayerNum = -1;
      this.cardPool = null;
      this.chosenIndices = null;
      if (type == 26) {
         this.choiceIndex = value;
         this.actionCode = 0;
      } else {
         this.actionCode = value;
      }

   }

   public int getType() {
      return this.type;
   }

   public String getMessage() {
      return this.message;
   }

   public int getSenderPlayerNum() {
      return this.senderPlayerNum;
   }

   public int getTargetPlayerNum() {
      return this.targetPlayerNum;
   }

   public int getActionCode() {
      return this.actionCode;
   }

   public int getChoiceIndex() {
      return this.choiceIndex;
   }

   public void setChoiceIndex(int choiceIndex) {
      this.choiceIndex = choiceIndex;
   }

   public void setChosenIndices(int[] indices) {
      this.chosenIndices = indices;
   }

   public List<Integer> getChosenIndices() {
      if (this.chosenIndices == null) {
         return new ArrayList();
      } else {
         List<Integer> list = new ArrayList();
         int[] var2 = this.chosenIndices;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int i = var2[var4];
            list.add(i);
         }

         return list;
      }
   }

   public Influence[] getCardPool() {
      return this.cardPool;
   }

   public void setCardPool(Influence[] cardPool) {
      this.cardPool = cardPool;
   }

   public String toString() {
      int var10000 = this.type;
      return "Command{type=" + var10000 + ", sender=" + this.senderPlayerNum + ", action=" + this.actionCode + ", target=" + this.targetPlayerNum + ", msg='" + this.message + "', cardPool=" + Arrays.toString(this.cardPool) + ", choice=" + Arrays.toString(this.chosenIndices) + "}";
   }
}
