/**
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */
package com.ca.mas.cordova.core;

import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGRuntimeException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by trima09 on 27/01/2017.
 */

public class MASCordovaPlugin extends CordovaPlugin {

    /**
     * Transform the throwable to a JSON error, used when calling back into JavaScript when for error
     *
     * @param error The Throwable object to format.
     * @return A JSON Object to represent the error
     */
    protected JSONObject getError(Throwable error) {
        String rootCauseErrorMessage = null;
        if (error instanceof MASException) {
            if (((MASException) error).getRootCause() != null) {
                rootCauseErrorMessage = ((MASException) error).getRootCause().getMessage();
            }
        }
        return getError(error, rootCauseErrorMessage);
    }

    protected JSONObject getError(Throwable throwable, String rootCauseErrorMessage) {
        int errorCode = MAGErrorCode.UNKNOWN;
        String errorMessage = throwable.getMessage();
        String errorMessageDetail = "";
        //Try to capture the root cause of the error
        if (throwable instanceof MAGException) {
            MAGException ex = (MAGException) throwable;
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable instanceof MAGRuntimeException) {
            MAGRuntimeException ex = (MAGRuntimeException) throwable;
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof MAGException) {
            MAGException ex = (MAGException) throwable.getCause();
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof MAGRuntimeException) {
            MAGRuntimeException ex = (MAGRuntimeException) throwable.getCause();
            errorCode = ex.getErrorCode();
            errorMessage = ex.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof MAGServerException) {
            MAGServerException serverException = ((MAGServerException) throwable.getCause());
            errorCode = serverException.getErrorCode();
            errorMessage = serverException.getMessage();
        } else if (throwable.getCause() != null && throwable.getCause() instanceof TargetApiException) {
            TargetApiException e = ((TargetApiException) throwable.getCause());
            try {
                errorCode = ServerClient.findErrorCode(e.getResponse());
            } catch (Exception ignore) {
            }
            errorMessage = e.getResponse() != null ? e.getResponse().getResponseMessage() : e.getMessage();
        } else if (errorMessage != null && errorMessage.equalsIgnoreCase("The session is currently locked.")) {
            errorCode = MAGErrorCode.UNKNOWN;
        } else if (throwable != null && throwable instanceof MASCordovaException) {
            errorMessage = throwable.getMessage();
        } else if ((throwable instanceof NullPointerException || throwable instanceof IllegalStateException) && (MAS.getContext() == null || MAS.getState(this.cordova.getActivity().getApplicationContext()) != MASConstants.MAS_STATE_STARTED)) {
            errorMessageDetail = "Mobile SSO has not been initialized.";
        } else {
            errorMessageDetail = throwable.getMessage();
        }

        JSONObject error = new JSONObject();
        try {
            error.put("errorCode", errorCode);
            error.put("errorMessage", errorMessage);
            StringWriter errors = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errors));
            error.put("errorInfo", errors.toString());
            if (!"".equals(errorMessageDetail)) {
                error.put("errorMessageDetail", errorMessageDetail);
                error.put("errorMessage", "Internal Server Error");
            }

            //If root cause message is available then set that as the error message 
            if (rootCauseErrorMessage != null) {
                error.put("errorMessage", rootCauseErrorMessage);
            }
        } catch (JSONException ignore) {
        }

        return error;
    }

    protected void success(CallbackContext callbackContext, boolean setKeepCallback) {
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        result.setKeepCallback(setKeepCallback);
        callbackContext.sendPluginResult(result);
    }

    protected void success(CallbackContext callbackContext, boolean value, boolean setKeepCallback) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, value);
        result.setKeepCallback(setKeepCallback);
        callbackContext.sendPluginResult(result);
    }

    protected void success(CallbackContext callbackContext, JSONObject resultData, boolean setKeepCallback) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, resultData);
        result.setKeepCallback(setKeepCallback);
        callbackContext.sendPluginResult(result);
    }

    protected void success(CallbackContext callbackContext, JSONArray resultDataArray, boolean setKeepCallback) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, resultDataArray);
        result.setKeepCallback(setKeepCallback);
        callbackContext.sendPluginResult(result);
    }

    protected void success(CallbackContext callbackContext, String resultString, boolean setKeepCallback) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, resultString);
        result.setKeepCallback(setKeepCallback);
        callbackContext.sendPluginResult(result);
    }

    protected void success(CallbackContext callbackContext, byte[] binary, boolean setKeepCallback) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, binary);
        result.setKeepCallback(setKeepCallback);
        callbackContext.sendPluginResult(result);
    }
}