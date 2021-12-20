package com.charles.route.api

interface IRouteInject {
    fun injectRoute(classMap: HashMap<String, Class<out Any>>)
}