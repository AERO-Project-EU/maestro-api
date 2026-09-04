package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryDto {

    private List<String> statements = new ArrayList<>();


    public List<String> getStatements() {
        return statements;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }

}
