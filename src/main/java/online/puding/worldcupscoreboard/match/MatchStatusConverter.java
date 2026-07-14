package online.puding.worldcupscoreboard.match;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Memetakan {@link MatchStatus} ke/dari nilai lowercase di kolom match_status. */
@Converter(autoApply = true)
public class MatchStatusConverter implements AttributeConverter<MatchStatus, String> {

    @Override
    public String convertToDatabaseColumn(MatchStatus attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public MatchStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MatchStatus.fromValue(dbData);
    }
}
