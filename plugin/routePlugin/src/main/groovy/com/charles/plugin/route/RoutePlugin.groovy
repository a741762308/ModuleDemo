package com.charles.plugin.route

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class RoutePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        if (!hasApp) {
            throw new IllegalStateException("'android application' plugin required.")
        }
        project.android.registerTransform(new RouteTransform())
    }
}
