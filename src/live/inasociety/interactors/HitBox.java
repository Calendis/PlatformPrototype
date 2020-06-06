package live.inasociety.interactors;

public class HitBox extends HBox {
    private double power;
    private double hitLag;
    public HitBox(double[] offset, double[] size, double power, double hitLag) {
        super(offset, size);
        this.power = power;
        this.hitLag = hitLag;
    }

    public double getPower() {
        return power;
    }

    public double getHitLag() {
        return hitLag;
    }
}
