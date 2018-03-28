import org.json.JSONObject;

import static java.lang.StrictMath.hypot;

/**
 * Базовый класс для определения объектов (<<юнитов>>) на игровом поле.
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "WeakerAccess"})
public class Unit {
    public double radius;
    public String id;
    public double x;
    public double y;
    public UnitType type;
    public double speedX;
    public double speedY;
    public int timeToFuse;
    public double mass;

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
        }  else {
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
        o.speedX = obj.getDouble("SX");
        o.speedY = obj.getDouble("SY");
        if (obj.has("TTF")) {
            o.timeToFuse = obj.getInt("TTF");
        }
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
}
