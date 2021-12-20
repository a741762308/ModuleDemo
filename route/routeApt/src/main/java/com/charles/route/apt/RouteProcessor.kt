package com.charles.route.apt

import com.charles.route.api.RoutInject
import com.charles.route.apt.RouteProcessor.ProcessorConfig.OPTIONS
import com.charles.route.apt.RouteProcessor.ProcessorConfig.ROUTE_NAME
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(OPTIONS)
@SupportedAnnotationTypes(ROUTE_NAME)
class RouteProcessor : AbstractProcessor() {

    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private var moduleName: String? = null
    private val routePath = arrayListOf<Element>()

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        messager = processingEnv?.messager ?: return
        filer = processingEnv.filer ?: return
        moduleName = processingEnv.options[OPTIONS]
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {

        if (annotations.isNullOrEmpty()) {
            log("无注解")
            return false
        }
        val elements = roundEnv?.getElementsAnnotatedWith(RoutInject::class.java) ?: return false
        if (elements.isEmpty()) {
            log("无注解")
            return false
        }
        elements.forEach fo@{
            log("类名：$it")
            val route = it.getAnnotation(RoutInject::class.java)
            val path = route.path
            if (path.isEmpty()) {
                log("$it path不可为空")
                return@fo
            }
            if (moduleName.isNullOrEmpty()) {
                log(
                    """
                    请配置module信息
                    kapt {
                        arguments {
                            arg("module", project.name)
                        }
                    }
                """.trimIndent()
                )
                return@fo
            }
            routePath.add(it)
        }
        createRoutePathClass()
        return true
    }

    /**
     * 生成
     *  class RouteModuleManager : IRouteInject{
     *       override fun injectRoute(classMap: Map<String, Class<*>>) {
     *              classMap[path]=class
     *       }
     *  }
     */
    private fun createRoutePathClass() {
        if (routePath.isNullOrEmpty()) {
            return log("${moduleName}未使用注解")
        }
        log("${moduleName}生成方法")
        log("参数类型${HashMap::class.asTypeName()}")
        //参数的类型
        val parasType = HashMap::class.asClassName()
            .parameterizedBy(
                String::class.asTypeName(),
                Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(Any::class))
            )
        log("参数类型$parasType")
        //方法名
        val funSpec = FunSpec.builder(FUNC_NAME)
            //override
            .addModifiers(KModifier.OVERRIDE)
            //返回值类型
//            .returns()
            //参数
            .addParameter(FUNC_PARA, parasType)
        routePath.forEach {
            val route = it.getAnnotation(RoutInject::class.java)
            funSpec.addStatement(
                "%N[%S] = %T::class.java",
                FUNC_PARA,
                route.path,
                it.asType().asTypeName()
            )
        }

        //父类
        val superinterface = ClassName("com.charles.route.api", "IRouteInject")
        //文件名
        val fileName = "Route${moduleName}Manager"
        //类
        val type = TypeSpec.classBuilder(fileName)
            .addFunction(funSpec.build())
            .addKdoc("Auto generated, please do not edit it.")
            .addSuperinterface(superinterface)
        //文件
        FileSpec.builder("com.charles.route.generate", fileName)
            .addType(type.build())
            .build()
            .writeTo(filer)

    }

    private fun log(msg: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg)
    }

    companion object ProcessorConfig {
        const val OPTIONS = "module"
        const val ROUTE_NAME = "com.charles.route.api.RoutInject"
        const val FUNC_NAME = "injectRoute"
        const val FUNC_PARA = "classMap"
    }
}