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

package org.onehippo.cms7.essentials.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.gallery.HippoGalleryNodeType;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.cms7.essentials.dashboard.ctx.DashboardPluginContext;
import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.utils.CndUtils;
import org.onehippo.cms7.essentials.dashboard.utils.GalleryUtils;
import org.onehippo.cms7.essentials.dashboard.utils.GlobalUtils;
import org.onehippo.cms7.essentials.dashboard.utils.HippoNodeUtils;
import org.onehippo.cms7.essentials.dashboard.utils.TranslationUtils;
import org.onehippo.cms7.essentials.rest.exc.RestException;
import org.onehippo.cms7.essentials.rest.model.PropertyRestful;
import org.onehippo.cms7.essentials.rest.model.TranslationRestful;
import org.onehippo.cms7.essentials.rest.model.gallery.ImageProcessorRestful;
import org.onehippo.cms7.essentials.rest.model.gallery.ImageSetRestful;
import org.onehippo.cms7.essentials.rest.model.gallery.ImageSetsRestful;
import org.onehippo.cms7.essentials.rest.model.gallery.ImageVariantRestful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version "$Id$"
 */
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
@Path("/imagegallery/")
public class ImageGalleryResource extends BaseResource {

    @Inject
    private EventBus eventBus;
    private static Logger log = LoggerFactory.getLogger(ImageGalleryResource.class);

    private static final String GALLERY_PROCESSOR_SERVICE_PATH = "/hippo:configuration/hippo:frontend/cms/cms-services/galleryProcessorService";

    private static final String DEFAULT_UPSCALING_VALUE = Boolean.FALSE.toString();
    private static final String DEFAULT_OPTIMIZATION_VALUE = "quality";
    private static final String DEFAULT_COMPRESSION_VALUE = "1";

    @GET
    @Path("/")
    public List<ImageProcessorRestful> getImageProcessor() {
        final ImageProcessorRestful processorRestful = new ImageProcessorRestful();
        // TODO verify the use and creation of the plugin context
        final PluginContext pluginContext = getPluginContext();

        final Session session = pluginContext.getSession();

        try {
            final Node processorNode = session.getNode(GALLERY_PROCESSOR_SERVICE_PATH);
            processorRestful.setPath(processorNode.getPath());
            processorRestful.setClassName(processorNode.getProperty("plugin.class").getString());
            processorRestful.setId(processorNode.getProperty("gallery.processor.id").getString());

            final Map<String, ImageVariantRestful> variantMap = fetchImageProcessorVariants(session, processorNode);
            processorRestful.addVariants(variantMap.values());


            populateImageSetsInVariants(session, variantMap.values());


        } catch (RepositoryException e) {
            log.error("Exception while trying to retrieve document types from repository {}", e);
        }
        List<ImageProcessorRestful> processors = new ArrayList<>();
        processors.add(processorRestful);

        return processors;
    }

    @PUT
    @Path("/save")
    @Consumes("application/json")
    @Produces("application/json")
    public ImageProcessorRestful saveImageProcessor(@Context HttpServletRequest request) {
        // TODO map directly to image processor
        final ImageProcessorRestful processor = extractImageProcessorFromRequestBody(request);
        if (processor != null) {
            final PluginContext pluginContext = getPluginContext();
            final Session session = pluginContext.getSession();
            try {
                saveImageProcessor(session, processor);
                session.save();
            } catch (RepositoryException e) {
                log.error("Error while trying to update image processor", e);
                throw new RestException("Error while trying to update image processor", Response.Status.INTERNAL_SERVER_ERROR);
            }
            return processor;
        }
        return null;
    }

    private ImageProcessorRestful extractImageProcessorFromRequestBody(final ServletRequest request) {
        try {
            final String requestBody = IOUtils.toString(request.getInputStream(), "UTF-8");
            return new Gson().fromJson(requestBody, ImageProcessorRestful.class);
        } catch (IOException e) {
            log.error("Error parsing JSON", e);
        }
        return null;
    }

    /**
     * Update the image processor, with the underlying variants.
     * <p/>
     * To persist the changes a session save needs to be performed.
     *
     * @param session        the JCR session
     * @param imageProcessor the image processor
     * @throws RepositoryException when an exception occurs.
     */
    private void saveImageProcessor(final Session session, final ImageProcessorRestful imageProcessor) throws RepositoryException {
        final Node processorNode = session.getNode(imageProcessor.getPath());
        // Remove all old non used variants
        deleteOldVariants(processorNode, imageProcessor);
        // save all variants
        for (final ImageVariantRestful variant : imageProcessor.getVariants()) {
            final Node variantNode;
            if (processorNode.hasNode(variant.getNodeType())) {
                log.debug("Update variant node {}", variant.getNodeType());
                variantNode = processorNode.getNode(variant.getNodeType());
            } else {
                log.debug("Add new variant node {}", variant.getNodeType());
                variantNode = processorNode.addNode(variant.getNodeType(), "frontend:pluginconfig");
            }
            updateVariantNode(variantNode, variant);
        }
    }

    private void deleteOldVariants(final Node processorNode, final ImageProcessorRestful imageProcessor) throws RepositoryException {
        final List<Node> nodesToDelete = new ArrayList<>();
        // Determine the nodes to delete
        final NodeIterator variantIterator = processorNode.getNodes();
        while (variantIterator.hasNext()) {
            final Node variantNode = variantIterator.nextNode();
            // TODO deteremine whether to check on id or on namespace/name combination
            final ImageVariantRestful variant = imageProcessor.getVariantForNodeType(variantNode.getName());
            if (variant == null) {
                // TODO check for hippogallery namespace
                if("hippogallery".equals(HippoNodeUtils.getPrefixFromType(variantNode.getName()))) {
                    log.error("Shouldn't delete variants which belong to hippogallery namespace");
                    continue;
                }
                nodesToDelete.add(variantNode);
            }
        }
        // Delete then nodes
        for (final Node nodeToDelete : nodesToDelete) {
            log.info("Remove variant node {}", nodeToDelete.getPath());
            nodeToDelete.remove();
        }
    }

    /**
     * Update width, height and additional properties of the variant node.
     * <p/>
     * Currently the translations are not saved here, because they are stored in another location (i.e. image sets),
     * which is unrelated to the variant.
     *
     * @param variantNode the node of the variant of the image processor
     * @param variant     the variant representation with values to store
     * @throws RepositoryException when a repository exception occurs
     */
    private void updateVariantNode(final Node variantNode, final ImageVariantRestful variant) throws RepositoryException {
        variantNode.setProperty("height", variant.getHeight());
        variantNode.setProperty("width", variant.getWidth());

        // Remove unused properties (default value is used)
        // Upscaling property
        final PropertyRestful upscaling = variant.getUpscaling();
        if (upscaling == null || DEFAULT_UPSCALING_VALUE.equals(upscaling.getValue()) || StringUtils.isBlank(upscaling.getValue())) {
            removeProperty(variantNode, "upscaling");
        } else {
            setProperty(variantNode, upscaling);
        }
        // Optimization property
        final PropertyRestful optimization = variant.getOptimization();
        if (optimization == null || DEFAULT_OPTIMIZATION_VALUE.equals(optimization.getValue()) || StringUtils.isBlank(optimization.getValue())) {
            removeProperty(variantNode, "optimize");
        } else {
            setProperty(variantNode, optimization);
        }
        // Compression property
        final PropertyRestful compression = variant.getCompression();
        if (compression == null || DEFAULT_COMPRESSION_VALUE.equals(compression.getValue()) || StringUtils.isBlank(compression.getValue())) {
            removeProperty(variantNode, "compression");
        } else {
            setProperty(variantNode, optimization);
        }

        // Set available properties
        for (final PropertyRestful property : variant.getProperties()) {
            // TODO check support for additional props
            //setProperty(variantNode, property);
        }

        // TODO: translations should be stored in relation to the variants (e.g. not stored underneath image sets)
    }

    /**
     * Set a property on a node. The type of the property is determined by the provided property type. Currently
     * Booleans and String are supported. Other types will be stored as a String property on the node.
     *
     * @param node     the node to set property on
     * @param property the property representation
     * @throws RepositoryException when a repository exception occurs
     */
    private void setProperty(final Node node, final PropertyRestful property) throws RepositoryException {
        switch (property.getType()) {
            case BOOLEAN:
                node.setProperty(property.getName(), Boolean.valueOf(property.getValue()).booleanValue());
            default:
                node.setProperty(property.getName(), property.getValue());
                break;
        }
    }

    private void removeProperty(final Node node, final String propertyName) throws RepositoryException {
        if (node.hasProperty(propertyName)) {
            final Property property = node.getProperty(propertyName);
            property.remove();
        }
    }

    private Map<String, ImageVariantRestful> fetchImageProcessorVariants(final Session session, final Node processorNode) throws RepositoryException {
        final Map<String, ImageVariantRestful> variants = new HashMap<>();
        final Map<String, Map<String, TranslationRestful>> variantTranslationsMap = getVariantTranslationsMap(session);

        final NodeIterator variantNodes = processorNode.getNodes();
        while (variantNodes.hasNext()) {
            final Node variantNode = variantNodes.nextNode();
            final ImageVariantRestful variantRestful = new ImageVariantRestful();
            variantRestful.setId(variantNode.getIdentifier());
            final String variantName = variantNode.getName();
            variantRestful.setNamespace(HippoNodeUtils.getPrefixFromType(variantName));
            variantRestful.setName(HippoNodeUtils.getNameFromType(variantName));
            if (variantNode.hasProperty("width")) {
                variantRestful.setWidth((int) variantNode.getProperty("width").getLong());
            }
            if (variantNode.hasProperty("height")) {
                variantRestful.setHeight((int) variantNode.getProperty("height").getLong());
            }

            // Upscaling property
            final PropertyRestful upscalingProperty = new PropertyRestful();
            upscalingProperty.setName("upscaling");
            if (variantNode.hasProperty("upscaling")) {
                upscalingProperty.setValue(variantNode.getProperty("upscaling").getString());
            }
            upscalingProperty.setType(PropertyRestful.PropertyType.BOOLEAN);
            variantRestful.addProperty(upscalingProperty);
            variantRestful.setUpscaling(upscalingProperty);

            // Optimize property
            final PropertyRestful optimizeProperty = new PropertyRestful();
            optimizeProperty.setName("optimize");
            if (variantNode.hasProperty("optimize")) {
                optimizeProperty.setValue(variantNode.getProperty("optimize").getString());
            }
            optimizeProperty.setType(PropertyRestful.PropertyType.STRING);
            variantRestful.addProperty(optimizeProperty);
            variantRestful.setOptimization(optimizeProperty);

            // Compression property
            final PropertyRestful compressionProperty = new PropertyRestful();
            compressionProperty.setName("compression");
            if (variantNode.hasProperty("compression")) {
                compressionProperty.setValue(variantNode.getProperty("compression").getString());
            }
            compressionProperty.setType(PropertyRestful.PropertyType.STRING);
            variantRestful.addProperty(compressionProperty);
            variantRestful.setCompression(compressionProperty);

            if (variantTranslationsMap.get(variantName) != null) {
                log.debug("Translations for {} : ", variantName, variantTranslationsMap.get(variantName).size());
                variantRestful.addTranslations(variantTranslationsMap.get(variantName).values());
            } else {
                log.debug("No translations for {}", variantName);
            }
            if (log.isTraceEnabled()) {
                for (final String key : variantTranslationsMap.keySet()) {
                    log.debug("Has translation for {}", key);
                }
            }

            variants.put(variantRestful.getNodeType(), variantRestful);
        }
        return variants;
    }

    private void fetchImageSet(final Session session, final String type) {
        //GalleryUtils.
    }

    private Map<String, Map<String, TranslationRestful>> getVariantTranslationsMap(final Session session) throws RepositoryException {
        final Map<String, Map<String, TranslationRestful>> map = new HashMap<>();
        for (final Node node : fetchVariantTranslations(session)) {
            final TranslationRestful translation = new TranslationRestful();
            translation.setLocale(TranslationUtils.getHippoLanguage(node));
            translation.setMessage(TranslationUtils.getHippoMessage(node));

            final String propertyName = TranslationUtils.getHippoProperty(node);
            if (StringUtils.isBlank(propertyName)) {
                log.debug("Skipping translation: {}", node.getPath());
                continue;
            } else {
                log.debug("Adding translation: {}", node.getPath());
            }
            if (!map.containsKey(propertyName)) {
                map.put(propertyName, new HashMap<String, TranslationRestful>());
            }
            map.get(propertyName).put(translation.getLocale(), translation);
        }
        return map;
    }

    @PUT
    @Path("/imagesets/save")
    public ImageSetsRestful saveImageSets(final ImageSetsRestful imageSets) throws RepositoryException {
        if(imageSets == null) {
            log.error("Unable to process image sets");
            return null;
        }

        final PluginContext pluginContext = getPluginContext();
        final Session session = pluginContext.getSession();
        try {
            for(final ImageSetRestful imageSet : imageSets.getImageSets()) {
                saveImageSet(pluginContext, session, imageSet);
            }
            session.save();
        } catch (RepositoryException e) {
            log.error("Error while trying to update image sets", e);
            throw new RestException("Error while trying to update image processor", Response.Status.INTERNAL_SERVER_ERROR);
        }
        // TODO determine return type
        return imageSets;
    }

    private boolean saveImageSet(final PluginContext pluginContext, final Session session, final ImageSetRestful imageSet) throws RepositoryException {
        log.error("Updating image set {}", imageSet.getType());

        final Node namespaceNode;

        // Check namespace registry
        if (!CndUtils.existsNamespacePrefix(pluginContext, imageSet.getNamespace())) {
            // TODO non existing namespace not supported
            log.error("Unexisting namespace {} not supported", imageSet.getNamespace());
            return false;
        }
        if (!CndUtils.existsNodeType(pluginContext, imageSet.getType())) {
            // register node type and add namespace node
            log.info("Create namespace node for {}", imageSet.getNamespace());
            namespaceNode = CndUtils.createHippoNamespace(pluginContext, imageSet.getNamespace());
            CndUtils.registerDocumentType(pluginContext, imageSet.getNamespace(), imageSet.getName(), false, false, GalleryUtils.HIPPOGALLERY_IMAGE_SET, GalleryUtils.HIPPOGALLERY_RELAXED);
        } else if (!CndUtils.isNodeType(pluginContext, imageSet.getType(), HippoGalleryNodeType.IMAGE_SET)) {
            // TODO incorrect node type
            log.error("Incorrect node type {}; not of type imageset", imageSet.getType());
            return false;
        } else if (!CndUtils.existsNodeType(pluginContext, imageSet.getType())) {
            // register node type and add namespace node
            log.info("Create namespace node for {}", imageSet.getNamespace());
            namespaceNode = CndUtils.createHippoNamespace(pluginContext, imageSet.getNamespace());
            CndUtils.registerDocumentType(pluginContext, imageSet.getNamespace(), imageSet.getName(), false, false, GalleryUtils.HIPPOGALLERY_IMAGE_SET, GalleryUtils.HIPPOGALLERY_RELAXED);
        } else {
            namespaceNode = CndUtils.getHippoNamespaceNode(pluginContext, imageSet.getNamespace());
            log.debug("Retrieved namespace node {}", namespaceNode.getPath());
        }

        // Check namespace node
        final Node imageSetNode;
        if(namespaceNode.hasNode(imageSet.getName())) {
            imageSetNode = namespaceNode.getNode(imageSet.getName());
            log.debug("Fetched existing image set namespace node {}", imageSetNode.getPath());
        } else {
            log.debug("Create new image set namespace node for image set {}", imageSet.getType());
            imageSetNode = GalleryUtils.createImagesetNamespace(session, imageSet.getNamespace(), imageSet.getName());
        }

        //GalleryUtils.getFieldVariantsFromTemplate();
        //GalleryUtils.get


        // Remove all old non used variants
        final List<Node> nodes = fetchFieldsFromNamespaceNode(imageSetNode, "hippogallery:image");
        for (final Node variantFieldNode : nodes) {
            log.debug("Check variant {}", variantFieldNode.getName());
            //final String documentType = JcrUtils.getStringProperty(node, "hipposysedit:path", null);
            final ImageVariantRestful variant = imageSet.getVariantByName(variantFieldNode.getName());
            if (variant == null) {
                // TODO or add to list of nodes to delete
                //variantFieldNode.remove();
                log.debug("Remove {}", variantFieldNode.getPath());

                if (imageSetNode.hasNode("editor:templates/_default_/" + variantFieldNode.getName())) {
                    final Node templateNode = imageSetNode.getNode("editor:templates/_default_/" + variantFieldNode.getName());
                    //templateNode.remove();
                    log.debug("Remove {}", templateNode.getPath());
                }
            }
        }

        if(!imageSetNode.hasNode("hipposysedit:nodetype/hipposysedit:nodetype")) {
            log.error("Node type node not available for {}", imageSetNode.getPath());
            return false;
        }
        final Node imageSetNodeTypeNode = imageSetNode.getNode("hipposysedit:nodetype/hipposysedit:nodetype");

        // save all variants
        for(final ImageVariantRestful variant : imageSet.getVariants()) {
            if(variant.getName() == null) {
                log.debug("Unable to process variant without name");
                continue;
            }
            if(imageSetNodeTypeNode.hasNode(variant.getName())) {
                log.debug("Variant {} already defined in node type", imageSetNode.getName());
                continue;
            }
            setTemplateNodeTypeForVariant(session, imageSetNode, variant);
            setTemplateFieldForVariant(session, imageSetNode, variant);

            for (final TranslationRestful translation : variant.getTranslations()) {
                TranslationUtils.setTranslationForNode(imageSetNode, variant.getNodeType(), translation.getLocale(), translation.getMessage());
            }
        }

        for (final TranslationRestful translation : imageSet.getTranslations()) {
            TranslationUtils.setTranslationForNode(imageSetNode, null, translation.getLocale(), translation.getMessage());
        }

/*
        for(final ImageVariantRestful variant : imageSet.getVariants()) {
            final Node fieldNode;
            if(processorNode.hasNode(variant.getNodeType())) {
                variantNode = processorNode.getNode(variant.getNodeType());
            } else {
                variantNode = processorNode.addNode(variant.getNodeType(), "frontend:pluginconfig");
            }
            final Node templateNode;
            if(imageSetNode.hasNode("editor:templates/_default_/" + variant.getName())) {
                templateNode = imageSetNode.hasNode("editor:templates/_default_/" + variant.getName());
            } else {
                templateNode = imageSetNode.getNode("editor:templates/_default_/" + variant.getName());
            }
                updateVariantNode(variantNode, variant);
        }
*/
        // TODO check
        // TODO save translations
        //session.save();
        return true;
    }

    private void setTemplateNodeTypeForVariant(final Session session, final Node imagesetTemplate, final ImageVariantRestful variant) throws RepositoryException {
        // TODO only required to retrieve node when copy is required
        final Node original = imagesetTemplate.getNode("hipposysedit:nodetype").getNode("hipposysedit:nodetype").getNode("original");
        final String sysPath = original.getParent().getPath() + '/' + variant.getName();
        final Node copy = HippoNodeUtils.retrieveExistingNodeOrCreateCopy(session, sysPath, original);
        copy.setProperty(HippoNodeUtils.HIPPOSYSEDIT_PATH, variant.getNodeType());
        copy.setProperty(HippoNodeType.HIPPOSYSEDIT_TYPE, HippoGalleryNodeType.IMAGE);
    }

    private void setTemplateFieldForVariant(final Session session, final Node imagesetTemplate, final ImageVariantRestful variant) throws RepositoryException {
        // TODO only required to retrieve node when copy is required
        final Node original = imagesetTemplate.getNode("editor:templates").getNode("_default_").getNode("original");
        final String sysPath = original.getParent().getPath() + '/' + variant.getName();
        final Node copy = HippoNodeUtils.retrieveExistingNodeOrCreateCopy(session, sysPath, original);
        copy.setProperty("caption", variant.getName());
        copy.setProperty("field", variant.getName());

    }



    protected void storeImageSetTranslations(final Node imageSetNode, final ImageSetRestful imageSet) {
        // TODO
        // save variant translations as well??
    }


    @GET
    @Path("/imagesets/")
    public ImageSetsRestful fetchImageSets() throws RepositoryException {

        final ImageProcessorRestful processorRestful = new ImageProcessorRestful();
        // TODO verify the use and creation of the plugin context
        final PluginContext pluginContext = getPluginContext();

        final Session session = pluginContext.getSession();


        final Node processorNode = session.getNode(GALLERY_PROCESSOR_SERVICE_PATH);
        final List<ImageSetRestful> imageSets = fetchImageSets(session);
        populateVariantsInImageSets(session, imageSets, processorNode);

        return new ImageSetsRestful(imageSets);
    }

    private void populateImageSetsInVariants(final Session session, final Collection<ImageVariantRestful> variants) throws RepositoryException {
        final List<ImageSetRestful> imageSets = fetchImageSets(session);
        for (final ImageVariantRestful variant : variants) {
            populateImageSetsInVariant(variant, imageSets);
        }
    }

    private void populateImageSetsInVariant(final ImageVariantRestful variant, final List<ImageSetRestful> availableImageSets) throws RepositoryException {
        final List<ImageSetRestful> imageSets = new ArrayList<>();
        for (final ImageSetRestful imageSet : availableImageSets) {
            if (imageSet.hasVariant(variant.getNamespace(), variant.getName())) {
                imageSets.add(imageSet);
            }
        }
        variant.setImageSets(imageSets);
    }


    private void populateVariantsInImageSets(final Session session, final List<ImageSetRestful> imageSets, final Node processorNode) throws RepositoryException {
        final Map<String, ImageVariantRestful> availableVariants = fetchImageProcessorVariants(session, processorNode);
        for (final ImageSetRestful imageSet : imageSets) {
            populateVariantsInImageSet(imageSet, availableVariants);
        }
    }

    private void populateVariantsInImageSet(ImageSetRestful imageSet, final Map<String, ImageVariantRestful> availableVariants) throws RepositoryException {
        final List<ImageVariantRestful> variants = new ArrayList<>();
        for (final ImageVariantRestful tempVariant : imageSet.getVariants()) {
            final ImageVariantRestful variant = availableVariants.get(tempVariant.getNodeType());
            if(variant != null) {
                variants.add(variant);
            }
        }
        imageSet.setVariants(variants);
    }


    private List<ImageSetRestful> fetchImageSets(final Session session) throws RepositoryException {
        final List<ImageSetRestful> imageSets = new ArrayList<>();
        final PluginContext pluginContext = getPluginContext();
        final List<Node> nodes = fetchImageSetNamespaceNodes(session, listImageSetTypes(pluginContext));
        for (final Node node : nodes) {
            final ImageSetRestful imageSet = new ImageSetRestful();
            imageSet.setId(node.getIdentifier());
            imageSet.setPath(node.getPath());
            imageSet.setName(node.getName());
            imageSet.setNamespace(node.getParent().getName());
            imageSet.setVariants(getVariantsForImageSetNamespaceNode(node));
            imageSet.setTranslations(getImageSetTranslations(node));

            imageSets.add(imageSet);
        }
        return imageSets;
    }

    private List<TranslationRestful> getImageSetTranslations(final Node imageSet) throws RepositoryException {
        final List<TranslationRestful> translations = new ArrayList<>();

        for (final Node node : TranslationUtils.getTranslationsFromNode(imageSet)) {
            final TranslationRestful translation = new TranslationRestful();
            translation.setLocale(TranslationUtils.getHippoLanguage(node));
            translation.setMessage(TranslationUtils.getHippoMessage(node));

            final String propertyName = TranslationUtils.getHippoProperty(node);
            if (!StringUtils.isBlank(propertyName)) {
                log.trace("Skipping translation: {}", node.getPath());
                continue;
            } else {
                log.trace("Adding translation: {}", node.getPath());
            }
            translations.add(translation);
        }
        return translations;
    }


    private List<Node> fetchFieldsFromNamespaceNode(final Node namespaceNode, final String fieldType) throws RepositoryException {
        if (!namespaceNode.isNodeType("hipposysedit:templatetype")) {
            return Collections.emptyList();
        }

        final Node nodeTypeHandle = JcrUtils.getNodeIfExists(namespaceNode, "hipposysedit:nodetype");
        if (nodeTypeHandle == null) {
            return Collections.emptyList();
        }

        final Node nodeTypeNode = JcrUtils.getNodeIfExists(nodeTypeHandle, "hipposysedit:nodetype");
        if (nodeTypeNode == null) {
            return Collections.emptyList();
        }

        final List<Node> fields = new ArrayList<>();
        final NodeIterator iterator = nodeTypeNode.getNodes();
        while (iterator.hasNext()) {
            final Node node = iterator.nextNode();
            if (fieldType == null || fieldType.equals(JcrUtils.getStringProperty(node, "hipposysedit:type", null))) {
                fields.add(node);
            }
        }
        return fields;
    }

    private List<ImageVariantRestful> getVariantsForImageSetNamespaceNode(final Node imageSetNode) throws RepositoryException {
        final List<ImageVariantRestful> imageSets = new ArrayList<>();
        final List<Node> nodes = fetchFieldsFromNamespaceNode(imageSetNode, "hippogallery:image");
        for (final Node node : nodes) {
            final String documentType = JcrUtils.getStringProperty(node, "hipposysedit:path", null);
            imageSets.add(new ImageVariantRestful(HippoNodeUtils.getPrefixFromType(documentType), HippoNodeUtils.getNameFromType(documentType)));
        }
        return imageSets;
    }


    private List<Node> fetchVariantTranslations(final Session session) throws RepositoryException {
        final List<Node> variantTranslations = new ArrayList<>();
        final PluginContext pluginContext = getPluginContext();
        final List<Node> nodes = fetchImageSetNamespaceNodes(session, listImageSetTypes(pluginContext));
        log.debug("Image set nodes: {}", nodes.size());
        for (final Node imageSetNSNode : nodes) {
            log.debug("Image set node: {}", imageSetNSNode.getPath());
            variantTranslations.addAll(TranslationUtils.getTranslationsFromNode(imageSetNSNode));
        }
        return variantTranslations;
    }

    private List<Node> fetchImageSetNamespaceNodes(final Session session, final List<String> imageSets) throws RepositoryException {
        final List<Node> nodes = new ArrayList<>();
        for (final String imageSet : imageSets) {
            log.debug("Fetch Image set NS node for: {}", imageSet);
            nodes.add(fetchImageSetNamespaceNode(session, imageSet));
        }
        return nodes;
    }

    private Node fetchImageSetNamespaceNode(final Session session, final String imageSet) throws RepositoryException {
        return session.getNode(getPathToNamespaceNode(imageSet));
    }

    private String getPathToNamespaceNode(final String documentType) {
        return "/hippo:namespaces/" + HippoNodeUtils.getPrefixFromType(documentType) + '/' + HippoNodeUtils.getNameFromType(documentType);
    }

    private List<String> listImageSetTypes(final PluginContext pluginContext) {
        try {
            return CndUtils.getNodeTypesOfType(pluginContext, HippoGalleryNodeType.IMAGE_SET, true);
        } catch (RepositoryException e) {
            log.warn("Unable to retrieve node types", e);
        }
        return Collections.emptyList();

    }

    private PluginContext getPluginContext() {
        return new DashboardPluginContext(GlobalUtils.createSession(), null);
    }


}
