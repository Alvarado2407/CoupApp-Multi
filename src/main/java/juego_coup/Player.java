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

    public int getNumInfluence() {
        int count = 0;
        if (this.influence1 != null && !this.influence1.isRevealed()) {
            ++count;
        }

        if (this.influence2 != null && !this.influence2.isRevealed()) {
            ++count;
        }

        return count;
    }

    public int numInfluences() {
        int count = 0;
        if (this.influence1 != null) {
            ++count;
        }

        if (this.influence2 != null) {
            ++count;
        }

        return count;
    }

    public boolean mustCoup() {
        return this.tokens >= 10;
    }

    public String getName() {
        return this.name;
    }

    public int getPlayerNum() {
        return this.playerNum;
    }

    public Influence getInfluence1() {
        return this.influence1;
    }

    public Influence getInfluence2() {
        return this.influence2;
    }

    public void setInfluence1(Influence influence) {
        this.influence1 = influence;
    }

    public void setInfluence2(Influence influence) {
        this.influence2 = influence;
    }

    public int getInfluenceId(int index) {
        Influence inf = index == 0 ? this.influence1 : (index == 1 ? this.influence2 : null);
        return inf != null ? inf.getId() : -1;
    }

    public void setInfluenceId(int index, int newCardId) {
        throw new UnsupportedOperationException("El Player no debe establecer la influencia por ID. Game.java debe pasar un objeto Influence.");
    }

    public boolean checkInfluence(int requiredRole) {
        if (this.influence1 != null && !this.influence1.isRevealed() && this.influence1.getId() == requiredRole) {
            return true;
        } else {
            return this.influence2 != null && !this.influence2.isRevealed() && this.influence2.getId() == requiredRole;
        }
    }

    public boolean checkBlockInfluence(int blockRole) {
        return this.checkInfluence(blockRole);
    }

    public Influence loseInfluence() {
        return null;
    }

    public Influence loseInfluence(int choice) {
        if (this.getNumInfluence() == 0) {
            return null;
        } else {
            Influence lost = null;
            if (choice == 0) {
                lost = this.influence1;
                if (lost != null) {
                    lost.setRevealed(true);
                }
            } else {
                if (choice != 1) {
                    System.err.println("Error: Elección de índice de influencia inválida.");
                    return null;
                }

                lost = this.influence2;
                if (lost != null) {
                    lost.setRevealed(true);
                }
            }

            if (this.getNumInfluence() == 0) {
                this.out = true;
            }

            return lost;
        }
    }
}
