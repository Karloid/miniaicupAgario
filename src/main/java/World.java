import org.json.JSONObject;

/**
 * Этот класс описывает игровой мир. Содержит также описания всех игроков, игровых объектов (<<юнитов>>) и сооружений.
 */
@SuppressWarnings("ForLoopWithMissingComponent")
public class World {
    public int tickIndex;
    public double width;
    public double height;

    public World(String nextString) {
        JSONObject jsonObject = new JSONObject(nextString);
    }

    public static World parse(String nextString) {
        return new World(nextString);
    }


    public int getTickIndex() {
        return tickIndex;
    }
}
