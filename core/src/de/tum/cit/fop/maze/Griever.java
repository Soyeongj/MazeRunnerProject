package de.tum.cit.fop.maze;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


public class Griever {
    private Texture grieverUp1, grieverUp2, grieverDown1, grieverDown2, grieverLeft1, grieverLeft2, grieverRight1, grieverRight2;
    private Texture griever;
    private float grieverStateTime;
    private String fixedGrieverDirection;
    private float monsterX, monsterY;
    private float monsterSpeed = 70.0f;
    private float detectionRange = 200.0f;
    private boolean isGrieverwaiting = false;
    private boolean isGrieverFollowingPlayer = false;
    private float grieverChaseDelayTimer = 0.0f;
    private final float grieverAnimationTime = 0.1f;  // Time between animation frames
    private Rectangle grieverRectangle;


    private boolean isGrieverStunned = false;
    private float stunTimer = 0.0f;


    public Griever(float startX, float startY) {
        this.monsterX = startX;
        this.monsterY = startY;


        // Load griever textures
        grieverRight1 = new Texture("grieverright.png");
        grieverRight2 = new Texture("grieverright2.png");
        grieverLeft1 = new Texture("grieverleft.png");
        grieverLeft2 = new Texture("grieverleft2.png");
        grieverUp1 = new Texture("grieverup.png");
        grieverUp2 = new Texture("grieverup2.png");
        grieverDown1 = new Texture("grieverdown.png");
        grieverDown2 = new Texture("grieverdown2.png");


        griever = grieverRight1;
        fixedGrieverDirection = "right";
        grieverStateTime = 0f;
        grieverRectangle = new Rectangle(monsterX, monsterY, griever.getWidth(), griever.getHeight());
    }


    public void render(SpriteBatch batch) {
        batch.draw(griever, monsterX, monsterY);
    }


    public void update(float delta, float playerX, float playerY, String playerDirection) {
        int diffX = (int) (playerX - monsterX);
        int diffY = (int) (playerY - monsterY);
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);


        if (distance <= detectionRange) {
            isGrieverwaiting = true;
        }


        if (isGrieverwaiting) {
            grieverChaseDelayTimer += delta;
            if (grieverChaseDelayTimer > 1.5f) {
                isGrieverFollowingPlayer = true;
            }
        }


        // Check if the griever should be stunned
        if (distance < 30 && isOppositeDirection(playerDirection, fixedGrieverDirection) && !isGrieverStunned) {
            isGrieverStunned = true;
            stunTimer = 0f;
        }

        if (isGrieverStunned) {
            stunTimer += delta;
            float deltaX = 0;
            float deltaY = 0;



            if (stunTimer >= 3f) {
                isGrieverStunned = false;
                stunTimer = 0;
            }
        } else if (isGrieverFollowingPlayer) {

            if (isGrieverFollowingPlayer) {
                if (fixedGrieverDirection == null  || !fixedGrieverDirection.equals(playerDirection)) {
                    fixedGrieverDirection = playerDirection;
                }


                switch (fixedGrieverDirection) {
                    case "right":
                        if (diffX < 0) {
                            fixedGrieverDirection = "left";
                        }
                        break;
                    case "left":
                        if (diffX > 0) {
                            fixedGrieverDirection = "right";
                        }
                        break;
                    case "up":
                        if (diffY < 0) {
                            fixedGrieverDirection = "down";
                        }
                        break;
                    case "down":
                        if (diffY > 0) {
                            fixedGrieverDirection = "up";
                        }
                        break;
                }


                // Griever starts moving towards player
                Vector2 grieverPosition = new Vector2(monsterX, monsterY);
                Vector2 playerPosition = new Vector2(playerX, playerY);
                Vector2 direction = new Vector2(playerPosition).sub(grieverPosition).nor();
                float deltaX = direction.x * monsterSpeed * delta;
                float deltaY = direction.y * monsterSpeed * delta;


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


                monsterX += deltaX;
                monsterY += deltaY;
                grieverRectangle.setPosition(monsterX, monsterY);



                if (fixedGrieverDirection != null) {
                    grieverStateTime += delta;


                    if (grieverStateTime >= grieverAnimationTime) {
                        griever = getGrieverTextureForDirection(fixedGrieverDirection);
                        grieverStateTime = 0;
                    }
                }
            }
        }
    }


    public float getX() {
        return monsterX;
    }


    public float getY() {
        return monsterY;
    }


    private boolean isOppositeDirection(String currentDirection, String newDirection) {
        return (currentDirection.equals("up") && newDirection.equals("down")) ||
                (currentDirection.equals("down") && newDirection.equals("up")) ||
                (currentDirection.equals("left") && newDirection.equals("right")) ||
                (currentDirection.equals("right") && newDirection.equals("left"));
    }


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
                return grieverRight1;
        }
    }



    public boolean isGrieverStunned() {
        return isGrieverStunned;
    }

    public Rectangle getGrieverRectangle() {
        return grieverRectangle;
    }

    public void setMonsterX(float monsterX) {
        this.monsterX = monsterX;
    }

    public void setMonsterY(float monsterY) {
        this.monsterY = monsterY;
    }

    public void dispose() {
        grieverRight1.dispose();
        grieverRight2.dispose();
        grieverLeft1.dispose();
        grieverLeft2.dispose();
        grieverUp1.dispose();
        grieverUp2.dispose();
        grieverDown1.dispose();
        grieverDown2.dispose();
    }


}

