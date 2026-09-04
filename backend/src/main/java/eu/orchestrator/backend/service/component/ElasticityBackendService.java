package eu.orchestrator.backend.service.component;

import eu.orchestrator.backend.transfer.ElasticityControllerModeTO;
import eu.orchestrator.backend.transfer.ElasticityControllerTO;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.repository.dao.ElasticityHistoryDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ElasticityHistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class ElasticityBackendService {

    @Autowired
    ElasticityHistoryDAO elasticityHistoryDAO;

    @Resource(name = "elasticityFrameworkAdapters")
    private List elasticityFrameworkAdapters;


    public List<ElasticityHistory> fetchHistoryByApplicationInstanceAndComponentNodeOrderByIdDesc(ApplicationInstance applicationInstance,
            ComponentNode componentNode) {
        Optional<List<ElasticityHistory>> elasticityHistoryOp = elasticityHistoryDAO
                .findByApplicationInstanceAndAndComponentNodeOrderByIdDesc(applicationInstance, componentNode);
        return elasticityHistoryOp.orElse(null);
    }

    public List<ElasticityHistory> fetchHistoryByApplicationInstanceAndAndComponentNodeOrderByDateCreated(ApplicationInstance applicationInstance,
            ComponentNode componentNode) {
        return elasticityHistoryDAO.findAllByApplicationInstanceAndAndComponentNodeOrderByDateCreated(applicationInstance, componentNode);
    }

    public List<ElasticityControllerTO> retrieveElasticityControllers() {

        List<ElasticityControllerTO> allElasticityControllers = new ArrayList<>();

        ElasticityControllerTO elasticityModeTO = new ElasticityControllerTO();
        elasticityModeTO.setValue("");
        elasticityModeTO.setLabel("-- Select --");
        allElasticityControllers.add(elasticityModeTO);

        ElasticityControllerTO elasticityModeTO1 = new ElasticityControllerTO();
        elasticityModeTO1.setValue("NONE");
        elasticityModeTO1.setLabel("none");
        allElasticityControllers.add(elasticityModeTO1);

        for (ElasticityFrameworkBackend elasticityAdapter : (List<ElasticityFrameworkBackend>) elasticityFrameworkAdapters) {
            ElasticityControllerTO elasticityModeTO2 = new ElasticityControllerTO();
            elasticityModeTO2.setValue(elasticityAdapter.getElasticityType().getName());
            elasticityModeTO2.setLabel(elasticityAdapter.getElasticityType().getName());

            allElasticityControllers.add(elasticityModeTO2);
        }

        return allElasticityControllers;

    }

    public List<ElasticityControllerModeTO> retrieveElasticityControllerModes(String elasticityController) {

        List<ElasticityControllerModeTO> allElasticityControllersModes = new ArrayList<>();

        for (ElasticityFrameworkBackend elasticityAdapter : (List<ElasticityFrameworkBackend>) elasticityFrameworkAdapters) {

            if (elasticityAdapter.getElasticityType().getName().equals(elasticityController)) {

                if (null != elasticityAdapter.getElasticityType().getElasticityMode() && !elasticityAdapter.getElasticityType().getElasticityMode().isEmpty()) {

                    ElasticityControllerModeTO elasticityControllerModeTO = new ElasticityControllerModeTO();
                    elasticityControllerModeTO.setValue("");
                    elasticityControllerModeTO.setLabel("-- Select --");
                    allElasticityControllersModes.add(elasticityControllerModeTO);

                    for (String mode : elasticityAdapter.getElasticityType().getElasticityMode()) {

                        ElasticityControllerModeTO elasticityControllerModeTO1 = new ElasticityControllerModeTO();
                        elasticityControllerModeTO1.setLabel(mode);
                        elasticityControllerModeTO1.setValue(mode);

                        allElasticityControllersModes.add(elasticityControllerModeTO1);
                    }
                }

                break;
            }
        }

        return allElasticityControllersModes;
    }
}
