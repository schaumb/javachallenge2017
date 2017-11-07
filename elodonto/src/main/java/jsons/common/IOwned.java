package jsons.common;

import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Player;
import logic.ILogic;

import java.util.Objects;

public interface IOwned {

    default Player getPlayer() {
        return GameDescription.LATEST_INSTANCE.getPlayer(getOwner());
    }

    default boolean isOurs() {
        return Objects.equals(ILogic.OUR_TEAM, getOwner());
    }

    String getOwner();
}
