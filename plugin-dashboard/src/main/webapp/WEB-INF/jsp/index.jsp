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
<!doctype html>
<html>
<head>
  <title>Hippo Essentials</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/components/angular-ui-tree/dist/angular-ui-tree.min.css"/>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/components/chosen/chosen.css"/>

  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/essentials-less/hippo-essentials.css"/>

  <script src="${pageContext.request.contextPath}/components/jquery/jquery.js"></script>
  <script src="${pageContext.request.contextPath}/components/angular/angular.js"></script>
  <script src="${pageContext.request.contextPath}/components/angular/angular-sanitize.min.js"></script>
  <script src="${pageContext.request.contextPath}/components/chosen/chosen.jquery.js"></script>
  <script src="${pageContext.request.contextPath}/components/underscore/underscore.js"></script>

  <script src="${pageContext.request.contextPath}/components/bootstrap/dist/js/bootstrap.js"></script>

  <script src="${pageContext.request.contextPath}/components/angular-bootstrap/ui-bootstrap.min.js"></script>
  <script src="${pageContext.request.contextPath}/components/angular-bootstrap/ui-bootstrap-tpls.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/angular-route.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/angular-ui-router.js"></script>

  <script src="${pageContext.request.contextPath}/js/lib/chosen.js"></script>

  <%-- HIPPO THEME DEPS --%>
  <script src="${pageContext.request.contextPath}/components/angular-ui-tree/dist/angular-ui-tree.js"></script>
  <script src="${pageContext.request.contextPath}/components/hippo-plugins/dist/js/main.js"></script>
  <script src="${pageContext.request.contextPath}/components/hippo-theme/dist/js/main.js"></script>

  <%-- ESSENTIALS --%>
  <script src="${pageContext.request.contextPath}/js/Essentials.js"></script>
  <script src="${pageContext.request.contextPath}/js/app.js"></script>
  <script src="${pageContext.request.contextPath}/js/routes.js"></script>
  <script src="${pageContext.request.contextPath}/js/directives.js"></script>
  <script src="${pageContext.request.contextPath}/js/controllers.js"></script>

  <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
  <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
</head>
<body id="container" ng-cloak>

<!-- LOADER ON HTTP REQUESTS -->
<div class="busy-loader ng-hide" ng-show="busyLoading">
  <%--<span class="fa fa-spin fa-refresh"></span>&nbsp;--%>
  <img src="${pageContext.request.contextPath}/images/loader.gif"/>
</div>
<!-- ERROR MESSAGES -->
<div class="alert-danger messages ng-hide" ng-show="globalError.length > 0">
  <strong>An error occurred:</strong>
  <div ng-repeat="message in globalError">
    {{message}}
  </div>
</div>
<div class="alert-success messages ng-hide" ng-show="feedbackMessages.length > 0">
  <div ng-repeat="message in feedbackMessages">
    <strong>{{message}}</strong>
  </div>
</div>
<%--
  CONTENT
--%>

<div  class="container">
  <div class="row">
    <h1 class="page-header">
      <div class="pull-left hippo-header-logo">
        <a href="${pageContext.request.contextPath}"><img src="${pageContext.request.contextPath}/images/hippo-logo.png"></a>
      </div>
      <div class="text-center">Hippo CMS<small>Essentials</small></div>
    </h1>
  </div>

  <div class="row">
    <div class="col-sm-2" style="margin-right: 20px;" ng-controller="mainMenuCtrl">
      <ul class="nav nav-stacked nav-pills" ng-show="packsInstalled">
        <li ng-repeat="item in menu" ng-class="{true:'active', false:''}[isPageSelected('{{item.link}}')]">
          <a href="{{item.link}}" ng-click="onMenuClick(item)">{{item.name}}</a>
        </li>
        <li>
          <a target="FEEDBACK" href="https://issues.onehippo.com/rest/collectors/1.0/template/form/a23eddf8?os_authType=none">Feedback</a>
        </li>
      </ul>
    </div>

    <div class="col-sm-9" ng-controller="homeCtrl">
      <div ui-view="submenu" autoscroll="false"></div>
      <div ui-view="plugintabs" autoscroll="false"></div>
      <div style="margin-left: 220px;" ui-view="plugininstance" autoscroll="false"></div>
      <div ui-view autoscroll="false"></div>
      <%--<div ng-view></div>--%>
    </div>
  </div>
  <div class="row footer">
    <p class="text-center">
      (C) 2013-2014 <a href="http://www.onehippo.com">Hippo B.V.</a>, All Rights Reserved |
      <a target="API" href="${pageContext.request.contextPath}/docs/rest-api/index.html">REST API</a>
    </p>
  </div>

</div>

<!-- Include the loader.js script -->
<script src="${pageContext.request.contextPath}/js/loader.js" data-modules="http://localhost:8080/essentials/rest/plugins/modules"></script>

</body>
</html>