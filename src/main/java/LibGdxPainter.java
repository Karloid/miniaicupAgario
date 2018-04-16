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

        data.mainUnit = myStrategy.world.getMainUnit();
        
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

        for (Unit enemy : myStrategy.world.enemiesGuessed) {
            data.objs.add(new LibGdxObj(enemy));
        }

        for (Unit food : myStrategy.world.foodGuessed) {
            data.objs.add(new LibGdxObj(food));
        }

        for (Unit point : myStrategy.world.mainTrace) {
            data.objs.add(new LibGdxObj(point));
        }

    }

    @Override
    public void setMYS(MyStrategy myStrategy) {
        this.myStrategy = myStrategy;
    }

    @Override
    public void onEndTick() {
        data.move = myStrategy.move;
        data.potentialMap = myStrategy.potentialCalcer.lastPotentialMap;

        shower.setObjects(data);
    }

    @Override
    public void onInitializeStrategy() {

    }

    @Override
    public void drawMove() {

    }
}
