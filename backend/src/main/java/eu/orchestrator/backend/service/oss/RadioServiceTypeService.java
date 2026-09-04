package eu.orchestrator.backend.service.oss;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.RadioServiceTypeDAO;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.RadioServiceType;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.applicationinstance.ConstraintService;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QRadioServiceType.radioServiceType;

@Service
@Transactional(rollbackOn = Exception.class)
public class RadioServiceTypeService {

    private static final Logger logger = Logger.getLogger(RadioServiceTypeService.class.getName());

    @Autowired
    private RadioServiceTypeDAO radioServiceTypeDAO;

    @Autowired
    private ConstraintService constraintService;


    public RadioServiceType fetchById(Long id) {
        Optional<RadioServiceType> radioServiceTypeOP = radioServiceTypeDAO.findById(id);
        return radioServiceTypeOP.orElse(null);
    }

    public boolean checkRadioServiceTypeAuthentication(User authenticatedUser) {
        return authenticatedUser.isAdmin();
    }

    public Page fetchRadioServiceTypes(Pageable pageable, String filters, RadioServiceType fRadioServiceType, User authenticatedUser) {
        BooleanExpression predicate = radioServiceType.eq(radioServiceType);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                RadioServiceType filterRadioServiceType = new Gson().fromJson(new String(Base64.decodeBase64(filters)), RadioServiceType.class);
                if (null != filterRadioServiceType) {
                    proceedWithRequestBody = false;
                    if (null != filterRadioServiceType.getServiceType() && !filterRadioServiceType.getServiceType().isEmpty()) {
                        predicate = predicate.and(radioServiceType.serviceType.containsIgnoreCase(filterRadioServiceType.getServiceType())
                                .or(radioServiceType.characteristics.containsIgnoreCase(filterRadioServiceType.getServiceType())));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fRadioServiceType) {
                if (null != fRadioServiceType.getServiceType() && !fRadioServiceType.getServiceType().isEmpty()) {
                    predicate = predicate.and(radioServiceType.serviceType.containsIgnoreCase(fRadioServiceType.getServiceType())
                            .or(radioServiceType.characteristics.containsIgnoreCase(fRadioServiceType.getServiceType())));
                }
            }
        }
        Page<RadioServiceType> page;
        if (pageable.getPageSize() > 100) {
            page = radioServiceTypeDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = radioServiceTypeDAO.findAll(predicate, pageable);
        }
        return new TOConverter(RadioServiceType.class.getName(), page, pageable, authenticatedUser).convertToTOWithPermissions();
    }

    public void create(RadioServiceType radioServiceType) {
        // Check if Radio Service Type already exists
        if (radioServiceTypeDAO.findBySstValue(radioServiceType.getSstValue()).isPresent()) {
            throw new GenericBusinessException(GenericMessage.RADIO_SERVICE_TYPE_ALREADY_EXISTS.getCode(), GenericMessage.RADIO_SERVICE_TYPE_ALREADY_EXISTS);

        }

        radioServiceType.setDateCreated(new Date());
        radioServiceTypeDAO.save(radioServiceType);


    }

    public void update(RadioServiceType radioServiceType) {
        // Check if Radio Service Type already exists or not
        RadioServiceType existingRadioServiceType = fetchById(radioServiceType.getId());
        if (existingRadioServiceType != null) {
            if (!existingRadioServiceType.getSstValue().equals(radioServiceType.getSstValue())
                    && radioServiceTypeDAO.findBySstValue(radioServiceType.getSstValue()).isPresent()) {
                throw new GenericBusinessException(GenericMessage.RADIO_SERVICE_TYPE_ALREADY_EXISTS.getCode(),
                        GenericMessage.RADIO_SERVICE_TYPE_ALREADY_EXISTS);
            }
            existingRadioServiceType.setSstValue(radioServiceType.getSstValue());
            existingRadioServiceType.setServiceType(radioServiceType.getServiceType());
            existingRadioServiceType.setCharacteristics(radioServiceType.getCharacteristics());
            radioServiceTypeDAO.save(existingRadioServiceType);
        } else {
            throw new GenericBusinessException(GenericMessage.RADIO_SERVICE_NOT_EXIST.getCode(),
                    GenericMessage.RADIO_SERVICE_NOT_EXIST);
        }
    }

    public void delete(Long id) {
        // Check if Radio Service Type already exists or not

        RadioServiceType existingRadioServiceType = fetchById(id);
        if (existingRadioServiceType != null) {
            // Check if Radio Service Type is used
            List<Constraint> constraints = constraintService.fetchConstrainsByRadioServiceTypeAndConstraintCategory(existingRadioServiceType,
                    Constraint.ConstraintCategory.ACCESS.name());
            if (NullCheckUtil.isNotEmpty(constraints)) {
                throw new GenericBusinessException(GenericMessage.RADIO_SERVICE_TYPE_USED.getCode(), GenericMessage.RADIO_SERVICE_TYPE_USED);
            }
            radioServiceTypeDAO.delete(existingRadioServiceType);
        } else {
            throw new GenericBusinessException(GenericMessage.RADIO_SERVICE_NOT_EXIST.getCode(), GenericMessage.RADIO_SERVICE_NOT_EXIST);
        }
    }

}
