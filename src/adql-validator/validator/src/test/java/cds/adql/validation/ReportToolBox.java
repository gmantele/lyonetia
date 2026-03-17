package cds.adql.validation;

import cds.adql.validation.report.ADQLValidationReport;
import cds.adql.validation.report.QueryValidationReport;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class ReportToolBox {

    private ReportToolBox() {}

    public static void checkCounts(final ADQLValidationReport report, final int expectedCountPassed, final int expectedCountFailed)
    {
        assertNotNull(report);

        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueries = report.getQueryReports();
        int cntPassed = 0, cntFailed = 0;

        while(itQueries.hasNext()){
            final Map.Entry<UUID, QueryValidationReport> queryReport = itQueries.next();
            cntPassed += queryReport.getValue().countPassed();
            cntFailed += queryReport.getValue().size() - queryReport.getValue().countPassed();
            // DEBUG: queryReport.getValue().forEach(record -> System.out.println(" - "+record.getParserID()+": "+record.getDuration()+"ms ; passed:"+record.isValidationPassed()+" ; error("+record.getErrorMessage()+")"));
        }

        assertEquals(expectedCountPassed, cntPassed);
        assertEquals(expectedCountFailed, cntFailed);
    }

}
