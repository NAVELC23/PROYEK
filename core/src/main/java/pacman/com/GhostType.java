package pacman.com;

public enum GhostType {
    RED(0.5f),
    PINK(1.0f),
    BLUE(0.5f),
    ORANGE(1.0f);

    private float speedMultiplier;

    GhostType(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public float getBaseSpeed() {
        return 100f * speedMultiplier;
    }

    public static String getTexturePath(GhostType type) {
        switch (type) {
            case RED: return "redGhost.png";
            case PINK: return "pinkGhost.png";
            case BLUE: return "blueGhost.png";
            case ORANGE: return "orangeGhost.png";
            default: return "redGhost.png";
        }
    }
}
