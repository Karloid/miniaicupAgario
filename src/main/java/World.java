import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Этот класс описывает игровой мир. Содержит также описания всех игроков, игровых объектов (<<юнитов>>) и сооружений.
 */
@SuppressWarnings("ForLoopWithMissingComponent")
public class World {
    private static final double FOOD_RADIUS = 2.5f;
    public int tickIndex;

    public List<Unit> food = new ArrayList<>(0);
    public List<Unit> ejections = new ArrayList<>(0);
    public List<Unit> mines = new ArrayList<>(0);
    public List<Unit> viruses = new ArrayList<>(0);
    public List<Unit> enemies = new ArrayList<>(0); //TODO groups
    public List<Unit> enemiesGuessed = new ArrayList<>(0);
    public List<Unit> foodGuessed = new ArrayList<>(0);

    public List<Unit> mainTrace = new ArrayList<>(0);

    public World(JSONObject jsonObject) {
        // Utils.log("World: " + jsonObject.toString());


        JSONArray minesObjects = jsonObject.getJSONArray("Mine");
        for (int i = 0; i < minesObjects.length(); i++) {
            JSONObject obj = minesObjects.getJSONObject(i);
            Unit unit = Unit.parseMine(obj);
            unit.world = this;
            switch (unit.type) {
                case PLAYER:
                    this.mines.add(unit);
                    break;
                default:
                    Utils.log("strange mine object " + obj.toString());
            }
        }

        JSONArray objects = jsonObject.getJSONArray("Objects");
        for (int i = 0; i < objects.length(); i++) {
            JSONObject obj = objects.getJSONObject(i);
            Unit unit = Unit.parse(obj);
            unit.world = this;
            switch (unit.type) {
                case FOOD:
                    food.add(unit);
                    break;
                case PLAYER:
                    enemies.add(unit);
                    break;
                case EJECTION:
                    ejections.add(unit);
                    break;
                case VIRUS:
                    viruses.add(unit);
                    break;
            }
        }
    }


    public int getTickIndex() {
        return tickIndex;
    }

    public void processPrev(World prevWorld) {
        if (prevWorld == null) {
            return;
        }


        for (Unit guessedEnemy : prevWorld.enemiesGuessed) {
            if (!enemies.contains(guessedEnemy) && guessedEnemy.guessAge(tickIndex) < 130) {
                simulateTick(guessedEnemy);
            }
        }


        for (Unit oldEnemy : prevWorld.enemies) {
            int i = enemies.indexOf(oldEnemy);
            if (i == -1) {
                if (!isApproximateVisible(oldEnemy)) {
                    oldEnemy.addedToGuessedAt = tickIndex;
                    oldEnemy.isGuessed = true;
                    enemiesGuessed.add(oldEnemy);
                }
            } else {
                processDiff(enemies.get(i), oldEnemy);
            }
        }

        for (Unit guessedFood : prevWorld.foodGuessed) {
            if (!Utils.containsByPosition(food, guessedFood) && guessedFood.guessAge(tickIndex) < 0) {
                if (!isApproximateVisibleFood(guessedFood)) {
                    foodGuessed.add(guessedFood);
                }
            }
        }


        for (Unit oldFood : prevWorld.food) {
            if (!Utils.containsByPosition(food, oldFood)) {
                if (!isApproximateVisibleFood(oldFood)) {
                    oldFood.addedToGuessedAt = tickIndex;
                    oldFood.isGuessed = true;
                    foodGuessed.add(oldFood);
                }
            } else {
                //processDiff(enemies.get(i), oldEnemy);
            }
        }


        mainTrace = prevWorld.mainTrace;

        if (tickIndex % 20 == 0) {
            Unit mainUnit = prevWorld.getMainUnit();

            if (mainUnit != null) {
                Unit t = new Unit(mainUnit);
                t.type = UnitType.TRACE;
                t.visibleFood = food.size();
                mainTrace.add(t);
                if (mainTrace.size() > 55) {
                    mainTrace.remove(0);
                }
            }
        }
    }

    private void processDiff(Unit newEnemy, Unit oldEnemy) {
        Point2D speed = newEnemy.getPos().sub(oldEnemy.getPos());
        newEnemy.setSpeedVector(speed);
    }

    private void simulateTick(Unit enemy) {
        double currentSpeed = enemy.getSpeedVector().length();
        double maxSpeed = Main.game.SPEED_FACTOR / Math.sqrt(enemy.mass);
        Optional<Unit> target = getBestTarget(enemy); //TODO best target for enemy
        if (target.isPresent()) {
            Unit targetReal = target.get();
            Point2D newDir = targetReal.getPos().sub(enemy.getPos());
            double newLength = newDir.length();
            double ratio = maxSpeed / newLength;
            newDir = newDir.mul(ratio);
            enemy.setSpeedVector(newDir);
        }

        enemy.x += enemy.speedX;
        enemy.y += enemy.speedY;

        double r = enemy.radius;
        {   //bounds
            if (enemy.x < 0 + r) {
                enemy.x = r;
            }

            if (enemy.y < 0 + r) {
                enemy.y = r;
            }

            if (enemy.x > Main.game.GAME_WIDTH - r) {
                enemy.x = Main.game.GAME_WIDTH - r;
            }

            if (enemy.y > Main.game.GAME_HEIGHT - r) {
                enemy.y = Main.game.GAME_HEIGHT - r;
            }
        }

        enemy.onSimulateTick();

        if (!isApproximateVisible(enemy)) {
            enemiesGuessed.add(enemy);
        }
    }

    private Optional<Unit> getBestTarget(Unit enemy) {
        return mines.stream().filter(unit -> enemy.mass / unit.mass > 1.17).min(Comparator.comparingDouble(value -> value.getDistanceTo(enemy)));
    }

    private boolean isApproximateVisible(Unit enemy) {
        for (int i = 0, minesSize = mines.size(); i < minesSize; i++) {
            Unit mine = mines.get(i);
            if (mine.getVisionDistance() - 10 > mine.getDistanceTo(enemy) - enemy.radius) {
                return true;
            }
        }
        return false;
    }

    private boolean isApproximateVisibleFood(Unit food) {
        for (int i = 0, minesSize = mines.size(); i < minesSize; i++) {
            Unit mine = mines.get(i);
            if (mine.getVisionDistance() - 10 > mine.getDistanceTo(food) - FOOD_RADIUS) {
                return true;
            }
        }
        return false;
    }

    public List<Unit> getAllEnemies() {
        ArrayList<Unit> units = new ArrayList<>(enemiesGuessed);
        units.addAll(enemies);
        return units;
    }

    public Unit getMainUnit() {
        if (mines.isEmpty()) {
            return null;
        }
        return Collections.max(mines, Comparator.comparingDouble(value -> value.mass));
    }

    public List<Unit> getAllFood() {
        ArrayList<Unit> units = new ArrayList<>(foodGuessed);
        units.addAll(food);
        return units;
    }
}
