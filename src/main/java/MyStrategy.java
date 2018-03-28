import java.util.*;

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


    private Point2D nextRandomPoint;
    private int randomPointGeneratedTick = -99999;

    public void move(Player me, World world, Game game, Move move) {
        try {
            long start = System.currentTimeMillis();
            initializeTick(me, world, game, move);
            initializeStrategy(world, game);
            log("move start");
            painter.onStartTick();


            doConstantPart();


            simpleMove();

            // potentialMove();


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

    private void simpleMove() {
        Unit me = world.mines.get(0);

        Optional<Unit> min = world.enemies.stream().filter(unit -> me.mass / unit.mass > 1.2).min(Comparator.comparingDouble(o -> o.getSquaredDistanceTo(me)));
        if (min.isPresent()) {
            Unit enemy = min.get();
            log("Moving to enemy! ratio is " + Utils.format(me.mass / enemy.mass) + " " + enemy);
            move.goTo(enemy);
            return;
        }

        List<Unit> targets = new ArrayList<>(world.food);
        targets.addAll(world.ejections);
        if (targets.isEmpty()) {
            if (world.tickIndex - randomPointGeneratedTick > 30) {
                nextRandomPoint = getMapRandomPoint();
                randomPointGeneratedTick = world.tickIndex;
            }
            move.goTo(nextRandomPoint);
            return;
        }

        Unit max = Collections.min(targets, Comparator.comparingDouble(o -> o.getSquaredDistanceTo(me)));
        move.goTo(max);

    }

    private Point2D getMapRandomPoint() {
        return new Point2D(random.nextDouble() * game.GAME_WIDTH, random.nextDouble() * game.GAME_HEIGHT / 2);
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
        Utils.log((world == null ? "0" : world.tickIndex) + ": " + s);
    }
}
