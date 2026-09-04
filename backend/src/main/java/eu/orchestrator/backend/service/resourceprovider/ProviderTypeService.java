package eu.orchestrator.backend.service.resourceprovider;

import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.repository.dao.ProviderTypeDAO;
import eu.orchestrator.repository.domain.ProviderType;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QProviderType.providerType;

@Service
@Transactional(rollbackOn = Exception.class)
public class ProviderTypeService {

    private static final Logger logger = Logger.getLogger(ProviderTypeService.class.getName());

    @Autowired
    private ProviderTypeDAO providerTypeDAO;


    public ProviderType fetchById(Long id) {
        Optional<ProviderType> providerTypeOptional = providerTypeDAO.findById(id);
        return providerTypeOptional.orElse(null);
    }

    public ProviderType fetchByFriendlyName(String friendlyName) {
        Optional<ProviderType> providerTypeOptional = providerTypeDAO.findByFriendlyName(friendlyName);
        return providerTypeOptional.orElse(null);
    }

    public ProviderType fetchByName(String name) {
        Optional<ProviderType> providerTypeOptional = providerTypeDAO.findByName(name);
        return providerTypeOptional.orElse(null);
    }


    public Page fetchProviderTypes(Pageable pageable, String filters, ProviderType fProviderType) {
        BooleanExpression predicate = providerType.eq(providerType).and(providerType.id.notIn(1L, 2L));
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                ProviderType filterProviderType = new Gson().fromJson(new String(Base64.decodeBase64(filters)), ProviderType.class);
                if (null != filterProviderType) {
                    proceedWithRequestBody = false;
                    if (null != filterProviderType.getName() && !filterProviderType.getName().isEmpty()) {
                        predicate = predicate.and(providerType.name.containsIgnoreCase(filterProviderType.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fProviderType) {
                if (null != fProviderType.getName() && !fProviderType.getName().isEmpty()) {
                    predicate = predicate.and(providerType.name.containsIgnoreCase(fProviderType.getName()));
                }
            }
        }
        Page<ProviderType> page;
        if (pageable.getPageSize() > 100) {
            page = providerTypeDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = providerTypeDAO.findAll(predicate, pageable);
        }
        return new TOConverter(ProviderType.class.getName(), page, pageable).convertToTO();
    }

}
