import java.util.ArrayList;

public class LibGdxPainter implements MyStrategyPainter {
    private final LibGdxShower shower;
    private MyStrategy myStrategy;
    private ArrayList<LibGdxObj> libGdxObjs;

    public LibGdxPainter(LibGdxShower shower) {
        this.shower = shower;
    }

    @Override
    public void onStartTick() {
        libGdxObjs = new ArrayList<>();
        
        for (Unit enemy : myStrategy.world.food) {
            libGdxObjs.add(new LibGdxObj(enemy, false));
        }

        for (Unit mine : myStrategy.world.mines) {
            libGdxObjs.add(new LibGdxObj(mine, true));
        }

        for (Unit enemy : myStrategy.world.enemies) {
            libGdxObjs.add(new LibGdxObj(enemy, false));
        }

        for (Unit enemy : myStrategy.world.viruses) {
            libGdxObjs.add(new LibGdxObj(enemy, false));
        }

        for (Unit enemy : myStrategy.world.ejections) {
            libGdxObjs.add(new LibGdxObj(enemy, false));
        }

    }

    @Override
    public void setMYS(MyStrategy myStrategy) {
        this.myStrategy = myStrategy;
    }

    @Override
    public void onEndTick() {
        shower.setObjects(libGdxObjs);
    }

    @Override
    public void onInitializeStrategy() {

    }

    @Override
    public void drawMove() {

    }
}
