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
package com.ibm.etools.maven.liberty.integration.ui.rtexplorer.internal;

import com.ibm.etools.liberty.integration.common.internal.ILibertyBuildPluginImpl;
import com.ibm.etools.liberty.integration.common.ui.rtexplorer.internal.AbstractLibertyBuildPluginRuntimeContentProvider;
import com.ibm.etools.maven.liberty.integration.internal.LibertyMaven;

public class MavenRuntimeContentProvider extends AbstractLibertyBuildPluginRuntimeContentProvider {

    /** {@inheritDoc} */
    @Override
    public ILibertyBuildPluginImpl getBuildPluginImpl() {
        return LibertyMaven.getInstance();
    }

}
