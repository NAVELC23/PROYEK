package pacman.com;

public enum GhostType {
    RED(1.1f), // Red Ghost with a speed multiplier
    PINK(1.1f), // Pink Ghost with a speed multiplier
    BLUE(1.1f), // Blue Ghost with a speed multiplier
    ORANGE(1.05f); // Orange Ghost with a speed multiplier

    private float speedMultiplier; //

    GhostType(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier; //
    }

    public float getBaseSpeed() {
        return 100f * speedMultiplier; // Calculates base speed
    }

    public static String getTexturePath(GhostType type) {
        switch (type) { //
            case RED: return "redGhost.png"; //
            case PINK: return "pinkGhost.png"; //
            case BLUE: return "blueGhost.png"; //
            case ORANGE: return "orangeGhost.png"; //
            default: return "redGhost.png"; // Default texture
        }
    }
}
