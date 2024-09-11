package com.codealike.client.intellij;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.internal.dto.HealthInfo;
import com.codealike.client.core.internal.model.exception.IncompatibleVersionException;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.LogManager;
import com.codealike.client.intellij.ui.AuthenticationDialog;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.util.List;
import java.util.Properties;

/**
 * Plugin initialization on startup
 *
 * @author afomkina
 */
public class CodealikeLifecycleListener implements AppLifecycleListener {
    private static final LogManager LOG = LogManager.INSTANCE;
    private static final String PROPERTIES_PATH = "/codealike.properties";

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        start();
        LOG.logInfo("Codealike plugin is initialized.");
    }

    private void start() {
        Properties properties = loadPluginProperties();
        PluginContext pluginContext = PluginContext.getInstance(properties);

        try {
            pluginContext.initializeContext();
            if (!pluginContext.checkVersion()) {
                throw new IncompatibleVersionException("Incompatible version detected. The application failed to start.");
            }

            pluginContext.getIdentityService().addListener(this::reloadOpenedProjects);
            if (!pluginContext.getIdentityService().tryLoginWithStoredCredentials()) {
                authenticate();
            }
        } catch (Exception exception) {
            try {
                ApiClient client = ApiClient.tryCreateNew();
                client.logHealth(new HealthInfo(exception, "Plugin could not start.", "intellij", HealthInfo.HealthInfoType.Error, pluginContext.getIdentityService().getIdentity()));
            } catch (KeyManagementException keyManagementException) {
                LOG.logError(exception, "Couldn't send HealthInfo.");
            }
            LOG.logError(exception, "Couldn't start plugin.");
        }
    }

    private Properties loadPluginProperties() {
        Properties properties = new Properties();
        InputStream stream = CodealikeLifecycleListener.class.getResourceAsStream(PROPERTIES_PATH);
        try {
            properties.load(stream);
            if (stream != null) {
                stream.close();
            } else {
                LOG.logWarn("Properties is null");
            }
        } catch (IOException e) {
            LOG.logError("Couldn't get properties");
        }

        return properties;
    }

    private void authenticate() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Project project = ProjectManager.getInstance().getDefaultProject();
            AuthenticationDialog dialog = new AuthenticationDialog(project);
            dialog.show();
        });
    }

    private void reloadOpenedProjects() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            ProjectManager.getInstance().reloadProject(project);
        }
    }
}
