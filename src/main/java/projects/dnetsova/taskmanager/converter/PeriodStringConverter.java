package projects.dnetsova.taskmanager.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Period;

@Converter(autoApply = false)
public class PeriodStringConverter implements AttributeConverter<Period, String> {
    @Override
    public String convertToDatabaseColumn(Period attribute) {
        return attribute == null ? null : attribute.toString(); // ISO-8601: PnYnMnD
    }

    @Override
    public Period convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isBlank()) ? null : Period.parse(dbData);
    }
}
