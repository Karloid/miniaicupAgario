import model.World;

import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        MyStrategy myStrategy = new MyStrategy();
        int i = 0;

        Scanner scanner = new Scanner(System.in);
        Game game = Game.from(scanner.next());

        for (;;) {
            String nextString = scanner.next();
            Move move = new Move();
            World world = World.parse(nextString);
            world.tickIndex = i;
            myStrategy.move(new Player(), world, game, move);

            System.out.println(move.toJson());
            i++;
        }
    }

}
