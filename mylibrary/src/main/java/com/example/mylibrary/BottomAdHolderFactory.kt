package com.example.mylibrary

import com.example.common.ApplicationConfigProvider
import com.example.common.ApplicationContextProvider
import com.example.common.Preferences
import com.example.common.PreferencesInt

interface BottomFactory


class BottomAdHolderFactory constructor(
        private val preferences: Preferences,
        private val applicationConfigProvider: ApplicationConfigProvider
): BottomFactory