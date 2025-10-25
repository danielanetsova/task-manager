package projects.dnetsova.taskmanager.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TaskUpdate {
    private Optional<String> title;
    private Optional<String> description;
    private Optional<String> priority;
    private Optional<LocalDate> start;
    private Optional<LocalDate> deadline;
    private Optional<LocalDate> repeat;
    private Optional<List<String>> assignees;
    private Optional<Boolean> isCompleted;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public TaskUpdate(JsonNode node) {
        this.title = parseOptional(node, "title", JsonNode::asText);
        this.description = parseOptional(node, "description", JsonNode::asText);
        this.priority = parseOptional(node, "priority", JsonNode::asText);
        this.start = parseOptional(node, "start", n -> LocalDate.parse(n.asText()));
        this.deadline = parseOptional(node, "deadline", n -> LocalDate.parse(n.asText()));
        this.repeat = parseOptional(node, "repeat", n -> LocalDate.parse(n.asText()));
        this.assignees = parseOptional(node, "assignees", n ->
                StreamSupport.stream(n.spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.toList()));
        this.isCompleted = parseOptional(node, "isCompleted", JsonNode::asBoolean);
    }

    private <T> Optional<T> parseOptional(JsonNode node, String field, Function<JsonNode, T> parser) {
        if (!node.has(field)) return Optional.empty();
        JsonNode valueNode = node.get(field);
        if (valueNode.isNull()) return null;
        if (valueNode.isTextual() && valueNode.asText().isEmpty()) return Optional.empty();
        return Optional.of(parser.apply(valueNode));
    }

    public Optional<String> getTitle() { return title; }
    public Optional<String> getDescription() { return description; }
    public Optional<String> getPriority() { return priority; }
    public Optional<LocalDate> getStart() { return start; }
    public Optional<LocalDate> getDeadline() { return deadline; }
    public Optional<LocalDate> getRepeat() { return repeat; }
    public Optional<List<String>> getAssignees() { return assignees; }
    public Optional<Boolean> getIsCompleted() { return isCompleted; }
}
