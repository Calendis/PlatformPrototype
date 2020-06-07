package live.inasociety.moves;

import live.inasociety.interactors.HitBox;
import live.inasociety.interactors.HurtBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class Move {
    private String name;
    private int startUp;
    private int activeFrames;
    private int endLag;
    private MoveFrame[] moveFrames;

    private int currentFrame;
    private int totalFrames;

    Move(String name, int startUp, int activeFrames, int endLag, MoveFrame[] moveFrames) {
        this.name = name;
        this.startUp = startUp;
        this.activeFrames = activeFrames;
        this.endLag = endLag;
        this.moveFrames = moveFrames;
        this.totalFrames = startUp + activeFrames + endLag;
        this.currentFrame = 0;

        System.out.println("   and: "+totalFrames);
    }

    public int getDuration() {
        return totalFrames;
    }

    public ArrayList<HitBox> getCurrentHitBoxes() {
        return moveFrames[currentFrame].getHitBoxes();
    }

    public ArrayList<HurtBox> getCurrentHurtBoxes() {
        return moveFrames[currentFrame].getHurtBoxes();
    }
}
