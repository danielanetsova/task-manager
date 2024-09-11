package projects.dnetsova.taskmanager.models;

import java.util.List;

public record CustomPage<T>(List<T> elements,
                            int totalPageCount,
                            long totalElementsCount) {

}
