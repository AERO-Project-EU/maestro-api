package eu.orchestator.core.model.agent;

import java.util.ArrayList;

/**
 * @author Panagiotis Parthenis.
 */
public class Dependencies {

    private ArrayList<String> dependsOnComponent = new ArrayList<>();

    public ArrayList<String> getDependsOnComponent() {
        return dependsOnComponent;
    }

    public void setDependsOnComponent(ArrayList<String> dependsOnComponent) {
        this.dependsOnComponent = dependsOnComponent;
    }
}
