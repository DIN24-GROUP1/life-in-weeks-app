package com.example.memento.utils

import com.example.memento.LifeGridRoute
import com.example.memento.SettingsRoute
import com.example.memento.StatsRoute


fun routeToIndex(route: String?): Int = when (route) {
        LifeGridRoute::class.qualifiedName -> 0
        StatsRoute::class.qualifiedName -> 1
        SettingsRoute::class.qualifiedName -> 2
        else -> -1 // e.g. StartRoute, hide bar or no tab selected
    }

