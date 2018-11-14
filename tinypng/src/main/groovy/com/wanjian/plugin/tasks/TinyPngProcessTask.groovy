package com.wanjian.plugin.tasks

import com.tinify.AccountException
import com.tinify.Tinify
import com.wanjian.plugin.utils.MD5
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TinyPngProcessTask extends DefaultTask {

    def variant
    String[] compressedPictureFiles
    String[] excludePictureFiles

    @TaskAction
    void process() {
        println(">>>>>>>>>>Start Compress Pictures")

        compressedPictureFiles = getCompressedPictureFiles();
        excludePictureFiles = getExcludePictureFiles();

        excludePictureFiles.each {
            println("excludePictureFiles  " + it)
        }
        def allImageMD5s = [];
        def compressingDirs = [];
        long total = 0;
        variant.sourceSets.each { source ->
            source.getRes().getSrcDirs().each { dir ->
                println(">>>>" + dir)
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
                            println("skip " + imgFile.getAbsolutePath())
                            return
                        }
                        String md5 = MD5.get(imgFile);
                        if (compressedPictureFiles.contains(md5) == false) {
                            print("compressing " + imgFile)
                            long originSize = imgFile.length();
                            //处理
                            if (compressPicture(imgFile)) {
                                File newImage = new File(imgFile.getAbsolutePath());
                                allImageMD5s.add(MD5.get(newImage))
                                println(" " + (1f * newImage.length() / originSize) + "%")
                                total += (newImage.length() - originSize)
                            } else {
                                println(" tinypng compress failed!")
                            }
                        } else {
                            println("compressed " + imgFile)
                            allImageMD5s.add(md5)
                        }
                    }
                }
            }
        }
        println("total size " + (total / 1024) + "KB");
        updateMD5Data(allImageMD5s);
    }

    boolean skip(img) {
        if (img.getName().endsWith(".9.png")) {
            return true
        }
        for (String path : excludePictureFiles) {
            if (img.getAbsolutePath().contains(path)) {
                return true;
            }
        }
        return false

    }

    boolean compressPicture(img) {
        def keys = project.tinyPng.keys;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys[i];
            try {
                Tinify.setKey(key);
                Tinify.validate();
                Tinify.fromFile(img.getAbsolutePath()).toFile(img.getAbsolutePath());
                return true
            } catch (AccountException accountExcep) {
                project.logger.error("Your monthly limit has been exceeded (HTTP 429/TooManyRequests)  key:" + key)
            } catch (Exception e) {
                project.logger.error("tinypng err", e)
            }
        }

        return false

    }

    void updateMD5Data(allImageMD5s) {
        File compressedPicturesFile = new File("${project.projectDir}/compressed_pictures")
        BufferedWriter writer = new BufferedWriter(new FileWriter(compressedPicturesFile, false))
        writer.write("# Do not modify this file !")
        allImageMD5s.each {
            writer.newLine()
            writer.write(it)
        }
        writer.close()
    }

    String[] getCompressedPictureFiles() {
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

    String[] getExcludePictureFiles() {
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
