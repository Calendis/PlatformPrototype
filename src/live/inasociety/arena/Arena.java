package live.inasociety.arena;

import java.util.ArrayList;

public class Arena {
    private String name;
    private int width;
    private int height;
    private ArrayList<ArenaBody> arenaBodies;

    public Arena(String name, int width, int height, ArrayList<ArenaBody> arenaBodies) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.arenaBodies = arenaBodies;
    }

    public ArrayList<ArenaBody> getArenaBodies() {
        return arenaBodies;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
