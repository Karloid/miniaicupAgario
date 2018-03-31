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


    public PotentialCalcer(MyStrategy m) {
        this.m = m;
    }

    public void move() {

        if (m.world.getTickIndex() % 5 == 0) {
            lastPotentialMap = calcMap();
            potentialMapCalcAt = m.world.getTickIndex();
        }

        Unit mainUnit = Collections.max(m.world.mines, Comparator.comparingDouble(value -> value.mass));
        Point2D averagePoint = mainUnit.getPos();

        averagePoint = averagePoint.toPotential();

        int myX = averagePoint.getIntX();
        int myY = averagePoint.getIntY();

        Point2D bestChoice = null;
        int half = 3;


        for (int x = myX - half; x <= myX + half; x++) {
            for (int y = myY - half; y <= myY + half; y++) {
                Point2D currentChoice = new Point2D(x, y);
                if (Math.ceil(currentChoice.getDistanceTo(myX, myY)) > half + 0.01) {
                    continue;
                }

         /*       if (y == myY && x == myX && myGroup.noMoveCount > 10) {
                    continue;
                }*/

                currentChoice.setVal(lastPotentialMap.map.get(x, y));
                if (bestChoice == null || bestChoice.getVal() < currentChoice.getVal()) {
                    //TODO check safety
                    bestChoice = currentChoice;
                }
            }
        }

        boolean noMove = bestChoice != null && !bestChoice.equals(new Point2D(myX, myY));


        if (bestChoice != null && noMove) {
            m.move.goTo(bestChoice.mul(cellSize).add(cellSize / 2, cellSize / 2));
        } else {
            m.log(Utils.WARN + "POTENTIAL BEST CHOICE NOT FOUND");
        }
    }

    private PotentialMap calcMap() { //TODO improve logic at final stages
        PotentialMap potentialMap = new PotentialMap(cellSize);
        PlainArray plainArray = potentialMap.map;

        myUnitsCount = null;
        enemyUnitsCount = null;


        Set<Map.Entry<Point2D, Integer>> food = getUnitsCount(true).get(UnitType.FOOD).entrySet();

        int yy = 10;
        /*
        boolean ifvShouldHeal = shouldHeal(IFV);
        boolean tankShouldHeal = shouldHeal(TANK);
        boolean heliShouldHeal = shouldHeal(HELICOPTER);
        boolean fighterShouldHeal = shouldHeal(FIGHTER);

        //boolean disableFear = isFacilityMode && world.getTickIndex() >= CAN_DISABLE_FEAR_SINCE_TICK && um.getUnitCount(Ownership.ALLY) > CAN_DISABLE_FEAR_SINCE_COUNT;
        boolean disableFear = false;


        int minValForContra = group.count > 12 && isFacilityMode ? 3 : -1;  //TODO disable when no facilities


        Set<Map.Entry<Point2D, Integer>> food = getUnitsCount(true).get(TANK).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemyArrvs = getUnitsCount(true).get(ARRV).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemyIfv = getUnitsCount(true).get(IFV).entrySet();

        Set<Map.Entry<Point2D, Integer>> enemyHelics = getUnitsCount(true).get(HELICOPTER).entrySet();
        Set<Map.Entry<Point2D, Integer>> enemyFighters = getUnitsCount(true).get(FIGHTER).entrySet();

        allEnInvisible = food.isEmpty() && enemyArrvs.isEmpty() && enemyIfv.isEmpty() && enemyHelics.isEmpty() && enemyFighters.isEmpty();


        if (group.vehicleType == FIGHTER) {

            Map<Point2D, Integer> enFighterAndHelics = new HashMap<>(getUnitsCount(true).get(FIGHTER));
            {

                Map<Point2D, Integer> helics = getUnitsCount(true).get(HELICOPTER);

                for (Map.Entry<Point2D, Integer> entry : helics.entrySet()) {
                    enFighterAndHelics.put(entry.getKey(), enFighterAndHelics.getOrDefault(entry.getValue(), 0) + entry.getValue()); //TODO tune
                }


                Set<Map.Entry<Point2D, Integer>> figAndHelicsSet = enFighterAndHelics.entrySet();


                double range = plainArray.cellsWidth * 1.2;

                addToArray(plainArray, figAndHelicsSet, range, 1.f);


                if (food.isEmpty() && enemyArrvs.isEmpty() && enemyIfv.isEmpty() && figAndHelicsSet.isEmpty()) {

                    boolean goToFacilities = true;

                    Set<Map.Entry<Point2D, Integer>> myArrvs = getUnitsCount(false).get(ARRV).entrySet();
                    if (myArrvs.isEmpty()) {
                        addToArrayNotOurFacilities(plainArray, range, 1, group);
                    } else {
                        VehicleGroupInfo maxArrvG = myGroups.stream().filter(g -> g.vehicleType == ARRV).max(Comparator.comparingInt(g -> g.vehicles.size())).orElse(null);

                        HashSet<Map.Entry<Point2D, Integer>> entries = new HashSet<>();
                        if (maxArrvG != null) {
                            entries.add(new AbstractMap.SimpleEntry<>(maxArrvG.getCellAveragePoint(), 1));
                        } else {
                            entries.addAll(myArrvs);
                            for (Map.Entry<Point2D, Integer> en : entries) {
                                en.setValue(1);
                            }
                        }
                        addToArray(plainArray, entries, range, .1f);

                    }

                } else {
                    addToArray(plainArray, enemyIfv, range, .1f);
                    addToArray(plainArray, food, range, .1f);
                    addToArray(plainArray, enemyArrvs, range, .1f);

                    if (enemyFighters.isEmpty() && enemyHelics.isEmpty()) {
                        subFromArray(plainArray, food, (game.getFighterVisionRange() - 15) / cellSize, 1.4f, -1);
                        subFromArray(plainArray, enemyArrvs, (game.getFighterVisionRange() - 15) / cellSize, 1.4f, -1);
                    } else {
                        subFromArray(plainArray, food, (game.getIfvAerialAttackRange() + 10) / cellSize, 1.4f, -1);
                        subFromArray(plainArray, enemyArrvs, (game.getIfvAerialAttackRange() + 10) / cellSize, 1.4f, -1);
                    }
                }
            }


            {
                Set<Map.Entry<Point2D, Integer>> ifvCount = getUnitsCount(true).get(IFV).entrySet();

                double range = (game.getIfvAerialAttackRange() * 2.4) / cellSize;

                int factor = 6;

                if (!ifvCount.isEmpty()) {
                    subFromArray(plainArray, ifvCount, range, factor, -1);
                }
            }

            {
                HashMap<Point2D, Integer> myHelicsMap = new HashMap<>(getUnitsCount(false).get(HELICOPTER));
                for (Map.Entry<Point2D, Integer> entry : myHelicsMap.entrySet()) {

                    Point2D center = entry.getKey();

                    int half = 3;
                    boolean intruders = false;
                    for (int x = center.getIntX() - half; x <= center.getIntX() + half; x++) {
                        for (int y = center.getIntY() - half; y <= center.getIntY() + half; y++) {
                            Integer fighterOrHelicsCount = enFighterAndHelics.get(new Point2D(x, y));
                            if (fighterOrHelicsCount != null && fighterOrHelicsCount > 0) {
                                intruders = true;
                                //entry.setValue(0)
                                break;
                            }
                        }
                    }

                    if (intruders) {
                        myHelicsMap.clear();
                        break;
                    }
                }

                Set<Map.Entry<Point2D, Integer>> myHelic = myHelicsMap.entrySet();

                double range = (GROUP_SIZE * 1.2) / cellSize;

                int factor = 6;

                if (!myHelic.isEmpty()) {
                    subFromArray(plainArray, myHelic, range, factor, -1);
                }

                Set<Map.Entry<Point2D, Integer>> otherFighters = getUnitsCount(false).get(FIGHTER).entrySet();
                if (!otherFighters.isEmpty()) {
                    subFromArray(plainArray, otherFighters, range * .8f, factor, -1);
                }
            }
        }

        if (group.vehicleType == HELICOPTER) {

            {

                double range = plainArray.cellsWidth * 1.2;

                //addToArray(plainArray, tanksAndArrvSet, range, 1.f);

                addToArray(plainArray, food, range, 1.f);
                addToArray(plainArray, enemyArrvs, range, 1.f);


                addToArray(plainArray, enemyIfv, range, .1f);
                addToArray(plainArray, enemyHelics, range, .3f);

                if (food.isEmpty() && enemyArrvs.isEmpty() && enemyIfv.isEmpty() && enemyHelics.isEmpty()) {

                    Set<Map.Entry<Point2D, Integer>> myIfvs = getUnitsCount(false).get(IFV).entrySet();
                    if (myIfvs.isEmpty()) {
                        addToArrayNotOurFacilities(plainArray, range, 1, group);
                    } else {
                        VehicleGroupInfo maxIfvG = myGroups.stream().filter(g -> g.vehicleType == IFV).max(Comparator.comparingInt(g -> g.vehicles.size())).orElse(null);

                        HashSet<Map.Entry<Point2D, Integer>> entries = new HashSet<>();
                        if (maxIfvG != null) {
                            entries.add(new AbstractMap.SimpleEntry<>(maxIfvG.getCellAveragePoint(), 1));
                        } else {
                            entries.addAll(myIfvs);
                            for (Map.Entry<Point2D, Integer> en : entries) {
                                en.setValue(1);
                            }
                        }
                        addToArray(plainArray, entries, range, .1f);
                    }
                }

                if (!disableFear) {
                    subFromArray(plainArray, enemyIfv, (game.getHelicopterAerialAttackRange() * 2.4) / cellSize, 3.2f, minValForContra);
                    subFromArray(plainArray, enemyHelics, (game.getIfvAerialAttackRange() + 10) / cellSize, 1.4f, minValForContra);
                }

                if (!disableFear && HELICS_WAIT_FOR_FIGHTES) {
                    VehicleGroupInfo myFighters = findGroup(myGroups, FIGHTER);
                    if (myFighters != null && myFighters.count > 30) {
                        double smallestDistance = Double.MAX_VALUE;
                        Point2D ap = new Point2D(myFighters.getAveragePoint().getX() / cellSize, myFighters.getAveragePoint().getY() / cellSize);
                        for (Map.Entry<Point2D, Integer> enemyFighter : enemyFighters) {
                            double distanceTo = enemyFighter.getKey().getDistanceTo(ap);
                            if (distanceTo < smallestDistance) {
                                smallestDistance = distanceTo;
                            }
                        }
                        if (smallestDistance < world.getWidth()) {
                            subFromArray(plainArray, enemyFighters, smallestDistance * 1.1, 1.4f, -1); //TODO NOT TUNED
                        }
                    }
                }

                HashMap<Point2D, Integer> myHelicsMap = new HashMap<>(getUnitsCount(false).get(HELICOPTER));
                //for (Map.Entry<Point2D, Integer> entry : myHelicsMap.entrySet()) {
                Map<Point2D, Integer> enFighters = getUnitsCount(true).get(FIGHTER);

                Point2D center = group.getCellAveragePoint();

                int half = 9;
                boolean intruders = false;
                int count = 0;
                for (int x = center.getIntX() - half; x <= center.getIntX() + half; x++) {
                    for (int y = center.getIntY() - half; y <= center.getIntY() + half; y++) {
                        Integer fighterOrHelicsCount = enFighters.get(new Point2D(x, y));

                        if (fighterOrHelicsCount != null) {
                            count += fighterOrHelicsCount;
                            if (fighterOrHelicsCount > 2) {
                                intruders = true;
                                //entry.setValue(0)
                            }
                        }
                    }

                    if (intruders && count > 0.05 * group.count && opponent.getRemainingNuclearStrikeCooldownTicks() > 150) {
                        Set<Map.Entry<Point2D, Integer>> myIfvs = new HashMap<>(getUnitsCount(false).get(IFV)).entrySet();
                        for (Map.Entry<Point2D, Integer> myIfv : myIfvs) {
                            myIfv.setValue(Math.max(15 - myIfv.getValue(), 1));
                        }
                        addToArray(plainArray, myIfvs, plainArray.cellsWidth * 1.2, 6);
                        break;
                    }
                }
            }


            {
                Set<Map.Entry<Point2D, Integer>> fighters = getUnitsCount(true).get(FIGHTER).entrySet();

                double range = (game.getFighterAerialAttackRange() * 2.8) / cellSize;

                int factor = 6;

                if (!disableFear && !fighters.isEmpty()) {
                    subFromArray(plainArray, fighters, range, factor, minValForContra);
                }

            }

            {
                HashMap<Point2D, Integer> myFightersMap = new HashMap<>(getUnitsCount(false).get(FIGHTER));

                Set<Map.Entry<Point2D, Integer>> myFighters = myFightersMap.entrySet();

                double range = (GROUP_SIZE) / cellSize;

                int factor = 2;

                if (!myFighters.isEmpty()) {
                    subFromArray(plainArray, myFighters, range, factor, -1);
                }

                Set<Map.Entry<Point2D, Integer>> otherHelicopters = getUnitsCount(false).get(HELICOPTER).entrySet();
                if (!otherHelicopters.isEmpty()) {
                    subFromArray(plainArray, otherHelicopters, range, factor, minValForContra);
                }
            }
        }

        if (group.vehicleType == IFV) {
            {

                double range = plainArray.cellsWidth * 1.2;

                //addToArray(plainArray, tanksAndArrvSet, range, 1.f);
                addToArray(plainArray, getUnitsCount(true).get(HELICOPTER).entrySet(), range, .9f);
                addToArray(plainArray, getUnitsCount(true).get(FIGHTER).entrySet(), range, 1.f);
                addToArray(plainArray, getUnitsCount(true).get(ARRV).entrySet(), range, 0.82f);


                addToArrayNotOurFacilities(plainArray, range, 0.88f, group);


                addToArray(plainArray, enemyIfv, range, .4f);
                addToArray(plainArray, food, range, .1f);

                if (!disableFear) {
                    subFromArray(plainArray, enemyIfv, (game.getIfvGroundAttackRange() + 20) / cellSize, .3f, minValForContra);
                    subFromArray(plainArray, food, (game.getTankGroundAttackRange() * 4.5) / cellSize, 3.2f, minValForContra);
                }

            }

            {
                HashMap<Point2D, Integer> myGroundUnits = new HashMap<>(getUnitsCount(false).get(TANK));

                if (!ifvShouldHeal) {
                    myGroundUnits.putAll(getUnitsCount(false).get(ARRV));
                }

                myGroundUnits.putAll(getUnitsCount(false).get(IFV));
                //TODO fix problems with overlaping?


                Set<Map.Entry<Point2D, Integer>> myGroundUnitsSet = myGroundUnits.entrySet();

                double range = (GROUP_SIZE * 1.3) / cellSize;

                int factor = 2;

                if (!myGroundUnitsSet.isEmpty()) {
                    subFromArray(plainArray, myGroundUnitsSet, range, factor, -1);
                }
            }
        }

        if (group.vehicleType == TANK) {
            {

                double range = plainArray.cellsWidth * 1.2;

                //addToArray(plainArray, tanksAndArrvSet, range, 1.f);
                //targets
                addToArray(plainArray, getUnitsCount(true).get(IFV).entrySet(), range, 1.f);
                addToArray(plainArray, getUnitsCount(true).get(TANK).entrySet(), range, .9f);
                addToArray(plainArray, getUnitsCount(true).get(ARRV).entrySet(), range, 0.8f);


                //if someone closer then my then pass that facility
                //removeFacilityIfSomeoneCloser(group);

                addToArrayNotOurFacilities(plainArray, range, .9f, group);


                //secondary targets
                Set<Map.Entry<Point2D, Integer>> helics = getUnitsCount(true).get(HELICOPTER).entrySet();
                Set<Map.Entry<Point2D, Integer>> fighters = getUnitsCount(true).get(FIGHTER).entrySet();

                addToArray(plainArray, helics, range, .1f);
                addToArray(plainArray, getUnitsCount(true).get(FIGHTER).entrySet(), range, .4f);

                //keep away from secondary targets
                if (!disableFear) {
                    subFromArray(plainArray, helics, (game.getHelicopterGroundAttackRange() * 3) / cellSize, 4.5f, minValForContra);
                }
                subFromArray(plainArray, fighters, (GROUP_SIZE) / cellSize, .4f, -1);

            }

            {   // my units as obstacle
                HashMap<Point2D, Integer> myGroundUnits = new HashMap<>(getUnitsCount(false).get(IFV));

                if (!tankShouldHeal) {
                    myGroundUnits.putAll(getUnitsCount(false).get(ARRV));
                }

                myGroundUnits.putAll(getUnitsCount(false).get(TANK));
                //TODO fix problems with overlaping1?

                Set<Map.Entry<Point2D, Integer>> myGroundUnits2 = myGroundUnits.entrySet();

                double range = (GROUP_SIZE * 1.05) / cellSize;

                int factor = 2;

                if (!myGroundUnits2.isEmpty()) {
                    subFromArray(plainArray, myGroundUnits2, range, factor, -1);
                }
            }
        }

        if (group.vehicleType == ARRV) {

            {

                double range = plainArray.cellsWidth * 1.2;

                //addToArray(plainArray, tanksAndArrvSet, range, 1.f);
                //targets
                if (ifvShouldHeal) {
                    addToArray(plainArray, getUnitsCount(false).get(IFV).entrySet(), range, 1.f);
                }
                if (tankShouldHeal) {
                    addToArray(plainArray, getUnitsCount(false).get(TANK).entrySet(), range, 1f);
                }
              *//*  addToArray(plainArray, getUnitsCount(false).get(FIGHTER).entrySet(), range, 0.8f);
                addToArray(plainArray, getUnitsCount(false).get(HELICOPTER).entrySet(), range, 0.8f);*//*


                //secondary targets
                Set<Map.Entry<Point2D, Integer>> helics = getUnitsCount(false).get(HELICOPTER).entrySet();
                Set<Map.Entry<Point2D, Integer>> fighters = getUnitsCount(false).get(FIGHTER).entrySet();
                Set<Map.Entry<Point2D, Integer>> tanks = getUnitsCount(false).get(TANK).entrySet();
                Set<Map.Entry<Point2D, Integer>> ifvs = getUnitsCount(false).get(IFV).entrySet();

                addToArray(plainArray, helics, range, .2f);
                addToArray(plainArray, fighters, range, .2f);
                addToArray(plainArray, tanks, range, .3f);
                addToArray(plainArray, ifvs, range, .3f);

                addToArrayNotOurFacilities(plainArray, range, .95f, group);

                //TODO chase enemies

                //keep away from secondary targets
                Set<Map.Entry<Point2D, Integer>> enHel = getUnitsCount(true).get(HELICOPTER).entrySet();
                Set<Map.Entry<Point2D, Integer>> enTanks = getUnitsCount(true).get(TANK).entrySet();
                Set<Map.Entry<Point2D, Integer>> enIfv = getUnitsCount(true).get(IFV).entrySet();
                subFromArray(plainArray, enHel, (game.getHelicopterGroundAttackRange() * 3) / cellSize, 1.5f, -1);
                subFromArray(plainArray, enTanks, (game.getTankGroundAttackRange() * 3) / cellSize, 1.5f, -1);
                subFromArray(plainArray, enIfv, (game.getIfvGroundAttackRange() * 3) / cellSize, 1.5f, -1);
            }

            {   // my units as obstacle
                HashMap<Point2D, Integer> grounUnits = new HashMap<>();

                if (!tankShouldHeal) {
                    grounUnits.putAll(getUnitsCount(false).get(TANK));
                }
                if (!ifvShouldHeal) {
                    grounUnits.putAll(getUnitsCount(false).get(IFV));
                }

                grounUnits.putAll(getUnitsCount(true).get(TANK));
                grounUnits.putAll(getUnitsCount(true).get(IFV));
                grounUnits.putAll(getUnitsCount(true).get(ARRV));

                grounUnits.putAll(getUnitsCount(false).get(ARRV));
                //TODO fix problems with overlaping1?

                Set<Map.Entry<Point2D, Integer>> myGroundUnits2 = grounUnits.entrySet();

                double range = (GROUP_SIZE * 1.3) / cellSize;

                int factor = 2;

                if (!myGroundUnits2.isEmpty()) {
                    subFromArray(plainArray, myGroundUnits2, range, factor, -1);
                }
            }
        }

        { //add negative to corners
            int maxDistanceSquare = 8 * 8;

            int distanceToFac = 6;

            int maxIndex = plainArray.cellsWidth - 1;
            Map<Point2D, Integer> allFacCounts;


            allFacCounts = new HashMap<>();
            for (Map<Point2D, Integer> counts : getFacilitiesCount().get(opponent.getId()).values()) {
                allFacCounts.putAll(counts);
            }

            for (Map<Point2D, Integer> counts : getFacilitiesCount().get(-1L).values()) {
                allFacCounts.putAll(counts);
            }

            if (cornersPushers == null) {
                cornersPushers = new HashMap<>();

                cornersPushers.put(new Point2D(0, 0), 1);
                cornersPushers.put(new Point2D(0, maxIndex), 1);
                cornersPushers.put(new Point2D(maxIndex, 0), 1);
                cornersPushers.put(new Point2D(maxIndex, maxIndex), 1);


            }
            HashMap<Point2D, Integer> cornerPushersFiltered = new HashMap<>(cornersPushers);
            cornerPushersFiltered.keySet().removeIf(corner -> {
                for (Point2D facPoint : allFacCounts.keySet()) {
                    if (facPoint.squareDistance(corner) < maxDistanceSquare) {
                        return true;
                    }
                }
                return false;
            });


            cornerPushersFiltered.keySet().removeIf(corner -> {
                int distanceThreshold = 13 * 13;
                for (Point2D facPoint : getUnitsCount(true).get(ARRV).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }

                for (Point2D facPoint : getUnitsCount(true).get(HELICOPTER).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }

                for (Point2D facPoint : getUnitsCount(true).get(FIGHTER).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }

                for (Point2D facPoint : getUnitsCount(true).get(TANK).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }
                for (Point2D unit : getUnitsCount(true).get(IFV).keySet()) {
                    if (unit.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }
                return false;
            });


            subFromArray(plainArray, cornerPushersFiltered.entrySet(), 3 * 4, 3, -1);

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

            sidesPushersFiltered.keySet().removeIf(side -> {
                for (Point2D facPoint : allFacCounts.keySet()) {
                    if (facPoint.squareDistance(side) < maxDistanceSquare) {
                        return true;
                    }
                }
                return false;
            });


            subFromArray(plainArray, sidesPushersFiltered.entrySet(), 3 * 3, 1, -1);


            //strict {

            for (int x = 0; x < plainArray.cellsWidth; x++) {
                for (int y = 0; y < plainArray.cellsHeight; y++) {

                    if (x == 0 || y == 0 || x == plainArray.cellsWidth - 1 || y == plainArray.cellsHeight - 1) {
                        plainArray.set(x, y, plainArray.get(x, y) - 30);
                    }
                }
            }
        }*/

        { //add negative to corners
            int maxDistanceSquare = 8 * 8;

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
                for (Point2D facPoint : allFacCounts.keySet()) {
                    if (facPoint.squareDistance(corner) < maxDistanceSquare) {
                        return true;
                    }
                }
                return false;
            });


            cornerPushersFiltered.keySet().removeIf(corner -> {
                int distanceThreshold = 13 * 13;
              /*  for (Point2D facPoint : getUnitsCount(true).get(ARRV).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }

                for (Point2D facPoint : getUnitsCount(true).get(HELICOPTER).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }

                for (Point2D facPoint : getUnitsCount(true).get(FIGHTER).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }

                for (Point2D facPoint : getUnitsCount(true).get(TANK).keySet()) {
                    if (facPoint.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }
                for (Point2D unit : getUnitsCount(true).get(IFV).keySet()) {
                    if (unit.squareDistance(corner) < distanceThreshold) {
                        return true;
                    }
                }*/
                return false;
            });


            subFromArray(plainArray, cornerPushersFiltered.entrySet(), 3 * 4, 3, -1);

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

            sidesPushersFiltered.keySet().removeIf(side -> {
                for (Point2D facPoint : allFacCounts.keySet()) {
                    if (facPoint.squareDistance(side) < maxDistanceSquare) {
                        return true;
                    }
                }
                return false;
            });


            subFromArray(plainArray, sidesPushersFiltered.entrySet(), 3 * 3, 1, -1);


            //strict {

            for (int x = 0; x < plainArray.cellsWidth; x++) {
                for (int y = 0; y < plainArray.cellsHeight; y++) {

                    if (x == 0 || y == 0 || x == plainArray.cellsWidth - 1 || y == plainArray.cellsHeight - 1) {
                        plainArray.set(x, y, plainArray.get(x, y) - 30);
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
            Map<Point2D, Integer> countMap = map.get(unit.type);
            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
        }
    }

    private void subFromArray(PlainArray plainArray, Set<Map.Entry<Point2D, Integer>> unitsCount, double range, float factor, float minVal) {
        double squareDelta = range * range;
        for (int x = 0; x < plainArray.cellsWidth; x++) {
            for (int y = 0; y < plainArray.cellsHeight; y++) {

                for (Map.Entry<Point2D, Integer> entry : unitsCount) {
                    float val = entry.getValue();
                    if (val < minVal) {
                        continue;
                    }
                    val = val * factor;
                    double value = (1 - entry.getKey().squareDistance(x, y) / squareDelta) * val;
                    if (value > 1) {
                        plainArray.set(x, y, plainArray.get(x, y) - value);
                    }
                }
            }
        }
    }
}
