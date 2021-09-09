package com.chinthaka.chinthaka_beta.ui.main.listeners

import com.chinthaka.chinthaka_beta.data.entities.User

interface NavigationUpdateListener {
    fun onUserDataChanged(user: User)
}