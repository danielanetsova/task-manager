package projects.dnetsova.taskmanager.models;

import java.time.Period;

public record RepeatPeriod(int years, int months, int days) {
    public java.time.Period toPeriod() {
        return java.time.Period.of(years, months, days);
    }

    public static RepeatPeriod fromPeriod(Period period) {
        if (period == null) {
            return null;
        }

        return new RepeatPeriod(
                period.getYears(),
                period.getMonths(),
                period.getDays()
        );
    }
}
