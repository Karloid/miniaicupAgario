import org.json.JSONObject;

import static java.lang.StrictMath.hypot;

/**
 * Базовый класс для определения объектов (<<юнитов>>) на игровом поле.
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "WeakerAccess"})
public class Unit {
    public static final int VISION_SHIFT = 10;
    public double radius;
    public String id;
    public double x;
    public double y;
    public UnitType type;
    public double speedX;
    public double speedY;
    public int timeToFuse;
    public double mass;
    public boolean isMy;


    public World world;

    public static Unit parse(JSONObject obj) {
        Unit o = new Unit();
        o.x = obj.getDouble("X");
        o.y = obj.getDouble("Y");
        if (obj.has("T")) {
            String t = obj.getString("T");
            UnitType ttt;
            switch (t) {
                case "F":
                    ttt = UnitType.FOOD;
                    break;
                case "V":
                    ttt = UnitType.VIRUS;
                    break;
                case "E":
                    ttt = UnitType.EJECTION;
                    break;
                case "P":
                    ttt = UnitType.PLAYER;
                    break;
                default:
                    ttt = UnitType.FOOD;
                    Utils.log("ERROR unknow type " + t);
            }
            o.type = ttt;
        } else {
            o.type = UnitType.UNKNOWN;
        }

        if (obj.has("Id")) {
            o.id = obj.getString("Id");
        }

        if (obj.has("M")) {
            o.mass = obj.getDouble("M");
        }

        if (obj.has("R")) {
            o.radius = obj.getDouble("R");
        }

        return o;
    }

    public static Unit parseMine(JSONObject obj) {
        Unit o = parse(obj);
        o.type = UnitType.PLAYER;
        o.speedX = obj.getFloat("SX");
        o.speedY = obj.getFloat("SY");
        if (obj.has("TTF")) {
            o.timeToFuse = obj.getInt("TTF");
        }
        o.isMy = true;
        return o;
    }

    public String getId() {
        return id;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public double getDistanceTo(double x, double y) {
        return hypot(x - this.x, y - this.y);
    }

    public double getDistanceTo(Unit unit) {
        return getDistanceTo(unit.x, unit.y);
    }

    /**
     * @param x X-координата точки.
     * @param y Y-координата точки.
     * @return Возвращает квадрат расстояния до точки от центра данного объекта.
     */
    public double getSquaredDistanceTo(double x, double y) {
        double dx = x - this.x;
        double dy = y - this.y;
        return dx * dx + dy * dy;
    }

    /**
     * @param unit Объект, до центра которого необходимо определить квадрат расстояния.
     * @return Возвращает квадрат расстояния от центра данного объекта до центра указанного объекта.
     */
    public double getSquaredDistanceTo(Unit unit) {
        return getSquaredDistanceTo(unit.x, unit.y);
    }

    /**
     * @return Возвращает радиус объекта.
     */
    public double getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Unit{");
        sb.append("radius=").append(radius);
        sb.append(", id='").append(id).append('\'');
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", type=").append(type);
        if (speedX != 0) {
            sb.append(", speedX=").append(speedX);
            sb.append(", speedY=").append(speedY);
            sb.append(", timeToFuse=").append(timeToFuse);
        }
        sb.append(", mass=").append(mass);
        sb.append('}');
        return sb.toString();
    }

    public Point2D getPos() {
        return new Point2D(x, y);
    }

    public double getAngleTo(Unit enemy) {
        return enemy.getPos().sub(getPos()).angle();
    }

    public double getSpeedAngle() {
        return Math.atan2(speedY, speedX);
    }

    public double getDistanceToClosestCorner(Game game) {
        int w = game.GAME_WIDTH;
        int h = game.GAME_HEIGHT;

        double result = getDistanceTo(0, 0);
        result = Math.min(result, getDistanceTo(0, h));
        result = Math.min(result, getDistanceTo(w, 0));
        result = Math.min(result, getDistanceTo(w, h));

        return result;
    }

    public double getVisionDistance() {
        if (type == UnitType.PLAYER) {
            if (isMy && world.mines.size() > 1) {
                return (float) (2.5 * radius * Math.sqrt(world.mines.size()));
            }
            return (float) (radius * 4);
        }
        return -1;
    }

    public Point2D getVisionCenter() {
        double speedAngle = getSpeedAngle();
        return new Point2D(VISION_SHIFT, 0).rotate(speedAngle).add(getPos());
    }

    public Point2D getSpeedVector() {
        return new Point2D(speedX, speedY);
    }

    public boolean equalsPos(Unit target) {
        if (target == null) {
            return false;
        }
        return (int) target.x == (int) x && (int) target.y == (int) y;
    }
}
