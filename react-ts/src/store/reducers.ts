import { combineReducers } from '@reduxjs/toolkit'
import meSlice from "./features/auth/meSlice"
import loginSlice from "./features/auth/loginSlice"
import registerSlice from "./features/auth/registerSlice"
import activateSlice from "./features/auth/activateSlice"
import resendEmailActivationSlice from "./features/auth/resendEmailActivationSlice"
import logoutSlice from "./features/auth/logoutSlice"
import resetPasswordSlice from "./features/auth/resetPasswordSlice"
import changePasswordSlice from "./features/auth/changePasswordSlice"

export default combineReducers({
    me: meSlice,
    login: loginSlice,
    register: registerSlice,
    activate: activateSlice,
    resendEmailActivation: resendEmailActivationSlice,
    logout: logoutSlice,
    resetPassword: resetPasswordSlice,
    changePassword: changePasswordSlice
})