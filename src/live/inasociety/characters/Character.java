package live.inasociety.characters;

import live.inasociety.arena.Arena;
import live.inasociety.arena.ArenaBody;
import live.inasociety.data.ControlParameters;
import live.inasociety.interactors.HitBox;
import live.inasociety.interactors.HurtBox;
import live.inasociety.moves.Move;
import live.inasociety.moves.MoveLoader;
import live.inasociety.mylib.EvictingList;
import org.newdawn.slick.Image;

import java.util.ArrayList;

public abstract class Character {
    private String name;

    private double[] pos;
    private double[] vel = new double[2];
    private double[] angularAccel = new double[2];
    private int width;
    private int height;
    private int[] boxColour = new int[]{0, 0, 0};

    private double maxGroundVel;
    private double maxWalkVel;
    private double maxAirVel;
    private double groundAccel;
    private double airAccel;
    private double fallSpeed;
    private double fastFallMultiplier;
    private double jumpStrength;
    private double doubleJumpStrength;
    private double groundStoppingSpeed;
    private double airStoppingSpeed;
    private double weight;
    private int dashLength;
    private int turnAroundLength;
    private int landingLength;
    private int airDashLength;

    private Move activeMove = null;
    private Move jab;

    private enum WalkState {
        WALKING,
        DASHING,
        TO_RUNNING,
        RUNNING,
        NONE
    }

    private WalkState walkState = WalkState.NONE;

    private boolean grounded = false;
    private boolean stopping = false;
    private boolean jumped = false;

    private boolean fastFalling = false;
    private boolean doubleJumped = false;
    private boolean droppingThrough = false;
    private boolean facingRight = true;

    private EvictingList<Boolean> landingDetector = new EvictingList<>(2);
    private boolean jumpPressed = false;
    private int lagFrames = 0;
    private int dashFrames = 0;
    private int turnAroundFrames = 0;
    private int airDashCancelFrames = 0;
    private int currentAnimationLength = 0;

    private CharacterSpriteSheet characterSpriteSheet;
    private ArrayList<HurtBox> hurtBoxes;
    private ArrayList<HitBox> hitBoxes;
    private HurtBox feetBox;

    Character(double[] pos, String name, int width, int height,
              double maxGroundVel, double maxWalkVel, double maxAirVel, double groundAccel, double airAccel,
              double fallSpeed, double fastFallMultiplier,
              double jumpStrength, double doubleJumpStrength, double groundStoppingSpeed, double airStoppingSpeed,
              double weight, int dashLength, int turnAroundLength, int landingLength, int airDashLength,
              CharacterSpriteSheet characterSpriteSheet, HurtBox feetBox) {
        this.pos = pos;
        this.name = name;
        this.width = width;
        this.height = height;
        this.maxGroundVel = maxGroundVel;
        this.maxWalkVel = maxWalkVel;
        this.maxAirVel = maxAirVel;
        this.groundAccel = groundAccel;
        this.airAccel = airAccel;
        this.fallSpeed = fallSpeed;
        this.fastFallMultiplier = fastFallMultiplier;
        this.jumpStrength = jumpStrength;
        this.doubleJumpStrength = doubleJumpStrength;
        this.groundStoppingSpeed = groundStoppingSpeed;
        this.airStoppingSpeed = airStoppingSpeed;
        this.weight = weight;
        this.dashLength = dashLength;
        this.turnAroundLength = turnAroundLength;
        this.landingLength = landingLength;
        this.airDashLength = airDashLength;
        this.characterSpriteSheet = characterSpriteSheet;
        this.feetBox = feetBox;

        landingDetector.evictingAdd(true);
        landingDetector.evictingAdd(true);

        hurtBoxes = new ArrayList<>();
        hitBoxes = new ArrayList<>();
        hurtBoxes.add(feetBox);

        loadMoves();
    }

    public void update() {
        // Count down lag frames
        if (lagFrames > 0) {
            lagFrames --;
        }
        if (dashFrames > 0) {
            dashFrames --;
        }
        if (turnAroundFrames > 0) {
            turnAroundFrames --;
        }
        if (airDashCancelFrames > 0) {
            airDashCancelFrames --;
        }

        // Calculate x and y components of acceleration
        double accelX = angularAccel[1]*Math.cos(angularAccel[0]);
        double accelY = -angularAccel[1]*Math.sin(angularAccel[0]); // y is swapped from normal cartesian plane!

        // Truncate to hundreds place. This smooths precision errors from converting degrees to radians
        accelX = truncateToHundreds(accelX);
        accelY = truncateToHundreds(accelY);

        /*
            UPDATE STATES/FLAGS
         */

        // Are we moving vertically?
        if (vel[1] != 0) {
            // Then we're not grounded
            grounded = false;
        }

        // Are we grounded?
        if (grounded) {
            // Then we get our jumps back, and we're not falling
            jumped = false;
            doubleJumped = false;
            fastFalling = false;
            landingDetector.evictingAdd(true);
            detectLanding();
        }
        else {
            landingDetector.evictingAdd(false);
        }

        // If we're about to run out of dash frames...
        if (dashFrames == 1) {
            // start the transition from dashing to running (this can be cancelled)
            walkState = WalkState.TO_RUNNING;
        }
        else if (dashFrames < 1) {
            // If we run out of dash frames and we're not running...
            if (walkState != WalkState.RUNNING) {
                // we must be walking or stopped. The stopped case is already covered
                walkState = WalkState.WALKING;
            }
        }

        // Are we stationary horizontally?
        if (vel[0] == 0) {
            // Then our walk state should be none
            walkState = WalkState.NONE;
        }

        // Reset accel
        angularAccel[1] = 0;

        // Apply velocity limits
        if (grounded) {
            // Apply grounded velocity limits
            double conditionalMaxVel = maxGroundVel;
            if (walkState == WalkState.WALKING) {
                conditionalMaxVel = maxWalkVel;
            }

            if (vel[0] > conditionalMaxVel) {
                vel[0] = conditionalMaxVel;
            }
            else if (vel[0] < -conditionalMaxVel) {
                vel[0] = -conditionalMaxVel;
            }

            if (stopping) {
                vel[0] /= groundAccel;
                //dashing = false;
                droppingThrough = true;
            } else {
                droppingThrough = false;
            }

            // Ground friction
            if (walkState != WalkState.DASHING && walkState != WalkState.TO_RUNNING) {
                if (vel[0] > 0) {
                    vel[0] -= groundStoppingSpeed;
                    if (vel[0] < 0) {
                        vel[0] = 0;
                    }
                } else if (vel[0] < 0) {
                    vel[0] += groundStoppingSpeed;
                    if (vel[0] > 0) {
                        vel[0] = 0;
                    }
                }
            }
        }
        // Character is actionable
        if (lagFrames < 1) {
            if (!grounded) {
                // Character is airborne
                // Apply aerial velocity limits, but only while not in hit stun
                if (vel[0] > maxAirVel) {
                    vel[0] = maxAirVel;
                }
                else if (vel[0] < -maxAirVel) {
                    vel[0] = -maxAirVel;
                }

                if (vel[1] > fallSpeed && !fastFalling) {
                    vel[1] = fallSpeed;
                } else if (vel[1] > fallSpeed*fastFallMultiplier && fastFalling) {
                    vel[1] = fallSpeed*fastFallMultiplier;
                }

                // Air friction
                if (vel[0] > 0) {
                    //vel[0] -= airStoppingSpeed;
                    if (vel[0] < 0) {
                        vel[0] = 0;
                    }
                } else if (vel[0] < 0) {
                    //vel[0] += airStoppingSpeed;
                    if (vel[0] > 0) {
                        vel[0] = 0;
                    }
                }
            }
        }

        // Update vel from accel
        vel[0] += accelX;
        vel[1] += accelY;

        // Update pos from vel
        pos[0] += vel[0];
        pos[1] += vel[1];

        // Update position of hurt boxes
        for (HurtBox hurtBox : hurtBoxes) {
            hurtBox.update(pos[0], pos[1]);
        }

        // Update position of hit boxes
        for (HitBox hitBox : hitBoxes) {
            hitBox.update(pos[0], pos[1]);
        }

        cycleAnimation();
    }

    public void applyForce(double angle, double magnitude) {
        // This method sets instantaneous acceleration
        angle = Math.toRadians(angle);
        double accelX = angularAccel[1]*Math.cos(angularAccel[0]);
        double accelY = angularAccel[1]*Math.sin(angularAccel[0]);
        double forceX = magnitude*Math.cos(angle);
        double forceY = magnitude*Math.sin(angle);
        double newX = accelX + forceX;
        double newY = accelY + forceY;
        angularAccel[0] = Math.atan2(newY, newX);
        angularAccel[1] = Math.sqrt(newX*newX + newY*newY);

    }

    public void testCollisions(Arena arena, int screenWidth, int screenHeight) {
        ArrayList<ArenaBody> arenaBodies = arena.getArenaBodies();
        grounded = false;
        for (ArenaBody arenaBody : arenaBodies) {
            int arenaBodyX = arenaBody.getX(screenWidth, arena.getWidth());
            int arenaBodyY = arenaBody.getY(screenHeight, arena.getHeight());

            /*
                THE FIRST CONDITION OF THIS IF STATEMENT DETECTS CURRENT COLLISIONS WITH PLATFORMS
                THE SECOND CONDITION DETECTS FUTURE COLLISIONS BASED ON VELOCITY ADDING TO POSITION
             */
            if (
                            (feetBox.isColliding(arenaBody, arenaBodyX, arenaBodyY, 0, 0))
                    ||
                            (feetBox.isColliding(arenaBody, arenaBodyX, arenaBodyY, vel[0], vel[1]))
            ) {
                switch (arenaBody.getWallType()) {
                    case FLOOR:
                        pos[1] = arenaBodyY-height;
                        vel[1] = 0;
                        grounded = true;
                        break;
                    case CEILING:
                        if (pos[0] < arenaBodyX || pos[0]+width > arenaBodyX+arenaBody.getWidth()) {
                            if (pos[0] < arenaBodyX) {
                                pos[0] = arenaBodyX - width;
                            }
                            else  {
                                pos[0] = arenaBodyX + arenaBody.getWidth();
                            }
                        }
                        else {
                            pos[1] = arenaBodyY + arenaBody.getHeight();
                            if (vel[1] < 0) {
                                vel[1] = 0;
                            }
                        }
                        break;
                    case DROPTHROUGH:
                        if (vel[1] > 0 && pos[1] + height <= arenaBodyY+arenaBody.getHeight()) {
                            // Dropthrough platforms don't interact with upward-moving characters
                            pos[1] = arenaBodyY-height;
                            vel[1] = 0;
                            grounded = true;
                            if (droppingThrough) {
                                pos[1] += arenaBody.getHeight();
                            }
                        }
                    case INTANGIBLE:
                        break;
                }
            }
        }
    }

    private void groundMove(double angle, double magnitude) {
        // Are we actionable?
        if (lagFrames < 1 && turnAroundFrames < 1) {
            double absoluteMagnitude = Math.abs(magnitude);
            int inputDirection = (int) (magnitude / absoluteMagnitude);
            int currentDirection = (int) (vel[0] / Math.abs(vel[0]));

            // Move differently depending on our current movement state
            switch (walkState) {

                // If we're stationary, or if we're walking...
                case NONE:

                case WALKING:
                    // Walk with no complications
                    if (absoluteMagnitude <= ControlParameters.dashThreshold) {
                        applyForce(angle, groundAccel*magnitude);
                        walkState = WalkState.WALKING;
                    }
                    // And dash with no complications
                    else {
                        applyForce(0, inputDirection*groundAccel);
                        walkState = WalkState.DASHING;
                        dashFrames = dashLength;
                    }
                    turnAround(false, inputDirection);
                    break;

                // If we're dashing...
                case DASHING:
                    // We can't walk at all
                    if (absoluteMagnitude > ControlParameters.dashThreshold) {
                        if (currentDirection != inputDirection) {
                            // We can chain another dash, but only in the opposite direction
                            dashFrames = dashLength;
                            vel[0] = maxGroundVel * magnitude / absoluteMagnitude;
                            turnAround(false, inputDirection);
                            walkState = WalkState.DASHING;
                        }
                        else
                        {
                            // If the direction is the same
                            // then maintain the dash velocity, but don't chain dash frames
                            applyForce(0, inputDirection*groundAccel);
                        }
                    }
                    break;

                // If we're transitioning to running...
                case TO_RUNNING:
                    if (absoluteMagnitude > ControlParameters.dashThreshold) {
                        walkState = WalkState.RUNNING;
                    }
                    else {
                        walkState = WalkState.WALKING;
                    }
                    break;

                // If we're running...
                case RUNNING:
                    // We can keep running but only in the same direction
                    if (
                            absoluteMagnitude > ControlParameters.dashThreshold &&
                            currentDirection == inputDirection
                    ) {

                        //vel[0] = maxGroundVel * inputDirection;
                        applyForce(0, inputDirection*groundAccel);
                    }

                    // Other movement is only available after a turnaround. This makes running most committal
                    if (currentDirection != inputDirection) {
                        turnAround(true, inputDirection);
                        walkState = WalkState.NONE;
                    }
                    break;
            }
        }
    }

    public void move(double angle, double horizontalMagnitude, double verticalMagnitude) {

        // Did the player push the stick past the move threshold?
        double absoluteHorizonstalMagnitude = Math.abs(horizontalMagnitude);
        if (absoluteHorizonstalMagnitude > ControlParameters.moveThreshold) {
            // Then move according to air and ground rules
            if (grounded) {
                groundMove(angle, horizontalMagnitude);
            }
            else {
                drift(angle, horizontalMagnitude);
            }
        }

        // Also turnaround if they pushed it at least a LITTLE
        if (absoluteHorizonstalMagnitude > 0 && grounded) {
            turnAround(false, horizontalMagnitude / absoluteHorizonstalMagnitude);
        }

        // Is the player trying to crouch or fastfall?
        // TODO: implement crouch method
        if (verticalMagnitude > ControlParameters.fastFallThreshold) {
            fastFall();
        }
        else stopping = verticalMagnitude > ControlParameters.crouchThreshold;

    }

    private void drift(double angle, double magnitude) {

        if (walkState == WalkState.DASHING && vel[0] / Math.abs(vel[0]) != magnitude / Math.abs(magnitude)) {
            // Cancel an air dash by tapping in the opposite direction
            // This is so you can use an aerial other than a dash aerial out of a dash
            dashFrames = 0;
        }
        if (airDashCancelFrames < 1) {
            applyForce(angle, airAccel*magnitude);
        } else if (magnitude / Math.abs(magnitude) == magnitudeOfFaceDirection()) {
            // You can't drift back for a few frames after an air dash cancel
            // This is so you don't get erroneous drift after an air dash cancel
            applyForce(angle, airAccel*magnitude);
        }
    }

    public void jump() {
        if (lagFrames < 1) {
            droppingThrough = false;
            if (grounded && !jumped && !jumpPressed) {
                vel[1] = 0;
                angularAccel[1] = 0;
                applyForce(90, jumpStrength);
                jumped = true;
                jumpPressed = true;
                if (dashFrames > 0) {
                    // Jumping during dash extends dash frames to allow for freer use of dash aerials
                    dashFrames = airDashLength;
                    airDashCancelFrames = airDashLength;
                }
            }
            if (!doubleJumped && !jumpPressed) {
                // Double jump
                vel[1] = 0;
                angularAccel[1] = 0;
                applyForce(90, doubleJumpStrength);
                doubleJumped = true;
                fastFalling = false;
                jumped = true;
                jumpPressed = true;
                if (dashFrames > 0) {
                    // Double jumping during dash extends dash frames to allow for freer use of dash aerials
                    dashFrames = airDashLength;
                    airDashCancelFrames = airDashLength;
                }
            }
        }
    }

    public void attack(double xMagnitude, double yMagnitude) {
        // Character is actionable
        if (lagFrames < 1 && turnAroundFrames < 1) {
            // No attacking in turnaround or lag
            if (grounded) {
                // Grounded attacks
                if (Math.abs(yMagnitude) < ControlParameters.verticalAttackThreshold) {

                    if (Math.abs(xMagnitude) < ControlParameters.horizontalAttackThreshold) {
                        System.out.println("Jab");
                        activateMove(jab);
                        characterSpriteSheet.setAnimationRow(1);
                    }
                    else {
                        System.out.println("Side attack");
                    }

                }
                else if (yMagnitude < ControlParameters.verticalAttackThreshold) {
                    System.out.println("Up attack");
                }
                else if (yMagnitude > ControlParameters.verticalAttackThreshold){
                    System.out.println("Down attack");
                }

            } else {
                // Aerial attacks
                if (dashFrames > 0){
                    System.out.println("Dash aerial");
                }
                else if (Math.abs(yMagnitude) < ControlParameters.aerialAttackThreshold) {
                    if (Math.abs(xMagnitude) < ControlParameters.aerialAttackThreshold) {
                        System.out.println("Neutral aerial");
                    }
                    else if (xMagnitude / Math.abs(xMagnitude) == magnitudeOfFaceDirection()) {
                        System.out.println("Forward aerial");
                    }
                    else {
                        System.out.println("Back aerial");
                    }
                }
                else if (yMagnitude < ControlParameters.aerialAttackThreshold) {
                    System.out.println("Up aerial");
                }
                else if (yMagnitude > ControlParameters.aerialAttackThreshold){
                    System.out.println("Down aerial");
                }
            }
        }
    }

    private void cycleAnimation() {
        // Play current animation, and check if it's completed
        if (characterSpriteSheet.getAnimationFrame() >= currentAnimationLength - 2) {
            characterSpriteSheet.setAnimationFrame(0);
            characterSpriteSheet.setAnimationRow(0);
            currentAnimationLength = 0;
            activeMove = null;
        }
        else {
            characterSpriteSheet.incrementAnimationFrame();
        }

        if (activeMove != null) {
            // Get the current hit/hurt boxes for the animation
            hitBoxes.clear();
            hitBoxes.addAll(activeMove.getCurrentHitBoxes());
            hurtBoxes.clear();
            hurtBoxes.addAll(activeMove.getCurrentHurtBoxes());
        }
        else {
            hitBoxes.clear();
            hurtBoxes.clear();
            hurtBoxes.add(feetBox);
        }

    }

    private void fastFall() {
        if(!fastFalling && vel[1] < fallSpeed*fastFallMultiplier && vel[1] > -0.1) {
            vel[1] = fallSpeed * fastFallMultiplier;
            fastFalling = true;
        }
    }

    private void turnAround(boolean lag, double direction) {
        // Turn the character around from the current direction
        boolean changed;
        boolean oldFacingRight = facingRight;
        facingRight = direction == 1;
        changed = !oldFacingRight == facingRight;
        if (lag && changed) {
            turnAroundFrames = turnAroundLength;
        }
    }

    private void detectLanding() {
        if (!landingDetector.get(0) && landingDetector.get(1)) {
            lagFrames = landingLength;
        }
    }

    private void loadMoves() {
        jab = MoveLoader.loadMove(getResourceName(), "Jab");
    }

    private void activateMove(Move move) {
        activeMove = move;
        currentAnimationLength = move.getDuration();
    }

    private double truncateToHundreds(double d) {
        // Rounds the position, velocity, and acceleration to two decimal places
        // This smooths out floating point errors from converting degrees to radians
        return ((double)(int)(d*100))/100;
    }

    private int magnitudeOfFaceDirection() {
        return (facingRight ? 1 : -1);
    }

    public double getX() {
        return pos[0];
    }
    public double getY() {
        return pos[1];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getGroundAccel() {
        return groundAccel;
    }

    public double getJumpStrength() {
        return jumpStrength;
    }

    public double getFastFallMultiplier() {
        return fastFallMultiplier;
    }

    public double getfallSpeed() {
        return fallSpeed;
    }

    public double getAirAccel() {
        return airAccel;
    }

    public double[] getVel() {
        return vel;
    }

    public double getMaxGroundVel() {
        return maxGroundVel;
    }

    public int[] getBoxColour() {
        if (lagFrames > 0) {
            return new int[] {255, 0, 0};
        }
        if (turnAroundFrames > 0) {
            return new int[] {255, 255, 0};
        }
        if (dashFrames > 0) {
            return new int[] {0, 0, 255};
        }
        return boxColour;
    }

    public Image getImage() {
        return characterSpriteSheet.getCurrentImage(!facingRight);
    }

    public HurtBox getFeetBox() {
        return feetBox;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isJumped() {
        return jumped;
    }

    public boolean isFastFalling() {
        return fastFalling;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }

    private String getResourceName() {
        return name.replaceAll("\\s", "");
    }

    public void setAccel(double angle, double magnitude) {
        angle = Math.toRadians(angle);
        angularAccel[0] = angle;
        angularAccel[1] = magnitude;
    }

    public void reverseVelX() {
        vel[0] *= -1;
    }

    public void setVel(double x, double y) {
        vel[0] = x;
        vel[1] = y;
    }

    public void setStopping(boolean stopping) {
        this.stopping = stopping;
    }

    public void setJumped(boolean jumped) {
        this.jumped = jumped;

    }

    public void setJumpPressed(boolean jumpPressed) {
        this.jumpPressed = jumpPressed;
    }

}
