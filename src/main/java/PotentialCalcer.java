import java.util.*;

public class PotentialCalcer {
    private final MyStrategy m;
    private int potentialMapCalcAt;
    public static int cellSize = 6;
    private HashMap<UnitType, List<Unit>> enemyUnits;
    private HashMap<UnitType, Map<Point2D, Integer>> enemyUnitsCount;
    private HashMap<UnitType, Map<Point2D, Integer>> myUnitsCount;
    private HashMap<Point2D, Integer> cornersPushers;
    private Map<Point2D, Integer> sidesPushers;
    public PotentialMap lastPotentialMap;
    private Unit mainUnit;
    private Set<Map.Entry<Point2D, Integer>> lastGuessFood;
    private int lastGuessFoodCellSize;
    private int lastFoodCount = -1;

    private Point2D lastBestChoice;
    private int lastBestChoiceCellSize;

    private int lastCalcMapTick;
    private boolean isShortMove;
    private int maxVisibleFoodTrace;


    public PotentialCalcer(MyStrategy m) {
        this.m = m;
    }

    public void move() {
        //TODO check guessed food
        //TODO fear pleyers more
        //TODO fear players which can split on you

        mainUnit = m.world.getMainUnit();

        cellSize = (int) (mainUnit.radius / 2);

        myUnitsCount = null;
        enemyUnitsCount = null;


        Set<Map.Entry<Point2D, Integer>> enemiesToScare = getUnitsCount(true).get(UnitType.ENEMIES_TO_SCARE).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemiesToEat = getUnitsCount(true).get(UnitType.ENEMIES_TO_EAT).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemiesNeutral = getUnitsCount(true).get(UnitType.PLAYER).entrySet();

        int currentFoodCount = getUnitsCount(true).get(UnitType.FOOD).size();

        isShortMove = !(enemiesToScare.isEmpty() && enemiesToEat.isEmpty() && currentFoodCount > 0);

        if (currentFoodCount != lastFoodCount
                || m.world.getTickIndex() % 15 == 0
                || !enemiesToScare.isEmpty()
                || !enemiesToEat.isEmpty()
                || mainUnit.getPos().toPotential().getDistanceTo(lastPotentialMap.mainUnitPosPotential) > lastPotentialMap.calcDistancePotential / 2) {
            lastPotentialMap = calcMap();
            potentialMapCalcAt = m.world.getTickIndex();
        }
        lastFoodCount = currentFoodCount;


        if (enemiesToScare.isEmpty() && enemiesToEat.isEmpty() && mainUnit.mass > 120 && enemiesNeutral.isEmpty()) {   //TODO CHECK enemiesNeutralProbably always empty
            m.move.setSplit(true);                                                                                      //TODO double check it is safe
        }

        Point2D averagePoint = mainUnit.getPos();

        averagePoint = averagePoint.toPotential();

        //  int myX = averagePoint.getIntX();
        //  int myY = averagePoint.getIntY();

        int myX = lastPotentialMap.mainUnitPosPotential.getIntX();
        int myY = lastPotentialMap.mainUnitPosPotential.getIntY();

        Point2D bestChoice = null;

        if (isShortMove) {
            int half = (int) lastPotentialMap.calcDistancePotential;

            double squareCalcRadius = lastPotentialMap.calcDistancePotential * lastPotentialMap.calcDistancePotential;

            for (int x = myX - half; x <= myX + half; x++) {
                for (int y = myY - half; y <= myY + half; y++) {
                    Point2D currentChoice = new Point2D(x, y);
                    if (lastPotentialMap.mainUnitPosPotential.squareDistance(x, y) > squareCalcRadius) {
                        continue;
                    }

                    currentChoice.setVal(lastPotentialMap.map.get(x, y));
                    // currentChoice.setVal(lastPotentialMap.map.get(x, y) - Point2D.getDistance(x, y, myX, myY) * 0.1);
                    if (bestChoice == null || bestChoice.getVal() < currentChoice.getVal()) {
                        //TODO check safety
                        bestChoice = currentChoice;
                    }
                }
            }
        } else {
            int bestX = 0;
            int bestY = 0;
            double bestVal = -99999;
            lastPotentialMap.map.min = 999_000;

            for (int x = 0; x < lastPotentialMap.map.cellsWidth; x++) {
                for (int y = 0; y < lastPotentialMap.map.cellsHeight; y++) {

          /*      if (lastPotentialMap.mainUnitPosPotential.squareDistance(x, y) > squareCalcRadius) {
                    continue;
                }*/

                    double val = lastPotentialMap.map.getUnsafe(x, y);
                    // currentChoice.setVal(lastPotentialMap.map.get(x, y) - Point2D.getDistance(x, y, myX, myY) * 0.1);
                    if (bestVal < val) {
                        //TODO check safety
                        bestX = x;
                        bestY = y;
                        bestVal = val;
                    }

                    if (val < lastPotentialMap.map.min) {
                        lastPotentialMap.map.min = val;
                    }

                }
            }
            bestChoice = new Point2D(bestX, bestY, bestVal);
        }

        lastPotentialMap.map.max = bestChoice.getVal();
        boolean correctMove = !bestChoice.equals(new Point2D(myX, myY));


        if (bestChoice != null /*&& correctMove*/) {
            if (false && lastBestChoice != null && cellSize == lastBestChoiceCellSize && !lastBestChoice.equals(bestChoice) && !lastBestChoice.equals(new Point2D(myX, myY))
                    && lastBestChoice.getVal() / lastPotentialMap.map.get(lastBestChoice.getIntX(), lastBestChoice.getIntY()) < 1.3
                    && lastBestChoice.getVal() / bestChoice.getVal() > 0.9
                    ) {
                applyMove(lastBestChoice);
                m.log(Utils.WARN + " GOING TO lastBestChoice");
                return;
            }
            lastBestChoice = bestChoice;
            lastBestChoiceCellSize = cellSize;
            applyMove(bestChoice);
        } else {
            m.log(Utils.WARN + "POTENTIAL BEST CHOICE NOT FOUND");
        }
    }

    private void applyMove(Point2D lastBestChoice) {
        Point2D targetPos = lastBestChoice.mul(cellSize).add(cellSize / 2, cellSize / 2);
        m.move.wantedTargetPos = targetPos;
        boolean normal = false;
        if (normal) {

            m.move.goTo(m.relativeMultiplyPoint(targetPos, 1, mainUnit.getPos()));
        } else {

            double speedAngle = mainUnit.getSpeedAngle();
            Point2D speedVector = mainUnit.getSpeedVector();
            Point2D targetVector = targetPos.sub(mainUnit.getPos());
            double targetAngle = targetVector.angle();

            double meanVector = targetVector.add(speedVector).div(2).angle();


            double delta = Math.abs(targetAngle - speedAngle);

            m.log("speedAngle: " + Utils.format(speedAngle) + " targetAngle: " + Utils.format(targetAngle) +
                    " speedV: " + speedVector + " targetV " + targetVector + " delta: " + Utils.format(delta));

            //delta = Math.min(delta,Math.PI / 2 )
            if (delta <= (Math.PI / 2)) {
                Point2D target = new Point2D(targetVector.length(), 0);
                if (targetAngle > speedAngle) {
                    target = target.rotate(delta + targetAngle);
                } else {
                    target = target.rotate(-delta + targetAngle);
                }

                m.move.goTo(mainUnit.getPos().add(target));
            } else {
                m.move.goTo(targetPos);
            }
        }
    }

    private PotentialMap calcMap() { //TODO improve logic at final stages

        //TODO predictions

        //TODO do not fear corners if no enemy
        //TODO do not return to places where food was eaten


        //TODO increase trace force

        m.log("calcMap, ticks without cacl: " + (m.world.tickIndex - lastCalcMapTick));
        lastCalcMapTick = m.world.tickIndex;
        PotentialMap potentialMap = new PotentialMap(cellSize);
        PlainArray plainArray = potentialMap.map;

        Set<Map.Entry<Point2D, Integer>> food = getUnitsCount(true).get(UnitType.FOOD).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemiesToScare = getUnitsCount(true).get(UnitType.ENEMIES_TO_SCARE).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemiesToEat = getUnitsCount(true).get(UnitType.ENEMIES_TO_EAT).entrySet();

        double visionDistance = mainUnit.getVisionDistance();
        Point2D mainUnitPosPotential = mainUnit.getPos().toPotential();
        double calcDistancePotential = (visionDistance * 1) / cellSize;

        potentialMap.calcDistancePotential = calcDistancePotential;
        potentialMap.mainUnitPosPotential = mainUnitPosPotential;


        double range = plainArray.cellsWidth * 1.2;


        boolean isNoEnemiesHere = enemiesToEat.isEmpty() && enemiesToScare.isEmpty();
        if (isNoEnemiesHere) { //food guessing/calcing
            double tmp = visionDistance * 1.3;
            float guessFoodSquareDist = (float) ((tmp / cellSize) * (tmp / cellSize));
            float visionSquareDist = (float) ((visionDistance / cellSize) * (visionDistance / cellSize));

            Point2D mainUnitPotentialPos = mainUnitPosPotential;

            if (cellSize != lastGuessFoodCellSize) {   //guess food in real coordinates
                lastGuessFood = null;
            }

            if (lastGuessFood != null) {
                lastGuessFood.removeIf(entry -> entry.getKey().squareDistance(mainUnitPotentialPos) < visionSquareDist);
            }

            if (food.isEmpty()) {

                if (lastGuessFood != null && !lastGuessFood.isEmpty()) {
                    food = lastGuessFood;
                } else {
                    //potential food
                    food = new HashSet<>(0);
                    lastGuessFood = food;
                    lastGuessFoodCellSize = cellSize;
                    for (int i = 0; i < 0; i++) {
                        double x = m.random.nextFloat() * plainArray.cellsWidth;
                        double y = m.random.nextFloat() * plainArray.cellsHeight;

                        if (mainUnitPotentialPos.squareDistance(x, y) > guessFoodSquareDist) {
                            food.add(new AbstractMap.SimpleEntry<>(new Point2D(x, y), 1));
                        }
                    }
                }
            }

            //TODO shadows from food
            // addCumulToArray(plainArray, food, range, 2.5f, (int) (Math.max(mainUnit.radius, cellSize) / cellSize),
            //         mainUnitPosPotential, calcDistancePotential);

            addShadowsArray(plainArray, food, range, 2.5f, (int) (Math.max(mainUnit.radius, cellSize) / cellSize),
                    mainUnitPosPotential, calcDistancePotential);
        }


        addCumulToArray(plainArray, enemiesToEat, range, enemiesToScare.isEmpty() ? 10.5f : 1, (int) (Math.max(mainUnit.radius * 0.5, cellSize) / cellSize),
                mainUnitPosPotential, calcDistancePotential);


        addAnglePositive(plainArray, mainUnitPosPotential, calcDistancePotential);

        if (isNoEnemiesHere) {
            //subEnemiesShadows(plainArray, visionDistance * 2 / cellSize,
            //        15.4f, -1, mainUnitPosPotential, calcDistancePotential, m.world.mainTrace, false);
            //  subFromArray(plainArray, getUnitsCount(false).get(UnitType.TRACE).entrySet(), visionDistance * 4 / cellSize,  //TODO increase force and radius
//          25.4f / maxVisibleFoodTrace, -1, mainUnitPosPotential, calcDistancePotential);
            subFromArray(plainArray, getUnitsCount(false).get(UnitType.TRACE).entrySet(), visionDistance * 4 / cellSize,  //TODO increase force and radius
                    25.4f, -1, mainUnitPosPotential, calcDistancePotential);
        }
        // subFromArray(plainArray, enemiesToScare, visionDistance * 2 / cellSize, 50.4f, -1, mainUnitPosPotential, calcDistancePotential);
        subFromArray(plainArray, enemiesToScare, potentialMap.map.cellsWidth * 1.5, 50.4f, -1, mainUnitPosPotential, calcDistancePotential); //TODO handle enemy angles

        //subCorners(plainArray, mainUnitPosPotential, calcDistancePotential, enemiesToScare.isEmpty() ? 0.005f : 1f);
        if (!(!enemiesToEat.isEmpty() && enemiesToScare.isEmpty())) {
            subCorners(plainArray, mainUnitPosPotential, calcDistancePotential, 1f);
        }

        subEnemiesShadows(plainArray, visionDistance * 2 / cellSize,
                15.4f, -1, mainUnitPosPotential, calcDistancePotential, enemyUnits.get(UnitType.ENEMIES_TO_SCARE), true);

        {
            //strict {
            int strictgap = (int) (mainUnit.radius / cellSize);
            if (strictgap > 0) {   //TODO never enable?    //TODO optimize

                for (int x = 0; x < plainArray.cellsWidth; x++) {
                    for (int y = 0; y < plainArray.cellsHeight; y++) {

                        if (x < strictgap || y < strictgap || x >= plainArray.cellsWidth - strictgap || y >= plainArray.cellsHeight - strictgap) {
                            double extra = 0;
                            if (x < strictgap) {
                                extra = 40 * (strictgap - x);
                            }
                            if (y < strictgap) {
                                extra = 40 * (strictgap - y);
                            }
                            if (x >= plainArray.cellsWidth - strictgap) {
                                extra = 40 * (strictgap - (plainArray.cellsWidth - x));
                            }
                            if (y >= plainArray.cellsHeight - strictgap) {
                                extra = 40 * (strictgap - (plainArray.cellsHeight - y));
                            }

                            plainArray.set(x, y, plainArray.getUnsafe(x, y) - 40 - extra);

                        }
                    }
                }
            }
        }


        return potentialMap;
    }

    private void subCorners(PlainArray plainArray, Point2D calcPoint, double calculateRadius, float ratio) {
        Point2D minePos = mainUnit.getPotentialPos();

        int mineQ = getMapQuarter(plainArray, minePos.getX(), minePos.getY());

        double squareCalcRadius = calculateRadius * calculateRadius;

        int radius = (Main.game.GAME_WIDTH / 2) / cellSize - 2;
        Point2D center = new Point2D(Main.game.GAME_WIDTH / 2, Main.game.GAME_HEIGHT / 2).toPotential();
        double diagonal = center.getDistanceTo(0, 0);
        double diagonalMinusRadius = diagonal - radius;

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (isShortMove && calcPoint.squareDistance(x, y) > squareCalcRadius) {
                    continue;
                }

                if (getMapQuarter(plainArray, x, y) != mineQ) {
                    continue;
                }

                Point2D point = new Point2D(x, y);
                double distanceFromCenter = point.getDistanceTo(center);
                if (distanceFromCenter < radius) {
                    continue;
                }
                double distanceFromRadius = distanceFromCenter - radius;

                plainArray.set(x, y, plainArray.getUnsafe(x, y) - 160 * (distanceFromRadius / diagonalMinusRadius) * ratio);
            }
        }
    }

    private int getMapQuarter(PlainArray plainArray, double x, double y) {
        int half = plainArray.cellsWidth / 2;
        if (x < half) {
            if (y < half) {
                return 0;
            } else {
                return 1;
            }
        }
        if (y < half) {
            return 2;
        } else {
            return 3;
        }
    }

    private void addAnglePositive(PlainArray plainArray, Point2D calcPoint, double calculateRadius) {
        double squareCalcRadius = calculateRadius * calculateRadius;
        //double visionDistance = mainUnit.getVisionDistance(); //TODO optimize max points to calculate

        double speedAngle = mainUnit.getSpeedAngle();

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (isShortMove && calcPoint.squareDistance(x, y) > squareCalcRadius) {
                    continue;
                }

                Point2D vectorToPoint = new Point2D(x, y).sub(calcPoint);
                double pointAngle = vectorToPoint.angle();

                double delta = pointAngle - speedAngle;
                delta = Utils.mod((delta + Math.PI), (Math.PI * 2)) - Math.PI;

                delta = Math.abs(delta);
                //    double delta = Math.abs(pointAngle - speedAngle);

                double addition = 30 * (1 - delta / Math.PI);

                double vectorLength = vectorToPoint.length();
                if (vectorLength <= 3) {
                    addition *= 0.75;
                }
                plainArray.set(x, y, plainArray.getUnsafe(x, y) + addition);
            }
        }
    }

    private Map<UnitType, Map<Point2D, Integer>> getUnitsCount(boolean enemy) {
        if (enemyUnitsCount == null) {
            enemyUnits = new HashMap<>();
            enemyUnitsCount = new HashMap<>();
            myUnitsCount = new HashMap<>();
            for (UnitType unitType : UnitType.values()) {   //TODO respect heals, respect movement
                enemyUnitsCount.put(unitType, new HashMap<>());
                myUnitsCount.put(unitType, new HashMap<>());
                enemyUnits.put(unitType, new ArrayList<>(5));
            }

            calcUnitCount(m.world.mines);
            calcUnitCount(m.world.getAllFood());
            calcUnitCount(m.world.ejections);
            calcUnitCount(m.world.getAllEnemies());
            calcUnitCount(m.world.viruses);
            if (!m.world.mainTrace.isEmpty()) {
                maxVisibleFoodTrace = Collections.max(m.world.mainTrace, Comparator.comparingInt(v -> v.visibleFood)).visibleFood;
                if (maxVisibleFoodTrace == 0) {
                    maxVisibleFoodTrace = 40;//TODO?
                }
                calcUnitCount(m.world.mainTrace);
            }
        }


        return enemy ? enemyUnitsCount : myUnitsCount;
    }

    private void calcUnitCount(List<Unit> units) {
        boolean isTraceLogic = false;
        if (!units.isEmpty()) {
            if (units.get(0).type == UnitType.TRACE) {
                isTraceLogic = true;
            }
        }

        for (Unit unit : units) {
            Point2D key = unit.getPos().toPotential();

            Map<UnitType, Map<Point2D, Integer>> map = !unit.isMy ? enemyUnitsCount : myUnitsCount;

            boolean mustAdd = true;
            if (unit.type == UnitType.PLAYER) {//special case
                if (!unit.isMy) {                               //TODO distance
                    key = unit.getPos().add(unit.getSpeedVector().mul(3)).toPotential();

                    if (!isSafeForMyUnits(unit)) {
                        enemyUnits.get(UnitType.ENEMIES_TO_SCARE).add(unit);
                        mustAdd = false;
                        Map<Point2D, Integer> countMap = map.get(UnitType.ENEMIES_TO_SCARE);
                        countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                    } else if (!unit.isGuessed && (mainUnit.mass / unit.mass > 1.2 || canBeEatenAfterFusion(unit))) {
                        enemyUnits.get(UnitType.ENEMIES_TO_EAT).add(unit);
                        mustAdd = false;
                        Map<Point2D, Integer> countMap = map.get(UnitType.ENEMIES_TO_EAT);
                        countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                    }
                }
            }

            if (mustAdd) {
                Map<Point2D, Integer> countMap = map.get(unit.type);
                if (isTraceLogic && false) {
                    countMap.put(key, countMap.getOrDefault(key, 0) + maxVisibleFoodTrace - unit.visibleFood + 1);
                } else {
                    countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                }
            }
        }
    }

 /*   private boolean canBeEatenAfterFusion(Unit unit) {
        return m.world.enemies.size() == 1 && m.world.mines.size() > 1;
    }*/


    private boolean canBeEatenAfterFusion(Unit unit) {

        boolean result = /*m.world.enemies.size() == 1 &&*/ m.world.mines.size() > 1;
        if (!result) {
            return false;
        }

        int timeToFuse = Collections.max(m.world.mines, Comparator.comparingInt(value -> value.timeToFuse)).timeToFuse;

        if (timeToFuse > 20) {
            return false;
        }
        double length = mainUnit.getSpeedVector().length();
        if (length <= 0) {
            length = 1;
        }
        double avgDistance = m.world.mines.stream().mapToDouble(mine -> mine.getDistanceTo(unit)).average().getAsDouble();
        result = timeToFuse < avgDistance / length;
        return result;
    }

    private boolean isSafeForMyUnits(Unit enemy) {
        Optional<Unit> closesteater = m.world.mines.stream().filter(unit -> unit.mass / enemy.mass > 1.2)
                .min(Comparator.comparingDouble(unit -> unit.getDistanceTo(enemy)));

        double minDistance = closesteater.map(unit -> unit.getDistanceTo(enemy) + unit.radius * 0.5).orElseGet(() -> Main.game.GAME_HEIGHT * 2d);
        long canEatMyUnits = m.world.mines.stream().filter(unit -> enemy.mass / unit.mass > 1.1 && enemy.getDistanceTo(unit) < minDistance).count();
        return canEatMyUnits == 0;
    }

    private void subFromArray(PlainArray plainArray, Set<Map.Entry<Point2D, Integer>> unitsCount, double spreadRange, float factor, float minVal,
                              Point2D calcPoint, double calcRadius) {

        if (unitsCount.isEmpty()) {
            return;
        }

        double squareSpread = spreadRange * spreadRange;

        double squareCalcRadius = calcRadius * calcRadius;

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (isShortMove && calcPoint.squareDistance(x, y) > squareCalcRadius) {
                    continue;
                }

                for (Map.Entry<Point2D, Integer> entry : unitsCount) {
                    float val = entry.getValue();
                    if (val < minVal) {
                        continue;
                    }


                    val = val * factor;
                    double squareDist = entry.getKey().squareDistance(x, y);
                    //
                    if (squareDist > squareSpread) {
                        continue;
                    }

                    double value = (1 - squareDist / squareSpread) * val;
                    if (value > 1) {
                        plainArray.set(x, y, plainArray.getUnsafe(x, y) - value);
                    }
                }
            }
        }
    }

    private void subEnemies(PlainArray plainArray, double spreadRange, float factor, float minVal,
                            Point2D calcPoint, double calcRadius) {


        double squareDelta = spreadRange * spreadRange;

        double squareCalcRadius = calcRadius * calcRadius;

        List<Unit> enemies = enemyUnits.get(UnitType.ENEMIES_TO_SCARE);
        if (enemies.isEmpty()) {
            return;
        }
        int yy = 10;

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                for (int i = 0, getSize = enemies.size(); i < getSize; i++) {
                    Unit enemy = enemies.get(i);
                    Point2D enemyPotential = enemy.getPotentialPos(); //TODO optimize

                    double enemyDanger = ((enemy.radius * 1.2f) / cellSize) + 1;
                    for (Unit mine : m.world.mines) {
                        if (!enemy.canEat(mine)) {
                            continue;
                        }
                        if (enemyPotential.getDistanceTo(x, y) <= enemyDanger) {
                            plainArray.set(x, y, plainArray.getUnsafe(x, y) - 100);
                        }
                    }
                }

/*
                for (Map.Entry<Point2D, Integer> entry : unitsCount) {
                    float val = entry.getValue();
                    if (val < minVal) {
                        continue;
                    }


                    val = val * factor;
                    double squareDist = entry.getKey().squareDistance(x, y);

                    if (squareDist > squareDelta) {
                        continue;
                    }

                    double value = (1 - squareDist / squareDelta) * val;
                    if (value > 1) {
                        plainArray.set(x, y, plainArray.get(x, y) - value);
                    }
                }
*/
            }
        }
    }

    private void subEnemiesShadows(PlainArray plainArray, double spreadRange, float factor, float minVal,
                                   Point2D calcPoint, double calcRadius, List<Unit> units, boolean checkCanEat) {


        double squareDelta = spreadRange * spreadRange;

        double squareCalcRadius = calcRadius * calcRadius;

        if (units.isEmpty()) {
            return;
        }


        for (Unit mine : m.world.mines) {

            Point2D minePos = mine.getPotentialPos();
            for (int i = 0, getSize = units.size(); i < getSize; i++) {
                Unit enemy = units.get(i);
                if (checkCanEat && !enemy.canEat(mine)) {
                    continue;
                }

                double enemyDanger = (enemy.radius * 1.2f) / cellSize;

                Point2D vectorToEnemy = enemy.getPotentialPos().sub(minePos);

                Point2D leftEnemy = enemy.getPotentialPos().add(vectorToEnemy.leftPerpendicular().length(enemyDanger));
                Point2D rightEnemy = enemy.getPotentialPos().add(vectorToEnemy.rightPerpendicular().length(enemyDanger));

                double leftAngle = leftEnemy.sub(minePos).angle();
                double rightAngle = rightEnemy.sub(minePos).angle();
                // subByPoint(plainArray, leftEnemy, -100);
                double minAngle = Math.min(leftAngle, rightAngle);
                double maxAngle = Math.max(leftAngle, rightAngle);

                for (int x = 0; x < plainArray.cellsWidth; x++) {
                    for (int y = 0; y < plainArray.cellsHeight; y++) {
                        double angleToPoint = Point2D.angle(x - minePos.getX(), y - minePos.getY());
                        if (itsBetween(angleToPoint, minAngle, maxAngle)) {
                            plainArray.set(x, y, plainArray.get(x, y) - 100);
                        } else {
                            double min = getAngleDelta(minAngle, maxAngle, angleToPoint);
                            if (min < Math.PI / 3) {
                                plainArray.set(x, y, plainArray.get(x, y) - 100 * (1 - min / (Math.PI / 3)));
                            }
                        }
                    }
                }

            }
        }

        int yy = 10;
    }

    private double getAngleDelta(double minAngle, double maxAngle, double angleToPoint) {
        double result = Math.min(Math.abs(angleToPoint - minAngle), Math.abs(angleToPoint - maxAngle));
        if (result < Math.PI / 3) {
            return result;
        }

        result = Math.min(result, Math.abs(-Math.PI - minAngle) + Math.PI - angleToPoint);
        result = Math.min(result, Math.abs(-Math.PI - angleToPoint) + Math.PI - maxAngle);

        return result;
    }

    private void subByPoint(PlainArray plainArray, Point2D point, int val) {
        plainArray.set(point.getIntX(), point.getIntY(), plainArray.get(point.getIntX(), point.getIntY()) + val);
    }

    private void addToArray(PlainArray plainArray, Set<Map.Entry<Point2D, Integer>> counts, double spreadRange, float factor) {
        double squareDelta = spreadRange * spreadRange; //1.4 - hypot
        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                for (Map.Entry<Point2D, Integer> entry : counts) {
                    float count;
                    count = Math.max(100 - entry.getValue(), 1) * factor;

                    double value = (1 - entry.getKey().squareDistance(x, y) / squareDelta) * count;
                    plainArray.set(x, y, Math.max(plainArray.get(x, y), value));
                }
            }
        }
    }

    private void addCumulToArray(PlainArray plainArray, Set<Map.Entry<Point2D, Integer>> counts, double spreadRange,
                                 float factor, int cumulRangle, Point2D calculationPoint, double calculateRadius) {
        if (counts.isEmpty()) {
            return;
        }

        spreadRange -= cumulRangle;
        double squareDelta = spreadRange * spreadRange; //1.4 - hypot
        double squareCalcRadius = calculateRadius * calculateRadius;
        //double visionDistance = mainUnit.getVisionDistance(); //TODO optimize max points to calculate


        double squareCumulRange = cumulRangle * cumulRangle;
        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (isShortMove && calculationPoint.squareDistance(x, y) > squareCalcRadius) {
                    continue;
                }

                for (Map.Entry<Point2D, Integer> entry : counts) {
                    Point2D point = entry.getKey();


                    float count;
                    count = Math.max(99 + entry.getValue(), 1) * factor;
                    double squareDist = point.squareDistance(x, y);

                    if (squareDelta < squareDist) {
                        continue;
                    }

                    double value = (1 - squareDist / squareDelta) * count;

                    double currentValue = plainArray.get(x, y);
                    if (squareDist <= squareCumulRange/* && currentValue > count*/) {
                        //plainArray.add(x, y, (currentValue > 0 ? currentValue  * 0.1 : 0) + value);
                        //   plainArray.set(x, y, Math.max(currentValue, value));
                        value *= 1.2f;
                        if (entry.getKey().getIntX() == x && entry.getKey().getIntY() == y) {
                            value *= 1.05f;
                        }
                        plainArray.set(x, y, currentValue > value ? currentValue + 0.1f * value : value);
                    } else {
                        plainArray.set(x, y, Math.max(currentValue, value));
                    }
                }
            }
        }
    }

    private void addShadowsArray2(PlainArray plainArray, Set<Map.Entry<Point2D, Integer>> counts, double spreadRange,
                                  float factor, int cumulRangle, Point2D calculationPoint, double calculateRadius) {
        if (counts.isEmpty()) {
            return;
        }


        for (Unit mine : m.world.mines) {

            Point2D minePos = mine.getPotentialPos();

            double countRadius = Math.max((mine.radius * 1) / cellSize, 1);
            for (Map.Entry<Point2D, Integer> count : counts) {

                Point2D countPos = count.getKey();

                Point2D vectorToCount = countPos.sub(minePos);

                Point2D leftEnemy = countPos.add(vectorToCount.leftPerpendicular().length(countRadius));
                Point2D rightEnemy = countPos.add(vectorToCount.rightPerpendicular().length(countRadius));

                double leftAngle = leftEnemy.sub(minePos).angle();
                double rightAngle = rightEnemy.sub(minePos).angle();
                double minAngle = Math.min(leftAngle, rightAngle);
                double maxAngle = Math.max(leftAngle, rightAngle);

                for (int x = 0; x < plainArray.cellsWidth; x++) {
                    for (int y = 0; y < plainArray.cellsHeight; y++) {
                        double angleToPoint = Point2D.angle(x - minePos.getX(), y - minePos.getY());
                        if (itsBetween(angleToPoint, minAngle, maxAngle)) {
                            plainArray.set(x, y, plainArray.get(x, y) + 100 * count.getValue());
                        }
                    }
                }

            }
        }

        int yy = 10;
    }

    private void addShadowsArray(PlainArray plainArray, Set<Map.Entry<Point2D, Integer>> counts, double spreadRange,
                                 float factor, int cumulRangle, Point2D calculationPoint, double calculateRadius) {
        if (counts.isEmpty()) {
            return;
        }

        List<AddShadowData> data = new ArrayList<>();


        for (Unit mine : m.world.mines) {

            Point2D minePos = mine.getPotentialPos();

            double countRadius = Math.max((mine.radius * 1) / cellSize, 1);
            for (Map.Entry<Point2D, Integer> count : counts) {

                Point2D countPos = count.getKey();

                Point2D vectorToCount = countPos.sub(minePos);

                Point2D leftEnemy = countPos.add(vectorToCount.leftPerpendicular().length(countRadius));
                Point2D rightEnemy = countPos.add(vectorToCount.rightPerpendicular().length(countRadius));

                double leftAngle = leftEnemy.sub(minePos).angle();
                double rightAngle = rightEnemy.sub(minePos).angle();
                data.add(new AddShadowData(minePos, Math.min(leftAngle, rightAngle), Math.max(leftAngle, rightAngle), count.getValue(), count.getKey(), count.getKey().squareDistance(minePos)));
            }
        }


        addShadowsArrayInner(plainArray, factor, data, true);
    }

    private void addShadowsArrayInner(PlainArray plainArray, float factor, List<AddShadowData> data, boolean onlyEven) {
        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (onlyEven && !(x % 2 == 0 && y % 2 == 0)) {
                    continue;
                }
                Set<Point2D> eatenPoints = null;
                for (AddShadowData d : data) {
                    if ((eatenPoints == null || !eatenPoints.contains(d.pos)) && d.minePos.squareDistance(x, y) >= d.squareGapDistance) {

                        double angleToPoint = Point2D.angle(x - d.minePos.getX(), y - d.minePos.getY());
                        if (itsBetween(angleToPoint, d.minAngle, d.maxAngle)) {
                            if (eatenPoints == null) {
                                eatenPoints = new HashSet<>(1);
                            }
                            eatenPoints.add(d.pos);

                            plainArray.set(x, y, plainArray.getUnsafe(x, y) + 100 * d.value * factor);
                        }
                    }
                }
            }
        }
    }

    private boolean itsBetween(double angleToPoint, double minAngle, double maxAngle) {
        if (minAngle < 0 && maxAngle > 0) {
            if (Math.abs(-Math.PI - minAngle) + Math.PI - maxAngle < maxAngle - minAngle) {
                return angleToPoint > maxAngle || angleToPoint < minAngle;
            }
        }
        return angleToPoint >= minAngle && angleToPoint <= maxAngle;
    }
}
