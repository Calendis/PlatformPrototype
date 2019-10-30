package live.inasociety.characters;

import live.inasociety.arena.Arena;
import live.inasociety.arena.ArenaBody;

import java.util.ArrayList;

public abstract class Character {
    private String name;

    private double[] pos = new double[2];
    private double[] vel = new double[2];
    private double[] angularAccel = new double[2];
    private int width;
    private int height;
    private int[] boxColour = new int[]{0, 0, 0};

    private double maxGroundVel;
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

    private boolean grounded = false;
    private boolean stopping = false;
    private boolean jumped = false;
    private boolean dashing = false;
    private boolean fastFalling = false;
    private boolean doubleJumped = false;
    private boolean droppingThrough = false;

    private int lagFrames = 0;
    private int dashFrames = 0;
    private int turnAroundFrames = 0;

    private boolean jumpPressed = false;

    protected Character(String name, int width, int height,
                        double maxGroundVel, double maxAirVel, double groundAccel, double airAccel,
                        double fallSpeed, double fastFallMultiplier,
                        double jumpStrength, double doubleJumpStrength, double groundStoppingSpeed, double airStoppingSpeed,
                        double weight, int dashLength, int turnAroundLength) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.maxGroundVel = maxGroundVel;
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

        pos[0] = 400;
        pos[1] = 340;

    }

    public void update() {
        // Reduce lag frames first
        if (lagFrames > 0) {
            lagFrames --;
        }
        if (dashFrames > 0) {
            dashFrames --;
        }
        if (turnAroundFrames > 0) {
            turnAroundFrames --;
        }

        // Update states
        if (vel[1] != 0) {
            grounded = false;
        }
        if (grounded) {
            jumped = false;
            doubleJumped = false;
            fastFalling = false;
        }
        if (dashFrames < 1) {
            dashing = false;
        }

        // Update vel from accel
        double accelX = angularAccel[1]*Math.cos(angularAccel[0]);
        double accelY = -angularAccel[1]*Math.sin(angularAccel[0]); // y is swapped from normal cartesian plane!
        vel[0] += accelX;
        vel[1] += accelY;

        // Update pos from vel
        pos[0] += vel[0];
        pos[1] += vel[1];

        // Reset accel
        angularAccel[1] = 0;

        if (grounded) {
            if (vel[0] > maxGroundVel) {
                vel[0] = maxGroundVel;
            }
            else if (vel[0] < -maxGroundVel) {
                vel[0] = -maxGroundVel;
            }

            if (stopping) {
                vel[0] /= groundAccel;
                //dashing = false;
                droppingThrough = true;
            } else {
                droppingThrough = false;
            }

            // Friction
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
        else {
            // Character is airborne
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
                vel[0] -= airStoppingSpeed;
                if (vel[0] < 0) {
                    vel[0] = 0;
                }
            } else if (vel[0] < 0) {
                vel[0] += airStoppingSpeed;
                if (vel[0] > 0) {
                    vel[0] = 0;
                }
            }
        }


    }

    public void applyForce(double angle, double magnitude) {
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
            TODO: ADD THIRD CONDITION BASED ON ACCELERATION
             */
            if (
                            (pos[1]+height > arenaBodyY && pos[1] < arenaBodyY + arenaBody.getHeight() &&
                            pos[0]+width > arenaBodyX && pos[0] < arenaBodyX+arenaBody.getWidth())
                    ||
                            (pos[1]+vel[1]+height > arenaBodyY && pos[1]+vel[1] < arenaBodyY + arenaBody.getHeight() &&
                            pos[0]+vel[0]+width > arenaBodyX && pos[0]+vel[0] < arenaBodyX+arenaBody.getWidth())
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

    public void dash(double angle, double magnitude) {
        if (lagFrames < 1) {
            if (Math.abs(magnitude) < groundStoppingSpeed / 1.5) {
                // Move normally if we're moving slowly
                applyForce(angle, groundAccel*magnitude);
            }
            else {
                // Otherwise, perform the dash logic
                if (vel[0] == 0) {
                    // If we're stationary and not currently dashing, perform a dash, where mag. is always 1 or -1
                    // Also we can never dash during turn-around
                    if (!dashing && turnAroundFrames == 0) {
                        dashing = true;
                        dashFrames = dashLength;
                        vel[0] = maxGroundVel * magnitude / Math.abs(magnitude);
                        //applyForce(angle, groundAccel * magnitude/Math.abs(magnitude));
                    }

                } else {
                    if (dashing) {
                        // If we're currently dashing, we can chain another dash, but only in the opposite direction
                        if (vel[0] / Math.abs(vel[0]) != magnitude / Math.abs(magnitude)) {
                            if (turnAroundFrames == 0) {
                                dashFrames = dashLength;
                                vel[0] = maxGroundVel * magnitude / Math.abs(magnitude);
                            }
                        } else {
                            // Move in same direction without dash
                            applyForce(angle, groundAccel * magnitude);
                        }
                    } else {
                        // We're not dashing, but not stationary
                        if (vel[0] / Math.abs(vel[0]) == magnitude / Math.abs(magnitude)) {
                            // Move in the same direction normally
                            applyForce(angle, groundAccel * magnitude);
                        } else {
                            // Turnaround movement, which has weaker acceleration
                            if (Math.abs(vel[0]) > maxGroundVel/2) {
                                turnAroundFrames = turnAroundLength;
                            }
                            applyForce(angle, groundAccel * magnitude / 100);
                        }
                    }

                }
            }
        }
    }

    public void drift(double angle, double magnitude) {
        applyForce(angle, airAccel*magnitude);
    }

    public void jump() {
        if (grounded && !jumped && !jumpPressed) {
            applyForce(90, jumpStrength);
            jumped = true;
            jumpPressed = true;
        }
        if (!doubleJumped && !jumpPressed) {
            // Double jump
            vel[1] = 0;
            applyForce(90, doubleJumpStrength);
            doubleJumped = true;
            fastFalling = false;
            jumped = true;
            jumpPressed = true;
        }
    }

    public void fastFall() {
        if(!fastFalling && vel[1] < fallSpeed*fastFallMultiplier && vel[1] > -0.1) {
            vel[1] = fallSpeed * fastFallMultiplier;
            fastFalling = true;
        }
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
        if (turnAroundFrames > 0) {
            return new int[] {255, 0, 255};
        }
        if (dashing) {
            return new int[] {0, 0, 255};
        }
        return boxColour;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isJumped() {
        return jumped;
    }

    public boolean isDashing() {
        return dashing;
    }

    public boolean isFastFalling() {
        return fastFalling;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
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

    public void setDashed(boolean dashing) {
        this.dashing = dashing;
    }

    public void setJumpPressed(boolean jumpPressed) {
        this.jumpPressed = jumpPressed;
    }

}
