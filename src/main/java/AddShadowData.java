public class AddShadowData {
    public final Point2D minePos;
    public final double minAngle;
    public final double maxAngle;
    public final Integer value;
    public final Point2D pos;
    public final double squareGapDistance;

    public AddShadowData(Point2D minePos, double minAngle, double maxAngle, Integer value, Point2D pos, double squareDistance) {

        this.minePos = minePos;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.value = value;
        this.pos = pos;
        squareGapDistance = squareDistance;
    }
}
