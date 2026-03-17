package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import cds.adql.validation.report.ADQLValidationReport;

import java.util.List;
import java.util.Objects;

public class QuerySetValidation {

    private final ValidationSet querySet;
    private final ThreadPool threadPool;
    private final List<ADQLParser> parsers;
    private final ADQLValidationReport report;

    public QuerySetValidation(final ValidationSet querySet, final ThreadPool threadPool, final List<ADQLParser> parsers, final ADQLValidationReport report) {
        this.querySet   = Objects.requireNonNull(querySet);
        this.threadPool = Objects.requireNonNull(threadPool);
        this.parsers    = Objects.requireNonNull(parsers);
        this.report     = Objects.requireNonNull(report);
    }

    public void startValidation() {
        for(ValidationQuery query : querySet) {
            for (ADQLParser parser : parsers){
                final SingleQueryValidation queryValidator = new SingleQueryValidation(query, parser, report);
                threadPool.submit(queryValidator);
            }
        }
    }

}
