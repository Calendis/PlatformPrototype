package live.inasociety.interactors;

public class HurtBox extends HBox {
    private boolean invincible;
    public HurtBox(double[] offset, double[] size, boolean invincible) {
        super(offset, size);
        this.invincible = invincible;
    }

    public boolean isInvincible() {
        return invincible;
    }
}
