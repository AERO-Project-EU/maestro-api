package eu.orchestrator.backend.rest.monitoring;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.Plugin;
import eu.orchestrator.backend.service.monitoring.MonitoringService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/plugin")
public class MonitoringController {

    private static final Logger logger = Logger.getLogger(MonitoringController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private MonitoringService monitoringService;


    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchPlugins(Pageable pageable, @RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) Plugin plugin, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(), GenericMessage.ENTITIES_FETCHED.getMessage(request),
                            (Serializable) monitoringService.fetchPlugins(filters, plugin, pageable, authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/list/organization")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchPluginsByOrganization(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) monitoringService.fetchPluginsByOrganization(
                    authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<Plugin>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.PLUGIN_FETCHED.getCode(),
                        GenericMessage.PLUGIN_FETCHED.getMessage(request), monitoringService.fetchById(id, authService.getAuthenticatedUser())));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
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

    @PostMapping
    public ResponseEntity<RestResponseSPA<Serializable>> create(@RequestBody Plugin plugin, HttpServletRequest request) {
        boolean requiredFields = null != plugin && null != plugin.getName() && !plugin.getName().isEmpty() && null != plugin.getPluginType()
                && !plugin.getPluginType().isEmpty() && null != plugin.getDefaultPlugin() && null != plugin.getImmutablePlugin()
                && null != plugin.getPublicPlugin() && null != plugin.getMetrics() && !plugin.getMetrics().isEmpty();
        if (requiredFields) {
            try {
                monitoringService.create(plugin, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
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

    @PutMapping
    public ResponseEntity<RestResponseSPA<Serializable>> update(@RequestBody Plugin plugin, HttpServletRequest request) {
        // Required Fields
        boolean requiredFields = null != plugin && null != plugin.getPluginID() && plugin.getPluginID() != 0 && null != plugin.getName()
                && !plugin.getName().isEmpty() && null != plugin.getPluginType() && !plugin.getPluginType().isEmpty() && null != plugin.getDefaultPlugin()
                && null != plugin.getImmutablePlugin() && null != plugin.getPublicPlugin() && null != plugin.getMetrics() && !plugin.getMetrics().isEmpty();
        if (requiredFields) {
            try {
                monitoringService.update(plugin, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
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

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<Serializable>> delete(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                monitoringService.delete(id, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.OK).build();
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
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

    /*@RequestMapping(method = RequestMethod.GET, value = "/list/organization")
  public ResponseEntity<RestResponseSPA> fetchOrganizationPlugins(
          Pageable pageable,
          @RequestParam(required = false, value = "filters") String filters,
          @RequestBody(required = false) Plugin fPlugin,
          HttpServletRequest request) {

    User authenticatedUser = null;

    try {

      authenticatedUser = authService.getAuthenticatedUser();

    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    if (null == authenticatedUser) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new RestResponseSPA(GenericMessage.NOT_AUTHORIZED.getCode(),
                      GenericMessage.NOT_AUTHORIZED.getMessage(request)));
    }

    BooleanExpression predicate = plugin.eq(plugin);

    boolean checkRequestParam = null != filters && !filters.isEmpty();
    boolean proceedWithRequestBody = true;

    if (checkRequestParam) {

      try {

        Plugin filterPlugin =
                new Gson().fromJson(new String(Base64.decodeBase64(filters)), Plugin.class);

        if (null != filterPlugin) {

          proceedWithRequestBody = false;

          if (null != filterPlugin.getName() && !filterPlugin.getName().isEmpty()) {
            predicate = predicate
                    .and(plugin.name.containsIgnoreCase(filterPlugin.getName())
                            .or(plugin.moduleName.containsIgnoreCase(filterPlugin.getName())));
          }

          if (null != filterPlugin.getDefaultPlugin()) {
            predicate = predicate
                    .and(plugin.defaultPlugin.eq(filterPlugin.getDefaultPlugin()));
          }

          if (null != filterPlugin.getImmutablePlugin()) {
            predicate = predicate
                    .and(plugin.immutablePlugin.eq(filterPlugin.getImmutablePlugin()));
          }

          if (null != filterPlugin.getPublicPlugin()) {
            predicate = predicate
                    .and(plugin.publicPlugin.eq(filterPlugin.getPublicPlugin()));
          }

        }

      } catch (Exception e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
      }

    }

    if (proceedWithRequestBody) {

      if (null != fPlugin) {

        if (null != fPlugin.getName() && !fPlugin.getName().isEmpty()) {
          predicate = predicate
                  .and(plugin.name.containsIgnoreCase(fPlugin.getName())
                          .or(plugin.moduleName.containsIgnoreCase(fPlugin.getName())));
        }

        if (null != fPlugin.getDefaultPlugin()) {
          predicate = predicate
                  .and(plugin.defaultPlugin.eq(fPlugin.getDefaultPlugin()));
        }

        if (null != fPlugin.getImmutablePlugin()) {
          predicate = predicate
                  .and(plugin.immutablePlugin.eq(fPlugin.getImmutablePlugin()));
        }

        if (null != fPlugin.getPublicPlugin()) {
          predicate = predicate
                  .and(plugin.publicPlugin.eq(fPlugin.getPublicPlugin()));
        }

      }

    }


    predicate = predicate.and(plugin.organization.eq(authenticatedUser.getOrganization())
            .and(plugin.defaultPlugin.eq(false))
            .and(plugin.immutablePlugin.eq(false)));

    Page<Plugin> page = null;

    if (pageable.getPageSize() > 100) {
      page = pluginDAO
              .findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
    } else {
      page = pluginDAO.findAll(predicate, pageable);
    }

    List<PluginTO> pluginTOs = new ArrayList<>();

    if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {

      User finalAuthenticatedUser = authenticatedUser;
      page.getContent().stream().forEach(plugin -> {

        PluginTO pluginTO = new PluginTO();
        pluginTO.setPluginID(plugin.getPluginID());
        pluginTO.setDateCreated(plugin.getDateCreated());
        pluginTO.setLastModified(plugin.getLastModified());
        pluginTO.setName(plugin.getName());
        pluginTO.setModuleName(null != plugin.getModuleName() ? plugin.getModuleName() : null);
        pluginTO.setMetricsCounter(plugin.getMetricIDs().size());
        pluginTO.setDefaultPlugin(plugin.getDefaultPlugin());
        pluginTO.setImmutablePlugin(plugin.getImmutablePlugin());
        pluginTO.setPublicPlugin(plugin.getPublicPlugin());
        pluginTO.setOrganizationName(plugin.getOrganization().getName());
        pluginTO.setAllowEdit(plugin.hasEditAllowance(finalAuthenticatedUser));
        pluginTO.setAllowDelete(plugin.hasDeleteAllowance(finalAuthenticatedUser));
        pluginTOs.add(pluginTO);

      });

    }

    return ResponseEntity.status(HttpStatus.OK)
            .body(new RestResponseSPA(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    new PageImpl<>(pluginTOs, pageable, page.getTotalElements())));
  }*/

}
