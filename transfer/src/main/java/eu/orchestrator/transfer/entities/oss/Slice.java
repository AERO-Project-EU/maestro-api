package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Slice implements Serializable {

    String sliceID;
    String sliceIntentID;
    List<VIM> vimDescriptors;
    List<ComponentPlacement> componentPlacements;
    List<ConstraintSatisfaction> constraintSatisfactions;

    public Slice() {
    }

    public String getSliceID() {
        return sliceID;
    }

    public void setSliceID(String sliceID) {
        this.sliceID = sliceID;
    }

    public String getSliceIntentID() {
        return sliceIntentID;
    }

    public void setSliceIntentID(String sliceIntentID) {
        this.sliceIntentID = sliceIntentID;
    }

    public List<VIM> getVimDescriptors() {
        return vimDescriptors != null ? vimDescriptors : Collections.emptyList();
    }

    public void setVimDescriptors(List<VIM> vimDescriptors) {
        this.vimDescriptors = vimDescriptors;
    }

    public List<ComponentPlacement> getComponentPlacements() {
        return componentPlacements != null ? componentPlacements : Collections.emptyList();
    }

    public void setComponentPlacements(List<ComponentPlacement> componentPlacements) {
        this.componentPlacements = componentPlacements;
    }

    public List<ConstraintSatisfaction> getConstraintSatisfactions() {
        return constraintSatisfactions != null ? constraintSatisfactions : Collections.emptyList();
    }

    public void setConstraintSatisfactions(List<ConstraintSatisfaction> constraintSatisfactions) {
        this.constraintSatisfactions = constraintSatisfactions;
    }
}
