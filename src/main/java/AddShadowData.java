public class AddShadowData {
    public final Point2D minePos;
    public final double leftAngle;
    public final double rightAngle;
    public final Integer value;
    public final Point2D pos;
    public final double squareGapDistance;

    public AddShadowData(Point2D minePos, double leftAngle, double rightAngle, Integer value, Point2D pos, double squareDistance) {

        this.minePos = minePos;
        this.leftAngle = leftAngle;
        this.rightAngle = rightAngle;
        this.value = value;
        this.pos = pos;
        squareGapDistance = squareDistance;
    }
}
