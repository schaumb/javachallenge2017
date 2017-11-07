package jsons.gamestate;

import com.google.gson.annotations.SerializedName;

public enum GameStatus {
    @SerializedName("PLAYING")
    PLAYING,
    @SerializedName("ENDED")
    ENDED,
    @SerializedName("ABORTED")
    ABORTED
}
