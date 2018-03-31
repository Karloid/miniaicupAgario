public class LibGdxPainter implements MyStrategyPainter {
    private final LibGdxShower shower;
    private MyStrategy myStrategy;
    private LibGdxDataToPaint data;

    public LibGdxPainter(LibGdxShower shower) {
        this.shower = shower;
    }

    @Override
    public void onStartTick() {
        data = new LibGdxDataToPaint();
        
        for (Unit enemy : myStrategy.world.food) {
            data.objs.add(new LibGdxObj(enemy));
        }

        for (Unit mine : myStrategy.world.mines) {
            data.objs.add(new LibGdxObj(mine));
        }

        for (Unit enemy : myStrategy.world.enemies) {
            data.objs.add(new LibGdxObj(enemy));
        }

        for (Unit enemy : myStrategy.world.viruses) {
            data.objs.add(new LibGdxObj(enemy));
        }

        for (Unit enemy : myStrategy.world.ejections) {
            data.objs.add(new LibGdxObj(enemy));
        }

    }

    @Override
    public void setMYS(MyStrategy myStrategy) {
        this.myStrategy = myStrategy;
    }

    @Override
    public void onEndTick() {
        data.move = myStrategy.move;
        shower.setObjects(data);
    }

    @Override
    public void onInitializeStrategy() {

    }

    @Override
    public void drawMove() {

    }
}
