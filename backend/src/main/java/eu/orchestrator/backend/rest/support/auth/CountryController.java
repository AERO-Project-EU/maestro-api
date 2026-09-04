package eu.orchestrator.backend.rest.support.auth;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.Country;
import eu.orchestrator.backend.service.support.auth.CountryService;

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
@RequestMapping("/api/v1/country")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchCountries(Pageable pageable, @RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) Country country, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) countryService.fetchCountries(pageable, filters, country)));
    }
}
