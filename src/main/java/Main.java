import org.json.JSONObject;

import java.io.*;
import java.util.Date;
import java.util.Scanner;

/*
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005,quiet=y -jar /Users/fox/projects/miniaicup2/ms.jar true
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y -jar /Users/fox/projects/miniaicup2/ms.jar true

﻿java -jar /Users/fox/projects/miniaicup2/9802.jar
﻿java -jar /Users/fox/projects/miniaicup2/9252_2.jar
﻿java -jar /Users/fox/projects/miniaicup2/9252.jar
﻿java -jar /Users/fox/projects/miniaicup2/8269_2.jar
﻿java -jar /Users/fox/projects/miniaicup2/8269.jar
﻿java -jar /Users/fox/projects/miniaicup2/7450_3.jar
﻿java -jar /Users/fox/projects/miniaicup2/7450_2.jar
﻿java -jar /Users/fox/projects/miniaicup2/7450.jar
﻿java -jar /Users/fox/projects/miniaicup2/7439.jar
﻿java -jar /Users/fox/projects/miniaicup2/7430.jar
﻿java -jar /Users/fox/projects/miniaicup2/7412.jar
﻿java -jar /Users/fox/projects/miniaicup2/6835.jar
﻿java -jar /Users/fox/projects/miniaicup2/6833.jar - more fear
﻿java -jar /Users/fox/projects/miniaicup2/6820.jar - first normal pp
﻿java -jar /Users/fox/projects/miniaicup2/ms6706.jar

﻿java -jar /Users/fox/projects/miniaicup2/ms6545.jar
﻿java -jar /Users/fox/projects/miniaicup2/ms5588.jar
﻿java -jar /Users/fox/projects/miniaicup2/ms5866.jar
﻿java -jar /Users/fox/projects/miniaicup2/ms6480.jar
//TODO fixed clients

* */
public class Main {
    static boolean isLocalRun = false;
    private static Scanner scanner;
    public static Game game;
    private static BufferedReader in;
    private static boolean isRepeater;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            isLocalRun = args[0].equals("true");
        }
        if (isLocalRun) {
            System.setErr(Utils.getPrintStream());
            System.setOut(new PrintStream(new TeeOutputStream(Utils.getPrintStream(), System.out)));
            Utils.log("Start " + new Date());
        }

        in = new BufferedReader(new InputStreamReader(System.in));

        isRepeater = isLocalRun && args.length >= 2;

        if (isRepeater) {
            FileReader fr = null;
            try {
                fr = new FileReader(args[1]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            in = new BufferedReader(fr);
        }

        //scanner = new Scanner(new BufferedInputStream(System.in));
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
        String tmp = null;
        for (; ; ) {
            try {

                tmp = in.readLine();
                if (tmp == null || (isLocalRun && (tmp.startsWith("T") || tmp.isEmpty()))) {
                    continue;
                }
                next += tmp;

                //noinspection UnnecessaryLocalVariable
                JSONObject result = parseJson(next);
                if (isLocalRun && !result.has("Mine") && !result.has("GAME_WIDTH")) {
                    next = "";
                    continue;
                }
                return result;
            } catch (Exception e) {
                Utils.print(e);
            }
        }
    }

    private static JSONObject parseJson(String next) {
        return new JSONObject(next);
    }
}
