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

package org.onehippo.cms7.essentials.dashboard.utils;

import javax.jcr.Item;

import org.junit.Test;
import org.onehippo.cms7.essentials.BaseRepositoryTest;
import org.onehippo.cms7.essentials.dashboard.model.hst.HstConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @version "$Id$"
 */
public class JcrPersistenceWriterTest extends BaseRepositoryTest {

    private static Logger log = LoggerFactory.getLogger(JcrPersistenceWriterTest.class);

    @Test
    public void testWrite() throws Exception {
        JcrPersistenceWriter writer = new JcrPersistenceWriter(getContext());
        final HstConfiguration hstConfiguration = new HstConfiguration("mytestconfiguration", "/hst:hst/hst:configurations");
        Item config = writer.write(hstConfiguration);
        // no parent yet:
        assertTrue(config == null);
        session.getRootNode().addNode("hst:hst", "hst:hst").addNode("hst:configurations", "hst:configurations");
        session.save();
        // expect object to be saved:
        config = writer.write(hstConfiguration);
        assertNotNull("Expected saved object", config);
    }
}
