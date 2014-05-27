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
  <link rel="stylesheet" href="${pageContext.request.contextPath}/theme/hippo-theme/main.css"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/hippo-essentials.css"/>
  <%--<script src="${pageContext.request.contextPath}/js/jquery.js"></script>--%>
  <script src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/jquery.ui.min.js"></script>

  <script src="${pageContext.request.contextPath}/js/lib/angular.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/angular-route.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/angular-ui-router.js"></script>

  <script src="${pageContext.request.contextPath}/js/lib/chosen.jquery.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/chosen.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/ui-sortable.js"></script>
  <script src="${pageContext.request.contextPath}/js/lib/ui-utils.js"></script>

  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/chosen.css"/>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/chosen-spinner.css"/>
  <%--<script src="${pageContext.request.contextPath}/js/require.js" data-main="${pageContext.request.contextPath}/js/main.js"></script>--%>
  <%--<script src="${pageContext.request.contextPath}/js/require.js"></script>--%>

  <!--<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.3/angular.min.js"></script>-->
  <%--  <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.3/angular-route.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>--%>
  <script src="${pageContext.request.contextPath}/js/lib/ui-bootstrap-0.10.js"></script>
  <script src="${pageContext.request.contextPath}/js/Essentials.js"></script>
  <script src="${pageContext.request.contextPath}/js/app.js"></script>
  <script src="${pageContext.request.contextPath}/js/routes.js"></script>
  <script src="${pageContext.request.contextPath}/js/controllers.js"></script>
  <script src="${pageContext.request.contextPath}/js/layout.js"></script>
  <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
  <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
</head>
<body id="container" class="essentials-skin">

<header class="header">
  <a href="/essentials" class="logo">Hippo Essentials</a>
  <nav class="navbar navbar-static-top" role="navigation">
    <a href="#" class="navbar-btn sidebar-toggle" data-toggle="offcanvas" role="button">
      <span class="sr-only">Toggle navigation</span>
      <span class="icon-bar"></span>
      <span class="icon-bar"></span>
      <span class="icon-bar"></span>
    </a>
    <form action="#" method="get" class="search-form">
      <div class="input-group">
        <input type="text" name="q" class="form-control" placeholder="Find plugins..."/>
        <span class="input-group-btn">
        <button type='submit' name='search' id='search-btn' class="btn btn-primary">
            <i class="fa fa-search"></i></button>
        </span>
      </div>
    </form>

    <div class="navbar-right">
      <ul class="nav navbar-nav">
        <li class="dropdown messages-menu ng-hide" ng-show="busyLoading">
          <div class="busy-loader ng-hide" >
            <img src="${pageContext.request.contextPath}/images/loader.gif"/>
          </div>
        </li>
      </ul>
    </div>
  </nav>
</header>
<div class="wrapper row-offcanvas row-offcanvas-left">
  <aside class="left-side sidebar-offcanvas">
    <section class="sidebar" ng-controller="mainMenuCtrl">
      <ul class="sidebar-menu" ng-show="packsInstalled">
        <li class="treeview">
          <a href="#">
            <i class="fa fa-bar-chart-o"></i>
            <span>Plugins</span>
            <i class="fa fa-angle-left pull-right"></i>
          </a>
          <ul class="treeview-menu">
            <li ng-repeat="plugin in plugins | filter:{needsInstallation:false} | filter:{type:'plugins'} | orderBy:'name'">
              <a href="#/plugins/{{plugin.pluginId}}" ng-click="showPluginDetail(plugin.pluginId)"><i class="fa fa-angle-double-right"></i>{{plugin.name}}</a>
            </li>
          </ul>
        </li>
        <li class="treeview">
          <a href="#">
            <i class="fa fa-gears"></i>
            <span>Tools</span>
            <i class="fa fa-angle-left pull-right"></i>
          </a>
          <ul class="treeview-menu">
            <li ng-repeat="plugin in plugins | filter:{needsInstallation:false} | filter:{type:'tools'} | orderBy:'name'">
              <a href="#/tools/{{plugin.pluginId}}" ng-click="showPluginDetail(plugin.pluginId)"><i class="fa fa-angle-double-right"></i>{{plugin.name}}</a>
            </li>
          </ul>
        </li>
        <li>
          <a target="API" href="${pageContext.request.contextPath}/docs/rest-api/index.html">
            <i class="fa fa-external-link"></i>
            <span>REST API</span>
          </a>
        </li>
        <li>
          <a target="FEEDBACK" href="https://issues.onehippo.com/rest/collectors/1.0/template/form/a23eddf8?os_authType=none">
            <i class="fa fa-envelope"></i> <span>Feedback</span>
          </a>
        </li>
      </ul>
    </section>
  </aside>
  <aside class="right-side" ng-controller="homeCtrl">
    <section class="content-header">
    </section>
    <section class="content">
      <div ui-view></div>
    </section>
  </aside>
</div>
<script src="${pageContext.request.contextPath}/js/loader.js" data-modules="http://localhost:8080/essentials/rest/plugins/modules"></script>
</body>

</html>