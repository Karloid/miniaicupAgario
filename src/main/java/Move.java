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
        jsonObject.put("Y", x);
        if (split) {
            jsonObject.put("Split", split);
        }

        if (split) {
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
}
