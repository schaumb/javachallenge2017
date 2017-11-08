package jsons.gamedesc;

import logic.ILogic;

import java.util.Objects;

public class Player {
    private String userID;
    private String userName;
    private int raceID;

    public boolean isUs() {
        return Objects.equals(userID, ILogic.OUR_TEAM);
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public int getRaceID() {
        return raceID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return userID != null ? userID.equals(player.userID) : player.userID == null;
    }

    @Override
    public int hashCode() {
        return userID != null ? userID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Player{" +
                "userID='" + userID + '\'' +
                ", userName='" + userName + '\'' +
                ", raceID=" + raceID +
                '}';
    }
}
