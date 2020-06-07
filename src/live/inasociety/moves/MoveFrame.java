package live.inasociety.moves;

import live.inasociety.interactors.HitBox;
import live.inasociety.interactors.HurtBox;

import java.util.ArrayList;

class MoveFrame {
    private ArrayList<HitBox> hitBoxes;
    private ArrayList<HurtBox> hurtBoxes;

    MoveFrame(ArrayList<HitBox> hitBoxes, ArrayList<HurtBox> hurtBoxes) {
        this.hitBoxes = hitBoxes;
        this.hurtBoxes = hurtBoxes;
    }

    ArrayList<HitBox> getHitBoxes() {
        return hitBoxes;
    }

    ArrayList<HurtBox> getHurtBoxes() {
        return hurtBoxes;
    }
}
