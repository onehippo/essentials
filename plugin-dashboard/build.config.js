// user configuration for the build process
// via: https://github.com/ngbp/ng-boilerplate/blob/v0.3.1-release/build.config.js

// example selectors for vendor files
// - all JS files 					= '**/*.js',
// - no .min.js files 				= '!**/*.min.js'
// - all JS files from AngularJS 	= 'angular/**/*.js'

module.exports = {

    // directories
    source: 'js/src',
    target: 'target/plugin-dashboard',
    bowerComponents: 'vendors',
    components: 'components'

};