import java.util.*;

public class MyStrategy {
    public static MyStrategyPainter activePainter;
    public final PotentialCalcer potentialCalcer;
    private long elapsed;
    ;
    public Player me;
    public World world;
    private Game game;
    public Move move;

    private static int constantId;


    public static final int PLAIN_SMOOTH = constantId++;
    public static final int SMOTHY_SMOOTH = constantId++;

    UnitManager um = new UnitManager(this);
    private MyStrategyPainter painter = new EmptyPaintner();
    public Random random;


    private Point2D nextRandomPoint;
    private int randomPointGeneratedTick = -99999;
    private Unit lastFoodTarget;
    
    private World prevWorld;

    public MyStrategy() {
        if (activePainter != null) {
            painter = activePainter;
            painter.setMYS(this);
        } else {
            painter = new EmptyPaintner();
        }
        activePainter = null;
        potentialCalcer = new PotentialCalcer(this);
    }

    public void move(Player me, World world, Game game, Move move) {
        try {
            long start = System.currentTimeMillis();
            initializeTick(me, world, game, move);
            initializeStrategy(world, game);
            log("move start");
             //TODO remember enemies positions


            if (world.mines.isEmpty()) {
                move.goTo(1, 1);
                System.exit(1);
            } else {
                doConstantPart();

                potentialCalcer.move();

               // fireAtenemy();
               // fireAtenemyPredict();
            }

            painter.onStartTick();

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
            move.goTo(0, 0);
            e.printStackTrace(); // is bad
            if (Main.isLocalRun) {
                throw new RuntimeException(e);
            }
        }

    }

    private boolean fireAtenemy() {
        Unit me = Collections.max(world.mines, Comparator.comparingDouble(value -> value.mass));
        Optional<Unit> closestEnemy = world.enemies.stream().filter(unit -> me.mass / unit.mass > 1.2).min(Comparator.comparingDouble(o -> o.getSquaredDistanceTo(me)));
        if (closestEnemy.isPresent()) {
            Unit enemy = closestEnemy.get();
            log("Moving to enemy! ratio is " + Utils.format(me.mass / enemy.mass) + " " + enemy);

            if ((me.mass * 0.5) / enemy.mass > 1.2) {
                double angleToEnemy = me.getAngleTo(enemy);
                double speedAngle = me.getSpeedAngle();
                if (Math.abs(angleToEnemy - speedAngle) < 0.15 && me.getDistanceTo(enemy) < me.getRadius() * 3) {
                    log("Also split! angles is enemy" + Utils.format(angleToEnemy) + " speed " + Utils.format(speedAngle));
                    move.setSplit(true);
                }
            }

            return true;
        }
        return false;
    }

    private void simpleMove() {
        @SuppressWarnings("ConstantConditions") Unit me = world.mines.stream().max(Comparator.comparing(unit -> unit.mass)).get();

        Optional<Unit> closestBigEnemy = world.enemies.stream().filter(unit -> me.mass < unit.mass).min(Comparator.comparingDouble(o -> o.getSquaredDistanceTo(me)));

        if (closestBigEnemy.isPresent()) {
            Unit enemy = closestBigEnemy.get();
            Point2D myPos = me.getPos();
            Point2D enemyPos = enemy.getPos();
            move.goTo(myPos.sub(enemyPos.sub(myPos)));
            log("! Moving from enemy! ratio is " + Utils.format(me.mass / enemy.mass) + " " + enemy);
            return;
        }

        Optional<Unit> min = world.enemies.stream().filter(unit -> me.mass / unit.mass > 1.2).min(Comparator.comparingDouble(o -> o.getSquaredDistanceTo(me)));
        if (min.isPresent()) {
            Unit enemy = min.get();
            log("Moving to enemy! ratio is " + Utils.format(me.mass / enemy.mass) + " " + enemy);
            move.goTo(enemy);

            if ((me.mass * 0.5) / enemy.mass > 1.2) {
                double angleToEnemy = me.getAngleTo(enemy);
                double speedAngle = me.getSpeedAngle();
                if (Math.abs(angleToEnemy - speedAngle) < 0.15) {
                    log("Also split! angles is enemy" + Utils.format(angleToEnemy) + " speed " + Utils.format(speedAngle));
                    move.setSplit(true);
                }
            }

            return;
        }

        //going to food

        int multiplyTargetAt = 10;

        List<Unit> targets = new ArrayList<>(world.food);
        targets.addAll(world.ejections);

        targets.removeIf(unit -> unit.getDistanceToClosestCorner(game) < me.radius
                || (unit.getDistanceTo(me) < me.radius && !unit.equalsPos(lastFoodTarget))
                || (unit.getDistanceTo(me) < me.radius + 10 && unit.type == UnitType.EJECTION && !unit.equalsPos(lastFoodTarget)));

        if (lastFoodTarget != null) {
            for (Unit target : targets) {
                if (lastFoodTarget.equalsPos(target)) {
                    move.goTo(relativeMultiplyPoint(lastFoodTarget.getPos(), multiplyTargetAt, me.getPos()));
                    return;
                }
            }
        }

        if (me.mass > 600) {
           // move.setSplit(true);
        }

        // move.setEject(true);


        if (!targets.isEmpty()) {
            lastFoodTarget = Collections.min(targets, Comparator.comparingDouble(o -> o.getSquaredDistanceTo(me)));
            move.goTo(relativeMultiplyPoint(lastFoodTarget.getPos(), multiplyTargetAt, me.getPos()));
            return;
        }


        if (world.tickIndex - randomPointGeneratedTick > 30) {
            nextRandomPoint = getMapRandomPoint();
            randomPointGeneratedTick = world.tickIndex;
        }
        move.goTo(relativeMultiplyPoint(nextRandomPoint, multiplyTargetAt, me.getPos()));
        return;


    }

    public Point2D relativeMultiplyPoint(Point2D target, int multiplyTargetAt, Point2D base) {
        if (multiplyTargetAt == 1) {
            return target;
        }
        return target.sub(base).mul(multiplyTargetAt).add(base);
    }

    private Point2D getMapRandomPoint() {
        return new Point2D(random.nextDouble() * game.GAME_WIDTH, random.nextDouble() * game.GAME_HEIGHT / 2);
    }

    private void doConstantPart() {
        //TODO something
    }

    private void initializeTick(Player me, World world, Game game, Move move) {
        prevWorld = this.world;
        this.me = me;
        //this.opponent = world.getOpponentPlayer();
        this.world = world;
        world.processPrev(prevWorld);

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
