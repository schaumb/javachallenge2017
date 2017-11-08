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
        return isOwns(ILogic.OUR_TEAM);
    }

    default boolean isOwns(String oth) {
        return Objects.equals(oth, getOwner());
    }

    default boolean hasOwner() {
        return getOwner() != null;
    }

    String getOwner();
}
