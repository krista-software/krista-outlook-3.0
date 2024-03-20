import React, {useCallback, useEffect, useState} from "react";
import {saveCredentials, testConnection, getAuthKey} from "../api";
import Toast from "./Toast";
import PrivateAuth from "./PrivateAuth";
import PublicAuth from "./PublicAuth";


export interface AuthPayload {
    authType: AuthType;
    email: string;
    allowMailAlert?: boolean;
    clientId?: string;
    clientSecret?: string;
    tenantId?: string;
    isAllRequiredFieldsHaveValue?: boolean;
    isCredentialsSaved?: boolean;
}

export enum AuthType {
    Public = "Public",
    Private = "Private"
}

const AuthPage = () => {
    const [selectedOption, setSelectedOption] = useState("Public");
    const [showToast, setShowToast] = useState<boolean>(false);
    const [loading, setLoading] = useState(false);
    const [toastMessage, setToastMessage] = useState<string>("");
    const [toastType, setToastType] = useState<string>("error");
    const [authPayload, setAuthPayload] = useState<AuthPayload | null>(null);
    const [isConnectionSuccess, setIsConnectionSuccess] = useState<boolean>(false);
    const [isSaved, setIsSaved] = useState<boolean>(false);

    useEffect(() => {
        getAuth().catch(error => console.log(error));
    }, []);

    const getAuth = async () => {
        try {
            const key = await getAuthKey();
            if (key !== selectedOption) {
                setSelectedOption(key as AuthType);
            }
        } catch (error) {
            console.error('Error fetching credentials:', error);
        }
    }

    const handleOptionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedOption(e.target.value as AuthType);
    };

    const showNotification = (message: string, type: string) => {
        setToastMessage(message);
        setToastType(type);
        setShowToast(true);
        setTimeout(() => {
            setShowToast(false);
        }, 3000);
    };

    const handleResponseForSavedCred = (response: any) => {
        let responseMessage = "Test connection failed";
        let responseType = "error";

        if (response.isSaved) {
            responseMessage = "Connection tested successfully. Changes saved!";
            responseType = "success";
            setToastType("success");
            const saveButton = document.getElementById("save-button") as HTMLButtonElement;
            if (saveButton) {
                saveButton.click();
            }
            setIsConnectionSuccess(false);
            setIsSaved(true)
        }
        showNotification(responseMessage, responseType);
    }

    const handleResponse = (response: any) => {
        let message = "Test connection failed";
        let type = "error";

        if (response.isSuccess) {
            message = "Connection tested successfully. Please save the changes!";
            type = "success";
            setIsConnectionSuccess(true);
            const saveButton = document.getElementById("save-button") as HTMLButtonElement;
            if (saveButton) {
                saveButton.click();
            }
        } else if (response.errorMessage) {
            message = response.errorMessage;
        }

        if (response.url) {
            handlePopup(response.url);
        } else {
            setLoading(false);
        }
        console.log("waiting for pop window close..")
        showNotification(message, type);
    }

    const handlePopup = (url: string) => {
        const popup = window.open(url, "_blank", "width=600,height=400");
        if (popup) {
            const interval = setInterval(() => {
                if (popup.closed) {
                    console.log("Pop up window closed.")
                    clearInterval(interval);
                    if (authPayload) {
                        setLoading(true);
                        testConnection(authPayload).then(response => {
                            if (response.isSuccess) {
                                setIsConnectionSuccess(true);
                                const saveButton = document.getElementById("save-button") as HTMLButtonElement;
                                if (saveButton) {
                                    saveButton.click();
                                }
                                showNotification("Connection tested successfully. Please save the changes.", "success");
                            } else if (response.errorMessage) {
                                setIsConnectionSuccess(false);
                                showNotification("Test Connection failed. Please authenticate again.", "error");
                            }
                            setLoading(false);
                        });
                    }
                }
            }, 1000); // Check every second if the popup is closed
        }
    };

    const testApiConnection = (/* parameters */) => {
        if (authPayload) {
            testConnection(authPayload).then(response => {
                handleResponse(response);
                setLoading(false);
            });
        }
    };

    const saveApiConnection = () => {
        if (authPayload) {
            saveCredentials(authPayload)
                .then(response => handleResponseForSavedCred(response));
        }
    };

    // Callback function to handle auth changes from the child component
    const handleAuthChange = useCallback((authPayload: AuthPayload) => {
        setAuthPayload(authPayload);
    }, []);

    const handleTestConnectionClick = () => {
        setLoading(true);
        testApiConnection();
    };

    const handleSaveChangesClick = () => {
        saveApiConnection();
    };

    const renderComponent = () => {
        switch (selectedOption) {
            case AuthType.Public:
                return <PublicAuth onAuthChange={handleAuthChange}/>;
            case AuthType.Private:
                return <PrivateAuth onAuthChange={handleAuthChange}/>;
            default:
                return <PublicAuth onAuthChange={handleAuthChange}/>;
        }
    };

    const setToastOff = () => {
        setShowToast(false);
    }

    const isCredSaved = (): boolean => {
        if (authPayload?.isCredentialsSaved != null) {
            return authPayload?.isCredentialsSaved;
        }
        return false;
    }

    const disableSaveButton = (): boolean => {
        if (isConnectionSuccess) {
            return false;
        } else if (!isCredSaved() && !isConnectionSuccess && isSaved) {
            return true;
        } else {
            return isCredSaved() || checkMandatoryFields();
        }
    }

    const checkMandatoryFields = (): boolean => {
        if (authPayload?.isAllRequiredFieldsHaveValue != null) {
            return !authPayload?.isAllRequiredFieldsHaveValue;
        }
        return true;
    }

    return (
        <>
            <div className="authOptions">
                <div>
                    <input
                        type="radio"
                        id="public"
                        value={AuthType.Public}
                        checked={selectedOption === AuthType.Public}
                        onChange={handleOptionChange}
                    />
                    <label htmlFor="public"></label>
                    Public
                </div>
                <div>
                    <input
                        type="radio"
                        id="private"
                        value={AuthType.Private}
                        checked={selectedOption === AuthType.Private}
                        onChange={handleOptionChange}
                    />
                    <label htmlFor="private"></label>
                    Private
                </div>
            </div>
            <div>
                {renderComponent()}
            </div>
            <div className="button-container">
                <button disabled={checkMandatoryFields() || loading}
                        className={`test-connection-button ${loading ? 'loading' : ''}`}
                        onClick={handleTestConnectionClick}
                >
                     <span className="button-content">
                         {loading && <div className="loading-spinner"></div>}
                         Test Connection
                     </span>
                </button>
                <button disabled={disableSaveButton()} className="save-changes-button"
                        onClick={handleSaveChangesClick}
                >
                    <span>Save Changes</span>
                </button>
            </div>
            {showToast ? <div>
                <Toast message={toastMessage} type={toastType} duration={1500} onClose={setToastOff}></Toast>
            </div> : null}
        </>
    );
};

export default AuthPage;
