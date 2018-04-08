import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Этот класс описывает игровой мир. Содержит также описания всех игроков, игровых объектов (<<юнитов>>) и сооружений.
 */
@SuppressWarnings("ForLoopWithMissingComponent")
public class World {
    public int tickIndex;

    public List<Unit> food = new ArrayList<>(0);
    public List<Unit> ejections = new ArrayList<>(0);
    public List<Unit> mines = new ArrayList<>(0);
    public List<Unit> viruses = new ArrayList<>(0);
    public List<Unit> enemies = new ArrayList<>(0); //TODO groups
    public List<Unit> enemiesGuessed = new ArrayList<>(0);

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
                    enemiesGuessed.add(oldEnemy);
                }
            } else {
                processDiff(enemies.get(i), oldEnemy);
            }
        }
    }

    private void processDiff(Unit newEnemy, Unit oldEnemy) {
        Point2D speed = newEnemy.getPos().sub(oldEnemy.getPos());
        newEnemy.setSpeedVector(speed);
    }

    private void simulateTick(Unit enemy) {
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

        if (!isApproximateVisible(enemy)) {
            enemiesGuessed.add(enemy);
        }
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

    public List<Unit> getAllEnemies() {
        ArrayList<Unit> units = new ArrayList<>(enemiesGuessed);
        units.addAll(enemies);
        return units;
    }
}
