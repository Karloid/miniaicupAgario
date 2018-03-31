import org.json.JSONObject;

/**
 * Стратегия игрока может управлять юнитами посредством установки свойств объекта данного класса.
 */
public class Move {

    public double x;
    public double y;


    public String debug;
    public boolean split;
    public boolean eject;

    public String spriteId;
    public String spireDebug;


    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Debug", debug);
        jsonObject.put("X", x);
        jsonObject.put("Y", y);
        if (split) {
            jsonObject.put("Split", split);
        }

        if (eject) {
            jsonObject.put("Eject", eject);
        }

        if (spriteId != null) {
            JSONObject sprite = new JSONObject();
            sprite.put("Id", spriteId);
            sprite.put("S", spireDebug);
            jsonObject.put("Sprite", sprite);
        }
        return jsonObject.toString();
    }

    public void goTo(Unit unit) {
        x = unit.getX();
        y = unit.getY();
    }

    public void goTo(double xx, double yy) {
        x = xx;
        y = yy;
    }

    public void goTo(Point2D point) {
        x = point.getX();
        y = point.getY();
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public void setEject(boolean eject) {
        this.eject = eject;
    }

    public float getFX() {
        return (float) x;
    }

    public float getFY() {
        return (float) y;
    }
}
