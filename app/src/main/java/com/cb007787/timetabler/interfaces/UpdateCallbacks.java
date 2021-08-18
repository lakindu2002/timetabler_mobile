package com.cb007787.timetabler.interfaces;

import com.cb007787.timetabler.model.SuccessResponseAPI;

public interface UpdateCallbacks {
    void onUpdate();

    void onUpdateCompleted(SuccessResponseAPI theResponse);

    void onUpdateFailed(String errorMessage);
}
