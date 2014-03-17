/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms7.essentials.dashboard.config;


import java.util.Calendar;

import org.onehippo.cms7.essentials.dashboard.utils.annotations.PersistentNode;
import org.onehippo.cms7.essentials.dashboard.utils.annotations.PersistentProperty;

/**
 * @version "$Id$"
 */

@PersistentNode(type = "essentials:document")
public class InstallerDocument extends BaseDocument {


    @PersistentProperty(name = "pluginId")
    private String pluginId;

    @PersistentProperty(name = "dateInstalled")
    private Calendar dateInstalled;


    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(final String pluginId) {
        this.pluginId = pluginId;
    }

    public Calendar getDateInstalled() {
        return dateInstalled;
    }

    public void setDateInstalled(final Calendar dateInstalled) {
        this.dateInstalled = dateInstalled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InstallerDocument{");
        sb.append("pluginId='").append(pluginId).append('\'');
        sb.append(", dateInstalled=").append(dateInstalled);
        sb.append('}');
        return sb.toString();
    }
}
