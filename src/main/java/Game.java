import org.json.JSONObject;

/**
 * Предоставляет доступ к различным игровым константам.
 */
public class Game {

    public int GAME_WIDTH;                   //- размеры мира (int);
    public int GAME_HEIGHT;                  //- размеры мира (int);
    public int GAME_TICKS;                   //- длительность игры в тиках (int);
    public double FOOD_MASS;                    //- масса еды (float) (меняется от 1.0 до 4.0);
    public int MAX_FRAGS_CNT;                    //- максимальное количество фрагментов у одного игрока (int) (от 4 до 16);
    public int TICKS_TIL_FUSION;                 //- кол-во тиков после деления или взрыва, когда можно слить фрагменты (int) (от 150 до 500);
    public double VIRUS_RADIUS;                 //- радиус вируса (float) (от 15.0 до 40.0);
    public double VIRUS_SPLIT_MASS;                 //- критическая масса, по достижению которой вирус поделится (float) (от 50.0 до 100.0)
    public double VISCOSITY;                    //- вязкость среды, от которой зависит скорость замедления вирусов, выбросов и фрагментов после деления (float) (от 0.05 - наименьшая вязкость, до 0.5)
    public double INERTION_FACTOR;                  //- параметр регулировки инерции (насколько быстро изменяется вектор скорости при смене направления) (float) (от 1.0 - наибольшая инерция, до 20.0)
    public double SPEED_FACTOR;                 //- параметр регулировки максимальной скорости (float) (от 25.0 до 100.0 - космические скорости)

    public Game(String next) {
        Utils.log("GAME: " + next);
        JSONObject obj = new JSONObject(next);

        GAME_WIDTH = obj.getInt("GAME_WIDTH");
        GAME_HEIGHT = obj.getInt("GAME_HEIGHT");
        GAME_TICKS = obj.getInt("GAME_TICKS");
        FOOD_MASS = obj.getDouble("FOOD_MASS");
        MAX_FRAGS_CNT = obj.getInt("MAX_FRAGS_CNT");
        TICKS_TIL_FUSION = obj.getInt("TICKS_TIL_FUSION");
        VIRUS_RADIUS = obj.getDouble("VIRUS_RADIUS");
        VIRUS_SPLIT_MASS = obj.getDouble("VIRUS_SPLIT_MASS");
        VISCOSITY = obj.getDouble("VISCOSITY");
        INERTION_FACTOR = obj.getDouble("INERTION_FACTOR");
        SPEED_FACTOR = obj.getDouble("SPEED_FACTOR");

    }

    public static Game from(String next) {
        return new Game(next);
    }
}
