package com.examples.game.snake.objects;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * The Snake is built up from SnakeBody objects.
 */
public class Snake extends ArrayList<Snake.SnakeBody> {

    private static final long serialVersionUID = 1L;

    public static final int SNAKE_BODY_SIZE = 20;

    public Snake() {
        for (int i = 0; i < 4; i++) {
            add(new SnakeBody((i + 1) * SNAKE_BODY_SIZE
                    , 3 * SNAKE_BODY_SIZE));
        }
    }

    public void paintSnake(Graphics g) {
        for (SnakeBody snakeBody : this) {
            g.setColor(Color.WHITE);
            snakeBody.paintSnakeBody(g);
        }
    }

    public int getHeadIndex() {
        return size() - 1;
    }

    public SnakeBody getHead() {
        return get(getHeadIndex());
    }

    public void addTail(int x, int y) {
        add(0, new Snake.SnakeBody(x, y));
    }

    public static class SnakeBody {

        public int x;
        public int y;

        public SnakeBody(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void paintSnakeBody(Graphics g) {
            g.setColor(Color.GREEN);
            g.fillOval(x, y, SNAKE_BODY_SIZE, SNAKE_BODY_SIZE);
        }
    }
}