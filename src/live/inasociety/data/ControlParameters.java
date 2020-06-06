package live.inasociety.data;

public class ControlParameters {
    // Buttons
    private static final int buttonA = 0;
    private static final int buttonB = 1;
    private static final int buttonX = 3;
    private static final int buttonY = 2;

    // Analog stick axes
    private static final int leftHorizontalAxis = 0;
    private static final int leftVerticalAxis = 1;
    private static final int rightHorizontalAxis = 2;
    private static final int rightVerticalAxis = 3;

    // Stick thresholds
    public static final double moveThreshold = 0.2;
    public static final double dashThreshold = 0.5;
    public static final double crouchThreshold = 0.5;
    public static final double fastFallThreshold = 0.72;
    public static final double horizontalAttackThreshold = 0.13;
    public static final double verticalAttackThreshold = 0.08;
    public static final double aerialAttackThreshold = 0.08;

    // Control mappings
    public static final int jumpButton = buttonA;
    public static final int attackButton = buttonB;
    public static final int shieldButton = buttonX;
    public static final int specialButton = buttonY;
    public static final int mainStickHorizontal = leftHorizontalAxis;
    public static final int mainStickVertical = leftVerticalAxis;
    public static final int altStickHorizontal = rightHorizontalAxis;
    public static final int altStickVertical = rightVerticalAxis;
}
