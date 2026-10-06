package com.example.taskmanager.utils

object Constants {
    const val APP_NAME = "Personal Task Manager"
    const val APP_TAGLINE = "Organize. Track. Complete."

    val CATEGORIES = listOf(
        "College",
        "Work",
        "Personal",
        "Shopping",
        "Other"
    )

    val PRIORITIES = listOf(
        "Low",
        "Medium",
        "High"
    )

    val STATUSES = listOf(
        "Pending",
        "In Progress",
        "Completed"
    )

    val SORT_OPTIONS = listOf(
        "Due Date",
        "Priority: High → Low",
        "Priority: Low → High",
        "Title: A → Z",
        "Title: Z → A",
        "Recently Created"
    )

    val APPRECIATION_MESSAGES = listOf(
        "🎉 BOOM!\nToday's tasks are 100% finished!\nEven your coffee is cheering for you! ☕🏆",
        "🌟 Outstanding Work!\nYou crushed all your goals for today!\nTime to relax and celebrate! 🎉🥳",
        "🚀 Total Domination!\nYou completed 100% of today's checklist!\nYou're on a roll! 💪🔥",
        "🏆 Champion Alert!\nZero pending tasks for today!\nKeep up this amazing momentum! ✨🎯"
    )
}
