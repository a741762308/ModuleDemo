package com.charles.plugin.route

import org.objectweb.asm.ClassReader;

class ClassUtil {
    static boolean hasImplSpecifiedInterfaces(String className, Set<String> interfaceSet) {
        try {
            ClassReader reader = new ClassReader(className)
            return hasImplSpecifiedInterfaces(reader, interfaceSet)
        } catch (Exception ignore) {
            return false
        }
    }
    /**
     * 判断是否实现了指定接口
     * @param reader
     * @param interfaceSet
     * @return
     */
    static boolean hasImplSpecifiedInterfaces(ClassReader reader, Set<String> interfaceSet) {
        if (isObject(reader.className)) {
            return false
        }
        try {
            if (containedTargetInterface(reader.getInterfaces(), interfaceSet)) {
                return true
            } else {
                ClassReader parent = new ClassReader(reader.getSuperName())
                return hasImplSpecifiedInterfaces(parent, interfaceSet)
            }
        } catch (IOException ignore) {
            return false
        }
    }
    /**
     * 检查当前类是 Object 类型
     * @param className
     * @return
     */
    static boolean isObject(String className) {
        return "java/lang/Object" == className
    }

    /**
     * 检查接口及其父接口是否实现了目标接口
     * @param interfaces 待检查接口
     * @param interfaceSet 目标接口
     * @return
     */
    static boolean containedTargetInterface(String[] interfaces, Set<String> interfaceSet) {
        for (String inter : interfaces) {
            if (interfaceSet.contains(inter)) {
                return true
            } else {
                ClassReader reader = new ClassReader(inter)
                if (containedTargetInterface(reader.getInterfaces(), interfaceSet)) {
                    return true
                }
            }
        }
        return false
    }
}
