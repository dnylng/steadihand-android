package com.dnylng.steadihand

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.ui.setupActionBarWithNavController
import com.dnylng.steadihand.util.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val bottomNav by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<BottomNavigationView>(R.id.bottom_nav)
    }

    private val navController by lazy(LazyThreadSafetyMode.NONE) {
        val navGraphIds = listOf(
            R.navigation.nav_foryou,
            R.navigation.nav_comics,
            R.navigation.nav_discover,
            R.navigation.nav_me,
            R.navigation.nav_settings
        )

        bottomNav.setupWithNavController(
            navGraphIds,
            supportFragmentManager,
            R.id.nav_host_container,
            intent
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            observeActionBar()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        observeActionBar()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.value?.navigateUp() ?: false
    }

    override fun onBackPressed() {
        if (navController.value?.navigateUp() == false) {
            super.onBackPressed()
        }
    }

    private fun observeActionBar() {
        navController.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })
    }
}
