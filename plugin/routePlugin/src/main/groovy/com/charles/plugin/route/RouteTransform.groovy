package com.charles.plugin.route

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.charles.plugin.base.AbsClassVisitor
import com.charles.plugin.base.Utils
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class RouteTransform extends Transform {

    private static def TAG = "RouteTransform"

    static final def routeInjects = new HashSet<AbsClassVisitor.ClassInfo>()

    private final def targetFile = new HashSet<File>()
    private final def targetJar = new HashSet<File>()

    @Override
    String getName() {
        return getClass().getSimpleName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        Utils.log "-------${TAG} start-------"
        routeInjects.clear()
        targetFile.clear()
        targetJar.clear()
        def startTime = System.currentTimeMillis()
        def inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }
        inputs.each { input ->
            input.directoryInputs.each { directory ->
                handDirectory(directory, outputProvider)
            }
            input.jarInputs.each { jar ->
                handJar(jar, outputProvider)
            }
        }
        if (!targetFile.isEmpty()) {
            targetFile.forEach { file ->
                Utils.log "-------${TAG} handDirectory attention class ${file.name} ------"
            }
        }
        if (!targetJar.isEmpty()) {
            targetJar.forEach { jar ->
                Utils.log "-------${TAG} handJar attention class ${jar.name} ------"
                processJar(jar)
            }
        }
        def costTime = (System.currentTimeMillis() - startTime) / 1000
        Utils.log "-------${TAG} end ==> cost time ${costTime} s -------"
    }

    private static void processJar(File file) throws Exception {
        if (file.absolutePath.endsWith(".jar")) {
            def jarFile = new JarFile(file)
            def entries = jarFile.entries()
            def tmpFile = new File(file.parent + File.separator + "class_opt.jar")
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            def jos = new JarOutputStream(new FileOutputStream(tmpFile))
            while (entries.hasMoreElements()) {
                def jarEntry = (JarEntry) entries.nextElement()
                def entryName = jarEntry.name
                def zipEntry = new ZipEntry(entryName)
                def is = jarFile.getInputStream(jarEntry)
                if (RouteClassVisitor.isWatchClass(entryName)) {
                    Utils.log "-------${TAG} handJar attention class ${entryName} ------"
                    jos.putNextEntry(zipEntry)
                    def bytes = Utils.visitClass(IOUtils.toByteArray(is), RouteClassVisitor.factory)
                    jos.write(bytes)
                } else {
                    jos.putNextEntry(zipEntry)
                    jos.write(IOUtils.toByteArray(is))
                }
                jos.closeEntry()
            }
            jos.close()
            jarFile.close()
            FileUtils.copyFile(tmpFile, file)
            tmpFile.delete()
        }
    }

    protected void handDirectory(DirectoryInput input, TransformOutputProvider provider) throws Exception {
        if (input.file.isDirectory()) {
            input.file.eachFileRecurse { file ->
                def fileName = file.name
                if (ScanClassVisitor.isWatchClass(fileName)) {
                    Utils.log "-------${TAG} findDirectory attention class ${fileName} ------"
                    byte[] bytes = Utils.visitClass(file.bytes, ScanClassVisitor.factory)
                    def fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + fileName)
                    fos.write(bytes)
                    fos.close()
                } else if (RouteClassVisitor.isWatchClass(fileName)) {
                    Utils.log "-------${TAG} findDirectory attention class ${fileName} ------"
                    targetFile.add(file)
                }
            }
        }
        def dest = provider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(input.file, dest)
    }

    protected void handJar(JarInput input, TransformOutputProvider provider) throws Exception {
        def findFlag = false
        if (input.file.absolutePath.endsWith(".jar")) {
            def jarName = input.name
            def md5 = DigestUtils.md5Hex(input.file.absolutePath)
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            def jarFile = new JarFile(input.file)
            def entries = jarFile.entries()
            def tmpFile = new File(input.file.parent + File.separator + "class_temp.jar")
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            def jos = new JarOutputStream(new FileOutputStream(tmpFile))
            while (entries.hasMoreElements()) {
                def jarEntry = (JarEntry) entries.nextElement()
                def entryName = jarEntry.name
                def zipEntry = new ZipEntry(entryName)
                def is = jarFile.getInputStream(jarEntry)
                if (ScanClassVisitor.isWatchClass(entryName)) {
                    Utils.log "-------${TAG} findJar attention class ${entryName} ------"
                    jos.putNextEntry(zipEntry)
                    def bytes = Utils.visitClass(IOUtils.toByteArray(is), ScanClassVisitor.factory)
                    jos.write(bytes)
                } else {
                    if (RouteClassVisitor.isWatchClass(entryName)) {
                        Utils.log "-------${TAG} findJar attention class ${entryName} ------"
                        findFlag = true
                    }
                    jos.putNextEntry(zipEntry)
                    jos.write(IOUtils.toByteArray(is))
                }
                jos.closeEntry()
            }
            jos.close()
            jarFile.close()
            def dest = provider.getContentLocation(jarName + md5, input.contentTypes, input.scopes, Format.JAR)
            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
            if (findFlag) {
                targetJar.add(dest)
            }
        }
    }

}
