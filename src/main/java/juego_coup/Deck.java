// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package juego_coup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Deck implements Serializable {
   private static final long serialVersionUID = 1L;
   private final List<Influence> drawPile = new ArrayList();
   private final List<Influence> discardPile = new ArrayList();

   public Deck() {
      for(int i = 0; i < 3; ++i) {
         this.drawPile.addAll(Arrays.asList(Influence.getInfluences()));
      }

      this.shuffle();
   }

   private void shuffle() {
      Collections.shuffle(this.drawPile);
   }

   private void replenishIfNeeded() {
      if (this.drawPile.isEmpty() && !this.discardPile.isEmpty()) {
         this.drawPile.addAll(this.discardPile);
         this.discardPile.clear();
         this.shuffle();
      }

   }

   public Influence draw() {
      this.replenishIfNeeded();
      if (this.drawPile.isEmpty()) {
         throw new IllegalStateException("No influences left to draw.");
      } else {
         return (Influence)this.drawPile.remove(0);
      }
   }

   public void discard(Influence influence) {
      if (influence != null) {
         this.discardPile.add(influence);
      }

   }

   public void returnCard(Influence influence) {
      if (influence != null) {
         this.drawPile.add(influence);
         this.shuffle();
      }

   }

   public void returnToDeck(Collection<Influence> influences) {
      boolean added = false;
      Iterator var3 = influences.iterator();

      while(var3.hasNext()) {
         Influence influence = (Influence)var3.next();
         if (influence != null) {
            this.drawPile.add(influence);
            added = true;
         }
      }

      if (added) {
         this.shuffle();
      }

   }
}
