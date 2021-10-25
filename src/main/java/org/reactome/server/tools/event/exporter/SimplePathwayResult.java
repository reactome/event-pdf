package org.reactome.server.tools.event.exporter;

import org.neo4j.driver.Record;
import org.reactome.server.graph.domain.result.CustomQuery;

public class SimplePathwayResult implements CustomQuery {
    private String stId;
    private String displayName;

    public SimplePathwayResult() {}

    public SimplePathwayResult(String stId, String displayName) {
        this.stId = stId;
        this.displayName = displayName;
    }

    public String getStId() {
        return stId;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public CustomQuery build(Record r) {
        return new SimplePathwayResult(r.get("stId").asString(), r.get("displayName").asString());
    }
}
