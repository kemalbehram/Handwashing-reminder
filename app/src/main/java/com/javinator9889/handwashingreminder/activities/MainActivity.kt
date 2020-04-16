/*
 * Copyright © 2020 - present | Handwashing reminder by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 15/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities

import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.support.ActionBarBase
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.how_to_wash_hands_layout.*
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.concurrent.thread
import kotlin.properties.Delegates

class MainActivity : ActionBarBase(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    override val layoutId: Int = R.layout.activity_main
    private val fragments: SparseArray<WeakReference<Fragment>> = SparseArray(4)
    private var activeFragment by Delegates.notNull<@IdRes Int>()
    private lateinit var app: HandwashingApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = HandwashingApplication.getInstance()
        delegateMenuIcons(menu)
        val ids =
            arrayOf(R.id.diseases, R.id.handwashing, R.id.news, R.id.settings)
        for (id in ids)
            createFragmentForId(id)
        activeFragment = R.id.diseases
        menu.setOnNavigationItemSelectedListener(this)
        initFragmentView()
    }

    protected fun delegateMenuIcons(menu: BottomNavigationView) {
        thread(start = true) {
            menu.menu.forEach { item ->
                val icon = when (item.itemId) {
                    R.id.diseases ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon.gmd_feedback
                        )
                    R.id.news ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon.gmd_chrome_reader_mode
                        )
                    R.id.settings ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon.gmd_settings
                        )
                    else -> null
                }
                icon?.let { runOnUiThread { item.icon = it } }
            }
        }
    }

    override fun onBackPressed() {
        if (activeFragment != R.id.diseases &&
            activeFragment != R.id.handwashing
        ) {
            menu.selectedItemId = R.id.diseases
            onNavigationItemSelected(menu.menu.findItem(R.id.diseases))
        } else {
            if (activeFragment == R.id.diseases)
                super.onBackPressed()
            else {
                val washingHandsFragment = fragments[activeFragment].get()
                    ?: createFragmentForId(R.id.handwashing)
                            as WashingHandsFragment
                if (washingHandsFragment.pager.currentItem != 0)
                    washingHandsFragment.pager.currentItem--
                else {
                    menu.selectedItemId = R.id.diseases
                    onNavigationItemSelected(menu.menu.findItem(R.id.diseases))
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean =
        onItemSelected(item.itemId)

    protected fun onItemSelected(@IdRes id: Int): Boolean {
        return try {
            loadFragment(id)
            if (id == R.id.handwashing)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            else
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            true
        } catch (e: Exception) {
            Timber.e(e, "Unexpected exception")
            false
        }
    }

    private fun initFragmentView() {
        with(supportFragmentManager.beginTransaction()) {
            fragments.forEach { id, reference ->
                val fragment = reference.get() ?: createFragmentForId(id)
                add(R.id.mainContent, fragment)
                hide(fragment)
            }
            show(
                fragments[activeFragment].get() ?: createFragmentForId(
                    activeFragment
                )
            )
            commit()
        }
    }

    private fun loadFragment(@IdRes id: Int) {
        if (id == activeFragment)
            return
        val fragment = fragments[id].get() ?: return
        val displayedFragment = fragments[activeFragment].get()!!
        with(supportFragmentManager.beginTransaction()) {
            show(fragment)
            hide(displayedFragment)
            activeFragment = id
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            disallowAddToBackStack()
        }.commit()
    }

    private fun createFragmentForId(@IdRes id: Int): Fragment {
        val fragment = when (id) {
            R.id.diseases -> DiseasesFragment()
            R.id.handwashing -> WashingHandsFragment()
            R.id.news -> NewsFragment()
            R.id.settings -> SettingsView()
            else -> Fragment()  // this should never happen
        }
        fragments[id] = WeakReference(fragment)
        return fragment
    }
}
