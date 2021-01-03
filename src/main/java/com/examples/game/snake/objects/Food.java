package com.examples.game.snake.objects;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Snake tries to catch the Food object.
 */
public class Food {

    private static final Color FOOD_COLOR = Color.YELLOW;
    public static final int FOOD_SIZE = 3 * Snake.SNAKE_BODY_SIZE / 4;

    public int x;
    public int y;

    public Food(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void paintFood(Graphics g) {
        g.setColor(FOOD_COLOR);
        g.fillOval(x, y, FOOD_SIZE, FOOD_SIZE);
    }
}