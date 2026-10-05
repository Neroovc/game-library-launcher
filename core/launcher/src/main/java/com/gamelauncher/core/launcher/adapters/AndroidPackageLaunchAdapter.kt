package com.gamelauncher.core.launcher.adapters

import com.gamelauncher.core.launcher.Installation
import com.gamelauncher.core.launcher.LaunchAdapter
import com.gamelauncher.core.launcher.LaunchResult

class AndroidPackageLaunchAdapter : LaunchAdapter {
    override fun canLaunch(installation: Installation): Boolean {
        return installation.packageName != null
    }

    override fun launch(installation: Installation): LaunchResult {
        return LaunchResult.UnsupportedPlatform
    }
}
