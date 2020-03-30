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
package com.javinator9889.handwashingreminder.utils

const val TIME_CHANNEL_ID = "timeNotificationsChannel"
const val ACTIVITY_CHANNEL_ID = "activityNotificationsChannel"

class Preferences {
    companion object {
        const val CREATE_CHANNEL_KEY = "create_channel_req"
        const val APP_INIT_KEY = "application_initialized"
    }
}

class TimeConfig {
    companion object {
        const val BREAKFAST_ID = 0L
        const val LUNCH_ID = 1L
        const val DINNER_ID = 2L
    }
}