package jsons.gamedesc;

public class Player {
    private String userID;
    private String userName;
    private int raceID;
    private int strength;
    private int score;

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public int getRaceID() {
        return raceID;
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
                ", strength=" + strength +
                ", score=" + score +
                '}';
    }
}
