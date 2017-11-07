package logic;

import jsons.Move;
import jsons.gamedesc.GameDescription;
import jsons.gamestate.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Consumer;

public class GuiLogic extends MouseAdapter implements ILogic {
    private JFrame frame;
    private Consumer<Move> consumer;
    private GameDescription gameDescription;
    private GameState currGameState;

    private class DrawPane extends JPanel {
        public void paintComponent(Graphics g) {
            paintTo((Graphics2D) g);
        }

    }

    private class MyFrame extends JFrame {
        MyFrame(Dimension dimension) {
            addMouseListener(GuiLogic.this);
            setContentPane(new DrawPane());
            setTitle("GuiClickLogic");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setResizable(false);
            setSize(dimension);

            setVisible(true);

            int left = getInsets().left;
            int top = getInsets().top;
            setSize(new Dimension(dimension.width + left, dimension.height + top));
        }
    }

    @Override
    public void setMessageConsumer(Consumer<Move> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setGameDescription(GameDescription gameDescription) {
        this.gameDescription = gameDescription;
        frame = new MyFrame(new Dimension(gameDescription.getMapSizeX(), gameDescription.getMapSizeY()));
    }

    @Override
    public void setGameState(GameState gameState) {
        this.currGameState = gameState;

        frame.revalidate();
        frame.repaint();
    }

    private void paintTo(Graphics2D g) {


    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }
}
