// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package juego_coup;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class Influence implements Serializable {
   private static final long serialVersionUID = 1L;
   private final String name;
   private final int id;
   private boolean revealed = false;
   private final boolean tax;
   private final boolean assassinate;
   private final boolean steal;
   private final boolean exchange;
   private final boolean blockAssassin;
   private final boolean blockSteal;
   private final boolean blockAid;
   public static final Influence[] INFLUENCES = new Influence[]{new Influence(1, "Duke", true, false, false, false, false, false, true), new Influence(2, "Assassin", false, true, false, false, false, false, false), new Influence(3, "Captain", false, false, true, false, false, true, false), new Influence(4, "Ambassador", false, false, false, true, false, true, false), new Influence(5, "Contessa", false, false, false, false, true, false, false)};

   private Influence(int id, String name, boolean tax, boolean assassinate, boolean steal, boolean exchange, boolean blockAssassin, boolean blockSteal, boolean blockAid) {
      this.id = id;
      this.name = name;
      this.tax = tax;
      this.assassinate = assassinate;
      this.steal = steal;
      this.exchange = exchange;
      this.blockAssassin = blockAssassin;
      this.blockSteal = blockSteal;
      this.blockAid = blockAid;
   }

   public int getId() {
      return this.id;
   }

   public void setRevealed(boolean revealed) {
      this.revealed = revealed;
   }

   public String getName() {
      return this.name;
   }

   public boolean isRevealed() {
      return this.revealed;
   }

   public void reveal() {
      this.setRevealed(true);
   }

   public boolean canTax() {
      return this.tax;
   }

   public boolean canAssassinate() {
      return this.assassinate;
   }

   public boolean canSteal() {
      return this.steal;
   }

   public boolean canExchange() {
      return this.exchange;
   }

   public boolean canBlockAssassin() {
      return this.blockAssassin;
   }

   public boolean canBlockSteal() {
      return this.blockSteal;
   }

   public boolean canBlockAid() {
      return this.blockAid;
   }

   public static Influence[] getInfluences() {
      Influence[] copies = new Influence[INFLUENCES.length];

      for(int i = 0; i < INFLUENCES.length; ++i) {
         copies[i] = INFLUENCES[i];
      }

      return copies;
   }

   public static Influence random() {
      return INFLUENCES[ThreadLocalRandom.current().nextInt(INFLUENCES.length)];
   }

   public static Influence getInfluenceById(int id) {
      Influence[] var1 = INFLUENCES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Influence inf = var1[var3];
         if (inf.getId() == id) {
            return inf;
         }
      }

      return null;
   }

   public String toString() {
      return this.name + (this.revealed ? " (REVELADA)" : "");
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Influence other = (Influence)obj;
         return this.name.equals(other.name);
      } else {
         return false;
      }
   }
}
