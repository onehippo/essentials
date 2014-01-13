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

/**
 * needed for dynamic loading of controllers
 * @type object {{$controllerProvider: $controllerProvider,
        $compileProvider: $compileProvider,
        $provide: $provide}}
 * @global
 */
(function () {
    "use strict";

    angular.module('hippo.essentials', ['hippo.theme',  'ngRoute', 'localytics.directives'])

//############################################
// GLOBAL LOADING
//############################################
    .config(function ($provide, $httpProvider, $controllerProvider, $compileProvider) {

        $provide.factory('MyHttpInterceptor', function ($q, $rootScope, $log) {
            return {
                //############################################
                // REQUEST
                //############################################
                request: function (config) {
                    $rootScope.busyLoading = true;
                    return config || $q.when(config);
                },
                requestError: function (error) {
                    $rootScope.busyLoading = true;
                    $rootScope.globalError = [];
                    if (error.data) {
                        $rootScope.globalError.push(error.data);
                    }
                    else {
                        $rootScope.globalError.push(error.status);
                    }
                    return $q.reject(error);
                },

                //############################################
                // RESPONSE
                //############################################
                response: function (data) {
                    $rootScope.busyLoading = false;
                    $rootScope.globalError = [];
                    $log.info(data);
                    return data || $q.when(data);
                },
                responseError: function (error) {
                    $rootScope.busyLoading = false;
                    $rootScope.globalError = [];
                    if (error.data) {
                        $rootScope.globalError.push(error.data);
                    }
                    else {
                        $rootScope.globalError.push(error.status);
                    }
                    $log.error(error);
                    return $q.reject(error);
                }
            };
        });
        $httpProvider.interceptors.push('MyHttpInterceptor');
    })

//############################################
// RUN
//############################################


    .run(function ($rootScope, $location, $log, $http, $templateCache, MyHttpInterceptor) {
        $rootScope.headerMessage = "Welcome on the Hippo Trail";
        // routing listener
        $rootScope.$on('$routeChangeStart', function (event, next, current) {
            // check if we need powerpacks install check
            /*if(!$rootScope.checkDone && ($location.url() != "/" || $location.url() != "")){
                var url = $location.url();
                $log.info("Redirecting to [/]: needs powerpack install check:");
                $location.path('/');
            }*/

        });



        var root = 'http://localhost:8080/essentials/rest';
        var plugins = root + "/plugins";
        /* TODO generate this server side */
        $rootScope.REST = {
            root: root,
            menus: root + '/menus/',
            /**
             * Returns list of all plugins
             */
            plugins: root + '/plugins/',
            /**
             * Returns list of all plugins that need configuration
             */
            pluginsToConfigure: plugins + '/configure/list/',
            /**
             *Add a plugin to the list of plugins that need configuration:
             * POST method
             * DELETE method deletes plugin from the list
             */
            pluginsAddToConfigure: plugins + '/configure/add/',
            /**
             *
             * /installstate/{className}
             */
            pluginInstallState: plugins + '/installstate/',
            /**
             *  * /install/{className}
             */
            pluginInstall: plugins + '/install/',

            status: root + '/status/',
            powerpacks: root + '/powerpacks/',
            beanwriter: root + '/beanwriter/',
            documentTypes: root + '/documenttypes/',
            controllers: root + '/controllers/',
            powerpacks_install: root + '/powerpacks/install/'  ,

            compounds: root + '/documenttypes/compounds',
            compoundsCreate: root + '/documenttypes/compounds/create/' ,
            contentblocksCreate: root + '/documenttypes/compounds/contentblocks/create/'
        };

        $rootScope.initData = function () {
            $http({
                method: 'GET',
                url: $rootScope.REST.controllers
            }).success(function (data) {
                        $rootScope.controllers = data;
                        // load all controller files:
                        /*var controller = $rootScope.controllers.controller;
                         for(var i = 0; i < controller.length; i++){
                         $log.info(controller[i].id);
                         require(["plugins/"+ controller[i].id+"/controller.js"]);
                         $log.info("Loaded: " + controller[i].id);
                         }*/

                    });

        };



        $rootScope.initData();
    })

//############################################
// FILTERS
//############################################
    .filter('startsWith', function () {
        return function (inputCollection, inputString) {
            var collection = [];
            if (inputCollection && inputString) {
                for (var i = 0; i < inputCollection.length; i++) {
                    if (inputCollection[i].value.slice(0, inputString.length) == inputString) {
                        collection.push(inputCollection[i]);
                    }
                }
                return collection;
            }
            return collection;
        }
    });
})();


