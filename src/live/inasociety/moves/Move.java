package live.inasociety.moves;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Move {
    private String name;
    private int startUp;
    private int activeFrames;
    private int endLag;
    private int totalFrames;
    private MoveFrame[] moveFrames;
    private int currentFrame;

    public Move(String name, int startUp, int activeFrames, int endLag) throws URISyntaxException, IOException {
        this.name = name;
        this.startUp = startUp;
        this.activeFrames = activeFrames;
        this.endLag = endLag;
        this.totalFrames = startUp + activeFrames + endLag;
        this.currentFrame = 0;
        loadMove();
    }

    private void loadMove() throws URISyntaxException, IOException {
        char hitChar = 'X';
        char hurtChar = 'C';

        // Load move file from package
        URL moveUrl = this.getClass().getResource("live/inasociety/moves" + name);
        File moveFile = new File(moveUrl.toURI());
        System.out.printf("move %s is %b long\n", name, moveFile.length());
        FileInputStream moveInputStream = new FileInputStream(moveFile);
        byte[] fileData = moveInputStream.readAllBytes();
        moveInputStream.close();
        System.out.printf("loaded file:\n%s\n", fileData);

        // Create hitboxes
        moveFrames = new MoveFrame[totalFrames];
        for (int i = 1; i < totalFrames+1; i++) {
            if (i <= startUp) {
                // TODO: Set the hitboxes on the moveFrame
                //moveFrames[i].set
            }
        }
    }

    public MoveFrame getCurrentMoveFrame() {
        return moveFrames[currentFrame];
    }

    public int getDuration() {
        return totalFrames;
    }
}
