package SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeEprunteur {
    ADMINISTRATIONS_PUBLIQUES,
    SOCIETES_NON_FINANCIERES_PUBLIQUES,
    GRANDE_ENTREPRISE,
    PME,
    SOCIETES_ASSURANCE,
    AUTRES_SOCIETES_FINANCIERES,
    MENAGES,
    INSTITUTIONS_SANS_BUT_LUCRATIF,
    PARTICULIER;

    @JsonCreator
    public static TypeEprunteur fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("TypeEprunteur ne peut pas être null");
        }

        for (TypeEprunteur type : TypeEprunteur.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("TypeEprunteur inconnu : \"" + value + "\"");
    }

    @JsonValue
    public String toValue() {
        return this.name(); // on retourne PARTICULIER
    }

    @Override
    public String toString() {
        return this.name();
    }
}
