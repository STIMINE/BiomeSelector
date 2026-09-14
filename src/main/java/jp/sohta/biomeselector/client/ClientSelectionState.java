package jp.sohta.biomeselector.client;

/** Lightweight, client-only copy of the local player's current selection. */
public final class ClientSelectionState {
    public static volatile int dimension;
    public static volatile int x1;
    public static volatile int z1;
    public static volatile int x2;
    public static volatile int z2;
    public static volatile boolean complete;
    public static volatile boolean present;

    private ClientSelectionState() { }

    public static void set(int dimensionId, int firstX, int firstZ, int secondX, int secondZ,
            boolean isComplete, boolean isPresent) {
        dimension = dimensionId;
        x1 = firstX;
        z1 = firstZ;
        x2 = secondX;
        z2 = secondZ;
        complete = isComplete;
        present = isPresent;
    }
}
