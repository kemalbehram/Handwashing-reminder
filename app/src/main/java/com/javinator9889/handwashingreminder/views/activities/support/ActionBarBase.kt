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
 * Created by Javinator9889 on 20/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.views.activities.support

import android.os.Bundle
import com.javinator9889.handwashingreminder.R
import javinator9889.localemanager.activity.BaseAppCompatActivity

abstract class ActionBarBase : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    abstract fun getLayoutId(): Int
}