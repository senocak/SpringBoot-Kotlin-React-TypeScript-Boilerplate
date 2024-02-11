import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import AuthApiClient from '../../../utils/http-client/AuthApiClient'
import { IState } from '../../types/global'

const authApiClient: AuthApiClient = AuthApiClient.getInstance()

export const fetchLogout = createAsyncThunk('auth/fetchLogout',
                                        async (_: void, { rejectWithValue }) => {
    try {
        const { data } = await authApiClient.logout()
        return data
    } catch (error: any) {
        if (!error.response) {
            throw error
        }

        return rejectWithValue(error)
    }
})

const initialState: IState<string> = {
    isLoading: false,
    response: null,
    error: null
}

const authLogoutSlice = createSlice({
    name: 'auth/register',
    initialState,
    reducers: {
        reset: () => initialState
    },
    extraReducers: builder => {
        builder.addCase(fetchLogout.pending, state => {
            state.isLoading = true
            state.response = null
            state.error = null
        })

        builder.addCase(fetchLogout.fulfilled, (state, action: PayloadAction<string>) => {
            state.isLoading = false
            state.response = "nocontent"
            state.error = null
        })

        builder.addCase(fetchLogout.rejected, (state, action) => {
            state.isLoading = false
            state.response = null
            state.error = action.payload
        })
    }
})

export default authLogoutSlice.reducer
export const {
    reset
} = authLogoutSlice.actions