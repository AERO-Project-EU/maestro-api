package eu.orchestrator.backend.service.support.auth;

import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.repository.dao.CountryDAO;
import eu.orchestrator.repository.domain.Country;
import eu.orchestrator.repository.domain.ProviderType;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QCountry.country;

@Service
@Transactional(rollbackOn = Exception.class)
public class CountryService {

    private static final Logger logger = Logger.getLogger(CountryService.class.getName());

    @Autowired
    private CountryDAO countryDAO;


    public Country fetchCountryById(Long id) {
        Optional<Country> countryOptional = countryDAO.findById(id);
        return countryOptional.orElse(null);
    }

    public Country fetchCountryByName(String name) {
        Optional<Country> countryOptional = countryDAO.findByName(name);
        return countryOptional.orElse(null);
    }

    public Page<Country> fetchCountries(Pageable pageable, String filters, Country fCountry) {
        BooleanExpression predicate = country.eq(country);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                ProviderType filterProviderType = new Gson().fromJson(new String(Base64.decodeBase64(filters)), ProviderType.class);
                if (null != filterProviderType) {
                    proceedWithRequestBody = false;
                    if (null != filterProviderType.getName() && !filterProviderType.getName().isEmpty()) {
                        predicate = predicate.and(country.name.containsIgnoreCase(filterProviderType.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fCountry) {
                if (null != fCountry.getName() && !fCountry.getName().isEmpty()) {
                    predicate = predicate.and(country.name.containsIgnoreCase(fCountry.getName()));
                }
            }
        }
        Page<Country> page = countryDAO.findAll(predicate, pageable);
        return new TOConverter(Country.class.getName(), page, pageable).convertToTO();
    }
}
