package com.games.snake;

import com.games.snake.objects.Food;
import com.games.snake.objects.GameState;
import com.games.snake.objects.Snake;
import com.games.snake.service.FoodGenerationService;
import com.games.snake.service.HighScoreService;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;

/**
 * Snake game panel: game window + thread.
 *
 * @author Matyas Ember
 */
public class SnakeGamePanel extends JPanel {

    private static final long serialVersionUID = 44212121212L;

    public static final int SNAKE_WINDOW_WIDTH = 600;
    public static final int SNAKE_WINDOW_HEIGHT = 600;

    public static final int DELAY = 60;

    private Snake snake;
    private Food food;

    private int counter;
    private boolean gameOver;
    private GameState gameState;

    private enum DIRECTION {DOWN, LEFT, RIGHT, UP}

    private DIRECTION actDir;

    SnakeGamePanel(JFrame mainFrame) {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(SNAKE_WINDOW_WIDTH,
                SNAKE_WINDOW_HEIGHT));

        gameState = GameState.MAIN_MENU;

        // The menu handling is defined here.
        mainFrame.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (gameState == GameState.STARTED) {
                    return;
                }

                if (gameState == GameState.FINISHED
                        && e.getKeyChar() != 'x') {
                    return;
                }

                switch (e.getKeyChar()) {

                    case '1': {
                        gameState = GameState.STARTED;

                        resetGame();

                        SnakeMove snakeMove = new SnakeMove();

                        CompletableFuture.runAsync(snakeMove)
                                .thenRun(() -> HighScoreService
                                        .showNewHighScoreEntryDialog(
                                                mainFrame.getRootPane(),
                                                counter));
                        break;
                    }

                    case '2': {
                        HighScoreService
                                .showHighScores(mainFrame.getRootPane());
                        break;
                    }

                    case '3': {
                        System.exit(0);
                        break;
                    }

                    case 'x': {
                        gameState = GameState.MAIN_MENU;
                        repaint();
                        break;
                    }
                }
            }
        });
    }

    /**
     * This method is called in the repaint
     * if the game session is active.
     */
    private void playGame(Graphics g) {
        snake.paintSnake(g);
        food.paintFood(g);

        g.setColor(Color.BLUE);
        g.setFont(new Font("default", Font.BOLD, 12));
        g.drawString("SCORE: " + counter, 10, 40);

        if (gameOver) {
            g.setFont(new Font("default", Font.BOLD, 16));
            g.drawString("GAME OVER!",
                    SNAKE_WINDOW_WIDTH / 2 - 2 * Snake.SNAKE_BODY_SIZE,
                    SNAKE_WINDOW_HEIGHT / 2);

            g.setFont(new Font("default", Font.BOLD, 12));
            g.drawString("PRESS X TO RETURN TO THE MAIN MENU",
                    SNAKE_WINDOW_WIDTH / 2 - 100,
                    SNAKE_WINDOW_HEIGHT / 2 + 65);
        }
    }

    /**
     * Restore the initial state
     * before the new game session started.
     */
    private void resetGame() {
        gameOver = false;
        counter = 0;

        snake = new Snake();
        food = FoodGenerationService.generateFood(snake);

        actDir = DIRECTION.RIGHT;
    }

    /**
     * Showing the main menu in the repaint.
     */
    private void showMenu(Graphics g) {
        g.setColor(Color.BLUE);
        g.setFont(new Font("default", Font.BOLD, 16));

        g.drawString("1. START GAME",
                SNAKE_WINDOW_WIDTH / 2 - 70, 150);
        g.drawString("2. HIGH SCORES",
                SNAKE_WINDOW_WIDTH / 2 - 70, 200);
        g.drawString("3. EXIT", SNAKE_WINDOW_WIDTH / 2 - 70, 250);
        g.drawString("x. MAIN MENU", SNAKE_WINDOW_WIDTH / 2 - 70, 300);
    }

    /**
     * If the user is in the main menu
     * then the options are shown,
     * otherwise the game session is active.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState == GameState.MAIN_MENU) {
            showMenu(g);
        } else {
            playGame(g);
        }
    }

    /**
     * The user interactions are defined here.
     * We can control the snake with
     * top, left, bottom, right cursors.
     */
    class KeyControl extends KeyAdapter {

        private long lastPressProcessed = 0;

        @Override
        public void keyPressed(KeyEvent e) {
            if (System.currentTimeMillis() - lastPressProcessed
                    > SnakeGamePanel.DELAY * 1.12) {

                switch (e.getKeyCode()) {

                    case KeyEvent.VK_UP:
                        if (actDir != DIRECTION.DOWN) {
                            actDir = DIRECTION.UP;
                        }
                        break;

                    case KeyEvent.VK_DOWN:
                        if (actDir != DIRECTION.UP) {
                            actDir = DIRECTION.DOWN;
                        }
                        break;

                    case KeyEvent.VK_LEFT:
                        if (actDir != DIRECTION.RIGHT) {
                            actDir = DIRECTION.LEFT;
                        }
                        break;

                    case KeyEvent.VK_RIGHT:
                        if (actDir != DIRECTION.LEFT) {
                            actDir = DIRECTION.RIGHT;
                        }
                        break;
                }

                lastPressProcessed = System.currentTimeMillis();
            }
        }
    }

    /**
     * This Runnable class represents the thread definition
     * of the movement of the snake.
     * We feed the snake here and check
     * if is collided to the boundaries of the screen
     * or if it is turned into itself.
     */
    class SnakeMove implements Runnable {

        @Override
        public void run() {
            boolean rolledIn = false;

            while (snake.getHead().x >= 0 &&
                    snake.getHead().x < SNAKE_WINDOW_WIDTH &&
                    snake.getHead().y >= 0 &&
                    snake.getHead().y < SNAKE_WINDOW_HEIGHT &&
                    !rolledIn) {

                int snakeHeadIndex = snake.getHeadIndex();

                for (int i = 0; i < snakeHeadIndex; i++) {
                    snake.get(i).x = snake.get(i + 1).x;
                    snake.get(i).y = snake.get(i + 1).y;
                }

                Snake.SnakeBody snakeHead = snake.getHead();

                switch (actDir) {

                    case RIGHT:
                        snakeHead.x += Snake.SNAKE_BODY_SIZE;
                        break;

                    case LEFT:
                        snakeHead.x -= Snake.SNAKE_BODY_SIZE;
                        break;

                    case UP:
                        snakeHead.y -= Snake.SNAKE_BODY_SIZE;
                        break;

                    case DOWN:
                        snakeHead.y += Snake.SNAKE_BODY_SIZE;
                        break;
                }

                for (int i = 0; i < snakeHeadIndex; i++) {

                    if (snakeHead.x == snake.get(i).x &&
                            snakeHead.y == snake.get(i).y) {

                        rolledIn = true;
                        break;
                    }
                }

                if (snakeHead.x <= food.x &&
                        snakeHead.x + Snake.SNAKE_BODY_SIZE >= food.x &&
                        snakeHead.y <= food.y &&
                        snakeHead.y + Snake.SNAKE_BODY_SIZE >= food.y) {

                    counter++;

                    int snakeTailX = snake.get(0).x == snake.get(1).x ?
                            snake.get(0).x :
                            snake.get(0).x - Snake.SNAKE_BODY_SIZE;

                    int snakeTailY = snake.get(0).x == snake.get(1).x ?
                            snake.get(0).y - Snake.SNAKE_BODY_SIZE :
                            snake.get(0).y;

                    snake.addTail(snakeTailX, snakeTailY);

                    food = FoodGenerationService.generateFood(snake);
                }

                repaint();

                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getLocalizedMessage(), e);
                }
            }

            gameOver = true;
            repaint();

            gameState = GameState.FINISHED;
        }
    }
}