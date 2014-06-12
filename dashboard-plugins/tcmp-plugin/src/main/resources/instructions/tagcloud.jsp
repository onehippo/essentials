<%--
  Copyright 2014 Hippo B.V. (http://www.onehippo.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  --%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix='hst' uri="http://www.hippoecm.org/jsp/hst/core" %>
<ul id="tagcloud">
  <li class="title">${tagcloud.title}</li>
  <li class="content">
    <c:forEach var="tag" items="${tagcloud.tags}">
      <c:choose>
        <c:when test="${tag.type eq 'TCMP_CUSTOMTAG'}">
          <c:choose>
            <c:when test="${tag.external}">
              <a href="${tag.url}" target="_blank" style="${tag.style}">${tag.label}</a>
            </c:when>
            <c:when test="${not empty tag.oneBean}">
              <a href="<hst:link hippobean="${tag.oneBean}" />" style="${tag.style}">${tag.label}</a>
            </c:when>
            <c:otherwise>
              <a href="<hst:link hippobean="${tag.bean}" />" style="${tag.style}">${tag.label}</a>
            </c:otherwise>
          </c:choose>
        </c:when>
        <c:otherwise>
          <a href="<hst:link hippobean="${tag.bean}" />" style="${tag.style}">${tag.label}</a>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </li>
</ul>