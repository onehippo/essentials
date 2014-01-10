'use strict';
module.exports = function (grunt) {

    // display execution time of each task
    require('time-grunt')(grunt);

    // load all grunt tasks automatically
    require('load-grunt-tasks')(grunt);

    // Load in our build configuration file.
    var buildConfig = require( './build.config.js' );
    buildConfig.remappedVendorFiles = [];

    // re-map component files to use in distribution, so it can by used by the src property
    var declutterConf = grunt.file.readJSON('declutter.conf.json');
    var components = Object.keys(declutterConf);
    components.forEach(function (component) {
        var componentRules = declutterConf[component];
        componentRules.forEach(function (rule) {
            buildConfig.remappedVendorFiles.push(component + '/' + rule);
        });
    });
    
    // project configuration
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        hippo: buildConfig,

        // compile less files for the app and modules
        less: {
            src: {
                expand: true,
                cwd: 'js/src/',
                src: [
                    'app/**/*.less',
                    'app/mock.less',
                    '<%= hippo.components %>/**/*.less',
                    '!**/_*.less'
                ],
                dest: '<%= hippo.target %>',
                ext: '.css'
            }
        },

        // clean target (distribution) folder
        clean: {
            target: {
                files: [{
                    dot: true,
                    src: [
                        '<%= hippo.target %>/*',
                        '!<%= hippo.target %>/META-INF',
                        '!<%= hippo.target %>/WEB-INF'
                    ]
                }]
            },

            docs: {
                files: [{
                    dot: true,
                    src: [
                        '<%= hippo.target %>/docs/*'
                    ]
                }]
            },

            bower: {
                files: [{
                    dot: true,
                    src: [
                        '<%= hippo.source %>/<%= hippo.bowerComponents %>/**'
                    ]
                }]
            }
        },

        // copy files
        copy: {
            html: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>',
                        dest: '<%= hippo.target %>',
                        src: [
                            '*.html',
                            'app/**/*.html'
                        ]
                    }
                ]
            },

            javascript: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>/app',
                        dest: '<%= hippo.target %>/app',
                        src: ['**/*.js']
                    }]
            },


            images: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>/app',
                        dest: '<%= hippo.target %>',
                        src: ['**/assets/images/**/*.{png,jpg,gif,jpeg}']
                    }
                ]
            },

            css: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>/app',
                        dest: '<%= hippo.target %>/app',
                        src: ['**/*.css']
                    }]
            },

            json: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>',
                        dest: '<%= hippo.target %>',
                        src: ['*.json']
                    }
                ]
            },

            dist: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>',
                        dest: '<%= hippo.target %>',
                        src: [
                            '.htaccess',
                            '*.html',
                            '*.json',
                            'app/**/*.html',
                            'app/**/assets/fonts/*.{eot,woff,ttf,svg}',
                            'app/**/assets/images/**/*.{png,jpg,gif,jpeg}',
                            'app/**/i18n/*.json',
                            'app/**/*.{css,js}'
                        ]
                    },
                    {
                        src: '<%= hippo.source %>/index.html',
                        dest: '<%= hippo.target %>/angular-perspective.html'
                    }
                ]
            },

            mock: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>/app/modules/shared/mocked-data',
                        dest: '<%= hippo.target %>/app/modules/shared/mocked-data',
                        src: [
                            'prevent-mocked-data.js',
                            'mocked-data-empty.js'
                        ],
                        rename: function (dest, src) {
                            if (grunt.option('mock')) {
                                return dest + '/' + src;
                            }

                            if (!grunt.option('mock') && !grunt.option('mock-empty') && (src == 'prevent-mocked-data.js')) {
                                return dest + '/mocked-data.js';
                            }

                            if (grunt.option('mock-empty') && (src == 'mocked-data-empty.js')) {
                                return dest + '/mocked-data.js';
                            }

                            return dest + '/' + src;
                        }
                    }
                ]
            },

            bowerComponents: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>/<%= hippo.bowerComponents %>',
                        dest: '<%= hippo.target %>/<%= hippo.components %>',
                        src: '<%= hippo.remappedVendorFiles %>'
                    }
                ]
            },

            components: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>/<%= hippo.components %>',
                        dest: '<%= hippo.target %>/<%= hippo.components %>',
                        src: '<%= hippo.remappedVendorFiles %>'
                    }
                ]
            },

            docs: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= hippo.source %>/docs',
                        dest: '<%= hippo.target %>/docs',
                        src: '**/**.*'
                    }
                ]
            }
        },

        // watch
        watch: {
            less: {
                files: [
                    '<%= hippo.source %>/app/**/*.less',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.less'
                ],
                tasks: ['less']
            },

            html: {
                files: [
                    '<%= hippo.source %>/*.html',
                    '<%= hippo.source %>/app/**/*.html',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.html'
                ],
                tasks: ['copy:html', 'copy:components']
            },

            javascript: {
                files: [
                    '<%= hippo.source %>/app/**/*.js',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.js'
                ],
                tasks: ['copy:javascript', 'copy:components']
            },


            images: {
                files: [
                    '<%= hippo.source %>/app/**/*.{png,jpg,jpeg,gif}',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.{png,jpg,jpeg,gif}'
                ],
                tasks: ['copy:images', 'copy:components']
            },

            css: {
                files: [
                    '<%= hippo.source %>/app/**/*.css',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.css'
                ],
                tasks: ['copy:css', 'copy:components']
            },

            json: {
                files: [
                    '<%= hippo.source %>/modules.json'
                ],
                tasks: ['copy:json']
            },

            docs: {
                files: [
                    '<%= hippo.source %>/docs/**/*.*'
                ],
                tasks: ['copy:docs']
            },

            livereload: {
                options: {
                    livereload: '<%= connect.options.livereload %>'
                },
                files: [
                    '<%= hippo.source %>/*.html',

                    '<%= hippo.source %>/app/**/*.html',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.html',
                    
                    '<%= hippo.source %>/app/**/*.js',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.js',



                    '<%= hippo.source %>/app/**/*.{css,less}',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.{css,less}',

                    '<%= hippo.source %>/app/**/*.{png,jpg,jpeg,gif}',
                    '<%= hippo.source %>/<%= hippo.components %>/**/*.{png,jpg,jpeg,gif}'
                ]
            }
        },



        // add new components
        shell: {
            gitclone: {
                options: {
                    execOptions: {
                        cwd: '<%= hippo.source %>/<%= hippo.components %>'
                    }
                },
                command: function (repositoryUrl) {
                    // create components folder when it is not present
                    if (!grunt.file.isDir(buildConfig.source + '/' + buildConfig.components)) {
                        grunt.file.mkdir(buildConfig.source + '/' + buildConfig.components);
                    }

                    // this task can be extended with the branchname etc. later on
                    // http://stackoverflow.com/a/18788461/363448
                    return 'git clone http://github.com/' + repositoryUrl;
                }
            }
        },

        // declutter
        declutter: {
            options: {
                rules: grunt.file.readJSON('declutter.conf.json')
            },
            files: [
                '<%= hippo.source %>/<%= hippo.bowerComponents %>/*',
                '<%= hippo.source %>/<%= hippo.components %>/*'
            ]
        }
    });

    // default
    grunt.registerTask('default', ['build']);

    // build
    grunt.registerTask('build', function (target) {
        var tasks = [
            'declutter',
            'clean:target',
            'copy:dist',
            'copy:components',
            'copy:bowerComponents',
            'less'
        ];

        grunt.task.run(tasks);
    });


    // server
    grunt.registerTask('server', function (target) {
        if (target === 'watch') {
            return grunt.task.run(['build', 'connect:livereload', 'watch' ]);
        }

        if (target === 'docs') {
            return grunt.task.run(['clean:docs', 'copy:docs', 'connect:docs:keepalive' ]);
        }

        return grunt.task.run(['build', 'connect:dist:keepalive']);
    });

    // add module
    grunt.registerTask('addModule', function (target) {
        return grunt.task.run(['shell:gitclone:' + target]);
    });
};
