package cds.adql.validation.query;

import cds.adql.validation.parser.adql.ADQLVersion;
import cds.adql.validation.parser.validationset.ValidationSetParser;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValidationQueryTest {

    /* *************************************************************************
     * CONSTRUCTOR
     */

    @Test
    void create_ShouldGenerateID_WhenNullUUID(){
        // Given + When:
        final ValidationQuery q = new ValidationQuery((UUID)null);

        // Then:
        assertNotNull(q.getId());
    }

    @Test
    void create_ShouldGenerateID_WhenNullString(){
        // Given + When:
        final ValidationQuery q = new ValidationQuery((String)null);

        // Then:
        assertNotNull(q.getId());
    }

    @Test
    void create_ShouldGenerateID_WhenEmptyString(){
        // Given + When:
        final ValidationQuery q = new ValidationQuery("  ");

        // Then:
        assertNotNull(q.getId());
    }


    /* *************************************************************************
     * EQUALS
     */

    @Test
    void equals_ShouldFail_WhenNull() {
        // Given + When:
        final ValidationQuery q = new ValidationQuery();

        // Then:
        assertNotEquals(null, q);
    }

    @Test
    void equals_ShouldFail_WhenAnotherClass() {
        // Given + When:
        final ValidationQuery q = new ValidationQuery();

        // Then:
        assertNotEquals("Hello", q);
    }

    @Test
    void equals_ShouldSucceed_WhenSameObject() {
        // Given + When:
        final ValidationQuery q = new ValidationQuery();

        // Then:
        assertEquals(q, q);
    }

    @Test
    void equals_ShouldSucceed_WhenSameId() {
        // Given:
        final UUID id = UUID.randomUUID();

        // When:
        final ValidationQuery q1 = new ValidationQuery(id);
        final ValidationQuery q2 = new ValidationQuery(id);

        // Then:
        assertEquals(q1, q2);
    }

    @Test
    void equals_ShouldFail_WhenDifferentId() {
        // Given + When:
        final ValidationQuery q1 = new ValidationQuery(UUID.randomUUID());
        final ValidationQuery q2 = new ValidationQuery(UUID.randomUUID());

        // Then:
        assertNotEquals(q1, q2);
    }


    /* *************************************************************************
     * SET_ADQL_VERSION
     */

    @Test
    void setADQLVersion_ShouldSetDefaultValue_WhenNull(){
        // Given:
        final ValidationQuery q = new ValidationQuery(UUID.randomUUID());
        assertEquals(ValidationSetParser.DEFAULT_ADQL_VERSION, q.getADQLVersion());
        assertNotEquals(ADQLVersion.V2_0, ValidationSetParser.DEFAULT_ADQL_VERSION);
        q.setADQLVersion(ADQLVersion.V2_0);
        assertEquals(ADQLVersion.V2_0, q.getADQLVersion());

        // When:
        q.setADQLVersion(null);

        // Then:
        assertEquals(ValidationSetParser.DEFAULT_ADQL_VERSION, q.getADQLVersion());
    }


    /* *************************************************************************
     * SET_DESCRIPTION
     */

    @Test
    void setDescription_ShouldSetNull_WhenEmptyString(){
        // Given:
        final ValidationQuery q = new ValidationQuery();
        final String initialDescription = "Blabla";
        q.setDescription(initialDescription);
        assertTrue(q.getDescription().isPresent());
        assertEquals(initialDescription, q.getDescription().orElse(null));

        // When:
        q.setDescription(" ");

        // Then:
        assertTrue(q.getDescription().isEmpty());
    }

    @Test
    void setDescription_ShouldTrim_WhenStringWithSpacesAround(){
        // Given:
        final ValidationQuery q = new ValidationQuery();
        final String initialDescription = "Blabla";

        // When:
        q.setDescription("\t" + initialDescription + "  ");

        // Then:
        assertTrue(q.getDescription().isPresent());
        assertEquals(initialDescription, q.getDescription().orElse(null));
    }
}