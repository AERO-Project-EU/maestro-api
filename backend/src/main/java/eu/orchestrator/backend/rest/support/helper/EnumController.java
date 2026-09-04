package eu.orchestrator.backend.rest.support.helper;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.transfer.entities.ui.UIEnum;
import eu.orchestrator.backend.service.support.helper.EnumService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/enum")
public class EnumController {

    private static final Logger logger = Logger.getLogger(EnumController.class.getName());

    @Autowired
    private EnumService enumService;


    @GetMapping(value = "/{enumClass}/{enumValue}/values")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchEnumValues(HttpServletRequest request, @PathVariable(value = "enumClass") String enumClass,
            @PathVariable(value = "enumValue") String enumValue) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) enumService.fetchEnumValues(enumClass, enumValue)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/{enumClass}/{enumValue}/value/{name}")
    public ResponseEntity<RestResponseSPA<UIEnum>> fetchEnumValue(HttpServletRequest request, @PathVariable(value = "enumClass") String enumClass,
            @PathVariable(value = "enumValue") String enumValue, @PathVariable(value = "name") String name) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), enumService.fetchEnumValue(enumClass, enumValue, name)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

}
