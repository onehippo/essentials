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

package org.onehippo.cms7.essentials.components;

import org.hippoecm.hst.content.beans.standard.HippoFacetNavigationBean;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.util.PathUtils;
import org.hippoecm.hst.util.SearchInputParsingUtils;
import org.hippoecm.hst.utils.BeanUtils;
import org.onehippo.cms7.essentials.components.info.EssentialsFacetsComponentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * @version "$Id$"
 */
@ParametersInfo(type = EssentialsFacetsComponentInfo.class)
public class EssentialsFacetsComponent extends CommonComponent {

    private static Logger log = LoggerFactory.getLogger(EssentialsFacetsComponent.class);

    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) {
        log.info("**** FACET COMPONENT **** ");
        final EssentialsFacetsComponentInfo componentInfo = getComponentParametersInfo(request);
        final String facetPath = componentInfo.getFacetPath();
        final String queryParam = cleanupSearchQuery(getAnyParameter(request, REQUEST_PARAM_QUERY));
        final HippoFacetNavigationBean hippoFacetNavigationBean = getFacetNavigationBean(request, facetPath, queryParam);
        if (hippoFacetNavigationBean == null) {
            log.warn("Facet navigation bean for facet path: {} was null", facetPath);
            return;
        }

        request.setAttribute(REQUEST_PARAM_QUERY, queryParam);
        request.setAttribute(REQUEST_PARAM_FACETS, hippoFacetNavigationBean);
    }

    protected HippoFacetNavigationBean getFacetNavigationBean(HstRequest request, String path, String query) {
        if (Strings.isNullOrEmpty(path)) {
            log.warn("Facetpath was empty {}", path);
            return null;
        }
        ResolvedSiteMapItem resolvedSiteMapItem = request.getRequestContext().getResolvedSiteMapItem();
        String resolvedContentPath = PathUtils.normalizePath(resolvedSiteMapItem.getRelativeContentPath());
        HippoFacetNavigationBean resolvedContentBean = getSiteContentBaseBean(request).getBean(resolvedContentPath, HippoFacetNavigationBean.class);
        String parsedQuery = SearchInputParsingUtils.parse(query, false);
        HippoFacetNavigationBean facNavBean;
        if (resolvedContentBean != null) {
            facNavBean = BeanUtils.getFacetNavigationBean(request, resolvedContentPath, parsedQuery, getObjectConverter());
        } else {
            facNavBean = BeanUtils.getFacetNavigationBean(request, path, parsedQuery, getObjectConverter());
        }
        return facNavBean;
    }

}
