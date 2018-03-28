import java.util.*;

public class UnitManager {
    private MyStrategy mys;

    final Map<Long, UnitWrapper> vehicleById = new HashMap<>();
    private List<UnitWrapper> deadVehicles = new ArrayList<>();

    TickStats myStats;
    TickStats enemyStats;
    private List<UnitWrapper> cachedMy;
    private List<UnitWrapper> cachedEnemy;
    public int maxMyHp;


    public UnitManager(MyStrategy mys) {

        this.mys = mys;
    }

    public void initializeTick() {
        cachedMy = null;
        cachedEnemy = null;

        myStats = new TickStats();
        enemyStats = new TickStats();

        initTickNewVehicles();

        initTickVehUpdates();


        initTickVehCalcStats();


       // maxMyHp = streamVehicles(Ownership.ALLY).max(Comparator.comparingInt(value -> value.v.getDurability())).map(vehicleWrapper -> vehicleWrapper.v.getDurability()).orElse(50);

        if (myStats.isNonEmpty()) {
            mys.log("My stats: " + myStats);
        }
        if (enemyStats.isNonEmpty()) {
            mys.log("Enemy stats: " + enemyStats);
        }
    }


    private void initTickVehCalcStats() {
        Collection<UnitWrapper> allUnits = vehicleById.values();

        for (Iterator<UnitWrapper> iterator = allUnits.iterator(); iterator.hasNext(); ) {
            UnitWrapper u = iterator.next();
            TickStats stats = u.isEnemy ? enemyStats : myStats;

            stats.remainingUnits++;
         //   stats.remainingHp += u.v.getDurability();
        }
    }

    private void initTickVehUpdates() {
/*
        for (VehicleUpdate vehicleUpdate : mys.world.getVehicleUpdates()) {
            long vehicleId = vehicleUpdate.getId();

            UnitWrapper veh = vehicleById.get(vehicleId);
            veh.update(new Vehicle(veh.v, vehicleUpdate));
            if (veh.hpDelta != 0) {
                TickStats stats = veh.isEnemy ? enemyStats : myStats;
                if (veh.hpDelta > 0) {
                    stats.healedPoints += veh.hpDelta;
                } else {
                    stats.damagedPoints += veh.hpDelta;
                    stats.damagedUnits += 1;
                    if (veh.v.getDurability() <= 0) {
                        stats.destroyedUnits += 1;
                    }
                }
            }

            if (vehicleUpdate.getDurability() == 0) {
                deadVehicles.add(veh);
                vehicleById.remove(veh.v.getId());
            }
        }
*/
    }

    private void initTickNewVehicles() {
/*
        for (Vehicle vehicle : mys.world.getNewVehicles()) {
            UnitWrapper mv = new UnitWrapper(vehicle, mys);
            vehicleById.put(vehicle.getId(), mv);
        }
*/
    }

    public UnitWrapper get(long id) {
        return vehicleById.get(id);
    }

/*
    Stream<UnitWrapper> streamVehicles(Ownership ownership, VehicleType vehicleType) {
        Stream<UnitWrapper> stream = vehicleById.values().stream();

        switch (ownership) {
            case ALLY:
                if (cachedMy == null) {
                    cachedMy = stream.filter(vehicle -> !vehicle.isEnemy).collect(Collectors.toList());
                }
                stream = cachedMy.stream();
                break;
            case ENEMY:
                if (cachedEnemy == null) {
                    cachedEnemy = stream.filter(vehicle -> vehicle.isEnemy).collect(Collectors.toList());
                }
                stream = cachedEnemy.stream();
                break;
            default:
        }

        if (vehicleType != null) {
            stream = stream.filter(vehicle -> vehicle.v.getType() == vehicleType);
        }

        return stream;
    }
*/

    /*Stream<UnitWrapper> streamVehicles(Ownership ownership) {
        return streamVehicles(ownership, null);
    }*/

    /*Stream<UnitWrapper> streamVehicles() {
        return streamVehicles(Ownership.ANY);
    }*/

    public int getMinTimeWithoutUpdates(VehicleGroupInfo vehicleGroupInfo) {
        return vehicleGroupInfo.vehicles.stream()
                .mapToInt(v -> mys.world.getTickIndex() - v.movedAt)
                .min().orElse(0);
    }

    public int getUnitCount(Ownership ownership) {
        int count = 0;
        for (UnitWrapper vw : vehicleById.values()) {
            switch (ownership) {
                case ANY:
                    count++;
                    break;
                case ALLY:
                    if (!vw.isEnemy) {
                        count++;
                    }
                    break;
                case ENEMY:
                    if (vw.isEnemy) {
                        count++;
                    }
                    break;
            }
        }
        return count;
    }
}
