package com.charles.plugin.base

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.tools.ant.util.FileUtils
import org.gradle.internal.impldep.com.amazonaws.util.IOUtils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.function.Function
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

abstract class AbsTransform extends Transform {
    static final def TAG = "YxTransform"

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

    protected boolean isAttentionFile(String name) {
        return !Utils.isNotAttentionClass(name)
    }

    protected abstract Function<ClassWriter, ClassVisitor> onEachFileClassFile(String name)

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        Utils.log "-------${TAG} start-------"
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
        def costTime = (System.currentTimeMillis() - startTime) / 1000
        Utils.log "-------${TAG} end ==> cost time ${costTime} s -------"

    }

    protected void handDirectory(DirectoryInput input, TransformOutputProvider provider) throws Exception {
//        Utils.log "-------${TAG} handDirectory file ${input.file.name} ------"
        if (input.file.isDirectory()) {
            input.file.eachFileRecurse { file ->
                def fileName = file.name
                if (isAttentionFile(fileName)) {
                    Utils.log "-------${TAG} handDirectory attention class ${fileName} ------"
                    byte[] bytes = Utils.visitCLass(file.bytes, onEachFileClassFile(fileName))
                    def fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + fileName)
                    fos.write(bytes)
                    fos.close()
                }
            }
        }
        def dest = provider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(input.file, dest)
    }

    protected void handJar(JarInput input, TransformOutputProvider provider) throws Exception {
//        Utils.log "-------${TAG} handJar file ${input.file.name} ------"
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
                if (isAttentionFile(entryName)) {
                    Utils.log "-------${TAG} handJar attention class ${entryName} ------"
                    jos.putNextEntry(zipEntry)
                    def bytes = Utils.visitCLass(IOUtils.toByteArray(is), onEachFileClassFile(entryName))
                    jos.write(bytes)
                } else {
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
        }
    }
}
