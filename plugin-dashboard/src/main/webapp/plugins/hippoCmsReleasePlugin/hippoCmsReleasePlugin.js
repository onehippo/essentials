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

(function () {
    "use strict";

    angular.module('hippo.essentials')
            .controller('hippoCmsReleaseCtrl', function ($scope, $sce, $log, $rootScope, $http) {
                $scope.options = [];
                $scope.introMessage = "Hippo Cms Release plugin inspects the current Hippo Cms Version";
                $scope.pluginClass = "org.onehippo.cms7.essentials.dashboard.hippocmsrelease.HippoCmsReleasePlugin";

                $scope.init = function () {
                    // check if plugin is installed
                    $http.get($rootScope.REST.pluginInstallState + $scope.pluginClass).success(function (data) {
                        // TODO enable check:
                        $scope.pluginInstalled = data.installed;
                    });

                };
                $scope.init();
            });


}());