import com.badlogic.gdx.graphics.Color;

public class LibGdxObj extends Unit {
    public static final int VISION_SHIFT = 10;
    public boolean isMy;

    public LibGdxObj(Unit other, boolean isMy) {
        this.radius = (float) other.radius;
        this.id = other.id;
        this.x = (float) other.x;
        this.y = (float) other.y;
        this.type = other.type;
        this.speedX = other.speedX;
        this.speedY = other.speedY;
        this.timeToFuse = other.timeToFuse;
        this.mass = other.mass;
        this.isMy = isMy;
        switch (type) {
            case FOOD:
                radius = 3;
                break;
            case PLAYER:
                break;
            case EJECTION:
                radius = 9;
                break;
            case VIRUS:
                radius = Main.game.VIRUS_RADIUS;
                break;
            case UNKNOWN:
                break;
        }
    }

    public Color getColor() {
        if (isMy) {
            return Color.GREEN;
        }
        switch (type) {
            case FOOD:
                return Color.GRAY;
            case PLAYER:
                return Color.RED;
            case EJECTION:
                return Color.BLUE;
            case VIRUS:
                return Color.DARK_GRAY;
            case UNKNOWN:
                return Color.FIREBRICK;
        }
        return Color.GOLD;
    }

    public float getFX() {
        return (float) x;
    }

    public float getFY() {
        return (float) y;
    }

    public float getFR() {
        return (float) radius;
    }

    public boolean wantPrintMass() {
        return type != UnitType.EJECTION && type != UnitType.VIRUS && type != UnitType.FOOD;
    }

    public float getVisionDistance() {
        if (type == UnitType.PLAYER) {
            return (float) (radius * 4);
        }
        return -1;
    }

    public Point2D getVisionCenter() {
        double speedAngle = getSpeedAngle();
        return new Point2D(VISION_SHIFT, 0).rotate(speedAngle).add(getPos());
    }

    private Point2D getSpeedVector() {
        return new Point2D(speedX, speedY);
    }
}
