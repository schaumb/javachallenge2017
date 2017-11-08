package logic;

import jsons.Move;
import jsons.common.Positioned;
import jsons.gamedesc.GameDescription;
import jsons.gamedesc.Planet;
import jsons.gamestate.Army;
import jsons.gamestate.GameState;
import jsons.gamestate.PlanetState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GuiLogic extends MouseAdapter implements ILogic, KeyListener {
    private Move move = new Move().setMoveFrom(-1).setMoveTo(-1);
    private JFrame frame;
    private Consumer<Move> consumer;
    private GameDescription gameDescription;
    private GameState currGameState;

    @Override
    public void setMessageConsumer(Consumer<Move> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setGameDescription(GameDescription gameDescription) {
        this.gameDescription = gameDescription;
        frame = new MyFrame(new Dimension(gameDescription.getMapSizeX(), gameDescription.getMapSizeY()));
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public void setGameState(GameState gameState) {
        this.currGameState = gameState;

        frame.revalidate();
        frame.repaint();
    }

    private Color getTransitionColor(Color from, Color to, double percent) {
        if (percent == 0.0)
            return from;
        if (percent == 1.0)
            return to;

        return new Color((int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * percent),
                (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * percent),
                (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * percent));
    }

    private void paintTo(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fill(g.getDeviceConfiguration().getBounds());

        if (gameDescription != null) {
            if (currGameState == null) {
                for (Planet planet : gameDescription.getPlanets()) {
                    g.setColor(Color.GRAY);
                    g.fillArc(planet.getX() - planet.getRadius(), planet.getY() - planet.getRadius(),
                            planet.getRadius() * 2, planet.getRadius() * 2, 0, 360);
                }
            } else {
                for (PlanetState planetState : currGameState.getPlanetStates()) {
                    Planet planet = planetState.getAsPlanet();

                    Color myColor = Color.GREEN;
                    if (move.getMoveFrom() == planet.getPlanetID()) {
                        myColor = new Color(139, 69, 19);
                    }
                    g.setColor(getTransitionColor(Color.GRAY, planetState.isOurs() ? myColor : Color.RED, planetState.getOwnershipRatio()));
                    g.fillArc(planet.getX() - planet.getRadius(), planet.getY() - planet.getRadius(),
                            planet.getRadius() * 2, planet.getRadius() * 2, 0, 360);
                    g.setFont(new Font("Arial", Font.PLAIN, 20));
                    g.setColor(Color.BLACK);
                    g.drawString(planetState.getPlanetID() + "", planet.getX() - planet.getRadius() * 2 / 3, planet.getY() + planet.getRadius() / 4);
                }
                for (PlanetState planetState : currGameState.getPlanetStates()) {
                    Planet planet = planetState.getAsPlanet();

                    List<Army> stationedArmies = planetState.getStationedArmies();
                    if (!stationedArmies.isEmpty()) {
                        int size = planet.getRadius() / stationedArmies.size();
                        g.setFont(new Font("Arial", Font.PLAIN, size));
                        for (int i = 0; i < stationedArmies.size(); ++i) {
                            Army army = stationedArmies.get(i);
                            g.setColor(army.isOurs() ? Color.BLUE : Color.MAGENTA);

                            g.drawString("" + army.getSize(), planet.getX() - planet.getRadius() * 2 / 3, planet.getY() + i * size);
                        }
                    }
                    for (Army army : planetState.getMovingArmies()) {
                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        g.setColor(army.isOurs() ? Color.BLUE : Color.MAGENTA);
                        g.drawString("" + army.getSize(), army.getX().intValue(), army.getY().intValue());
                    }
                }

            }

        }

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        if (gameDescription != null) {
            Optional<Planet> first = gameDescription.getPlanets()
                    .stream().filter(p -> p.distance(new Positioned<>(mouseEvent.getX(), mouseEvent.getY())) < p.getRadius())
                    .findFirst();

            if (first.isPresent()) {
                int planetID = first.get().getPlanetID();
                if (move.getMoveFrom() == -1) {
                    move.setMoveFrom(planetID);
                } else {
                    move.setMoveTo(planetID);
                }
            } else {
                move.setMoveFrom(-1).setMoveTo(-1);
            }

            if (move.getMoveFrom() != -1 && move.getMoveTo() != -1) {
                if (move.getArmySize() < gameDescription.getMinMovableArmySize()) {
                    LOG.warning("Min moveable army: " + gameDescription.getMinMovableArmySize() + " sent army: " + move.getArmySize() + " set to min");
                    move.setArmySize(gameDescription.getMinMovableArmySize());
                }
                consumer.accept(move);
                move.setMoveFrom(-1).setMoveTo(-1);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

        System.err.println(keyEvent.getKeyChar());
        if (keyEvent.getKeyChar() == '\n') {
            move.setArmySize(0);
            LOG.info("KeyEvent - clear army");
            return;
        }

        int kc = keyEvent.getKeyChar() - '0';

        if (kc < 0 || kc > 9)
            return;

        move.setArmySize(move.getArmySize() * 10 + kc);
        LOG.info("KeyEvent - setArmyTo " + move.getArmySize());
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }

    @Override
    public void close() {
        frame.dispose();
    }

    private class DrawPane extends JPanel {
        public void paintComponent(Graphics g) {
            paintTo((Graphics2D) g);
        }

    }

    private class MyFrame extends JFrame {
        MyFrame(Dimension dimension) {
            addMouseListener(GuiLogic.this);
            addKeyListener(GuiLogic.this);
            setContentPane(new DrawPane());
            setTitle("GuiClickLogic");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setResizable(false);
            setSize(dimension);

            setVisible(true);

            int left = getInsets().left;
            int top = getInsets().top;
            setSize(new Dimension(dimension.width + left, dimension.height + top));
        }
    }
}
