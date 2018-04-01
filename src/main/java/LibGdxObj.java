import com.badlogic.gdx.graphics.Color;

public class LibGdxObj extends Unit {

    public static final Color MY_PLAYER_COLOR = new Color(0, 1, 0, 0.6f);
    public static final Color ENEMY_PLAYER_COLOR = new Color(1, 0, 0, 0.6f);
    public static final Color VIRUS_COLOR = new Color(0, 0, 0, 0.8f);

    public LibGdxObj(Unit other) {
        this.radius = (float) other.radius;
        this.id = other.id;
        this.x = (float) other.x;
        this.y = (float) other.y;
        this.type = other.type;
        this.speedX = other.speedX;
        this.speedY = other.speedY;
        this.timeToFuse = other.timeToFuse;
        this.mass = other.mass;
        this.isMy = other.isMy;
        this.world = other.world;
        onConstruct();
    }

    private void onConstruct() {
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
            return MY_PLAYER_COLOR;
        }
        switch (type) {
            case FOOD:
                return Color.GRAY;
            case PLAYER:
                return ENEMY_PLAYER_COLOR;
            case EJECTION:
                return Color.BLUE;
            case VIRUS:
                return VIRUS_COLOR;
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

}
