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

package org.onehippo.cms7.essentials.dashboard.panels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version "$Id$"
 */
public class DoubleSelectBox<T> extends Panel {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DoubleSelectBox.class);
    private final Collection<EventListener<T>> listeners = new CopyOnWriteArrayList<>();
    private final ListMultipleChoice<T> rightBox;
    private final ListMultipleChoice<T> leftBox;
    @SuppressWarnings("UnusedDeclaration")
    private List<T> rightItems;
    private List<T> leftItems;
    private List<T> selectedLeftItems;
    private List<T> selectedRightItems;
    private boolean removeFromLeft = true;

    public DoubleSelectBox(final String id, final String title, final Form<?> form, final Collection<T> initialLeftItems, final Collection<T> initialRightItems, final EventListener<T> listener) {
        this(id, title, form, initialLeftItems, initialRightItems);
        listeners.add(listener);
    }

    public DoubleSelectBox(final String id, final String title, final Form<?> form, final Collection<T> initialLeftItems, final Collection<T> initialRightItems, final Collection<EventListener<T>> listeners) {
        this(id, title, form, initialLeftItems, initialRightItems);
        listeners.addAll(listeners);
    }


    public DoubleSelectBox(final String id, final String title, final Form<?> form, final Collection<T> initialLeftItems) {
        this(id, title, form, initialLeftItems, new ArrayList<T>());
    }

    public DoubleSelectBox(final String id, final String title, final Form<?> form, final Collection<T> initialLeftItems, final Collection<T> initialRightItems) {
        super(id);
        //############################################
        // LEFT BOX
        //############################################
        leftItems = new ArrayList<>();
        leftItems.addAll(initialLeftItems);
        final PropertyModel<List<T>> leftModel = new PropertyModel<>(this, "selectedLeftItems");

        leftBox = new ListMultipleChoice<>("leftBox", leftModel, leftItems);

        leftBox.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                log.debug("@selected left-box items {}", selectedLeftItems);
                // notify listeners for changes
                for (EventListener<T> listener : listeners) {
                    listener.onSelected(target, selectedLeftItems);
                }
                final Collection<T> selectedItems = new ArrayList<>(selectedLeftItems);
                removeFromLeftBox(target, selectedItems);
                addToRightBox(target, selectedItems);

            }
        });

        //############################################
        // RIGHT BOX
        //############################################
        final PropertyModel<List<T>> rightModel = new PropertyModel<>(this, "selectedRightItems");
        rightItems = new ArrayList<>();
        rightItems.addAll(initialRightItems);
        rightBox = new ListMultipleChoice<>("rightBox", rightModel, rightItems);
        rightBox.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                log.debug("@selected right-box items {}", selectedRightItems);
                // add items to the right box:
                final Collection<T> selected = new ArrayList<>(selectedRightItems);
                addToLeftBox(target, selected);
                removeFromRightBox(target, selected);
            }
        });


        //############################################
        // ADD COMPONENTS
        //############################################
        rightBox.setOutputMarkupId(true);
        leftBox.setOutputMarkupId(true);
        add(new Label("title", title));
        add(rightBox);
        add(leftBox);
        form.add(this);


    }

    private void removeFromLeftBox(final AjaxRequestTarget target, final Collection<T> selected) {
        if (!removeFromLeft) {
            log.info("@remove form left box not enabled ");
            return;
        }
        log.debug("@left removing {}", selected);
        leftItems.removeAll(selected);
        target.add(leftBox, rightBox);
    }



    public void addToLeftBox(final AjaxRequestTarget target, final Iterable<T> selected) {
        if (!removeFromLeft) {
            log.info("@add to left box not enabled");
            return;
        }
        for (T item : selected) {
            if (!leftItems.contains(item)) {
                log.debug("@left adding {}", item);
                leftItems.add(item);
            }
        }
        target.add(leftBox);
    }

    public void removeFromRightBox(final AjaxRequestTarget target, final Collection<T> selected) {
        log.debug("@right removing {}", selected);
        rightItems.removeAll(selected);
        target.add(rightBox);
    }

    public void addToRightBox(final AjaxRequestTarget target, final Iterable<T> selected) {
        log.info("@right selected {}", selected);
        for (T item : selected) {
            if (!rightItems.contains(item)) {
                log.debug("@right adding {}", item);
                rightItems.add(item);
            } else {
                log.debug("@right skipping, exists {}", item);
            }
        }
        target.add(rightBox);

    }

    public List<T> getSelectedRightItems() {
        return selectedRightItems;
    }

    public List<T> getSelectedLeftItems() {
        return selectedLeftItems;
    }

    public ListMultipleChoice<T> getRightBox() {
        return rightBox;
    }

    public ListMultipleChoice<T> getLeftBox() {
        return leftBox;
    }

    public boolean isRemoveFromLeft() {
        return removeFromLeft;
    }

    public void setRemoveFromLeft(final boolean removeFromLeft) {
        this.removeFromLeft = removeFromLeft;
    }
}