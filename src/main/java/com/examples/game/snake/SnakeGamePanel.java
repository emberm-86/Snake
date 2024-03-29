package com.examples.game.snake;

import com.examples.game.snake.objects.Food;
import com.examples.game.snake.objects.GameState;
import com.examples.game.snake.objects.Snake;
import com.examples.game.snake.service.FoodGenerationService;
import com.examples.game.snake.util.HighScoreUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Snake game panel: game window + thread.
 *
 * @author Matyas Ember
 */
public class SnakeGamePanel extends JPanel {

  private static final long serialVersionUID = 44212121212L;

  public static final int SNAKE_WINDOW_WIDTH = 600;
  public static final int SNAKE_WINDOW_HEIGHT = 600;
  public static final Color BACKGROUND_COLOR = Color.BLACK;
  public static final Color MENU_COLOR = Color.WHITE;
  public static final int DELAY = 60;
  private static final Font SMALLER_FONT = new Font("default", Font.BOLD, 12);
  private static final Font BIGGER_FONT = new Font("default", Font.BOLD, 16);

  private Snake snake;
  private Food food;
  private final FoodGenerationService foodGenerationService;
  private int counter;
  private boolean gameOver;
  private GameState gameState;

  private enum DIRECTION {DOWN, LEFT, RIGHT, UP}

  private DIRECTION actDir;

  SnakeGamePanel(JFrame mainFrame, FoodGenerationService foodGenerationService) {
    setBackground(BACKGROUND_COLOR);
    setPreferredSize(new Dimension(SNAKE_WINDOW_WIDTH, SNAKE_WINDOW_HEIGHT));
    this.foodGenerationService = foodGenerationService;
    this.gameState = GameState.MAIN_MENU;

    mainFrame.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (gameState == GameState.STARTED) {
          return;
        }

        if (gameState == GameState.FINISHED && e.getKeyChar() != '3') {
          return;
        }

        switch (e.getKeyChar()) {
          case '1': {
            gameState = GameState.STARTED;
            resetGame();
            SnakeMove snakeMove = new SnakeMove();
            CompletableFuture.runAsync(snakeMove).thenRun(
                () -> HighScoreUtil.showNewHighScoreEntryDialog(mainFrame.getRootPane(), counter));
            break;
          }

          case '2': {
            HighScoreUtil.showHighScores(mainFrame.getRootPane());
            break;
          }

          case '3': {
            gameState = GameState.MAIN_MENU;
            repaint();
            break;
          }

          case 'x': {
            System.exit(0);
            break;
          }
        }
      }
    });
  }

  /**
   * This method is called in the repaint if the game session is active.
   */
  private void playGame(Graphics g) {
    snake.paintSnake(g);
    food.paintFood(g);

    g.setColor(MENU_COLOR);
    g.setFont(SMALLER_FONT);
    g.drawString("SCORE: " + counter, 10, 40);

    if (gameOver) {
      g.setFont(BIGGER_FONT);
      g.drawString("GAME OVER!",
          SNAKE_WINDOW_WIDTH / 2 - 2 * Snake.SNAKE_BODY_SIZE - 15, SNAKE_WINDOW_HEIGHT / 2);
      g.setFont(SMALLER_FONT);
      g.drawString("PRESS '3' TO RETURN TO THE MAIN MENU.",
          SNAKE_WINDOW_WIDTH / 2 - 110, SNAKE_WINDOW_HEIGHT / 2 + 65);
    }
  }

  /**
   * Restore the initial state before the new game session started.
   */
  private void resetGame() {
    gameOver = false;
    counter = 0;
    snake = new Snake();
    food = foodGenerationService.generateFood(snake);
    actDir = DIRECTION.RIGHT;
  }

  /**
   * Showing the main menu in the repaint.
   */
  private void showMenu(Graphics g) {
    g.setColor(MENU_COLOR);
    g.setFont(BIGGER_FONT);

    List<String> menuOpts = List.of("1. START GAME", "2. HIGH SCORES", "3. MAIN MENU", "x. EXIT");
    IntStream.range(0, menuOpts.size())
        .forEach(i -> g.drawString(menuOpts.get(i), SNAKE_WINDOW_WIDTH / 2 - 65, 150 + i * 50));
  }

  /**
   * If the user is in the main menu then the options are shown, otherwise the game session is
   * active.
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
   * The user interactions are defined here. We can control the snake with top, left, bottom, right
   * cursors.
   */
  class KeyControl extends KeyAdapter {

    private long lastPressProcessed = 0;

    @Override
    public void keyPressed(KeyEvent e) {
      if (System.currentTimeMillis() - lastPressProcessed > SnakeGamePanel.DELAY * 1.12) {
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
   * This Runnable class represents the task definition of the snake's movement. We feed the snake
   * here and check if it collides into the bounds of the screen, or it turns into itself.
   */
  class SnakeMove implements Runnable {

    @Override
    public void run() {
      boolean rolledIn = false;

      while (snake.getHead().x >= 0 && snake.getHead().x < SNAKE_WINDOW_WIDTH
          && snake.getHead().y >= 0 && snake.getHead().y < SNAKE_WINDOW_HEIGHT && !rolledIn) {
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
          if (snakeHead.x == snake.get(i).x && snakeHead.y == snake.get(i).y) {
            rolledIn = true;
            break;
          }
        }

        if (snakeHead.x <= food.x && snakeHead.x + Snake.SNAKE_BODY_SIZE >= food.x &&
            snakeHead.y <= food.y && snakeHead.y + Snake.SNAKE_BODY_SIZE >= food.y) {
          counter++;

          int snakeTailX = snake.get(0).x == snake.get(1).x ?
              snake.get(0).x : snake.get(0).x - Snake.SNAKE_BODY_SIZE;

          int snakeTailY = snake.get(0).x == snake.get(1).x ?
              snake.get(0).y - Snake.SNAKE_BODY_SIZE : snake.get(0).y;

          snake.addTail(snakeTailX, snakeTailY);
          food = foodGenerationService.generateFood(snake);
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