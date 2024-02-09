import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import AuthApiClient from '../../../utils/http-client/AuthApiClient'
import {IRegisterParams, IRegisterResponse} from "../../types/auth"
import { IState } from '../../types/global'

const authApiClient: AuthApiClient = AuthApiClient.getInstance()

export const fetchActivate = createAsyncThunk('auth/fetchActivate',
                                        async (token: string, { rejectWithValue }) => {
    try {
        const { data } = await authApiClient.activate(token)
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

const authActivateSlice = createSlice({
    name: 'auth/activate',
    initialState,
    reducers: {
        reset: () => initialState
    },
    extraReducers: builder => {
        builder.addCase(fetchActivate.pending, state => {
            state.isLoading = true
            state.response = null
            state.error = null
        })

        builder.addCase(fetchActivate.fulfilled, (state, action: PayloadAction<IRegisterResponse>) => {
            state.isLoading = false
            state.response = action.payload
            state.error = null
        })

        builder.addCase(fetchActivate.rejected, (state, action) => {
            state.isLoading = false
            state.response = null
            state.error = action.payload
        })
    }
})

export default authActivateSlice.reducer
export const {
    reset
} = authActivateSlice.actions