package com.examples.game.snake.service;

import com.examples.game.snake.SnakeGamePanel;
import com.examples.game.snake.objects.Food;
import com.examples.game.snake.objects.Snake;

import java.util.Random;

/**
 * New position generation for food.
 */
public class FoodGenerationService {

    private static final Random ran = new Random();

    public static Food generateFood(Snake snake) {

        boolean isInSnake = false;

        int x = 1;
        int y = 1;

        while (!(x % Snake.SNAKE_BODY_SIZE == 0
                && y % Snake.SNAKE_BODY_SIZE == 0 && !isInSnake)) {

            x = ran.nextInt(SnakeGamePanel.SNAKE_WINDOW_WIDTH
                    - Snake.SNAKE_BODY_SIZE) + Snake.SNAKE_BODY_SIZE;

            y = ran.nextInt(SnakeGamePanel.SNAKE_WINDOW_HEIGHT
                    - Snake.SNAKE_BODY_SIZE) + Snake.SNAKE_BODY_SIZE;

            isInSnake = false;

            for (Snake.SnakeBody snakeBody : snake) {

                if (snakeBody.x - Snake.SNAKE_BODY_SIZE <= x &&
                        snakeBody.x + Snake.SNAKE_BODY_SIZE >= x &&
                        snakeBody.y - Snake.SNAKE_BODY_SIZE <= y &&
                        snakeBody.y + Snake.SNAKE_BODY_SIZE >= y) {

                    isInSnake = true;
                    break;
                }
            }
        }

        x += (Snake.SNAKE_BODY_SIZE - Food.FOOD_SIZE) / 2;
        y += (Snake.SNAKE_BODY_SIZE - Food.FOOD_SIZE) / 2;

        return new Food(x, y);
    }
}
