/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.st.ui.internal.marker;

import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.osgi.util.NLS;

import com.ibm.ws.st.core.internal.config.ConfigurationFile;
import com.ibm.ws.st.core.internal.config.validation.AbstractConfigurationValidator;
import com.ibm.ws.st.ui.internal.Messages;
import com.ibm.ws.st.ui.internal.Trace;

/**
 * Quick fix for unrecognized element name - best match
 */
public class QuickFixDuplicateFactoryRef extends AbstractMarkerResolution {
    private final String itemName;
    private final int itemIndex;

    public QuickFixDuplicateFactoryRef(String itemName, int itemIndex) {
        this.itemName = itemName;
        this.itemIndex = itemIndex;
    }

    @Override
    public String getLabel() {
        return NLS.bind(Messages.duplicateFactoryRefQuickFix, new String[] { itemName });
    }

    @Override
    public void run(IMarker marker) {
        String xpath = marker.getAttribute(AbstractConfigurationValidator.XPATH_ATTR, "");
        IResource resource = getResource(marker);
        if (resource == null)
            return;

        final ConfigurationFile configFile = getConfigFile(resource);
        if (configFile == null)
            return;

        if (configFile.removeDupFactoryRef(xpath, itemName, itemIndex)) {
            try {
                configFile.save(null);
            } catch (IOException e) {
                if (Trace.ENABLED)
                    Trace.trace(Trace.ERROR, "Quick fix for duplicate factory ref failed. Error trying to save configuration file: " + configFile.getURI(), e);
                showErrorMessage();
            }
        } else {
            if (Trace.ENABLED) {
                Trace.trace(Trace.ERROR,
                            "Quick fix for duplicate factory ref failed. Error trying to remove duplicate factory references from configuration: " + configFile.getURI(), null);
            }
            showErrorMessage();
        }
    }

    @Override
    protected String getErrorMessage() {
        return Messages.removeDupFactoryRefFailedMessage;
    }
}
