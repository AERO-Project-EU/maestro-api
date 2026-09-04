package eu.orchestrator.backend.rest.component;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.repository.domain.Label;
import eu.orchestrator.backend.service.component.LabelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/label")
public class LabelController {

    @Autowired
    private LabelService labelService;


    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchLabels(Pageable pageable, @RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) Label label, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                GenericMessage.ENTITIES_FETCHED.getMessage(request),
                (Serializable) new TOConverter(Label.class.getName(), labelService.fetchLabels(pageable, filters, label), pageable).convertToTO()));
    }

}
