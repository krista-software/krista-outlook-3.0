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

    const handleResponseForSavedCred = (response: any) => {
        if (response.isSaved) {
            setToastMessage("Connection tested successfully. Changes saved!");
            setToastType("success");
            const saveButton = document.getElementById("save-button") as HTMLButtonElement;
            if (saveButton) {
                saveButton.click();
            }
            setIsConnectionSuccess(false);
            setIsSaved(true)
        } else if (response.errorWhileSaving) {
            setToastMessage("Test connection failed");
            setToastType("error");
        }
        setShowToast(true);
        setTimeout(() => {
            setShowToast(false);
        }, 3000);
    }

    const handleResponse = (response: any) => {
        if (response.isSuccess) {
            setToastMessage("Connection tested successfully. Please save the changes!");
            setToastType("success");
            const saveButton = document.getElementById("save-button") as HTMLButtonElement;
            if (saveButton) {
                saveButton.click();
            }
            setIsConnectionSuccess(true);
        } else if (response.errorMessage) {
            setToastMessage(response.errorMessage);
            setToastType("error");
        }
        if (response.url) {
            const popup = window.open(response.url, "_blank", "width=600,height=400");
            if (popup) {
                const interval = setInterval(() => {
                    if (popup.closed) {
                        clearInterval(interval);
                        if (authPayload) {
                            setLoading(true);
                            testConnection(authPayload).then(response => {
                                if (response.isSuccess) {
                                    setLoading(false);
                                    setIsConnectionSuccess(true);
                                    setToastMessage("Connection tested successfully. Please save the changes.");
                                    setToastType("success");
                                    const saveButton = document.getElementById("save-button") as HTMLButtonElement;
                                    if (saveButton) {
                                        saveButton.click();
                                    }
                                    setShowToast(true);
                                    setTimeout(() => {
                                        setLoading(false);
                                        setShowToast(false);
                                    }, 3000);
                                } else if (response.errorMessage) {
                                    setIsConnectionSuccess(false);
                                    setToastMessage("Test Connection failed. Please authenticate again.");
                                    setToastType("error");
                                    setShowToast(true);
                                    setTimeout(()=> {
                                        setLoading(false);
                                        setShowToast(false);
                                    }, 3000);
                                }

                            });
                        }
                    }
                }, 1000); // Check every second if the popup is closed
            }
        }
        setLoading(false);
        setShowToast(true);
        setTimeout(() => {
            setLoading(false);
            setShowToast(false);
        }, 3000);
    }

    const testApiConnection = (/* parameters */) => {
        if (authPayload) {
            testConnection(authPayload).then(response => handleResponse(response));
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
                        id="basic"
                        value={AuthType.Public}
                        checked={selectedOption === AuthType.Public}
                        onChange={handleOptionChange}
                    />
                    <label htmlFor="basic"></label>
                    Public
                </div>
                <div>
                    <input
                        type="radio"
                        id="token"
                        value={AuthType.Private}
                        checked={selectedOption === AuthType.Private}
                        onChange={handleOptionChange}
                    />
                    <label htmlFor="token"></label>
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
