package com.dnylng.steadihand

import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dnylng.steadihand.features.comics.*
import com.dnylng.steadihand.features.discover.DiscoverFragment
import com.dnylng.steadihand.features.foryou.ForYouFragment
import com.dnylng.steadihand.features.me.MeFragment
import com.dnylng.steadihand.features.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    companion object {
        const val SAVED_STATE_SPARSE_ARRAY_KEY = "SparsedArrayKey"
        const val SAVED_STATE_SELECTED_ID_KEY = "SelectedIdKey"
    }

    private var savedStateSparseArray = SparseArray<Fragment.SavedState>()
    private var selectedItemId = R.id.nav_foryou

    private val bottomNav by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<BottomNavigationView>(R.id.bottom_nav)
    }

    private val onNavItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_foryou -> swapFragments(item.itemId, "For You")
            R.id.nav_comics -> swapFragments(item.itemId, "Comics")
            R.id.nav_discover -> swapFragments(item.itemId, "Discover")
            R.id.nav_me -> swapFragments(item.itemId, "Me")
            R.id.nav_settings -> swapFragments(item.itemId, "Settings")
            else -> return@OnNavigationItemSelectedListener false
        }
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            savedStateSparseArray = savedInstanceState.getSparseParcelableArray(SAVED_STATE_SPARSE_ARRAY_KEY) ?: SparseArray()
            selectedItemId = savedInstanceState.getInt(SAVED_STATE_SELECTED_ID_KEY)
        }
        setContentView(R.layout.activity_main)
        bottomNav.setOnNavigationItemSelectedListener(onNavItemSelectedListener)
        bottomNav.selectedItemId = selectedItemId
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSparseParcelableArray(SAVED_STATE_SPARSE_ARRAY_KEY, savedStateSparseArray)
        outState?.putInt(SAVED_STATE_SELECTED_ID_KEY, selectedItemId)
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment != null && fragment.isVisible) {
                with(fragment.childFragmentManager) {
                    if (backStackEntryCount > 0) {
                        popBackStack()
                        return
                    }
                }
            }
        }
        super.onBackPressed()
    }

    private fun swapFragments(@IdRes actionId: Int, key: String) {
        if (supportFragmentManager.findFragmentByTag(key) == null) {
            savedFragmentState(actionId)
            createFragment(actionId, key)
        }
    }

    private fun createFragment(@IdRes actionId: Int, key: String) = when (actionId) {
        R.id.nav_foryou -> { setInitFragment(ForYouFragment.newInstance(key), actionId, key) }
        R.id.nav_comics -> { setInitFragment(ComicsFragment.newInstance(key), actionId, key) }
        R.id.nav_discover -> { setInitFragment(DiscoverFragment.newInstance(key), actionId, key) }
        R.id.nav_me -> { setInitFragment(MeFragment.newInstance(key), actionId, key) }
        R.id.nav_settings -> { setInitFragment(SettingsFragment.newInstance(key), actionId, key) }
        else -> { setInitFragment(ForYouFragment.newInstance(key), actionId, key) }
    }

    private fun setInitFragment(fragment: Fragment, @IdRes actionId: Int, key: String) {
        fragment.setInitialSavedState(savedStateSparseArray[actionId])
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, key)
            .commit()
    }

    private fun savedFragmentState(@IdRes actionId: Int) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            savedStateSparseArray.put(
                selectedItemId,
                supportFragmentManager.saveFragmentInstanceState(currentFragment)
            )
        }
        selectedItemId = actionId
    }
}
