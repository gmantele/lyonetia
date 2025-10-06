package cds.adql.validation.query;

import java.util.Objects;

/**
 * Description of a User Defined Function (UDF).
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version (04/2025)
 *
 * @see UDF
 */
public class UDFParameter {

    private final String name;

    private final String type;

    public UDFParameter(final String name, final String type) {
        this.name = formatIdentifier(name);
        this.type = formatType(type);
    }

    private String formatIdentifier(final String id){
        return Objects.requireNonNull(nullifyIfEmpty(id)).trim();
    }

    private String formatType(final String type){
        return Objects.requireNonNull(nullifyIfEmpty(type)).trim().toUpperCase();
    }

    private String nullifyIfEmpty(final String str){
        if (str == null || str.isBlank())
            return null;
        else
            return str;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UDFParameter that = (UDFParameter) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return name + ' ' + type;
    }
}
