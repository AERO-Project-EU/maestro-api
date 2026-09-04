package eu.orchestrator.backend.service.monitoring;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.component.ComponentNodeService;
import eu.orchestrator.backend.transfer.PluginTO;
import eu.orchestrator.repository.dao.MetricDAO;
import eu.orchestrator.repository.dao.PluginDAO;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.Metric;
import eu.orchestrator.repository.domain.Plugin;
import eu.orchestrator.repository.domain.Plugin.PluginType;
import eu.orchestrator.repository.domain.User;

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
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QPlugin.plugin;

@Service
@Transactional(rollbackOn = Exception.class)
public class MonitoringService {

    private static final Logger logger = Logger.getLogger(MonitoringService.class.getName());

    @Autowired
    private ComponentNodeService componentNodeService;

    @Autowired
    private PluginDAO pluginDAO;

    @Autowired
    private MetricDAO metricDAO;


    public Page fetchPlugins(String filters, Plugin fPlugin, Pageable pageable, User authenticatedUser) {
        BooleanExpression predicate = plugin.eq(plugin);

        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;

        if (checkRequestParam) {
            try {
                Plugin filterPlugin = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Plugin.class);
                if (null != filterPlugin) {
                    proceedWithRequestBody = false;
                    if (null != filterPlugin.getName() && !filterPlugin.getName().isEmpty()) {
                        predicate = predicate.and(plugin.name.containsIgnoreCase(filterPlugin.getName())
                                .or(plugin.moduleName.containsIgnoreCase(filterPlugin.getName())));
                    }
                    if (null != filterPlugin.getDefaultPlugin()) {
                        predicate = predicate.and(plugin.defaultPlugin.eq(filterPlugin.getDefaultPlugin()));
                    }
                    if (null != filterPlugin.getImmutablePlugin()) {
                        predicate = predicate.and(plugin.immutablePlugin.eq(filterPlugin.getImmutablePlugin()));
                    }
                    if (null != filterPlugin.getPublicPlugin()) {
                        predicate = predicate.and(plugin.publicPlugin.eq(filterPlugin.getPublicPlugin()));
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
                    predicate = predicate.and(plugin.defaultPlugin.eq(fPlugin.getDefaultPlugin()));
                }
                if (null != fPlugin.getImmutablePlugin()) {
                    predicate = predicate.and(plugin.immutablePlugin.eq(fPlugin.getImmutablePlugin()));
                }
                if (null != fPlugin.getPublicPlugin()) {
                    predicate = predicate.and(plugin.publicPlugin.eq(fPlugin.getPublicPlugin()));
                }
            }
        }

        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(plugin.user.eq(authenticatedUser).or(plugin.publicPlugin.eq(true)).
                    or(plugin.immutablePlugin.eq(true)).or(plugin.organization.eq(authenticatedUser.getOrganization())));
        }

        Page<Plugin> page;
        if (pageable.getPageSize() > 100) {
            page = pluginDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = pluginDAO.findAll(predicate, pageable);
        }

        List<PluginTO> pluginTOs = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            User finalAuthenticatedUser = authenticatedUser;
            page.getContent().forEach(plugin -> {
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
//        pluginTO.setOrganizationName(plugin.getOrganization().getName()); // todo - need fix
                pluginTO.setAllowEdit(plugin.hasEditAllowance(finalAuthenticatedUser));
                pluginTO.setAllowDelete(plugin.hasDeleteAllowance(finalAuthenticatedUser));
                pluginTO.setOrganization(plugin.getOrganization().getName());
                pluginTOs.add(pluginTO);
            });
        }
        return new PageImpl<>(pluginTOs, pageable, page.getTotalElements());
    }

    public List<PluginTO> fetchPluginsByOrganization(User authenticatedUser) {
        List<PluginTO> pluginTOs = new ArrayList<>();

        if (authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())) {
            pluginDAO.findAllByDefaultPlugin(false).forEach(plugin -> {
                PluginTO pluginTO = new PluginTO();
                pluginTO.setPluginID(plugin.getPluginID());
                pluginTO.setName(plugin.getName());
                pluginTO.setModuleName(plugin.getModuleName());
                pluginTOs.add(pluginTO);
            });
        } else {
            BooleanExpression predicate = plugin.eq(plugin);
            predicate = predicate.and(plugin.user.eq(authenticatedUser));
            if (null != authenticatedUser.getOrganization()) {
                predicate = predicate.or(plugin.organization.eq(authenticatedUser.getOrganization()));
            }
            pluginDAO.findAll(predicate).iterator().forEachRemaining(plugin -> {
                PluginTO pluginTO = new PluginTO();
                pluginTO.setPluginID(plugin.getPluginID());
                pluginTO.setName(plugin.getName());
                pluginTO.setModuleName(plugin.getModuleName());
                pluginTOs.add(pluginTO);
            });
        }
        return pluginTOs;
    }

    public Plugin fetchById(Long id, User authenticatedUser) {
        Optional<Plugin> pluginOP = pluginDAO.findById(id);
        if (pluginOP.isPresent() && pluginOP.get().hasEditAllowance(authenticatedUser)) {
            List<Metric> metrics = metricDAO.findAllByPluginOrderByDateCreatedDesc(pluginOP.get());
            pluginOP.get().setMetrics(metrics);
            return pluginOP.get();
        } else {
            throw new NotAuthorizedException(GenericMessage.PLUGIN_FETCH_NOT_ALLOWED.getCode(), GenericMessage.PLUGIN_FETCH_NOT_ALLOWED);
        }
    }

    public void create(Plugin plugin, User authenticatedUser) {
        if (Boolean.TRUE.equals(plugin.getImmutablePlugin())) {
            plugin.setPublicPlugin(Boolean.TRUE);
        }

        if (authenticatedUser.getOrganization() == null) {
            throw new GenericBusinessException(GenericMessage.NO_ORGANIZATION.getCode(), GenericMessage.NO_ORGANIZATION);
        }

        if (Boolean.TRUE.equals(plugin.getImmutablePlugin()) && !authenticatedUser.isAdmin()) {
            throw new GenericBusinessException(GenericMessage.IMMUTABLE_PLUGIN_NOT_AUTHORIZED.getCode(), GenericMessage.IMMUTABLE_PLUGIN_NOT_AUTHORIZED);
        }

        if (Boolean.TRUE.equals(plugin.getPublicPlugin()) && !authenticatedUser.isAdmin()) {
            throw new GenericBusinessException(GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED);
        }

        if (Boolean.TRUE.equals(plugin.getDefaultPlugin()) && (!authenticatedUser.isOrganizationAdmin()
                && !authenticatedUser.isAdmin())) {
            throw new GenericBusinessException(GenericMessage.DEFAULT_PLUGIN_NOT_AUTHORIZED.getCode(), GenericMessage.DEFAULT_PLUGIN_NOT_AUTHORIZED);
        }

        // Check if Plugin already exists
        if (null != plugin.getModuleName() && !plugin.getModuleName().isEmpty()) {
            if (authenticatedUser.isAdmin()) {
                if (pluginDAO.findByNameAndModuleName(plugin.getName(), plugin.getModuleName()).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                }
            } else {
                if (pluginDAO.findByNameAndModuleNameAndOrganization(plugin.getName(),
                        plugin.getModuleName(), authenticatedUser.getOrganization()).isPresent()
                        || pluginDAO.findByNameAndModuleNameAndPublicPlugin(plugin.getName(), plugin.getModuleName(), true).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                }
            }
        } else {
            if (authenticatedUser.isAdmin()) {
                if (pluginDAO.findByName(plugin.getName()).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                }
            } else {
                if (pluginDAO.findByNameAndOrganization(plugin.getName(), authenticatedUser.getOrganization()).isPresent()
                        || pluginDAO.findByNameAndPublicPlugin(plugin.getName(), true).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                }
            }
        }

        if (plugin.getPluginType().equals(PluginType.DOWNLOAD_CONF.name())) {
            if (null == plugin.getEndpoint() || plugin.getEndpoint().isEmpty()) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        } else if (plugin.getPluginType().equals(PluginType.HTTP.name())) {
            if ((null == plugin.getEndpoint() || plugin.getEndpoint().isEmpty()) && (null == plugin.getPort() || plugin.getPort().isEmpty())) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        } else if (plugin.getPluginType().equals(PluginType.SOCKET.name())) {
            if (null == plugin.getPort() || plugin.getPort().isEmpty()) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        }

        plugin.setUser(authenticatedUser);
        plugin.setOrganization(authenticatedUser.getOrganization());
        plugin.setDateCreated(new Date());
        plugin.setLastModified(new Date());
        List<Metric> metrics = plugin.getMetrics();
        plugin.setMetrics(null);

        pluginDAO.save(plugin);

        metrics.forEach(metric -> {
            if (null != metric.getFriendlyName() && !metric.getFriendlyName().isEmpty() && null != metric.getName() && !metric.getName().isEmpty()
                    && null != metric.getUnit() && !metric.getUnit().isEmpty()) {
                Metric mT = new Metric();
                mT.setDateCreated(new Date());
                mT.setLastModified(new Date());
                mT.setFriendlyName(metric.getFriendlyName());
                mT.setName(metric.getName());
                mT.setUnit(metric.getUnit());
                mT.setPlugin(plugin);
                metricDAO.save(mT);

            } else {
                logger.log(Level.SEVERE, "Missing Required Fields");
            }
        });
    }

    public void update(Plugin plugin, User authenticatedUser) {
        if (Boolean.TRUE.equals(plugin.getImmutablePlugin())) {
            plugin.setPublicPlugin(true);
        }

        if (authenticatedUser.getOrganization() == null) {
            throw new GenericBusinessException(GenericMessage.NO_ORGANIZATION.getCode(), GenericMessage.NO_ORGANIZATION);
        }

        if (Boolean.TRUE.equals(plugin.getDefaultPlugin()) && (!authenticatedUser.isOrganizationAdmin() && !authenticatedUser.isAdmin())) {
            throw new GenericBusinessException(GenericMessage.DEFAULT_PLUGIN_NOT_AUTHORIZED.getCode(), GenericMessage.DEFAULT_PLUGIN_NOT_AUTHORIZED);
        }

        // Check if Plugin already exists or not
        Optional<Plugin> existingPluginOP = pluginDAO.findById(plugin.getPluginID());

        if (existingPluginOP.isPresent() && Boolean.TRUE.equals(existingPluginOP.get().hasEditAllowance(authenticatedUser))) {

            Plugin existingPlugin = existingPluginOP.get();

            if (!existingPlugin.getName().equals(plugin.getName()) ||
                    (existingPlugin.getModuleName() != null && plugin.getModuleName() == null) ||
                    (existingPlugin.getModuleName() == null && plugin.getModuleName() != null) ||
                    (existingPlugin.getModuleName() != null && plugin.getModuleName() != null &&
                            !existingPlugin.getModuleName().equals(plugin.getModuleName()))) {

                // Check if Plugin already exists
                if (null != plugin.getModuleName() && !plugin.getModuleName().isEmpty()) {
                    if (authenticatedUser.isAdmin()) {
                        if (pluginDAO.findByNameAndModuleName(plugin.getName(), plugin.getModuleName()).isPresent()) {
                            throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                        }
                    } else {
                        if (pluginDAO.findByNameAndModuleNameAndOrganization(plugin.getName(), plugin.getModuleName(),
                                authenticatedUser.getOrganization()).isPresent()
                                || pluginDAO.findByNameAndModuleNameAndPublicPlugin(plugin.getName(),
                                plugin.getModuleName(), true).isPresent()) {
                            throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                        }
                    }
                } else {
                    if (authenticatedUser.isAdmin()) {
                        if (pluginDAO.findByName(plugin.getName()).isPresent()) {
                            throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                        }
                    } else {
                        if (pluginDAO.findByNameAndOrganization(plugin.getName(), authenticatedUser.getOrganization()).isPresent()
                                || pluginDAO.findByNameAndPublicPlugin(plugin.getName(), true).isPresent()) {
                            throw new GenericBusinessException(GenericMessage.PLUGIN_ALREADY_EXISTS.getCode(), GenericMessage.PLUGIN_ALREADY_EXISTS);
                        }
                    }
                }
            }

            //check if isImmutable
            if (!existingPlugin.getImmutablePlugin().equals(plugin.getImmutablePlugin())) {
                if (authenticatedUser.isAdmin() && existingPlugin.getOrganization().getName().equals("Admin_Organization")) {
                    existingPlugin.setImmutablePlugin(plugin.getImmutablePlugin());
                } else {
                    throw new GenericBusinessException(GenericMessage.IMMUTABLE_PLUGIN_NOT_AUTHORIZED.getCode(),
                            GenericMessage.IMMUTABLE_PLUGIN_NOT_AUTHORIZED);
                }
            }

            //check if isPublic
            if (!existingPlugin.getPublicPlugin().equals(plugin.getPublicPlugin())) {
                if (authenticatedUser.isAdmin() && existingPlugin.getOrganization().getName().equals("Admin_Organization")) {
                    existingPlugin.setPublicPlugin(plugin.getPublicPlugin());
                } else {
                    throw new GenericBusinessException(GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED);
                }
            }

            //check if isPublic
            if (!existingPlugin.getDefaultPlugin().equals(plugin.getDefaultPlugin())) {
                if ((authenticatedUser.isOrganizationAdmin() || authenticatedUser.isAdmin())
                        && existingPlugin.getOrganization().equals(authenticatedUser.getOrganization())) {
                    existingPlugin.setDefaultPlugin(plugin.getDefaultPlugin());
                } else {
                    throw new GenericBusinessException(GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED);
                }
            }

            if (plugin.getPluginType().equals(PluginType.DOWNLOAD_CONF.name())) {
                if (null == plugin.getEndpoint() || plugin.getEndpoint().isEmpty()) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }

                existingPlugin.setEndpoint(plugin.getEndpoint());

            } else if (plugin.getPluginType().equals(PluginType.HTTP.name())) {
                if ((null == plugin.getEndpoint() || plugin.getEndpoint().isEmpty()) && (null == plugin.getPort() || plugin.getPort().isEmpty())) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }
                existingPlugin.setEndpoint(null != plugin.getEndpoint() && !plugin.getEndpoint().isEmpty() ? plugin.getEndpoint() : null);
                existingPlugin.setPort(null != plugin.getPort() && !plugin.getPort().isEmpty() ? plugin.getPort() : null);
            } else if (plugin.getPluginType().equals(PluginType.SOCKET.name())) {
                if (null == plugin.getPort() || plugin.getPort().isEmpty()) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }
                existingPlugin.setPort(plugin.getPort());
            }

            existingPlugin.setName(plugin.getName());
            existingPlugin.setModuleName(plugin.getModuleName());
            existingPlugin.setPluginType(plugin.getPluginType());
            existingPlugin.setDownloadURL(null != plugin.getDownloadURL() && !plugin.getDownloadURL().isEmpty() ? plugin.getDownloadURL() : null);
            existingPlugin.setLastModified(new Date());
            pluginDAO.save(existingPlugin);

            List<Long> existingMetricIDs = existingPlugin.getMetricIDs();
            List<Metric> newMetrics = plugin.getMetrics();

            if (null != newMetrics && !newMetrics.isEmpty()) {
                newMetrics.forEach(newMetric -> {
                    if (null != newMetric.getMetricID() && newMetric.getMetricID() != 0) {
                        // Existing, needs to be updated
                        Optional<Metric> exMetricOP = metricDAO.findById(newMetric.getMetricID());
                        if (exMetricOP.isPresent()) {
                            Metric exMetric = exMetricOP.get();
                            exMetric.setName(newMetric.getName());
                            exMetric.setFriendlyName(newMetric.getFriendlyName());
                            exMetric.setUnit(newMetric.getUnit());
                            exMetric.setLastModified(new Date());
                            metricDAO.save(exMetric);
                            existingMetricIDs.remove(newMetric.getMetricID());
                        }
                    } else {
                        // New, needs to be added
                        newMetric.setLastModified(new Date());
                        newMetric.setDateCreated(new Date());
                        newMetric.setPlugin(existingPlugin);
                        metricDAO.save(newMetric);
                    }
                });
            }
            if (!existingMetricIDs.isEmpty()) {
                existingMetricIDs.forEach(existingMetricID -> {
                    metricDAO.deleteById(existingMetricID);
                });
            }
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void delete(Long id, User authenticatedUser) {
        // Check if Plugin exists or not
        Optional<Plugin> existingPluginOP = pluginDAO.findById(id);
        if (existingPluginOP.isPresent() && Boolean.TRUE.equals(existingPluginOP.get().hasDeleteAllowance(authenticatedUser))) {
            Plugin existingPlugin = existingPluginOP.get();
            // Check if plugin is the default one
            if (Boolean.TRUE.equals(existingPlugin.getDefaultPlugin())) {
                throw new GenericBusinessException(GenericMessage.DEFAULT_PLUGIN.getCode(), GenericMessage.DEFAULT_PLUGIN);
            }
            // Check if Plugin is used
            List<Component> components = componentNodeService.fetchAllByUserOrPublicComponentOrderByNameAsc(authenticatedUser);
            if (null != components && !components.isEmpty()) {
                for (Component component : components) {
                    if (null != component.getPlugins() && !component.getPlugins().isEmpty() && !component.getPlugins().stream()
                            .filter(tPlugin -> tPlugin.getPluginID().equals(existingPlugin.getPluginID())).collect(Collectors.toList()).isEmpty()) {
                        throw new GenericBusinessException(GenericMessage.PLUGIN_USED_IN_COMPONENTS.getCode(), GenericMessage.PLUGIN_USED_IN_COMPONENTS);
                    }
                }
            }
            if (!authenticatedUser.isAdmin()
                    && (Boolean.TRUE.equals(existingPlugin.getDefaultPlugin()) || Boolean.TRUE.equals(existingPlugin.getImmutablePlugin()))) {
                throw new GenericBusinessException(GenericMessage.DEFAULT_PLUGIN.getCode(), GenericMessage.DEFAULT_PLUGIN);
            }
            pluginDAO.delete(existingPlugin);
        } else {
            throw new GenericBusinessException(GenericMessage.NOT_AUTHORIZED.getCode(), GenericMessage.NOT_AUTHORIZED);
        }
    }
}
