import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Этот класс описывает игровой мир. Содержит также описания всех игроков, игровых объектов (<<юнитов>>) и сооружений.
 */
@SuppressWarnings("ForLoopWithMissingComponent")
public class World {
    public int tickIndex;
    public double width;
    public double height;

    public List<Unit> food = new ArrayList<>(0);
    public List<Unit> ejections = new ArrayList<>(0);
    public List<Unit> mines = new ArrayList<>(0);
    public List<Unit> viruses = new ArrayList<>(0);
    public List<Unit> enemies = new ArrayList<>(0); //TODO groups

    public World(String nextString) {
        Utils.log("World: " + nextString);

        JSONObject jsonObject = new JSONObject(nextString);

        JSONArray minesObjects = jsonObject.getJSONArray("Mine");
        for (int i = 0; i < minesObjects.length(); i++) {
            JSONObject obj = minesObjects.getJSONObject(i);
            Unit unit = Unit.parseMine(obj);
            switch (unit.type) {
                case PLAYER:
                    this.mines.add(unit);
                    break;
                default:
                    Utils.log("strange mine object " + obj.toString());
            }
        }

        JSONArray objects = jsonObject.getJSONArray("Objects");
        for (int i = 0; i < objects.length(); i++) {
            JSONObject obj = objects.getJSONObject(i);
            Unit unit = Unit.parse(obj);
            switch (unit.type) {
                case FOOD:
                    food.add(unit);
                    break;
                case PLAYER:
                    enemies.add(unit);
                    break;
                case EJECTION:
                    ejections.add(unit);
                    break;
                case VIRUS:
                    viruses.add(unit);
                    break;
            }
        }
    }

    public static World parse(String nextString) {
        return new World(nextString);
    }


    public int getTickIndex() {
        return tickIndex;
    }
}
