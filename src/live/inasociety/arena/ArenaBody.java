package live.inasociety.arena;

public class ArenaBody {
    private int width;
    private int height;
    private int horizCentre;
    private int vertCentre;
    private int[] colour;
    private WallType wallType;

    public ArenaBody(int width, int height, int horizCentre, int vertCentre, int r, int g, int b, WallType wallType) {
        this.width = width;
        this.height = height;
        this.horizCentre = horizCentre;
        this.vertCentre = vertCentre;
        this.colour = new int[]{r, g, b};
        this.wallType = wallType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getHorizCentre() {
        return horizCentre;
    }

    public int getVertCentre() {
        return vertCentre;
    }

    public int getX(int screenWidth, int arenaWidth) {
        return screenWidth*horizCentre/arenaWidth - width/2;
    }

    public int getY(int screenHeight, int arenaHeight) {
        return screenHeight*vertCentre/arenaHeight - height/2;
    }

    public int[] getColour() {
        return colour;
    }

    public WallType getWallType() { return wallType; }
}
