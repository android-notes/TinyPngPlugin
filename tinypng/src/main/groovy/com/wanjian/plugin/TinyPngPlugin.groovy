package com.wanjian.plugin

import com.wanjian.plugin.config.TinyPng
import com.wanjian.plugin.tasks.TinyPngProcessTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class TinyPngPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
//
        def variants;
        if (project.plugins.hasPlugin('com.android.application')) {
            variants = project.android.applicationVariants
        } else if (project.plugins.hasPlugin('com.android.library')) {
            variants = project.android.libraryVariants
        } else {
            throw new GradleException('Android plugin required')
        }

        project.extensions.create('tinyPng', TinyPng)

        project.afterEvaluate {
            if (project.tinyPng.enable == false) {
                project.logger.error("tinypng is disabled.")
                return
            }

            variants.all { variant ->
                def variantName = variant.name.capitalize()
                TinyPngProcessTask tinyPngProcessTask = project.tasks.create("tinyPng${variantName}", TinyPngProcessTask)
                tinyPngProcessTask.setVariant(variant)
//                project.tasks.findByName("generate${variantName}BuildConfig").dependsOn tinyPngProcessTask
                project.tasks.findByName("generate${variantName}Resources").dependsOn tinyPngProcessTask
            }
        }
    }
}
