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

package org.onehippo.cms7.essentials.components.gui.panel;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeExistsException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.PropertyModel;
import org.onehippo.cms7.essentials.components.gui.ComponentsWizard;
import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.utils.CndUtils;
import org.onehippo.cms7.essentials.dashboard.utils.ComponentsUtils;
import org.onehippo.cms7.essentials.dashboard.wizard.EssentialsWizardStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attach component to a component container.
 *
 * @version "$Id$"
 */
public class AttachComponentPanel extends EssentialsWizardStep {

    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(AttachComponentPanel.class);

    private final ComponentsWizard parent;
    private List<String> selectedDocuments;
    private final ListChoice<String> sitesChoice;
    private String selectedSite;


    public AttachComponentPanel(final ComponentsWizard parent, final String id) {
        super(id);
        this.parent = parent;
        PluginContext context = parent.getContext();
        final Form<?> form = new Form("form");
        //############################################
        // SITES SELECT
        //############################################
        final List<String> sites = ComponentsUtils.getAllAvailableSites(context);

        final PropertyModel<String> siteModel = new PropertyModel<>(this, "selectedSite");
        sitesChoice = new ListChoice<>("sites", siteModel, sites);
        sitesChoice.setNullValid(false);
        sitesChoice.setOutputMarkupId(true);

        //############################################
        // ADD COMPONENTS
        //############################################
        add(form);
        form.add(sitesChoice);
    }


    @Override
    public void applyState() {
        setComplete(false);

        setComplete(true);

    }
}


