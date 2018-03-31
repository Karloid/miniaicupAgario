import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Scanner;

/*
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005,quiet=y -jar /Users/fox/projects/miniaicup2/ms.jar true
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y -jar /Users/fox/projects/miniaicup2/ms.jar true

﻿java -jar /Users/fox/projects/miniaicup2/ms6545.jar
﻿java -jar /Users/fox/projects/miniaicup2/ms5588.jar
﻿java -jar /Users/fox/projects/miniaicup2/ms5866.jar
﻿java -jar /Users/fox/projects/miniaicup2/ms6480.jar

* */
public class Main {
    static boolean isLocalRun = false;
    private static Scanner scanner;
    public static Game game;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            isLocalRun = args[0].equals("true");
        }
        if (isLocalRun) {
            System.setErr(Utils.getPrintStream());
            System.setOut(new PrintStream(new TeeOutputStream(Utils.getPrintStream(), System.out)));
            Utils.log("Start " + new Date());
        }


        scanner = new Scanner(new BufferedInputStream(System.in));
        game = new Game(readJsonObject());

        if (isLocalRun) {
            try {
                Class.forName("LibGdxShower");
            } catch (ClassNotFoundException e) {
                Utils.print(e);
            }
        }

        Player player = new Player();

        MyStrategy myStrategy = new MyStrategy();
        int i = 0;


        for (; ; ) {
            try {

                World world = new World(readJsonObject());
                world.tickIndex = i;


                Move move = new Move();
                myStrategy.move(player, world, game, move);

                System.out.println(move.toJson());
                i++;
            } catch (Exception e) {
                Utils.print(e);
                if (Main.isLocalRun) {
                    throw e;
                }
            }
        }
    }

    private static JSONObject readJsonObject() {
        String next = "";
        for (; ; ) {
            try {
                next += scanner.next();

                //noinspection UnnecessaryLocalVariable
                JSONObject result = new JSONObject(next);
                return result;
            } catch (Exception e) {
                Utils.print(e);
            }
        }
    }
}
