package hsbc.ssd.utils.zephyr;

public class ZephyrParams {

    private static volatile String zephyrCycleID;
    private static volatile String zephyrCycleFolderID;
    private static volatile boolean creatingTestCycle;

    public static String getZephyrCycleID() {
        return zephyrCycleID;
    }

    public static void setZephyrCycleID(String zephyrCycleID) {
        ZephyrParams.zephyrCycleID = zephyrCycleID;
    }

    public static String getZephyrCycleFolderID() {
        return zephyrCycleFolderID;
    }

    public static void setZephyrCycleFolderID(String zephyrCycleFolderID) {
        ZephyrParams.zephyrCycleFolderID = zephyrCycleFolderID;
    }

    public static boolean isCreatingTestCycle() {
        return creatingTestCycle;
    }

    public static void setCreatingTestCycle(boolean creatingTestCycle) {
        ZephyrParams.creatingTestCycle = creatingTestCycle;
    }
}
