package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.List;

public class SliceTO implements Serializable {

    String sliceID;
    List<PlacementTO> placements;
    List<ConstraintSatisfactionTO> constraintSatisfactions;

    public SliceTO() {
    }

    public String getSliceID() {
        return sliceID;
    }

    public void setSliceID(String sliceID) {
        this.sliceID = sliceID;
    }

    public List<PlacementTO> getPlacements() {
        return placements;
    }

    public void setPlacements(List<PlacementTO> placements) {
        this.placements = placements;
    }

    public List<ConstraintSatisfactionTO> getConstraintSatisfactions() {
        return constraintSatisfactions;
    }

    public void setConstraintSatisfactions(List<ConstraintSatisfactionTO> constraintSatisfactions) {
        this.constraintSatisfactions = constraintSatisfactions;
    }
}
