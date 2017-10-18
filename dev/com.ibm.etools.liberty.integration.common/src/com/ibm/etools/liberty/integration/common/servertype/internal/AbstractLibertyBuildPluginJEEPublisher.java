/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2017 All Rights Reserved
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 */

package com.ibm.etools.liberty.integration.common.servertype.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.ibm.etools.liberty.integration.common.internal.Activator;
import com.ibm.etools.liberty.integration.common.internal.ConfigurationType;
import com.ibm.etools.liberty.integration.common.internal.ILibertyBuildPluginImpl;
import com.ibm.etools.liberty.integration.common.internal.ILibertyBuildPluginImplProvider;
import com.ibm.etools.liberty.integration.common.internal.LibertyBuildPluginConfiguration;
import com.ibm.etools.liberty.integration.common.internal.Trace;
import com.ibm.etools.liberty.integration.common.manager.internal.AbstractLibertyProjectMapping;
import com.ibm.etools.liberty.integration.common.manager.internal.AbstractLibertyProjectMapping.ProjectMapping;
import com.ibm.ws.st.core.internal.Constants;
import com.ibm.ws.st.core.internal.ExcludeSyncModuleInfo;
import com.ibm.ws.st.core.internal.PublishUnit;
import com.ibm.ws.st.core.internal.WebSphereServer;
import com.ibm.ws.st.core.internal.WebSphereServerBehaviour;
import com.ibm.ws.st.jee.core.internal.JEEPublisher;

/**
 * Liberty Maven publishing implementation
 */
@SuppressWarnings("restriction")
public abstract class AbstractLibertyBuildPluginJEEPublisher extends JEEPublisher implements ILibertyBuildPluginImplProvider {

    ILibertyBuildPluginImpl buildPluginHelper = getBuildPluginImpl();
    AbstractLibertyProjectMapping mappingHandler = getBuildPluginImpl().getMappingHandler();

    @Override
    public void postPublishApplication(int kind, PublishUnit app, MultiStatus status, IProgressMonitor monitor) {
        WebSphereServer wsServer = getWebSphereServer();

        if (wsServer != null) {
            String type = wsServer.getServerType();
            if (Constants.SERVER_TYPE_LIBERTY_MAVEN.equals(type)) {
                super.postPublishApplication(kind, app, status, monitor);
            }
        }
    }

    @Override
    public void handleLooseConfigModeChange(int kind, PublishUnit pu, MultiStatus multi, IProgressMonitor monitor) {
        // Do nothing. At this point, the server would already be in a bad state with two applications
        // added to it. The cleanup of the old loose config files is done in
        // com.ibm.etools.liberty.integration.common.manager.internal.LibertyManager.ScanJob.handleProjectChanged(IProject, IProgressMonitor)
        // Also, do not call super.handleLooseConfigModeChange, as that will delete files
    }

    /**
     * Return the path to the published module. The path is built from the Liberty
     * Maven Configuration.
     *
     * @param config
     * @return the path if the serverDir, appsDir, and appName attributes exist in the file
     *         (it would not exist if the user manually modifies the file). Return empty string if these
     *         attributes are missing
     */
    protected String getPathToPublishedModule(LibertyBuildPluginConfiguration config) {
        String serverDir = config.getConfigValue(ConfigurationType.serverDirectory);
        String appsDir = config.getConfigValue(ConfigurationType.appsDirectory);
        String appName = config.getConfigValue(ConfigurationType.applicationFilename);

        Trace.trace(Trace.INFO, "serverDir: " + serverDir + " appsDir:" + appsDir + " appName:" + appName);

        String pathToPublishedModule = "";
        if (serverDir != null && appsDir != null && appName != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(serverDir);
            buffer.append("/");
            buffer.append(appsDir);
            buffer.append("/");
            buffer.append(appName);
            pathToPublishedModule = buffer.toString();
        }
        return pathToPublishedModule;
    }

    @Override
    public void prePublishApplication(int kind, PublishUnit app, MultiStatus status, IProgressMonitor monitor) {
        WebSphereServer wsServer = getWebSphereServer();

        if (wsServer != null) {
            String type = wsServer.getServerType();
            if (Constants.SERVER_TYPE_LIBERTY_MAVEN.equals(type)) {
                super.prePublishApplication(kind, app, status, monitor);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.st.core.internal.ApplicationPublisher#getModuleDeployName(org.eclipse.wst.server.core.IModule)
     */
    @Override
    protected String getModuleDeployName(IModule module) {
        IProject moduleProject = module.getProject();
        if (moduleProject != null) {
            LibertyBuildPluginConfiguration config = buildPluginHelper.getLibertyBuildPluginConfiguration(moduleProject, new NullProgressMonitor());
            if (config != null) {
                String deployName = config.getConfigValue(ConfigurationType.applicationFilename);
                if (deployName != null && !deployName.isEmpty()) {
                    if (deployName.endsWith(".xml"))
                        deployName = deployName.substring(0, deployName.length() - 4);
                    return deployName;
                }
            }
        }
        return super.getModuleDeployName(module);
    }

    @Override
    public boolean requireConsoleOutputBeforePublishComplete(int kind, PublishUnit unit, MultiStatus status, IProgressMonitor monitor) {
        int kind2 = unit.getDeltaKind();
        // On the initial modify modules on Liberty Maven server setup, the getAddedResourceList will be empty
        // but the type will be added. We do not want the console to be waiting for a publish that will never happen
        // (since it already happened)
        if ((kind2 == ServerBehaviourDelegate.ADDED && !getAddedResourceList().isEmpty())
            || (kind2 == ServerBehaviourDelegate.REMOVED)) {
            return true;
        }
        if (getAddedResourceList().isEmpty() && getRemovedResourceList().isEmpty() && getChangedResourceList().isEmpty())
            return false;

        if (checkFileExtension(getAddedResourceList())
            || checkFileExtension(getChangedResourceList())
            || checkFileExtension(getRemovedResourceList())) {
            return true;
        }

        return false;
    }

    @Override
    protected IPath getAppsPathOverride() {

        WebSphereServer wsServer = getWebSphereServer();

        if (wsServer != null) {
            IProject moduleProject = mappingHandler.getMappedProject(wsServer.getServer());

            if (moduleProject != null) {

                ProjectMapping mapping = mappingHandler.getMapping(moduleProject.getName());

                if (mapping != null && mapping.getServerID().equals(wsServer.getServer().getId())) {

                    LibertyBuildPluginConfiguration config = buildPluginHelper.getLibertyBuildPluginConfiguration(moduleProject, new NullProgressMonitor());

                    String serverDir = config.getConfigValue(ConfigurationType.serverDirectory);
                    String appsDir = config.getConfigValue(ConfigurationType.appsDirectory);

                    Trace.trace(Trace.INFO, "serverDir: " + serverDir + " appsDir:" + appsDir);

                    if (serverDir != null && appsDir != null) {
                        IPath returnPath = new Path(serverDir);
                        returnPath = returnPath.append(appsDir);
                        return returnPath;
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected void removePublishedAppFiles(PublishUnit app, boolean looseCfg, MultiStatus multi, IProgressMonitor monitor) {
        WebSphereServer wsServer = getWebSphereServer();
        WebSphereServerBehaviour wsBehaviour = getWebSphereServerBehaviour();
        if (wsServer == null || wsBehaviour == null)
            return;

        IModule[] appModule = app.getModule();
        if (appModule != null && appModule.length > 0) {
            HashMap<IModule, ExcludeSyncModuleInfo> map = wsBehaviour.getExcludeSyncModules();
            if (map != null) {
                ExcludeSyncModuleInfo info = map.get(appModule[0]);
                if (info != null) {
                    HashMap<String, String> props = info.getProperties();
                    if (props != null) {
                        String configDropins = props.get(ExcludeSyncModuleInfo.INSTALL_APPS_CONFIG_DROPINS);

                        boolean failedToRemove = false;

                        if (configDropins != null) {
                            File toDelete = new File(configDropins);
                            if (toDelete.exists()) {
                                if (!toDelete.delete()) {
                                    Trace.trace(Trace.WARNING, "Could not delete config dropins file: " + toDelete.toString());
                                } else {
                                    getRemovedResourceList().add(configDropins);
                                    try {
                                        notifyUpdatedApplicationResourcesViaJMX();
                                    } catch (Exception e) {
                                        Trace.logError("Failed to notify app update via jmx", e);
                                        failedToRemove = true;
                                    }
                                }
                            }
                        }

                        // Remove the non-loose config application file
                        if (!wsServer.isLooseConfigEnabled()) {
                            String fullAppPath = props.get(ExcludeSyncModuleInfo.FULL_APP_PATH);
                            if (fullAppPath != null) {
                                File toDelete = new File(fullAppPath);
                                if (toDelete.exists()) {
                                    if (!toDelete.delete()) {
                                        Trace.trace(Trace.WARNING, "Could not delete application file: " + toDelete.toString());
                                        failedToRemove = true;
                                    }
                                }
                            }
                        }

                        // Provide error handling for failing to remove
                        if (failedToRemove) {
                            List<IStatus> status = new ArrayList<IStatus>();
                            status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(com.ibm.ws.st.core.internal.Messages.errorPublishModule,
                                                                                               app.getModule()[0].getName())));
                            multi.add(combineModulePublishStatus(status, app.getModule()[0].getName()));
                        }
                    }

                    // Cleanup the map post remove
                    map.remove(appModule[0]);
                } // end info not null
            }
        }
    }
}