/*
* IBM Confidential
*
* OCO Source Materials
*
* Copyright IBM Corp. 2017 All Rights Reserved.
*
* The source code for this program is not published or otherwise divested
* of its trade secrets, irrespective of what has been deposited with the
* U.S. Copyright Office.
*/
package com.ibm.etools.maven.liberty.integration.ui.actions.internal;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;

import com.ibm.etools.liberty.integration.common.internal.ILibertyBuildPluginImpl;
import com.ibm.etools.liberty.integration.common.ui.actions.internal.AbstractGenerationAction;
import com.ibm.etools.maven.liberty.integration.internal.LibertyMaven;

public class GenerationAction extends AbstractGenerationAction {

    /**
     * @param selectionProvider
     * @param viewer
     */
    public GenerationAction(ISelectionProvider selectionProvider, StructuredViewer viewer) {
        super(selectionProvider, viewer);
    }

    /** {@inheritDoc} */
    @Override
    public ILibertyBuildPluginImpl getBuildPluginImpl() {
        return LibertyMaven.getInstance();
    }

//    private IProject mavenProject = null;
//
//    public GenerationAction(ISelectionProvider selectionProvider, StructuredViewer viewer) {
//        super(selectionProvider, Messages.createServerAction);
//        setImageDescriptor(null);
//        selectionChanged(getStructuredSelection());
//    }
//
//    @Override
//    public void selectionChanged(IStructuredSelection sel) {
//        if (sel.size() != 1) {
//            setEnabled(false);
//            return;
//        }
//
//        Object obj = sel.getFirstElement();
//        if (obj instanceof MavenNode) {
//            mavenProject = ((MavenNode) obj).getProject();
//        } else {
//            setEnabled(false);
//            return;
//        }
//
//        setEnabled(true);
//    }
//
//    @Override
//    public void run() {
//        if (mavenProject == null)
//            return;
//        LibertyManager.getInstance().triggerAddProject(mavenProject, null);
//    }
}
