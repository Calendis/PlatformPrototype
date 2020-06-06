package live.inasociety;

import live.inasociety.arena.Arena;
import live.inasociety.arena.ArenaBody;
import live.inasociety.arena.ArenaLoader;
import live.inasociety.characters.Character;
import live.inasociety.characters.Rizzko;
import live.inasociety.data.ControlParameters;
import live.inasociety.data.PhysicsParameters;
import org.newdawn.slick.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PlatformPrototype extends BasicGame{
    private static int screenWidth = 900;
    private static int screenHeight = 700;
    private Input input = new Input(screenHeight);

    private Arena currentArena;
    private Character currentCharacter;

    private PlatformPrototype(String n) {
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

        currentArena = ArenaLoader.loadArena("test_arena");
        currentCharacter = new Rizzko(new double[] {200, 0});

    }


    @Override
    public void update(GameContainer gameContainer, int i) throws SlickException {
        // Poll the controller
        input.poll(screenWidth, screenHeight);

        /*
            PROCESS DIGITAL INPUTS
        */

        // Is the jump button pressed?
        if (input.isButtonPressed(ControlParameters.jumpButton, 0)) {
                currentCharacter.jump();
        }
        else {
            currentCharacter.setJumpPressed(false);
        }

        // Is the attack button pressed?
        if (input.isButtonPressed(ControlParameters.attackButton, 0)) {
            currentCharacter.attack(
                    input.getAxisValue(0, ControlParameters.mainStickHorizontal),
                    input.getAxisValue(0, ControlParameters.mainStickVertical)
            );
        }

        // Is the shield button pressed?
        if (input.isButtonPressed(ControlParameters.shieldButton, 0)) {
            System.out.println("shield");
        }

        // Is the special button pressed?
        if (input.isButtonPressed(ControlParameters.specialButton, 0)) {
            System.out.println("special");
        }

        /*
            PROCESS ANALOG INPUTS
         */
        double currentMainStickHorizontal = input.getAxisValue(0, ControlParameters.mainStickHorizontal);
        double currentMainStickVertical = input.getAxisValue(0, ControlParameters.mainStickVertical);
        double currentAltStickHorizontal = input.getAxisValue(0, ControlParameters.altStickHorizontal);
        double currentAltStickVertical = input.getAxisValue(0, ControlParameters.altStickVertical);

        // The character handles controller inputs for movement
        currentCharacter.move(0, currentMainStickHorizontal, currentMainStickVertical);

        // Gravity
        currentCharacter.applyForce(PhysicsParameters.gravityDirection, PhysicsParameters.gravityMagnitude);

        // Do physics
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

        graphics.fillRect((float)currentCharacter.getFeetBox().getX(), (float)currentCharacter.getFeetBox().getY(),
                (float)currentCharacter.getFeetBox().getWidth(), (float)currentCharacter.getFeetBox().getHeight());

        currentCharacter.getImage().draw((float)currentCharacter.getX(), (float)currentCharacter.getY());
    }


}
