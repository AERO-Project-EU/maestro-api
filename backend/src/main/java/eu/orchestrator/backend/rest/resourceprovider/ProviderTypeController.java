package eu.orchestrator.backend.rest.resourceprovider;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.backend.service.resourceprovider.ProviderTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/providertype")
public class ProviderTypeController {

    private static final Logger logger = Logger.getLogger(ProviderTypeController.class.getName());

    @Autowired
    private ProviderTypeService providerTypeService;


    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchProviderTypes(Pageable pageable,
            @RequestParam(required = false, value = "filters") String filters, @RequestBody(required = false) ProviderType providerType,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) providerTypeService.fetchProviderTypes(pageable, filters, providerType)));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<ProviderType>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                ProviderType providerType = providerTypeService.fetchById(id);
                if (providerType != null) {
                    return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.PROVIDER_FETCHED.getCode(),
                            GenericMessage.PROVIDER_TYPE_FETCHED.getMessage(request), providerType));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                            GenericMessage.GENERIC_ERROR.getMessage(request)));
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

}
