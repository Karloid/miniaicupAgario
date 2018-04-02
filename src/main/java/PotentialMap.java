public class PotentialMap {
    public PlainArray map;
    public int cellSize;
    public double calcDistancePotential;
    public Point2D mainUnitPosPotential;

    public PotentialMap(int cellSize) {
        this.cellSize = cellSize;
        map = new PlainArray(Main.game.GAME_WIDTH / cellSize, Main.game.GAME_HEIGHT / PotentialCalcer.cellSize);
    }
}
