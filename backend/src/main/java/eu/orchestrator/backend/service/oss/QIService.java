package eu.orchestrator.backend.service.oss;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.QITO;
import eu.orchestrator.repository.dao.QIDAO;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.QI;
import eu.orchestrator.repository.domain.QI.ResourceType;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.applicationinstance.ConstraintService;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QQI.qI;

@Service
@Transactional(rollbackOn = Exception.class)
public class QIService {

    private static final Logger logger = Logger.getLogger(QIService.class.getName());

    @Autowired
    private QIDAO qiDAO;

    @Autowired
    private ConstraintService constraintService;


    public boolean checkQIAuthentication(User authenticatedUser) {
        return authenticatedUser.isAdmin();
    }

    public Page fetchQIs(Pageable pageable, String filters, QI qi) {
        BooleanExpression predicate = qI.eq(qI);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                QI filterQI = new Gson().fromJson(new String(Base64.decodeBase64(filters)), QI.class);
                if (null != filterQI) {
                    proceedWithRequestBody = false;
                    if (null != filterQI.getQiValue() && !filterQI.getQiValue().isEmpty()) {
                        predicate = predicate.and(qI.qiValue.containsIgnoreCase(filterQI.getQiValue()));
                    }
                    if (null != filterQI.getResourceType() && !filterQI.getResourceType().isEmpty()) {
                        predicate = predicate.and(qI.resourceType.eq(filterQI.getResourceType()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        if (proceedWithRequestBody && null != qi) {
            if (null != qi.getQiValue() && !qi.getQiValue().isEmpty()) {
                predicate = predicate.and(qI.qiValue.containsIgnoreCase(qi.getQiValue()));
            }
            if (null != qi.getResourceType() && !qi.getResourceType().isEmpty()) {
                predicate = predicate.and(qI.resourceType.eq(qi.getResourceType()));
            }
        }

        Page<QI> page;
        if (pageable.getPageSize() > 100) {
            page = qiDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = qiDAO.findAll(predicate, pageable);
        }

        List<QITO> qiTOs = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            page.getContent().stream().forEach(tQI -> {
                QITO qiTO = new QITO();
                qiTO.setId(tQI.getId());
                qiTO.setDateCreated(tQI.getDateCreated());
                qiTO.setQiValue(tQI.getQiValue());
                qiTO.setDefaultPriorityLevel(tQI.getDefaultPriorityLevel());
                qiTO.setResourceType(ResourceType.valueOf(tQI.getResourceType()).getFriendlyName());
                qiTO.setPacketDelayBudget(String.valueOf(tQI.getPacketDelayBudget()));
                qiTO.setPacketErrorRate(String.valueOf(tQI.getPacketErrorRate()));
                qiTO.setAllowDelete(true);
                qiTO.setAllowEdit(true);
                qiTOs.add(qiTO);
            });
        }
        return new PageImpl<>(qiTOs, pageable, page.getTotalElements());
    }

    public QI fetchById(Long id) {
        Optional<QI> qiOP = qiDAO.findById(id);
        return qiOP.orElse(null);
    }

    public void create(QI qi) {
        // Check if QI value already exists
        if (qiDAO.findByQiValue(qi.getQiValue()).isPresent()) {
            throw new GenericBusinessException(GenericMessage.QI_ALREADY_EXISTS.getCode(), GenericMessage.QI_ALREADY_EXISTS);
        }
        qi.setDateCreated(new Date());
        qiDAO.save(qi);
    }

    public void update(QI qi) {
        QI existingQI = fetchById(qi.getId());
        if (existingQI != null) {
            if (!existingQI.getQiValue().equals(qi.getQiValue()) && qiDAO.findByQiValue(qi.getQiValue()).isPresent()) {
                throw new GenericBusinessException(GenericMessage.QI_ALREADY_EXISTS.getCode(), GenericMessage.QI_ALREADY_EXISTS);
            }
            existingQI.setQiValue(qi.getQiValue());
            existingQI.setDefaultAveragingWindow(qi.getDefaultAveragingWindow());
            existingQI.setDefaultMaximumDataBurstVolume(qi.getDefaultMaximumDataBurstVolume());
            existingQI.setDefaultPriorityLevel(qi.getDefaultPriorityLevel());
            existingQI.setPacketDelayBudget(qi.getPacketDelayBudget());
            existingQI.setPacketErrorRate(qi.getPacketErrorRate());
            existingQI.setServices(qi.getServices());
            existingQI.setResourceType(qi.getResourceType());
            qiDAO.save(existingQI);
        } else {
            throw new GenericBusinessException(GenericMessage.QI_NOT_EXIST.getCode(), GenericMessage.QI_NOT_EXIST);
        }
    }

    public void delete(Long id) {
        QI existingQI = fetchById(id);
        if (existingQI != null) {
            // Check if QI is used
            List<Constraint> constraints = constraintService.fetchConstrainsByQiAndConstraintCategory(existingQI, Constraint.ConstraintCategory.ACCESS.name());
            if (null != constraints && !constraints.isEmpty()) {
                throw new GenericBusinessException(GenericMessage.QI_USED.getCode(), GenericMessage.QI_USED);
            }
            qiDAO.delete(existingQI);
        } else {
            throw new GenericBusinessException(GenericMessage.QI_NOT_EXIST.getCode(), GenericMessage.QI_NOT_EXIST);
        }
    }

}