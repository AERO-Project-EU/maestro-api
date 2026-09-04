package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.io.Serializable;

public class StreamDto implements Serializable {

    private QueryDto ksql;


    public QueryDto getKsql() {
        return ksql;
    }

    public void setKsql(QueryDto ksql) {
        this.ksql = ksql;
    }
}
