package com.charles.plugin.base

import org.objectweb.asm.MethodVisitor

interface IClassMethodVisitor {
    MethodVisitor watch(MethodVisitor visitor, AbsClassVisitor.ClassInfo classInfo, AbsClassVisitor.MethodInfo methodInfo)
}
