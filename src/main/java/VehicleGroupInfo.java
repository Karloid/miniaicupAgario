import java.util.ArrayList;
import java.util.List;

public class VehicleGroupInfo {
    public long facilityId = -100;

    final Object vehicleType; //just testing
    private MyStrategy myStrategy;
    public int count;
    public Ownership ownership;
    public Point2D moveToPoint;
    public int moveToPointAt;
    public PointsInfo pointsInfo;
    public List<UnitWrapper> vehicles = new ArrayList<>();
    public int lastShrinkI;
    public int lastShrinkForGatherI;
    public boolean isScaled;
    public boolean isRotated;
    public boolean shouldHeal;
    public int groupNumber;
    public PlainArray potentialMap;
    public int potentialMapCalcAt;
    public int switchCount;
    public boolean nextShrinkIsScale;
    public boolean shrinkRotateToRight;
    public int noMoveCount;
    public int shrinkCount;
    public boolean isScaledNuclear;

    public VehicleGroupInfo(Ownership ownership, Object vehicleType, MyStrategy myStrategy) {
        this.ownership = ownership;
        this.vehicleType = vehicleType;
        this.myStrategy = myStrategy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(vehicleType);
        sb.append(", point=").append(getAveragePoint());
        sb.append(", count=").append(count);
        sb.append('}');
        return sb.toString();
    }

    Point2D getAveragePoint() {
        return pointsInfo != null ? pointsInfo.averagePoint : null;
    }

    public boolean isMovingToPoint() {  //TODO calc distance
        return moveToPoint != null && myStrategy.um.getMinTimeWithoutUpdates(this) < 2; //TODO check size
    }

    public boolean itsTooBig() {
        return pointsInfo != null && (pointsInfo.rect.getHeight() > 100 || pointsInfo.rect.getWidth() > 100);
    }

    public List<UnitWrapper> countWillBeFurtherThenBefore(Point2D moveVector, Point2D target) {
        List<UnitWrapper> o = new ArrayList<>();


        return o;
    }
}
