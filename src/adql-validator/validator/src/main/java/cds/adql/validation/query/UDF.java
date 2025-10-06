package cds.adql.validation.query;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Definition of a User Defined Function.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 */
public class UDF {

    private final String name;

    private final List<UDFParameter> parameters;

    private final String returnType;

    private String form = null;

    private String description = null;

    public UDF(final String name){
        this(name, null);
    }

    public UDF(final String name, final String returnType, final UDFParameter... parameters){
        this.name       = formatIdentifier(name);
        this.returnType = formatReturnType(returnType);
        this.parameters = Arrays.asList(parameters);
    }

    private String formatIdentifier(final String id){
        return Objects.requireNonNull(nullifyIfEmpty(id)).trim();
    }

    private String formatReturnType(String type){
        type = nullifyIfEmpty(type);
        if (type != null)
            return formatType(type);
        else
            return null;
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

    public List<UDFParameter> getParameters() {
        return parameters;
    }

    public Optional<String> getReturnType() {
        return Optional.ofNullable(returnType);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(final String newDescription){
        this.description = newDescription;
    }

    public String getForm() {
        if (form == null)
            form = buildForm();
        return form;
    }

    private String buildForm(){
        final StringBuilder fctForm = new StringBuilder();

        fctForm.append(name).append('(');

        for(int i=0; i<parameters.size(); i++){
            final UDFParameter param = parameters.get(i);
            if (i>0)
                fctForm.append(", ");
            fctForm.append(param.getName()).append(' ').append(param.getType());
        }

        fctForm.append(')');

        if (returnType != null)
            fctForm.append(" -> ").append(returnType);

        return fctForm.toString();
    }

    @Override
    public String toString() {
        return "Function{form='" + form + '\'' + ", description=" + description.replace("'", "\\'") + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UDF udf = (UDF) o;
        return Objects.equals(form, udf.form);
    }

    @Override
    public int hashCode() {
        return Objects.hash(form);
    }

}
