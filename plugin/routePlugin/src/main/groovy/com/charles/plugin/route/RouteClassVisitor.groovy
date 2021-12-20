package com.charles.plugin.route

import com.charles.plugin.base.AbsClassVisitor
import com.charles.plugin.base.AbsMethodVisitor
import com.charles.plugin.base.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import java.util.function.Function

/**
 * 调用路由配置
 */
class RouteClassVisitor extends AbsClassVisitor {
    static final def className = "com/charles/route/RouteManager"
    static final def func = "init"
    static final Factory factory = new Factory()

    private RouteClassVisitor(ClassVisitor classVisitor) {
        super(classVisitor)
    }

    static boolean isWatchClass(String name) {
        return "${className}.class" == name
    }

    private static class Factory implements Function<ClassWriter, ClassVisitor> {

        @Override
        ClassVisitor apply(ClassWriter classWriter) {
            return new RouteClassVisitor(classWriter)
        }
    }

    @Override
    MethodVisitor watch(MethodVisitor visitor, ClassInfo classInfo, MethodInfo methodInfo) {
        if (className == classInfo.name) {
            if (func == methodInfo.name) {
                Utils.log("-------find RouteManager  init function-------")
                return new RouteMethodVisitor(visitor)
            }
        }
        return null
    }

    private static class RouteMethodVisitor extends AbsMethodVisitor {

        RouteMethodVisitor(MethodVisitor methodVisitor) {
            super(methodVisitor)
        }

        @Override
        void visitCode() {
            super.visitCode()
            Utils.log("-------RouteManager init start-------")
            Utils.log("-------RouteManager inject size ${RouteTransform.getRouteInjects().size()}-------")

            //System.out.println("-------RouteManager init start-------")
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            mv.visitLdcInsn("-------RouteManager init start-------")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)

            RouteTransform
                    .getRouteInjects()
                    .forEach { classInfo ->
                        //new xxx().injectRoute(sClassMap)
                        mv.visitTypeInsn(Opcodes.NEW, classInfo.name)
                        mv.visitInsn(Opcodes.DUP)
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, classInfo.name, "<init>", "()V", false)
                        mv.visitFieldInsn(Opcodes.GETSTATIC, className, "sClassMap", "Ljava/util/HashMap;")
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classInfo.name, "injectRoute", "(Ljava/util/HashMap;)V", false)
                    }

            //System.out.println("-------RouteManager inject size: ${sClassMap.size}-------")
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
            mv.visitLdcInsn("-------RouteManager inject size: ")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
            mv.visitFieldInsn(Opcodes.GETSTATIC, className, "sClassMap", "Ljava/util/HashMap;")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashMap", "size", "()I", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "（I)Ljava/lang/StringBuilder;", false)
            mv.visitLdcInsn("-------")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)

            //System.out.println("-------RouteManager init end-------")
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            mv.visitLdcInsn("-------RouteManager init end-------")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)

            Utils.log("-------RouteManager init end-------")
        }
    }
}
