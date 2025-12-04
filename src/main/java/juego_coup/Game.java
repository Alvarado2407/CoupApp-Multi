
package juego_coup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game implements Serializable {
   private static final long serialVersionUID = 1L;
   private Player[] players;
   private final Deck deck = new Deck();
   private int currentPlayerIndex = 0;
   private int gameStatus = 0;

   public Game() {
   }

   public Player[] getPlayers() {
      return this.players;
   }

   public int getCurrentPlayerIndex() {
      return this.currentPlayerIndex;
   }

   public int getGameStatus() {
      return this.gameStatus;
   }

   public Player getActingPlayer() {
      return this.players[this.currentPlayerIndex];
   }

   public void setPlayersAtStart(List<Player> joinedPlayers) {
      int numPlayers = joinedPlayers.size();
      if (numPlayers >= 2 && numPlayers <= 6) {
         this.players = (Player[])joinedPlayers.toArray(new Player[0]);

         for(int i = 0; i < numPlayers; ++i) {
            Player p = this.players[i];
            int initialTokens = numPlayers == 2 && i < 2 ? 1 : 2;
            p.setTokens(initialTokens);
            p.setInfluence1(this.deck.draw());
            p.setInfluence2(this.deck.draw());
         }

         this.gameStatus = 1;
         System.out.println("Configuración del juego completa. Enviando estado inicial a clientes.");
      } else {
         System.err.println("Error: Número de jugadores inválido.");
      }
   }

   public String getAvailablePlayersList(int excludingPlayerNum) {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < this.players.length; ++i) {
         if (i != excludingPlayerNum && this.players[i].getNumInfluence() > 0) {
            if (sb.length() > 0) {
               sb.append(", ");
            }

            sb.append(String.format("[%d] %s", i, this.players[i].getName()));
         }
      }

      return sb.toString();
   }

   public void advanceTurn() {
      int max = this.players.length;

      for(this.currentPlayerIndex = (this.currentPlayerIndex + 1) % max; this.players[this.currentPlayerIndex].isOut() && !this.checkWin(); this.currentPlayerIndex = (this.currentPlayerIndex + 1) % max) {
      }

   }

   public boolean income(int player_i) {
      this.players[player_i].addTokens(1);
      return true;
   }

   public boolean foreignAid(int player_i) {
      this.players[player_i].addTokens(2);
      return true;
   }