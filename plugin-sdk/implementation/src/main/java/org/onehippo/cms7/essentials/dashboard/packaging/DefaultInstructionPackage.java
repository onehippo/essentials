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

package org.onehippo.cms7.essentials.dashboard.packaging;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.event.DisplayEvent;
import org.onehippo.cms7.essentials.dashboard.instruction.executors.PluginInstructionExecutor;
import org.onehippo.cms7.essentials.dashboard.instructions.InstructionExecutor;
import org.onehippo.cms7.essentials.dashboard.instructions.InstructionParser;
import org.onehippo.cms7.essentials.dashboard.instructions.InstructionSet;
import org.onehippo.cms7.essentials.dashboard.instructions.InstructionStatus;
import org.onehippo.cms7.essentials.dashboard.instructions.Instructions;
import org.onehippo.cms7.essentials.dashboard.model.Restful;
import org.onehippo.cms7.essentials.dashboard.utils.EssentialConst;
import org.onehippo.cms7.essentials.dashboard.utils.GlobalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;

/**
 * Default implementation of a instruction package. Reads instruction XML from {@code /META-INF/instructions.xml}
 *
 * @version "$Id$"
 */
public class DefaultInstructionPackage implements InstructionPackage {


    private static Logger log = LoggerFactory.getLogger(DefaultInstructionPackage.class);

    @Inject
    private InstructionParser instructionParser;

    @Inject
    private EventBus eventBus;

    private Map<String, Object> properties;
    private Instructions instructions;
    private String path;

    @Override
    public Map<String, Object> getProperties() {

        if (properties == null) {
            return new HashMap<>();
        }
        return properties;
    }

    @Override
    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
        prepareProperties();
    }

    private void prepareProperties() {
        if (properties == null) {
            return;
        }
        // do not reset:
        if (properties.containsKey(EssentialConst.TEMPLATE_PARAM_REPOSITORY_BASED)) {
            return;
        }
        // set boolean value for freemarker templates
        final String templateName = (String) properties.get(EssentialConst.PROP_TEMPLATE_NAME);
        if (Strings.isNullOrEmpty(templateName) || templateName.equals(EssentialConst.TEMPLATE_JSP) || templateName.equals(EssentialConst.TEMPLATE_FREEMARKER)) {
            properties.put(EssentialConst.TEMPLATE_PARAM_REPOSITORY_BASED, false);
            properties.put(EssentialConst.TEMPLATE_PARAM_FILE_BASED, true);
        } else {
            properties.put(EssentialConst.TEMPLATE_PARAM_FILE_BASED, false);
            properties.put(EssentialConst.TEMPLATE_PARAM_REPOSITORY_BASED, true);
        }
    }

    @Override
    public Set<String> groupNames() {
        return EssentialConst.DEFAULT_GROUPS;
    }

    @Override
    public Multimap<MessageGroup, ? extends Restful> getInstructionsMessages(final PluginContext context) {
        final Instructions myInstructions = getInstructions();
        if (myInstructions == null) {
            return ArrayListMultimap.create();

        }
        final Set<InstructionSet> instructionSets = instructions.getInstructionSets();
        final InstructionExecutor executor = new PluginInstructionExecutor();
        final Set<String> myGroupNames = groupNames();
        final Multimap<MessageGroup, Restful> instructionsMessages = ArrayListMultimap.create();
        for (InstructionSet instructionSet : instructionSets) {
            final Set<String> groups = instructionSet.getGroups();
            for (String group : groups) {
                // execute only or group(s)
                if (myGroupNames.contains(group)) {
                    final Multimap<MessageGroup, Restful> instr = executor.getInstructionsMessages(instructionSet, context);
                    instructionsMessages.putAll(instr);

                } else {
                    log.debug("Skipping instruction group for name: [{}]", group);
                }
            }
        }
        return instructionsMessages;
    }


    @Override
    public String getInstructionPath() {
        if (Strings.isNullOrEmpty(path)) {
            return EssentialConst.DEFAULT_INSTRUCTIONS_PATH;
        }
        return path;
    }


    @Override
    public void setInstructionPath(final String path) {
        this.path = path;
    }

    @Override
    public Instructions getInstructions() {
        if (instructions == null) {

            final InputStream resourceAsStream = getClass().getResourceAsStream(getInstructionPath());
            final String content = GlobalUtils.readStreamAsText(resourceAsStream);
            if (instructionParser == null) {
                final StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append('\n')
                        .append("=============================================================================")
                        .append('\n')
                        .append("InstructionParser was null. This can means it is not injected by Spring.")
                        .append('\n')
                        .append("If you are running a test case, make sure you autowire beans like:")
                        .append('\n')
                        .append("InstructionPackage instructionPackage = new MyInstructionPackage();injector.autowireBean(instructionPackage);")
                        .append('\n')
                        .append("=============================================================================")
                        .append('\n');

                log.error("{}", errorBuilder);
                throw new IllegalArgumentException("Instruction parser not injected");
            }
            instructions = instructionParser.parseInstructions(content);
        }
        return instructions;

    }

    @Override
    public InstructionStatus execute(final PluginContext context) {
        // NOTE: we'll add any additional context properties into context:
        context.addPlaceholderData(properties);


        if (instructions == null) {
            instructions = getInstructions();
        }
        if (instructions == null) {
            eventBus.post(new DisplayEvent("Couldn't parse instructions"));
            log.error("Failed to parse instructions");
            return InstructionStatus.FAILED;
        }
        final Set<InstructionSet> instructionSets = instructions.getInstructionSets();
        InstructionStatus status = InstructionStatus.SUCCESS;
        final InstructionExecutor executor = new PluginInstructionExecutor();
        final Set<String> myGroupNames = groupNames();
        for (InstructionSet instructionSet : instructionSets) {
            final Set<String> groups = instructionSet.getGroups();
            for (String group : groups) {
                // execute only or group(s)
                if (myGroupNames.contains(group)) {
                    // currently we return fail if any of instructions is failed
                    if (status == InstructionStatus.FAILED) {
                        executor.execute(instructionSet, context);
                        continue;
                    }
                    status = executor.execute(instructionSet, context);
                } else {
                    log.debug("Skipping instruction group for name: [{}]", group);
                }
            }

        }
        return status;
    }


    @Override
    public InstructionParser getInstructionParser() {
        return instructionParser;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
}
