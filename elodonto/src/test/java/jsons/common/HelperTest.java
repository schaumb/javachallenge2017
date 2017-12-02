package jsons.common;


import jsons.gamestate.Army;
import org.junit.Test;
import wsimpl.Main;
import wsimpl.ServerImitator;

import java.util.Arrays;
import java.util.Map;

public class HelperTest {
    @Test
    public void test() {
        // 40 távolság
        // B áll 14
        Main.logic = gameState -> {};
        new ServerImitator();

        // A bolygó 50,

        Map<String, Double> mi = Helper.killAtTime(Arrays.asList(
                new Army().setSize(20).setOwner("Mi"),
                new Army().setSize(19).setOwner("1"),
                new Army().setSize(18).setOwner("2"),
                new Army().setSize(17).setOwner("3")
        ), Long.MAX_VALUE);

        System.err.println(mi);

    }
}
