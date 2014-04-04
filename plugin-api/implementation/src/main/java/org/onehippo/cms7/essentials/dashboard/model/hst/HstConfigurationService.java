/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.dashboard.model.hst;

import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.utils.JcrPersistenceWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version "$Id$"
 */
public class HstConfigurationService {


    private static final Logger log = LoggerFactory.getLogger(HstConfigurationService.class);
    private final HstConfiguration config;
    private final PluginContext context;

    public HstConfigurationService(final String name, final PluginContext context) {
        this.config = new HstConfiguration(name, "/hst:hst/hst:configurations");
        this.context = context;
    }


    public HstConfiguration build(final PluginContext context) {
        try (JcrPersistenceWriter writer = new JcrPersistenceWriter(context.createSession(), this.context)) {
            writer.write(config);
        } catch (Exception e) {
            log.error("Error ", e);
        }
        return config;
    }

}
