package com.sliit.se3092.GanuDenu

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
// The base application starts Koin for authentication and income; Hilt supplies Room.
class GanuDenuApp : GanuDenuApplication()
