package com.charles.plugin.base

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.function.Function

class Utils {
    static boolean debug = true

    static def log(msg) {
        if (debug) {
            println(msg)
        }
    }

    static boolean isNotAttentionClass(String name) {
        return name.startsWith("R\$") || "R.class" == name || "BuildConfig.class" == name || !name.endsWith(".class")
    }

    static byte[] visitClass(byte[] bytes, Function<ClassWriter, ClassVisitor> mapper) {
        def classReader = new ClassReader(bytes)
        def classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        classReader.accept(mapper.apply(classWriter), ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}