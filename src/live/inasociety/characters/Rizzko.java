package live.inasociety.characters;

import live.inasociety.interactors.HurtBox;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.io.IOException;
import java.net.URISyntaxException;

public class Rizzko extends Character {
    private static int width = 78;
    private static int height = 59;
    public Rizzko(double[] pos) throws SlickException {
        super(pos,
                "Captain Rizzko",
                width/2,
                height,
                5.3,
                2.5,
                4,
                2,
                1.6,
                7.8,
                2,
                13.6,
                18.1,
                0.3,
                0.17,
                20,
                11,
                12,
                4,
                14,
                new CharacterSpriteSheet(new Image("./resources/img/characters/rizzko_sheet.png"), width, height),
                new HurtBox(new double[] {20, height-10}, new double[] {width/2.0, 9}, true));
    }
}
