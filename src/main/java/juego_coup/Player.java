package juego_coup;

import java.io.Serializable;

public class Player implements Serializable {
   private static final long serialVersionUID = 1L;
   private final String name;
   private int tokens;
   private Influence influence1;
   private Influence influence2;
   private final int playerNum;
   private boolean out;

   public Player(String name, int playerNum) {
      this(name, playerNum, 2);
   }

   public Player(String name, int playerNum, int tokens) {
      this.name = name;
      this.tokens = tokens;
      this.influence1 = null;
      this.influence2 = null;
      this.playerNum = playerNum;
      this.out = false;
   }

   public void setTokens(int tokens) {
      this.tokens = tokens;
   }

   public void addTokens(int token) {
      this.tokens += token;
   }

   public void removeTokens(int token) {
      this.tokens -= token;
      if (this.tokens < 0) {
         this.tokens = 0;
      }

   }

   public int getTokens() {
      return this.tokens;
   }

   public boolean isOut() {
      return this.out || this.getNumInfluence() == 0;
   }

   public void setOut(boolean out) {
      this.out = out;
   }