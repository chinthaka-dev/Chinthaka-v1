package com.chinthaka.chinthaka_beta.data.entities

class Metric {
    private val dailyLogins: String? = null
    private val timeSpent: Long? = null
    private val interactions: Long? = null
    private val postViews: Long? = null
    private val clicksOnViewSolutions: Long? = null
    private val clicksOnShare: Long? = null
    private val clicksOnBookmark: Long? = null
    private val numberOfAttempts: Long? = null
    private val numberOfAnswersSubmitted: Long? = null
    private val numberOfLikes: Long? = null

    companion object {
        const val CLICKS_ON_VIEW_SOLUTIONS = "clicksOnViewSolutions"
        const val NUMBER_OF_ATTEMPTS = "numberOfAttempts"
        const val NUMBER_OF_ANSWERS_SUBMITTED = "numberOfAnswersSubmitted"
        const val NUMBER_OF_LIKES = "numberOfLikes"
        const val CLICKS_ON_SHARE = "clicksOnShare"
        const val CLICKS_ON_BOOKMARK = "clicksOnBookmark"
        const val CLICKS_ON_INVITE = "clicksOnInvite"
    }
}