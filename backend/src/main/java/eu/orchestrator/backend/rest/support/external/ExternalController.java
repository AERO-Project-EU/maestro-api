package eu.orchestrator.backend.rest.support.external;

import eu.orchestrator.common.exception.NotFoundException;
import eu.orchestrator.backend.service.support.external.ExternalService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/external")
public class ExternalController {

    private static final Logger logger = Logger.getLogger(ExternalController.class.getName());

    @Autowired
    private ExternalService externalService;


    @GetMapping(value = "/applicationInstances", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> fetchApplicationInstances() {
        try {
            return new ResponseEntity<>(externalService.fetchApplicationInstances(), HttpStatus.OK);
        } catch (NotFoundException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/applicationInstance/{applicationInstanceHexID}/metrics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> fetchApplicationInstanceMetrics(@PathVariable("applicationInstanceHexID") String applicationInstanceHexId) {
        try {
            return new ResponseEntity<>(externalService.fetchApplicationInstanceMetrics(applicationInstanceHexId), HttpStatus.OK);
        } catch (NotFoundException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Serializable> sendFakeNotification() {
        externalService.sendFakeNotification();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //TODO evaluator
    @GetMapping(value = "/retrieve/{componentNodeInstanceID}/public/interface", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> getComponentNodeInstancePublicIp(@PathVariable("componentNodeInstanceID") String componentNodeInstanceId) {
        try {
            return new ResponseEntity<>(externalService.getComponentNodeInstancePublicIp(componentNodeInstanceId), HttpStatus.OK);
        } catch (NotFoundException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
