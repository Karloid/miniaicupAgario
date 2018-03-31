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
        for (Unit mine : myStrategy.world.mines) {
            libGdxObjs.add(new LibGdxObj(mine, true));
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
