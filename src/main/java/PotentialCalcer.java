import java.util.*;

public class PotentialCalcer {
    private final MyStrategy m;
    private int potentialMapCalcAt;
    public static int cellSize = 6;
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

        int currentFoodCount = m.world.food.size();

        if (currentFoodCount != lastFoodCount
                || m.world.getTickIndex() % 15 == 0
                || !enemiesToScare.isEmpty()
                || !enemiesToEat.isEmpty()
                || mainUnit.getPos().toPotential().getDistanceTo(lastPotentialMap.mainUnitPosPotential) > lastPotentialMap.calcDistancePotential / 2) {
            lastPotentialMap = calcMap();
            potentialMapCalcAt = m.world.getTickIndex();
        }
        lastFoodCount = currentFoodCount;


        if (enemiesToScare.isEmpty() && enemiesToEat.isEmpty() && mainUnit.mass > 100) {
            m.move.setSplit(true);
        }

        Point2D averagePoint = mainUnit.getPos();

        averagePoint = averagePoint.toPotential();

        //  int myX = averagePoint.getIntX();
        //  int myY = averagePoint.getIntY();

        int myX = lastPotentialMap.mainUnitPosPotential.getIntX();
        int myY = lastPotentialMap.mainUnitPosPotential.getIntY();

        Point2D bestChoice = null;
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

        boolean correctMove = bestChoice != null && !bestChoice.equals(new Point2D(myX, myY));


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
        //TODO calc angles
        //TODO remember enemies

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


        if (enemiesToEat.isEmpty() && enemiesToScare.isEmpty()) { //food guessing/calcing
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

            addCumulToArray(plainArray, food, range, 2.5f, (int) (Math.max(mainUnit.radius, cellSize) / cellSize),
                    mainUnitPosPotential, calcDistancePotential);
        }


        addCumulToArray(plainArray, enemiesToEat, range, enemiesToScare.isEmpty() ? 10.5f : 1, (int) (Math.max(mainUnit.radius * 0.5, cellSize) / cellSize),
                mainUnitPosPotential, calcDistancePotential);


        addAnglePositive(plainArray, mainUnitPosPotential, calcDistancePotential);

        subFromArray(plainArray, enemiesToScare, visionDistance * 2 / cellSize, 50.4f, -1, mainUnitPosPotential, calcDistancePotential);

        subCorners(plainArray, mainUnitPosPotential, calcDistancePotential);

        { //add negative to corners
          /*  int maxDistanceSquare = 3 * 3;

            int distanceToFac = 6;

            int maxIndex = plainArray.cellsWidth - 1;
            Map<Point2D, Integer> allFacCounts;


            allFacCounts = new HashMap<>();
      *//*      for (Map<Point2D, Integer> counts : getFacilitiesCount().get(opponent.getId()).values()) {
                allFacCounts.putAll(counts);
            }

            for (Map<Point2D, Integer> counts : getFacilitiesCount().get(-1L).values()) {
                allFacCounts.putAll(counts);
            }*//*

            if (cornersPushers == null) {
                cornersPushers = new HashMap<>();

                cornersPushers.put(new Point2D(0, 0), 1);
                cornersPushers.put(new Point2D(0, maxIndex), 1);
                cornersPushers.put(new Point2D(maxIndex, 0), 1);
                cornersPushers.put(new Point2D(maxIndex, maxIndex), 1);
            }
            HashMap<Point2D, Integer> cornerPushersFiltered = new HashMap<>(cornersPushers);
            cornerPushersFiltered.keySet().removeIf(corner -> {
           *//*     for (Map.Entry<Point2D, Integer> facPoint : food) {
                    if (facPoint.getKey().squareDistance(corner) < maxDistanceSquare) {
                        return true;
                    }
                }*//*
                return false;
            });


            cornerPushersFiltered.keySet().removeIf(corner -> {
                int distanceThreshold = 13 * 13;

                return false;
            });


            subFromArray(plainArray, cornerPushersFiltered.entrySet(), (Main.game.GAME_WIDTH * 0.35) / cellSize, 4, -1, mainUnitPosPotential, calcDistancePotential);

            if (sidesPushers == null) {
                sidesPushers = new HashMap<>();
                int[] coordinates = {0, maxIndex};
                for (int i = 0; i < plainArray.cellsWidth; i++) {
                    sidesPushers.put(new Point2D(i, 0), 1);
                    sidesPushers.put(new Point2D(i, maxIndex), 1);
                    sidesPushers.put(new Point2D(0, i), 1);
                    sidesPushers.put(new Point2D(maxIndex, i), 1);

                }
            }
            HashMap<Point2D, Integer> sidesPushersFiltered = new HashMap<>(sidesPushers);

            Set<Map.Entry<Point2D, Integer>> finalFood = food;
            sidesPushersFiltered.keySet().removeIf(side -> {
                for (Map.Entry<Point2D, Integer> facPoint : finalFood) {
                    if (facPoint.getKey().squareDistance(side) < maxDistanceSquare) {
                        return true;
                    }
                }
                return false;
            });*/


            //subFromArray(plainArray, sidesPushersFiltered.entrySet(), (mainUnit.radius * 2) / cellSize, 1.1f, -1, mainUnitPosPotential, calcDistancePotential);


            //strict {
            int strictgap = (int) (mainUnit.radius / cellSize) + 1;
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

                        plainArray.set(x, y, plainArray.get(x, y) - 40 - extra);

                    }
                }
            }
        }


        return potentialMap;
    }

    private void subCorners(PlainArray plainArray, Point2D calcPoint, double calculateRadius) {
        double squareCalcRadius = calculateRadius * calculateRadius;

        int radius = (Main.game.GAME_WIDTH / 2) / cellSize - 2;
        Point2D center = new Point2D(Main.game.GAME_WIDTH / 2, Main.game.GAME_HEIGHT / 2).toPotential();
        double diagonal = center.getDistanceTo(0, 0);
        double diagonalMinusRadius = diagonal - radius;

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (calcPoint.squareDistance(x, y) > squareCalcRadius) {
                    continue;
                }

                Point2D point = new Point2D(x, y);
                double distanceFromCenter = point.getDistanceTo(center);
                if (distanceFromCenter < radius) {
                    continue;
                }
                double distanceFromRadius = distanceFromCenter - radius;

                plainArray.set(x, y, plainArray.get(x, y) - 160 * (distanceFromRadius / diagonalMinusRadius));
            }
        }
    }

    private void addAnglePositive(PlainArray plainArray, Point2D calcPoint, double calculateRadius) {
        double squareCalcRadius = calculateRadius * calculateRadius;
        //double visionDistance = mainUnit.getVisionDistance(); //TODO optimize max points to calculate

        double speedAngle = mainUnit.getSpeedAngle();

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (calcPoint.squareDistance(x, y) > squareCalcRadius) {
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
                plainArray.set(x, y, plainArray.get(x, y) + addition);
            }
        }
    }

    private Map<UnitType, Map<Point2D, Integer>> getUnitsCount(boolean enemy) {
        if (enemyUnitsCount == null) {
            enemyUnitsCount = new HashMap<>();
            myUnitsCount = new HashMap<>();
            for (UnitType unitType : UnitType.values()) {   //TODO respect heals, respect movement
                enemyUnitsCount.put(unitType, new HashMap<>());
                myUnitsCount.put(unitType, new HashMap<>());
            }

            calcUnitCount(m.world.mines);
            calcUnitCount(m.world.food);
            calcUnitCount(m.world.ejections);
            calcUnitCount(m.world.getAllEnemies());
            calcUnitCount(m.world.viruses);
        }


        return enemy ? enemyUnitsCount : myUnitsCount;
    }

    private void calcUnitCount(List<Unit> units) {
        for (Unit unit : units) {
            Point2D key = unit.getPos().toPotential();

            Map<UnitType, Map<Point2D, Integer>> map = !unit.isMy ? enemyUnitsCount : myUnitsCount;

            boolean mustAdd = true;
            if (unit.type == UnitType.PLAYER) {//special case
                if (!unit.isMy && mainUnit.mass > unit.mass) {
                    mustAdd = false;
                    if (mainUnit.mass / unit.mass > 1.2) {
                        Map<Point2D, Integer> countMap = map.get(UnitType.ENEMIES_TO_EAT);
                        countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                    }
                } else if (!unit.isMy && mainUnit.mass <= unit.mass) {
                    mustAdd = false;
                    Map<Point2D, Integer> countMap = map.get(UnitType.ENEMIES_TO_SCARE);
                    countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                }
            }

            if (mustAdd) {
                Map<Point2D, Integer> countMap = map.get(unit.type);
                countMap.put(key, countMap.getOrDefault(key, 0) + 1);
            }
        }
    }

    private void subFromArray(PlainArray plainArray, Set<Map.Entry<Point2D, Integer>> unitsCount, double spreadRange, float factor, float minVal,
                              Point2D calcPoint, double calcRadius) {

        if (unitsCount.isEmpty()) {
            return;
        }

        double squareDelta = spreadRange * spreadRange;

        double squareCalcRadius = calcRadius * calcRadius;

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                if (calcPoint.squareDistance(x, y) > squareCalcRadius) {
                    continue;
                }

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
            }
        }
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

                if (calculationPoint.squareDistance(x, y) > squareCalcRadius) {
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
}
