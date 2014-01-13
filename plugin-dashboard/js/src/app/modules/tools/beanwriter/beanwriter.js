(function () {
    "use strict";

    angular.module('hippo.essentials', ['hippo.theme', 'ngRoute', 'localytics.directives'])

            .controller('beanWriterCtrl', function ($scope, $sce, $log, $rootScope, $http, MyHttpInterceptor) {
                $scope.resultMessages = [];
                $scope.runBeanWriter = function () {
                    $http({
                        method: 'POST',
                        url: $rootScope.REST.beanwriter
                    }).success(function (data) {
                        $scope.resultMessages = data;
                    });

                };
            })
})();