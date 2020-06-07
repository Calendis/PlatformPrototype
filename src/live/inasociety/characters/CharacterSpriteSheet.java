package live.inasociety.characters;

import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

public class CharacterSpriteSheet extends SpriteSheet {
    private int animationRow = 0;
    private int animationFrame = 0;
    private int tw;
    private int th;

    public CharacterSpriteSheet(Image image, int tw, int th) {
        super(image, tw, th);
        this.tw = tw;
        this.th = th;
    }

    public void setAnimationRow(int animationRow) {
        this.animationRow = animationRow;
    }

    public void setAnimationFrame(int animationFrame) {
        this.animationFrame = animationFrame;
    }

    public void incrementAnimationFrame() {
        setAnimationFrame(animationFrame + 1);
    }

    public Image getCurrentImage(boolean facingRight) {
        return getSubImage(animationFrame, animationRow).getFlippedCopy(facingRight, false);
    }

    public int getAnimationFrame() {
        return animationFrame;
    }
}
