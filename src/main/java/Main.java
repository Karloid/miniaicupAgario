import java.io.BufferedInputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Scanner;

/*
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005,quiet=y -jar /Users/fox/projects/miniaicup2/build/libs/miniaicup2-1.0-all.jar
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y -jar /Users/fox/projects/miniaicup2/build/libs/miniaicup2-1.0-all.jar


* */
public class Main {
    static boolean isLocalRun = System.getProperty("user.name").equals("fox");

    public static void main(String[] args) {
        if (isLocalRun) {
            System.setErr(Utils.getPrintStream());
            System.setOut(new PrintStream(new TeeOutputStream(Utils.getPrintStream(), System.out)));
            Utils.log("Start " + new Date());
        }

        Player player = new Player();

        MyStrategy myStrategy = new MyStrategy();
        int i = 0;

        Scanner scanner = new Scanner(new BufferedInputStream(System.in));
        Game game = Game.from(scanner.next());

        for (; ; ) {
            try {

                String nextString = scanner.next();

                World world = World.parse(nextString);
                world.tickIndex = i;


                Move move = new Move();
                myStrategy.move(player, world, game, move);

                System.out.println(move.toJson());
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
