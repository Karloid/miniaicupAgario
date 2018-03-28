import java.util.Random;

public class MyStrategy {
    private long elapsed;
    ;
    public Player me;
    public World world;
    private Game game;
    private Move move;

    private static int constantId;


    public static final int PLAIN_SMOOTH = constantId++;
    public static final int SMOTHY_SMOOTH = constantId++;

    UnitManager um = new UnitManager(this);
    private MyStrategyPainter painter = new EmptyPaintner();
    private Random random;

    public void move(Player me, World world, Game game, Move move) {
        try {
            long start = System.currentTimeMillis();
            initializeTick(me, world, game, move);
            initializeStrategy(world, game);
            log("move start");
            painter.onStartTick();


            doConstantPart();
            //TODO do something with nuclear attacks

            potentialMove();


            long timeTaken = System.currentTimeMillis() - start;
            elapsed += timeTaken;
            if (timeTaken > 400) {
                log("too much work " + timeTaken);
            }
            if (world.getTickIndex() % 1000 == 0) {
                log("time taken total: " + elapsed);
            }

            painter.onEndTick();
        } catch (Throwable e) {
            e.printStackTrace(); // is bad
            if (Main.isLocalRun) {
                throw new RuntimeException(e);
            }
        }

    }

    private void potentialMove() {
        move.x = 10;
        move.y = 10;
    }

    private void doConstantPart() {
        //TODO something
    }

    private void initializeTick(Player me, World world, Game game, Move move) {
        this.me = me;
        //this.opponent = world.getOpponentPlayer();
        this.world = world;
        this.game = game;
        this.move = move;

        um.initializeTick();
    }

    private void initializeStrategy(World world, Game game) {
        if (random == null) {
            random = new Random(/*game.getRandomSeed()*/);

            // worldWidth = (int) world.getWidth(); //TODO
            // worldHeight = (int) world.getHeight(); //TODO
            // centerPoint = new Point2D(worldWidth / 2, worldHeight / 2); //TODO

            painter.onInitializeStrategy();
        }
    }

    public void log(String s) {
        if (Main.isLocalRun) {
            Utils.appendToLogFile((world == null ? "0" : world.tickIndex) + ": " + s);
        }
    }
}
