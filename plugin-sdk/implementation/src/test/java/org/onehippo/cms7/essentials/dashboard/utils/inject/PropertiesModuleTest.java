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

package org.onehippo.cms7.essentials.dashboard.utils.inject;


import javax.inject.Inject;

import org.junit.Test;
import org.onehippo.cms7.essentials.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version "$Id$"
 */
public class PropertiesModuleTest extends BaseTest {

    private static Logger log = LoggerFactory.getLogger(PropertiesModuleTest.class);

    @Inject
    PropertyTestObj instance;

    @Test
    public void testConfigure() throws Exception {
        assertTrue(!Strings.isNullOrEmpty(instance.getValue()));
        assertTrue(!Strings.isNullOrEmpty(instance.getName()));
        log.info("instance {}", instance);
        assertEquals("Copied file from: {{source}} to: {{target}}", instance.getName());
        assertEquals("Deleted file: {{target}}", instance.getValue());


    }

    /**
     * @version "$Id$"
     */
    @Component
    public static class PropertyTestObj {


        @Value("${instruction.message.file.delete}")
        private String value;


        @Value("${instruction.message.file.copy}")
        private String name;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PropertyTestObj{");
            sb.append("value='").append(value).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append('}');
            return sb.toString();
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}
