package cds.adql.validation.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UDFTest {

    @Test
    void getForm_ShouldSucceed_WhenOnlyAName(){
        // Given + When:
        final UDF udf = new UDF("foo");

        // Then:
        assertEquals("foo()", udf.getForm());
    }

    @Test
    void getForm_ShouldSucceed_WhenNameAndReturnType(){
        // Given + When:
        final UDF udf = new UDF("foo", "varchar");

        // Then:
        assertEquals("foo() -> VARCHAR", udf.getForm());
    }

    @Test
    void getForm_ShouldSucceed_WhenNameAndParameters(){
        // Given + When:
        final UDFParameter param1 = new UDFParameter("param1", "double");
        final UDFParameter param2 = new UDFParameter("param2", "integer");
        final UDF udf = new UDF("foo", null, param1, param2);

        // Then:
        assertEquals("foo(param1 DOUBLE, param2 INTEGER)", udf.getForm());
    }

    @Test
    void getForm_ShouldSucceed_WhenFullDefinition(){
        // Given + When:
        final UDFParameter param1 = new UDFParameter("param1", "double");
        final UDFParameter param2 = new UDFParameter("param2", "integer");
        final UDF udf = new UDF("foo", "Varchar", param1, param2);

        // Then:
        assertEquals("foo(param1 DOUBLE, param2 INTEGER) -> VARCHAR", udf.getForm());
    }

    @Test
    void getForm_ShouldSucceed_WhenFullDefinitionWithUselessSpaces(){
        // Given + When:
        final UDFParameter param1 = new UDFParameter("param1  ", "double ");
        final UDFParameter param2 = new UDFParameter("  param2", " integer");
        final UDF udf = new UDF("   foo  ", "  VarChar ", param1, param2);

        // Then:
        assertEquals("foo(param1 DOUBLE, param2 INTEGER) -> VARCHAR", udf.getForm());
    }
}