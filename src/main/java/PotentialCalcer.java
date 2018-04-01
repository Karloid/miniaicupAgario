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


    public PotentialCalcer(MyStrategy m) {
        this.m = m;
    }

    public void move() {
        //TODO check guessed food
        //TODO fear pleyers more
        //TODO fear players which can split on you

        mainUnit = Collections.max(m.world.mines, Comparator.comparingDouble(value -> value.mass));

        cellSize = (int) (mainUnit.radius / 2);

        myUnitsCount = null;
        enemyUnitsCount = null;


        Set<Map.Entry<Point2D, Integer>> enemies = getUnitsCount(true).get(UnitType.PLAYER).entrySet();

        int currentFoodCount = m.world.food.size();

        if (currentFoodCount != lastFoodCount || m.world.getTickIndex() % 15 == 0 || (!enemies.isEmpty() && m.world.getTickIndex() % 4 == 0)) {
            lastPotentialMap = calcMap();
            potentialMapCalcAt = m.world.getTickIndex();
        }
        lastFoodCount = currentFoodCount;


        Point2D averagePoint = mainUnit.getPos();

        averagePoint = averagePoint.toPotential();

        int myX = averagePoint.getIntX();
        int myY = averagePoint.getIntY();

        Point2D bestChoice = null;
        int half = 20;


        for (int x = myX - half; x <= myX + half; x++) {
            for (int y = myY - half; y <= myY + half; y++) {
                Point2D currentChoice = new Point2D(x, y);
                if (Math.ceil(currentChoice.getDistanceTo(myX, myY)) > half + 0.01) {
                    continue;
                }

         /*       if (y == myY && x == myX && myGroup.noMoveCount > 10) {
                    continue;
                }*/

                //currentChoice.setVal(lastPotentialMap.map.get(x, y));
                currentChoice.setVal(lastPotentialMap.map.get(x, y) - Point2D.getDistance(x, y, myX, myY) * 0.1);
                if (bestChoice == null || bestChoice.getVal() < currentChoice.getVal()) {
                    //TODO check safety
                    bestChoice = currentChoice;
                }
            }
        }

        boolean correctMove = bestChoice != null && !bestChoice.equals(new Point2D(myX, myY));


        if (bestChoice != null /*&& correctMove*/) {
            if (lastBestChoice != null && cellSize == lastBestChoiceCellSize && !lastBestChoice.equals(bestChoice) && !lastBestChoice.equals(new Point2D(myX, myY))
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
        Point2D realPoint = lastBestChoice.mul(cellSize).add(cellSize / 2, cellSize / 2);
        //m.move.goTo(realPoint);
        m.move.goTo(m.relativeMultiplyPoint(realPoint, 1, mainUnit.getPos()));
    }

    private PotentialMap calcMap() { //TODO improve logic at final stages
        PotentialMap potentialMap = new PotentialMap(cellSize);
        PlainArray plainArray = potentialMap.map;

        Set<Map.Entry<Point2D, Integer>> food = getUnitsCount(true).get(UnitType.FOOD).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemiesToScare = getUnitsCount(true).get(UnitType.PLAYER).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemiesToEat = getUnitsCount(true).get(UnitType.ENEMIES_TO_EAT).entrySet();

        double visionDistance = mainUnit.getVisionDistance();
        Point2D mainUnitPosPotential = mainUnit.getPos().toPotential();
        double calcDistancePotential = (visionDistance * 1) / cellSize;

        { //food guessing
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
                    for (int i = 0; i < 50; i++) {
                        double x = m.random.nextFloat() * plainArray.cellsWidth;
                        double y = m.random.nextFloat() * plainArray.cellsHeight;

                        if (mainUnitPotentialPos.squareDistance(x, y) > guessFoodSquareDist) {
                            food.add(new AbstractMap.SimpleEntry<>(new Point2D(x, y), 1));
                        }
                    }
                }
            }
        }


        int yy = 10;


        double range = plainArray.cellsWidth * 1.2;

        addCumulToArray(plainArray, food, range, 2.5f, (int) (Math.max(mainUnit.radius, cellSize) / cellSize),
                mainUnitPosPotential, calcDistancePotential);

        addCumulToArray(plainArray, enemiesToEat, range, 10.5f, (int) (Math.max(mainUnit.radius * 0.5, cellSize) / cellSize),
                mainUnitPosPotential, calcDistancePotential);


        subFromArray(plainArray, enemiesToScare, visionDistance * 2 / cellSize, 15.4f, -1, mainUnitPosPotential, calcDistancePotential);


        { //add negative to corners
            int maxDistanceSquare = 3 * 3;

            int distanceToFac = 6;

            int maxIndex = plainArray.cellsWidth - 1;
            Map<Point2D, Integer> allFacCounts;


            allFacCounts = new HashMap<>();
      /*      for (Map<Point2D, Integer> counts : getFacilitiesCount().get(opponent.getId()).values()) {
                allFacCounts.putAll(counts);
            }

            for (Map<Point2D, Integer> counts : getFacilitiesCount().get(-1L).values()) {
                allFacCounts.putAll(counts);
            }*/

            if (cornersPushers == null) {
                cornersPushers = new HashMap<>();

                cornersPushers.put(new Point2D(0, 0), 1);
                cornersPushers.put(new Point2D(0, maxIndex), 1);
                cornersPushers.put(new Point2D(maxIndex, 0), 1);
                cornersPushers.put(new Point2D(maxIndex, maxIndex), 1);
            }
            HashMap<Point2D, Integer> cornerPushersFiltered = new HashMap<>(cornersPushers);
            cornerPushersFiltered.keySet().removeIf(corner -> {
           /*     for (Map.Entry<Point2D, Integer> facPoint : food) {
                    if (facPoint.getKey().squareDistance(corner) < maxDistanceSquare) {
                        return true;
                    }
                }*/
                return false;
            });


            cornerPushersFiltered.keySet().removeIf(corner -> {
                int distanceThreshold = 13 * 13;

                return false;
            });


            subFromArray(plainArray, cornerPushersFiltered.entrySet(), 250 / cellSize, 2, -1, mainUnitPosPotential, calcDistancePotential);

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
            });


            subFromArray(plainArray, sidesPushersFiltered.entrySet(), (mainUnit.radius * 2) / cellSize, 1.1f, -1, mainUnitPosPotential, calcDistancePotential);


            //strict {
            int strictgap = (int) (mainUnit.radius / cellSize) + 1;
            for (int x = 0; x < plainArray.cellsWidth; x++) {
                for (int y = 0; y < plainArray.cellsHeight; y++) {

                    if (x < strictgap || y < strictgap || x >= plainArray.cellsWidth - strictgap || y >= plainArray.cellsHeight - strictgap) {
                        plainArray.set(x, y, plainArray.get(x, y) - 40);
                    }
                }
            }
        }


        return potentialMap;
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
            calcUnitCount(m.world.enemies);
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
        double squareDelta = spreadRange * spreadRange;

        double squareCalcRadius = calcRadius * calcRadius;

        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                for (Map.Entry<Point2D, Integer> entry : unitsCount) {
                    float val = entry.getValue();
                    if (val < minVal) {
                        continue;
                    }

                    if (calcPoint.squareDistance(x, y) > squareCalcRadius) {
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
        spreadRange -= cumulRangle;
        double squareDelta = spreadRange * spreadRange; //1.4 - hypot
        double squareCalcRadius = calculateRadius * calculateRadius;
        //double visionDistance = mainUnit.getVisionDistance(); //TODO optimize max points to calculate


        double squareCumulRange = cumulRangle * cumulRangle;
        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                for (Map.Entry<Point2D, Integer> entry : counts) {
                    Point2D point = entry.getKey();


                    if (calculationPoint.squareDistance(x, y) > squareCalcRadius) {
                        continue;
                    }

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
