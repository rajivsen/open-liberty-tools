/*
* IBM Confidential
*
* OCO Source Materials
*
* (C) Copyright IBM Corp. 2017 All Rights Reserved.
*
* The source code for this program is not published or otherwise divested
* of its trade secrets, irrespective of what has been deposited with the
* U.S. Copyright Office.
*/
package com.ibm.etools.maven.liberty.integration.ui.actions.internal;

import com.ibm.etools.liberty.integration.common.internal.ILibertyBuildPluginImpl;
import com.ibm.etools.liberty.integration.common.ui.actions.internal.AbstractCreateRuntimeServerProjectMenuAction;
import com.ibm.etools.maven.liberty.integration.internal.LibertyMaven;

public class CreateRuntimeServerProjectMenuAction extends AbstractCreateRuntimeServerProjectMenuAction {

    /** {@inheritDoc} */
    @Override
    public ILibertyBuildPluginImpl getBuildPluginImpl() {
        return LibertyMaven.getInstance();
    }

}
