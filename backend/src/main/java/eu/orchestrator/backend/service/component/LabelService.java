package eu.orchestrator.backend.service.component;

import eu.orchestrator.repository.dao.LabelDAO;
import eu.orchestrator.repository.domain.Label;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QLabel.label;

@Service
@Transactional(rollbackOn = Exception.class)
public class LabelService {


    private static final Logger logger = Logger.getLogger(LabelService.class.getName());

    @Autowired
    LabelDAO labelDAO;

    public Page<Label> fetchLabels(Pageable pageable, String filters, Label fLabel) {
        BooleanExpression predicate = label.eq(label);

        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;

        if (checkRequestParam) {
            try {
                Label filterLabel = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Label.class);
                if (null != filterLabel) {
                    proceedWithRequestBody = false;
                    if (null != filterLabel.getName() && !filterLabel.getName().isEmpty()) {
                        predicate = predicate.and(label.name.containsIgnoreCase(filterLabel.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        if (proceedWithRequestBody && null != fLabel && null != fLabel.getName() && !fLabel.getName().isEmpty()) {
            predicate = predicate.and(label.name.containsIgnoreCase(fLabel.getName()));
        }

        if (pageable.getPageSize() > 100) {
            return labelDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        }
        return labelDAO.findAll(predicate, pageable);
    }
}
