package de.tum.cit.fop.maze;








import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;








public class Player {
    private Texture currentTexture;
    private Texture up1, up2, down1, down2, left1, left2, right1, right2, dead;
    private float x, y;
    private float speed, runningSpeed;
    private float runTimer = 0f, cooldownTimer = 0f;
    private boolean isRunning = false, canRun = true;
    private float stateTime = 0f;
    private String direction = "right";
    private Rectangle boundingBox;
    private float scale = 0.2f;
    private boolean moved = false;
    private float previousX, previousY;




    private final float runDuration = 4f;
    private final float cooldownDuration = 6f;
    private final float walkAnimationTime = 0.1f;




    private boolean isDead;








    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 10.0f;
        this.runningSpeed = 20.0f;
        this.previousX = startX;
        this.previousY = startY;








        // Load textures
        this.up1 = new Texture("boy_up1.png");
        this.up2 = new Texture("boy_up2.png");
        this.down1 = new Texture("boy_down1.png");
        this.down2 = new Texture("boy_down2.png");
        this.left1 = new Texture("boy_left1.png");
        this.left2 = new Texture("boy_left2.png");
        this.right1 = new Texture("boy_right1.png");
        this.right2 = new Texture("boy_right2.png");
        this.dead = new Texture("boydead.png");


        this.currentTexture = right1;
        this.boundingBox = new Rectangle(x, y, currentTexture.getWidth(), currentTexture.getHeight());


        this.isDead = false;
    }








    public void update(float delta, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean runKeyPressed) {
        previousX = x;
        previousY = y;
        boundingBox.setSize(currentTexture.getWidth() * scale, currentTexture.getHeight() * scale);
        // Handle running logic
        if (canRun && runKeyPressed) {
            isRunning = true;
        } else {
            isRunning = false;
        }

        if (isRunning) {
            runTimer += delta;
            if (runTimer >= runDuration) {
                canRun = false;
                runTimer = runDuration;
            }
        } else if (!canRun) {
            cooldownTimer += delta;
            if (cooldownTimer >= cooldownDuration) {
                canRun = true;
                cooldownTimer = 0;
                runTimer = 0;
            }
        }

        float currentSpeed = isRunning ? runningSpeed : speed;


        if (moveUp) {
            y += currentSpeed * delta;
            direction = "up";
            animate(delta, up1, up2);
            moved = true;
        } else if (moveDown) {
            y -= currentSpeed * delta;
            direction = "down";
            animate(delta, down1, down2);
            moved = true;
        } else if (moveLeft) {
            x -= currentSpeed * delta;
            direction = "left";
            animate(delta, left1, left2);
            moved = true;
        } else if (moveRight ) {
            x += currentSpeed * delta;
            direction = "right";
            animate(delta, right1, right2);
            moved = true;
        }
        if (moved) {
            System.out.println("X coordinate:" + x + " Y coordinate:" + y);
        }

        boundingBox.setPosition(x, y);
    }

    public void revertToPrevious() {
        x = previousX;
        y = previousY;
        boundingBox.setPosition(x, y);
    }


    private void animate(float delta, Texture texture1, Texture texture2) {
        stateTime += delta;
        if (stateTime >= walkAnimationTime) {
            currentTexture = (currentTexture == texture1) ? texture2 : texture1;
            stateTime = 0f;
        }
    }








    public void render(SpriteBatch batch) {
        batch.draw(currentTexture, x, y, currentTexture.getWidth() * scale, currentTexture.getHeight() * scale);
    }












    public Rectangle getBoundingBox() {
        return boundingBox;
    }




    public float getX() {
        return x;
    }








    public float getY() {
        return y;
    }








    public void setX(float x) {
        this.x = x;
    }








    public void setY(float y) {
        this.y = y;
    }








    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }












    public boolean isDead() {
        return isDead;
    }




    public void setTexture(Texture texture) {
        this.currentTexture = texture;
    }




    // This method marks the player as dead and sets the dead texture
    public void setDead() {
        this.isDead = true;
        setTexture(dead);  // Change the texture to the dead texture
    }








    public void dispose() {
        up1.dispose();
        up2.dispose();
        down1.dispose();
        down2.dispose();
        left1.dispose();
        left2.dispose();
        right1.dispose();
        right2.dispose();
        dead.dispose();
    }
}



