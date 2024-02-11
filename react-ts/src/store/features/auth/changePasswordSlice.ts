import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import AuthApiClient from '../../../utils/http-client/AuthApiClient'
import {IChangePasswordRequest, IRegisterResponse} from "../../types/auth"
import { IState } from '../../types/global'

const authApiClient: AuthApiClient = AuthApiClient.getInstance()

export const fetchChangePassword = createAsyncThunk('auth/fetchChangePassword',
                                        async (body: { body: IChangePasswordRequest, token: string }, { rejectWithValue }) => {
    try {
        const { data } = await authApiClient.changePassword(body.body, body.token)
        return data
    } catch (error: any) {
        if (!error.response) {
            throw error
        }
        return rejectWithValue(error)
    }
})

const initialState: IState<IRegisterResponse> = {
    isLoading: false,
    response: null,
    error: null
}

const authChangePasswordSlice = createSlice({
    name: 'auth/changePassword',
    initialState,
    reducers: {
        reset: () => initialState
    },
    extraReducers: builder => {
        builder.addCase(fetchChangePassword.pending, state => {
            state.isLoading = true
            state.response = null
            state.error = null
        })

        builder.addCase(fetchChangePassword.fulfilled, (state, action: PayloadAction<IRegisterResponse>) => {
            state.isLoading = false
            state.response = action.payload
            state.error = null
        })

        builder.addCase(fetchChangePassword.rejected, (state, action) => {
            state.isLoading = false
            state.response = null
            state.error = action.payload
        })
    }
})

export default authChangePasswordSlice.reducer
export const {
    reset
} = authChangePasswordSlice.actions