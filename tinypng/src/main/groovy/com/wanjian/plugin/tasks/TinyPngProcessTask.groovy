package com.wanjian.plugin.tasks

import com.tinify.AccountException
import com.tinify.Tinify
import com.wanjian.plugin.utils.MD5
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class TinyPngProcessTask extends DefaultTask {

    def variant
    def compressedPictureFiles
    def excludePictureFiles

    @TaskAction
    void process() {
        println(">>>>>>>>>>Start Compress Pictures")

        compressedPictureFiles = getCompressedPictureFiles();
        excludePictureFiles = getExcludePictureFiles();

        excludePictureFiles.each {
            println("excludePictureFiles  ${it}")
        }
        def allImageMD5s = [];
        def compressingDirs = [];
        long total = 0;
        variant.sourceSets.each { source ->
            source.getRes().getSrcDirs().each { dir ->
                println(">>>> ${dir}")
                if (compressingDirs.contains(dir)) {
                    return;
                } else {
                    compressingDirs.add(dir)
                }
                dir.listFiles(new FilenameFilter() {
                    @Override
                    boolean accept(File file, String s) {
                        return s.startsWith("drawable") || s.startsWith("mipmap")
                    }
                }).each { imgFolder ->
                    imgFolder.listFiles(new FilenameFilter() {
                        @Override
                        boolean accept(File file, String s) {
                            return s.endsWith("xml") == false
                        }
                    }).each { imgFile ->
                        if (skip(imgFile)) {
                            println("skip ${imgFile.getAbsolutePath()}")
                            return
                        }
                        String md5 = MD5.get(imgFile);
                        if (compressedPictureFiles.contains(md5) == false) {
                            long originSize = imgFile.length();
                            if (compressPicture(imgFile)) {
                                File newImage = new File(imgFile.getAbsolutePath());
                                allImageMD5s.add(MD5.get(newImage))
                                println("${(1f * newImage.length() / originSize)}%")
                                total += (originSize - newImage.length())
                            } else {
                                if (project.tinyPng.abortOnError) {
                                    throw new GradleException("tinypng compress failed! ${imgFile.getAbsolutePath()}")
                                } else {
                                    println("tinypng compress failed! ${imgFile.getAbsolutePath()}")
                                }
                            }
                        } else {
                            println("compressed ${imgFile}")
                            allImageMD5s.add(md5)
                        }
                    }
                }
            }
        }
        println("total size ${total / 1024} KB");
        updateMD5Data(allImageMD5s);
    }

    boolean skip(img) {
        if (img.getName().endsWith(".9.png") && project.tinyPng.skip9Png) {
            return true
        }
        String relativePath = img.getAbsolutePath().replaceFirst(project.projectDir.getAbsolutePath(), "");
        for (String path : excludePictureFiles) {
            if (relativePath.contains(path)) {
                return true;
            }
        }
        return false

    }

    boolean compressPicture(img) {
        def keys = project.tinyPng.keys;
        Iterator iterator = keys.iterator()
        while (iterator.hasNext()) {
            String key = iterator.next()
            println("compressing:${img}  key:${key}")
            try {
                Tinify.setKey(key);
                Tinify.validate();
                Tinify.fromFile(img.getAbsolutePath()).toFile(img.getAbsolutePath());
                return true
            } catch (AccountException accountExcep) {
                iterator.remove()
                project.logger.error("Your monthly limit has been exceeded (HTTP 429/TooManyRequests)  key:${key}")
            } catch (Exception e) {
                project.logger.error("tiny png err", e)
            }
        }
        return false
    }

    void updateMD5Data(allImageMD5s) {
        File compressedPicturesFile = new File("${project.projectDir}/compressed_pictures")
        BufferedWriter writer = new BufferedWriter(new FileWriter(compressedPicturesFile, false))
        writer.write("# Do not modify this file !")

        if (project.tinyPng.appendCompressRecord) {
            allImageMD5s.addAll(compressedPictureFiles)
        }
        allImageMD5s.unique()
        allImageMD5s.sort()
        allImageMD5s.each {
            writer.newLine()
            writer.write(it)
        }
        writer.close()
    }

    def getCompressedPictureFiles() {
        File compressedPictureFiles = new File("${project.projectDir}/compressed_pictures")
        def compressedPicturesList = []
        if (compressedPictureFiles.isFile()) {
            compressedPictureFiles.eachLine { line ->
                if (!line.trim().isEmpty() && line.startsWith("#") == false) {
                    compressedPicturesList.add(line.trim())
                }
            }
        }
        return compressedPicturesList;
    }

    def getExcludePictureFiles() {
        File excludePicturesFile = new File("${project.projectDir}/exclude_pictures.txt")
        def excludeList = []
        if (excludePicturesFile.isFile()) {
            excludePicturesFile.eachLine { line ->
                if (!line.trim().isEmpty() && line.startsWith("#") == false) {
                    excludeList.add(line.trim())
                }
            }
        }
        return excludeList;
    }

    void setVariant(variant) {
        this.variant = variant
    }
}
