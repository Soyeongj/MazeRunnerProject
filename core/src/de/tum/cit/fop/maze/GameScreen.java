package de.tum.cit.fop.maze;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.fop.maze.MazeRunnerGame;


public class GameScreen implements Screen {


    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private SpriteBatch batch;
    private Stage stage;

    // Timer
    private float runTimer = 0f;
    private float cooldownTimer = 0f;
    private float stateTime = 0f;
    private float grieverStateTime = 0f;
    private float grieverChaseDelayTimer = 0f;
    private float deathTransitionTimer = 0f;
    private float stunTimer = 0f;

    // Boolean Flags
    private boolean canRun = true;
    private boolean isRunning = false;
    private boolean isGrieverwaiting = false;
    private boolean isGrieverFollowingPlayer = false;
    private boolean isPlayerDead = false;
    private boolean isGrieverStunned = false;

    // Texture variables
    private Texture griever, grieverUp1, grieverUp2, grieverDown1, grieverDown2, grieverLeft1, grieverLeft2, grieverRight1, grieverRight2, playerDead;
    private Texture player, playerUp1, playerUp2, playerDown1, playerDown2, playerRight1, playerRight2, playerLeft1, playerLeft2;
    private Texture brick;

    // Rectangles
    private Rectangle playerRect, brickRect, grieverRect;

    // Set up for player
    private final float runDuration = 5f;
    private final float cooldownDuration = 6f;
    private String playerDirection = "right";
    private float Speed = 50.0f, runningSpeed = 78.0f;
    private float playerx = 320, playery = 120, previousx, previousy;
    private final float walkAnimationTime = 0.1f;

    // Set up for griever
    private final float grieverAnimationTime = 0.2f;
    private float monsterX = 100, monsterY = 100, monsterSpeed = 70.0f, detectionRange = 200.0f;
    private String fixedGrieverDirection = null;

    public GameScreen(MazeRunnerGame game) {
        this.game = game;
        stage = new Stage();
        batch = game.getSpriteBatch();

        // Camera setup
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 0.75f;

        // Initializing the textures (griever)
        grieverRight1 = new Texture("grieverright.png");
        grieverRight2 = new Texture("grieverright2.png");
        grieverLeft1 = new Texture("grieverleft.png");
        grieverLeft2 = new Texture("grieverleft2.png");
        grieverUp1 = new Texture("grieverup.png");
        grieverUp2 = new Texture("grieverup2.png");
        grieverDown1 = new Texture("grieverdown.png");
        grieverDown2 = new Texture("grieverdown2.png");
        griever = grieverRight1;

        // Initializing the textures (player)
        playerUp1 = new Texture("boy_up1.png");
        playerUp2 = new Texture("boy_up2.png");
        playerDown1 = new Texture("boy_down1.png");
        playerDown2 = new Texture("boy_down2.png");
        playerLeft1 = new Texture("boy_left1.png");
        playerLeft2 = new Texture("boy_left2.png");
        playerRight1 = new Texture("boy_right1.png");
        playerRight2 = new Texture("boy_right2.png");
        playerDead = new Texture("boydead.png");
        player = playerRight1;

        // Initializing the textures (brick)
        brick = new Texture("wall.png");

        // Initializing the rectangles
        grieverRect = new Rectangle(monsterX, monsterY, griever.getWidth(), griever.getHeight());
        playerRect = new Rectangle(playerx, playery, player.getWidth(), player.getHeight());
        brickRect = new Rectangle(200, 200, brick.getWidth(), brick.getHeight());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 0);


        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        //Drawing methods
        batch.begin();
        stage.draw();
        batch.draw(player, playerx, playery);
        batch.draw(brick, 200, 200);
        batch.draw(griever, monsterX, monsterY);

        //Stores all coordinates/paths the player has taken
        previousx = playerx;
        previousy = playery;


        // Handle input for ESC key to go to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }


        // Increment the timer
        stateTime += delta;
        grieverStateTime += delta;


        // Running logic and cooldown
        if (!isPlayerDead) {
            if (canRun && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                isRunning = true;
            } else {
                isRunning = false;
            }
            if (isRunning) {
                runTimer += delta;
                if (runTimer >= runDuration) {
                    runTimer = runDuration;
                    canRun = false;
                }
            }
            if (!canRun) {
                cooldownTimer += delta;
                if (cooldownTimer >= cooldownDuration) {
                    canRun = true;
                    cooldownTimer = 0f;
                    runTimer = 0f;
                }
            }
        }


        float currentSpeed = isRunning ? runningSpeed : Speed;


        // Transition to the menu screen after game over
        if (isPlayerDead) {
            deathTransitionTimer += delta;
            if (deathTransitionTimer >= 1f) {
                game.goToMenu();
            }
        }

        // Handle Player movement
        boolean moveUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean moveDown = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D);


        if (moveUp && !moveDown && !moveLeft && !moveRight) {
            playery += currentSpeed * delta;
            playerDirection = "up";
            if (stateTime >= walkAnimationTime) {
                stateTime = 0;
                player = (player == playerUp1) ? playerUp2 : playerUp1;
            }
        } else if (moveDown && !moveUp && !moveLeft && !moveRight) {
            playery -= currentSpeed * delta;
            playerDirection = "down";
            if (stateTime >= walkAnimationTime) {
                stateTime = 0;
                player = (player == playerDown1) ? playerDown2 : playerDown1;
            }
        } else if (moveLeft && !moveUp && !moveDown && !moveRight) {
            playerx -= currentSpeed * delta;
            playerDirection = "left";
            if (stateTime >= walkAnimationTime) {
                stateTime = 0;
                player = (player == playerLeft1) ? playerLeft2 : playerLeft1;
            }
        } else if (moveRight && !moveUp && !moveDown && !moveLeft) {
            playerx += currentSpeed * delta;
            playerDirection = "right";
            if (stateTime >= walkAnimationTime) {
                stateTime = 0;
                player = (player == playerRight1) ? playerRight2 : playerRight1;
            }
        }


        // Move the player's rectangle as the player moves
        playerRect.setPosition(playerx, playery);


        // Collision detection + stops the character if it collides with the brick
        if (brickRect.overlaps(playerRect)) {
            playerx = previousx;
            playery = previousy;
        }


        // Griever movement mechanism - check if the player is close to the griever
        int diffX = (int) (playerx - monsterX);
        int diffY = (int) (playery - monsterY);
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);


        if (distance <= detectionRange) {
            isGrieverwaiting = true;
        }


        // Griever movement mechanism - griever starts chasing the player after 1.5 seconds
        if (isGrieverwaiting) {
            grieverChaseDelayTimer += delta;
            if (grieverChaseDelayTimer > 1.5f) {
                isGrieverFollowingPlayer = true;
            }
        }


        // Update Griever's movement mechanism to fix its direction when it starts chasing
        if (isGrieverFollowingPlayer) {
            if (fixedGrieverDirection == null) {
                fixedGrieverDirection = playerDirection;
            }


            // Dynamically update Griever's direction if the player changes direction (but not the opposite direction)
            if (!fixedGrieverDirection.equals(playerDirection) && !isOppositeDirection(fixedGrieverDirection, playerDirection)) {
                fixedGrieverDirection = playerDirection;
            }


            // Ensure griever direction matches player movement direction but doesn't move in the opposite direction
            switch (fixedGrieverDirection) {
                case "right":
                    if (diffX < 0) {
                        fixedGrieverDirection = "left"; // Don't allow moving right if player is moving left
                    }
                    break;
                case "left":
                    if (diffX > 0) {
                        fixedGrieverDirection = "right"; // Don't allow moving left if player is moving right
                    }
                    break;
                case "up":
                    if (diffY < 0) {
                        fixedGrieverDirection = "down"; // Don't allow moving up if player is moving down
                    }
                    break;
                case "down":
                    if (diffY > 0) {
                        fixedGrieverDirection = "up"; // Don't allow moving down if player is moving up
                    }
                    break;
            }

            // Griever's movement is calculated by the angle between x-axis and the vector from griever towards the player
            float angle = (float) Math.atan2(diffY, diffX);
            float deltaX = (float) (monsterSpeed * Math.cos(angle) * delta);
            float deltaY = (float) (monsterSpeed * Math.sin(angle) * delta);

            //Stunning mechanism
            if (distance < 30 && isOppositeDirection(playerDirection, fixedGrieverDirection) && !isGrieverStunned) {
                isGrieverStunned = true;
                stunTimer = 0f;
            }


            // If the Griever is stunned, stop chasing for 3 seconds
            if (isGrieverStunned) {
                stunTimer += delta;
                if (stunTimer >= 3f) {
                    isGrieverStunned = false; // Stop the stun after 3 seconds
                } else {
                    // Don't move the Griever during the stun
                    deltaX = 0;
                    deltaY = 0;
                }
            }


            // Restrict movement based on fixed direction
            //example: If the initial direction of the griever is right and player is approaching to the left(deltaX < 0), disallow griever to move left so that griever and player can collide while forwarding each other
            switch (fixedGrieverDirection) {
                case "right":
                    if (deltaX < 0) deltaX = 0;
                    break;
                case "left":
                    if (deltaX > 0) deltaX = 0;
                    break;
                case "up":
                    if (deltaY < 0) deltaY = 0;
                    break;
                case "down":
                    if (deltaY > 0) deltaY = 0;
                    break;
            }

            // Update Griever's position
            monsterX += deltaX;
            monsterY += deltaY;
            grieverRect.setPosition(monsterX, monsterY);

            // Death of the player if griever catches the player
            if (distance < 30 && !isGrieverStunned) {
                isPlayerDead = true;
                player = playerDead;
                //player is stopped when dead
                playerx = previousx;
                playery = previousy;
            }

            // Handle griever animation based on its fixed direction and player's direction
            if (fixedGrieverDirection != null) {
                if (grieverStateTime >= grieverAnimationTime) {
                    griever = getGrieverTextureForDirection(fixedGrieverDirection);
                    grieverStateTime = 0;
                }
            }
        } else if (isGrieverwaiting) {
            fixedGrieverDirection = null; // Reset direction if griever is waiting
        }


        batch.end();


    }


    // Helper method to check if the directions are opposite
    private boolean isOppositeDirection(String currentDirection, String newDirection) {
        return (currentDirection.equals("up") && newDirection.equals("down")) ||
                (currentDirection.equals("down") && newDirection.equals("up")) ||
                (currentDirection.equals("left") && newDirection.equals("right")) ||
                (currentDirection.equals("right") && newDirection.equals("left"));
    }
    // Helper method to render the matching walking animation based on the direction
    private Texture getGrieverTextureForDirection(String direction) {
        switch (direction) {
            case "right":
                return (griever == grieverRight1) ? grieverRight2 : grieverRight1;
            case "left":
                return (griever == grieverLeft1) ? grieverLeft2 : grieverLeft1;
            case "up":
                return (griever == grieverUp1) ? grieverUp2 : grieverUp1;
            case "down":
                return (griever == grieverDown1) ? grieverDown2 : grieverDown1;
            default:
                return grieverRight1; // Default to right if no valid direction
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }


    @Override
    public void show() {
    }


    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        playerUp1.dispose();
        playerUp2.dispose();
        playerDown1.dispose();
        playerDown2.dispose();
        playerLeft1.dispose();
        playerLeft2.dispose();
        playerRight1.dispose();
        playerRight2.dispose();
        brick.dispose();
        grieverUp1.dispose();
        grieverUp2.dispose();
        grieverDown1.dispose();
        grieverDown2.dispose();
        grieverLeft1.dispose();
        grieverLeft2.dispose();
        grieverRight1.dispose();
        grieverRight2.dispose();
    }
}



