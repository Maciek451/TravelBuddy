package uk.ac.aber.dcs.chm9360.travelbuddy.utils

object AuthenticationState {
    const val LOGGED_IN_SUCCESSFULLY = 0
    const val PASSWORD_WRONG = 1
    const val ACCOUNT_DOES_NOT_EXIST = 2
    const val OTHER = 3
    const val USER_IS_NOT_VERIFIED = 7
    const val SIGNED_UP_SUCCESSFULLY = 0
    const val USER_ALREADY_EXISTS = 4
    const val EMAIL_WRONG_FORMAT = 5
    const val PASSWORD_WRONG_FORMAT = 6
}