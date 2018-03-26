import org.json.JSONObject;

/**
 * Предоставляет доступ к различным игровым константам.
 */
public class Game {

    public Game(String next) {
        new JSONObject(next);
    }

    public static Game from(String next) {
        return new Game(next);
    }
}
