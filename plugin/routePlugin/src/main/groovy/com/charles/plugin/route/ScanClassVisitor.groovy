package com.charles.plugin.route

import com.charles.plugin.base.AbsClassVisitor
import com.charles.plugin.base.Utils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor

import java.util.function.Function

/**
 * 扫描实现了IRouteInject的类
 */
class ScanClassVisitor extends AbsClassVisitor {
    static final def classPackage = "com/charles/route/generate"
    static final def interfaceNames = ["com/charles/route/api/IRouteInject"]

    static final Factory factory = new Factory()

    private ScanClassVisitor(ClassVisitor classVisitor) {
        super(classVisitor)
    }

    static boolean isWatchClass(String name) {
        return !Utils.isNotAttentionClass(name) && attentionClass(name)
    }

    private static boolean attentionClass(String name) {
        return name.startsWith(classPackage) || ClassUtil.hasImplSpecifiedInterfaces(name, interfaceNames.toSet())
    }

    private static class Factory implements Function<ClassWriter, ClassVisitor> {

        @Override
        ClassVisitor apply(ClassWriter classWriter) {
            return new ScanClassVisitor(classWriter)
        }
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        Utils.log("-------find className: ${classInfo.name} interfaces: ${classInfo.interfaces}-------")
        //如果实现IRouteInject接口就添加到集合数据中
        if (ClassUtil.containedTargetInterface(interfaces, interfaceNames.toSet())) {
            RouteTransform.routeInjects.add(classInfo)
        }
    }

    @Override
    MethodVisitor watch(MethodVisitor visitor, ClassInfo classInfo, MethodInfo methodInfo) {
        return null
    }
}
