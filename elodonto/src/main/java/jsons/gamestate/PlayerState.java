package jsons.gamestate;

import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Player;

public class PlayerState {
    private String userID;
    private int strength;
    private int score;

    public Player getAsPlayer() {
        return GameDescription.LATEST_INSTANCE.getPlayer(getUserID());
    }

    public String getUserID() {
        return userID;
    }

    public int getStrength() {
        return strength;
    }

    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerState that = (PlayerState) o;

        return userID != null ? userID.equals(that.userID) : that.userID == null;
    }

    @Override
    public int hashCode() {
        return userID != null ? userID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PlayerState{" +
                "userID='" + userID + '\'' +
                ", strength=" + strength +
                ", score=" + score +
                '}';
    }
}
