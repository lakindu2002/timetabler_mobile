package com.cb007787.timetabler.interfaces;


import com.cb007787.timetabler.model.SuccessResponseAPI;

/**
 * Callbacks to be executed on API
 */
public interface DeleteCallbacks {
    /**
     * To be executed when API returns 200
     *
     * @param theSuccessObject The success object returned from API
     */
    void onDeleteSuccessResponse(SuccessResponseAPI theSuccessObject);

    /**
     * To be executed "onFailure" or on isSuccessful == false
     *
     * @param message The message to be displayed to the user as error.
     */
    void onDeleteFailure(String message);

    /**
     * Executed when the user clicks on "Delete" on the modal
     */
    void onDeleteCalled();
}

