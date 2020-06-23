/*
 * Copyright (c) 2020. BoostTag E.I.R.L. Romell D.Z.
 * All rights reserved
 * porfile.romellfudi.com
 */

package com.romellfudi.fudinfc.gear.interfaces;

public interface TaskCallback {

    /**
     * Used to deliver the result of the operation.
     * @param result of the operation
     */
    void onReturn(Boolean result);

    /**
     * Used to signal current state of the task.
     * @param values containing the state in values[0]
     */
    void onProgressUpdate(Boolean... values);

    /**
     * When any error occurs, the error is passed here.
     * @param e the cause of the task to stopping or malfunctioning
     */
    void onError(Exception e);
}
