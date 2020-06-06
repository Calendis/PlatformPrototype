package live.inasociety.interactors;

import live.inasociety.arena.ArenaBody;

public abstract class HBox {
    private double[] pos = new double[2];
    private double[] offset;
    private double[] size;
    private double[][] points = new double[4][2];
    public HBox(double[] offset, double[] size) {
        this.offset = offset;
        this.size = size;
    }

    public void update(double x, double y) {
        pos[0] = x + offset[0];
        pos[1] = y + offset[1];
        points[0] = new double[] {pos[0], pos[1]};
        points[1] = new double[] {pos[0]+size[0], pos[1]};
        points[2] = new double[] {pos[0]+size[0], pos[1]+size[1]};
        points[3] = new double[] {pos[0], pos[1]+size[1]};
    }

    public double[] getOffset() {
        return offset;
    }

    public double[] getPos() {
        return pos;
    }

    public double getX() {
        return pos[0];
    }

    public double getY() {
        return pos[1];
    }

    public double getWidth() {
        return size[0];
    }

    public double getHeight() {
        return size[1];
    }

    public boolean isColliding(double otherX, double otherY, double otherWidth, double otherHeight) {
        if (pos[0] < otherX + otherWidth && pos[0] + size[0] > otherX &&
        pos[1] < otherY + otherHeight && pos[1] + size[1] > otherY) {
            return true;
        }
        return false;
    }

    public boolean isColliding(HBox hBox) {
        return isColliding(hBox.getX(), hBox.getY(), hBox.getWidth(), hBox.getHeight());
    }

    public boolean isColliding(ArenaBody arenaBody, double arenaBodyX, double arenaBodyY, double vx, double vy) {
        return isColliding(arenaBodyX-vx, arenaBodyY-vy, arenaBody.getWidth(), arenaBody.getHeight());
    }
}
