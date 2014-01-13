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

//############################################
// ROUTES
//############################################
(function () {
    "use strict";

    angular.module('hippo.essentials', ['hippo.theme', 'ngRoute', 'localytics.directives'])

.config(function ($routeProvider) {

    $routeProvider
            .when('/', {
                templateUrl: 'pages/home.html',
                controller: 'homeCtrl',
                resolve: {
                    factory: checkPackInstalled
                }
            })
            .when('/powerpacks', {
                templateUrl: 'plugins/newsEventsPowerpack/index.html',
                controller: 'newsEventsCtrl'
            }).when('/plugins', {
                templateUrl: 'pages/plugins.html',
                controller: 'pluginCtrl'
            }).when('/find-plugins', {
                templateUrl: 'pages/find-plugins.html',
                controller: 'pluginCtrl'
            }).when('/tools', {
                templateUrl: 'pages/tools.html',
                controller: 'toolCtrl'
            })
        //############################################
        // PLUGINS: TODO make dynamic
        //############################################
            .when('/plugins/:pluginId',
            {
                templateUrl: function (params) {
                    return 'plugins/' + params.pluginId + '/index.html';
                },
                controller: 'pluginLoaderCtrl'

            })
            .when('/tools/:toolId',
            {
                templateUrl: function (params) {
                    return 'tools/' + params.toolId + '/index.html';
                },
                controller: 'toolCtrl'

            })
            .otherwise({redirectTo: '/'})

});

var checkPackInstalled = function ($q, $rootScope, $location, $http, $log, MyHttpInterceptor) {
    $rootScope.checkDone = true;
    if ($rootScope.packsInstalled) {
        $log.info("powerpack is installed");
        return true;
    } else {
        var deferred = $q.defer();
        $http.get($rootScope.REST.status + 'powerpack')
                .success(function (response) {
                    $rootScope.packsInstalled = response.status;
                    deferred.resolve(true);
                    if (!$rootScope.packsInstalled) {
                        $location.path("/powerpacks");
                    }

                })
                .error(function () {
                    deferred.reject();
                    $rootScope.packsInstalled = false;
                    $location.path("/powerpacks");
                });
        return deferred.promise;
    }
}})();