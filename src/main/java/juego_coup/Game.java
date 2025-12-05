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
            this.players = (Player[]) joinedPlayers.toArray(new Player[0]);

            for (int i = 0; i < numPlayers; ++i) {
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

        for (int i = 0; i < this.players.length; ++i) {
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

        for (this.currentPlayerIndex = (this.currentPlayerIndex + 1) % max; this.players[this.currentPlayerIndex].isOut() && !this.checkWin(); this.currentPlayerIndex = (this.currentPlayerIndex + 1) % max) {
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

    public boolean coup(int player_i, int player_target, int influenceIndex) {
        if (this.players[player_i].getTokens() < 7) {
            return false;
        } else {
            Influence lost = this.loseInfluence(player_target, influenceIndex);
            if (lost == null) {
                return false;
            } else {
                this.players[player_i].removeTokens(7);
                return true;
            }
        }
    }

    public boolean tax(int player_i) {
        this.players[player_i].addTokens(3);
        return true;
    }

    public boolean assassination(int player_killer, int player_target, int influenceIndex) {
        if (this.players[player_killer].getTokens() < 3) {
            return false;
        } else {
            this.players[player_killer].removeTokens(3);
            Influence lost = this.loseInfluence(player_target, influenceIndex);
            return lost != null;
        }
    }

    public boolean steal(int player_thief, int player_target) {
        int amount = Math.min(2, this.players[player_target].getTokens());
        if (amount > 0) {
            this.players[player_thief].addTokens(amount);
            this.players[player_target].removeTokens(amount);
        }

        return true;
    }

    public List<Influence> startExchange(int player_i) {
        Player exchanging = this.players[player_i];
        List<Influence> pool = new ArrayList();
        if (exchanging.getInfluence1() != null) {
            pool.add(exchanging.getInfluence1());
        }

        if (exchanging.getInfluence2() != null) {
            pool.add(exchanging.getInfluence2());
        }

        pool.add(this.deck.draw());
        pool.add(this.deck.draw());
        return pool;
    }

    public void finishExchange(int player_i, List<Influence> keptInfluences, List<Influence> returnedInfluences) {
        Player p = this.players[player_i];
        p.setInfluence1((Influence) keptInfluences.get(0));
        if (keptInfluences.size() > 1) {
            p.setInfluence2((Influence) keptInfluences.get(1));
        } else {
            p.setInfluence2((Influence) null);
        }

        this.deck.returnToDeck(returnedInfluences);
    }

    public int checkValidTarget(int current_p, int targetPlayerIndex) {
        if (targetPlayerIndex >= 0 && targetPlayerIndex < this.players.length && !this.players[targetPlayerIndex].isOut() && targetPlayerIndex != current_p) {
            return targetPlayerIndex;
        } else {
            throw new IllegalArgumentException("Target player index is invalid.");
        }
    }

    public boolean checkWin() {
        int numActive = 0;
        Player[] var2 = this.players;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Player player = var2[var4];
            if (!player.isOut()) {
                ++numActive;
            }
        }

        if (numActive <= 1 && this.gameStatus == 1) {
            this.gameStatus = 2;
            return true;
        } else {
            return false;
        }
    }

    public Player getWinner() {
        if (this.gameStatus != 2) {
            return null;
        } else {
            Player[] var1 = this.players;
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Player player = var1[var3];
                if (!player.isOut()) {
                    return player;
                }
            }

            return null;
        }
    }

    public void receiveTokens(int playerNum, int amount) {
        this.players[playerNum].addTokens(amount);
    }

    public void payTokens(int playerNum, int amount) {
        this.players[playerNum].removeTokens(amount);
    }

    public void stealTokens(int thiefNum, int targetNum, int maxAmount) {
        Player target = this.players[targetNum];
        int actualSteal = Math.min(maxAmount, target.getTokens());
        target.removeTokens(actualSteal);
        this.players[thiefNum].addTokens(actualSteal);
    }

    public Influence loseInfluence(int playerNum, int influenceIndex) {
        Player player = this.players[playerNum];
        Influence lostCard = player.loseInfluence(influenceIndex);
        if (lostCard != null) {
            this.deck.discard(lostCard);
        }

        if (player.getNumInfluence() <= 0) {
            player.setOut(true);
            this.checkWin();
        }

        return lostCard;
    }

    public void shuffleAndReplaceInfluence(int playerNum, int influenceIndex) {
        Player player = this.players[playerNum];
        player.getInfluenceId(influenceIndex);
        Influence oldInfluence;
        if (influenceIndex == 0) {
            oldInfluence = player.getInfluence1();
        } else {
            oldInfluence = player.getInfluence2();
        }

        if (oldInfluence != null) {
            this.deck.returnCard(oldInfluence);
            Influence newInfluence = this.deck.draw();
            if (influenceIndex == 0) {
                player.setInfluence1(newInfluence);
            } else {
                player.setInfluence2(newInfluence);
            }

        }
    }

    public String getInfluenceName(int cardId) {
        Influence inf = Influence.getInfluenceById(cardId);
        return inf != null ? inf.getName() : "Desconocida";
    }
}
