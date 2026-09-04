package eu.orchestrator.backend.rest.support.helper;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.service.support.helper.AutocompleteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/autocomplete")
public class AutocompleteController {

    private static final Logger logger = Logger.getLogger(AutocompleteController.class.getName());

    @Autowired
    private AutocompleteService autocompleteService;


    @GetMapping(value = "/interface")
    public ResponseEntity<RestResponseSPA<String>> fetchInterfaces(@RequestParam(required = false) String query,
            @RequestParam(value = "_type", required = false) String type, HttpServletRequest request) {
        try {
            String interfacesBatch = autocompleteService.fetchInterfaces(query, type);
            if (null != interfacesBatch) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.NOT_AUTHORIZED.getCode(),
                        GenericMessage.NOT_AUTHORIZED.getMessage(request), interfacesBatch));
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                GenericMessage.GENERIC_ERROR.getMessage(request)));

    }

}
