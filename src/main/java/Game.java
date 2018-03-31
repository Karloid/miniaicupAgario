import org.json.JSONObject;

/**
 * Предоставляет доступ к различным игровым константам.
 */
public class Game {

    public int GAME_WIDTH = 990;                   //- размеры мира (int);
    public int GAME_HEIGHT = 990;                  //- размеры мира (int);
    public int GAME_TICKS = 7500;                   //- длительность игры в тиках (int);
    public double FOOD_MASS = 2;                    //- масса еды (float) (меняется от 1.0 до 4.0);
    public int MAX_FRAGS_CNT = 10;                    //- максимальное количество фрагментов у одного игрока (int) (от 4 до 16);
    public int TICKS_TIL_FUSION = 300;                 //- кол-во тиков после деления или взрыва, когда можно слить фрагменты (int) (от 150 до 500);
    public double VIRUS_RADIUS = 30;                 //- радиус вируса (float) (от 15.0 до 40.0);
    public double VIRUS_SPLIT_MASS = 66;                 //- критическая масса, по достижению которой вирус поделится (float) (от 50.0 до 100.0)
    public double VISCOSITY = 0.2;                    //- вязкость среды, от которой зависит скорость замедления вирусов, выбросов и фрагментов после деления (float) (от 0.05 - наименьшая вязкость, до 0.5)
    public double INERTION_FACTOR = 11;                  //- параметр регулировки инерции (насколько быстро изменяется вектор скорости при смене направления) (float) (от 1.0 - наибольшая инерция, до 20.0)
    public double SPEED_FACTOR = 40;                 //- параметр регулировки максимальной скорости (float) (от 25.0 до 100.0 - космические скорости)

    public Game(String next) {
        this(new JSONObject(next));
    }

    public Game(JSONObject obj) {
        try {
            GAME_WIDTH = obj.getInt("GAME_WIDTH");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            GAME_HEIGHT = obj.getInt("GAME_HEIGHT");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            GAME_TICKS = obj.getInt("GAME_TICKS");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            FOOD_MASS = obj.getDouble("FOOD_MASS");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            MAX_FRAGS_CNT = obj.getInt("MAX_FRAGS_CNT");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            TICKS_TIL_FUSION = obj.getInt("TICKS_TIL_FUSION");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            VIRUS_RADIUS = obj.getDouble("VIRUS_RADIUS");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            VIRUS_SPLIT_MASS = obj.getDouble("VIRUS_SPLIT_MASS");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            VISCOSITY = obj.getDouble("VISCOSITY");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            INERTION_FACTOR = obj.getDouble("INERTION_FACTOR");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
        try {
            SPEED_FACTOR = obj.getDouble("SPEED_FACTOR");
        } catch (Exception e) {
            Utils.print(e);
        }
        ;
    }
}
