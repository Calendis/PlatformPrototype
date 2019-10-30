package live.inasociety;

import live.inasociety.arena.Arena;
import live.inasociety.arena.ArenaBody;
import live.inasociety.arena.ArenaLoader;
import live.inasociety.characters.Character;
import live.inasociety.characters.Rizzko;
import org.newdawn.slick.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PlatformPrototype extends BasicGame{
    private static int screenWidth = 900;
    private static int screenHeight = 700;
    private Input input = new Input(screenHeight);

    private Arena currentArena;
    private Character currentCharacter;

    public PlatformPrototype(String n) {
        super(n);
    }

    public static void main(String[] args) {
        System.out.println("Platform Prototype by Calendis");
        System.out.println("https://inasociety.live/");
        System.out.println("Creating game container...");
        try {
            AppGameContainer gameContainer = new AppGameContainer(new PlatformPrototype("Platform Prototype"));
            gameContainer.setDisplayMode(screenWidth, screenHeight, false);
            gameContainer.setTargetFrameRate(60);
            gameContainer.start();
        } catch (SlickException se) {
            Logger.getLogger(PlatformPrototype.class.getName()).log(Level.SEVERE, null, se);
        }
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {

        currentArena = ArenaLoader.loadArena("default");
        currentCharacter = new Rizzko();

    }

    @Override
    public void update(GameContainer gameContainer, int i) throws SlickException {
        // Poll the controller
        input.poll(screenWidth, screenHeight);
        if (input.isButtonPressed(0, 0)) {
            //System.out.println("A");
                currentCharacter.jump();
        } else {
            currentCharacter.setJumpPressed(false);
        }
        if (input.isButtonPressed(1, 0)) {
            //System.out.println("B");
            System.out.println("B pressed");
        }
        if (input.isButtonPressed(2, 0)) {
            //System.out.println("X");
            System.out.println("X pressed");
        }
        if (input.isButtonPressed(3, 0)) {
            //System.out.println("Y");
            System.out.println("Y pressed");
        }
        if (input.getAxisValue(0, 0) < 0 || input.getAxisValue(0, 0) > 0) {
            if (currentCharacter.isGrounded()) {
                currentCharacter.dash(0, input.getAxisValue(0, 0));
            }
            else {
                currentCharacter.drift(0, input.getAxisValue(0, 0));
            }
        }

        if (input.getAxisValue(0, 0) < 0.4 && input.getAxisValue(0, 0) > -0.4 &&
        input.getAxisValue(0, 1) > 0.9) {
            if (currentCharacter.isGrounded()) {
                currentCharacter.setStopping(true);
            }
            else {
                currentCharacter.fastFall();
            }

        }
        else {
            currentCharacter.setStopping(false);
        }


        // Gravity
        currentCharacter.applyForce(270, 1.2);

        currentCharacter.testCollisions(currentArena, screenWidth, screenHeight);
        currentCharacter.update();

    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        graphics.setBackground(Color.cyan);
        // Render the arena
        for (ArenaBody arenaBody : currentArena.getArenaBodies()) {
            int[] arenaBodyColour = arenaBody.getColour();
            graphics.setColor(new Color(arenaBodyColour[0], arenaBodyColour[1], arenaBodyColour[2]));
            graphics.fillRect(
                    arenaBody.getX(screenWidth, currentArena.getWidth()),
                    arenaBody.getY(screenHeight, currentArena.getHeight()),
                    arenaBody.getWidth(), arenaBody.getHeight());
        }
        // Render the character
        graphics.setColor(new Color(currentCharacter.getBoxColour()[0], currentCharacter.getBoxColour()[1], currentCharacter.getBoxColour()[2]));
        graphics.fillRect((float)currentCharacter.getX(), (float)currentCharacter.getY(), currentCharacter.getWidth(), currentCharacter.getHeight());
    }


}
