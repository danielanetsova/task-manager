package projects.dnetsova.taskmanager.utils;

/**
 * Enum of acceptable values tasks priority
 */
public enum Priority {
    P0(0),
    P1(1),
    P2(2),
    P3(3),
    P4(4);

    private final int rank;

    Priority(int rank) {
        this.rank = rank;
    }

    public boolean isHigherThan(Priority other) {
        return this.rank < other.rank;
    }
}
