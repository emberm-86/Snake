package com.examples.game.snake;

import com.examples.game.snake.service.FoodGenerationService;

import javax.swing.JFrame;
import java.awt.BorderLayout;

/**
 * Snake game
 *
 * @author Matyas Ember
 */
public class SnakeGame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String GAME_TITLE = "Snake v1.0";

    public SnakeGame(int x, int y) {
        setLayout(new BorderLayout());
        setLocation(x, y);
        setTitle(GAME_TITLE);

        SnakeGamePanel panel = new SnakeGamePanel(this,
                new FoodGenerationService());

        add(panel, BorderLayout.CENTER);
        addKeyListener(panel.new KeyControl());
        pack();

        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new SnakeGame(100, 200);
    }
}