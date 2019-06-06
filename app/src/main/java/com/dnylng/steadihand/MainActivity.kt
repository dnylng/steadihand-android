package com.dnylng.steadihand

import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.dnylng.steadihand.features.comics.*
import com.dnylng.steadihand.features.discover.DiscoverFragment
import com.dnylng.steadihand.features.foryou.ForYouFragment
import com.dnylng.steadihand.features.me.MeFragment
import com.dnylng.steadihand.features.settings.SettingsFragment
import com.dnylng.steadihand.util.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val bottomNav by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<BottomNavigationView>(R.id.bottom_nav)
    }

    private var navController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(
            R.navigation.nav_foryou,
            R.navigation.nav_comics,
            R.navigation.nav_discover,
            R.navigation.nav_me,
            R.navigation.nav_settings
        )

        val controller = bottomNav.setupWithNavController(
            navGraphIds,
            supportFragmentManager,
            R.id.nav_host_container,
            intent
        )

        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })
        navController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.value?.navigateUp() ?: false
    }
}
