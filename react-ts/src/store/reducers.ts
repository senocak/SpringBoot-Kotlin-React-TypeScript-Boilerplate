import { combineReducers } from '@reduxjs/toolkit'
import meSlice from "./features/auth/meSlice"
import getSinglePostSlice from "./features/post/getSinglePostSlice"
import addCommentSlice from "./features/post/addCommentSlice"
import loginSlice from "./features/auth/loginSlice"
import createPostSlice from "./features/post/createPostSlice"
import getAllPostsSlice from "./features/post/getAllPostsSlice"
import deletePostSlice from "./features/post/deletePostSlice"
import patchPostSlice from "./features/post/patchPostSlice"
import patchCommentVisibilitySlice from "./features/post/patchCommentVisibilitySlice"
import registerSlice from "./features/auth/registerSlice"
import activateSlice from "./features/auth/activateSlice"
import resendEmailActivationSlice from "./features/auth/resendEmailActivationSlice"

export default combineReducers({
    me: meSlice,
    login: loginSlice,
    register: registerSlice,
    activate: activateSlice,
    resendEmailActivation: resendEmailActivationSlice,

    addComment: addCommentSlice,

    getAllPosts: getAllPostsSlice,
    getSinglePost: getSinglePostSlice,
    createPost: createPostSlice,
    deletePost: deletePostSlice,
    patchPost: patchPostSlice,
    patchCommentVisibility: patchCommentVisibilitySlice,

})