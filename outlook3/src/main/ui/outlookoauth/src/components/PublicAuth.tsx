import React, {useCallback, useEffect, useState} from "react";
import {AuthPayload, AuthType} from "./AuthPage";
import {getCredentials} from "../api";

const PublicAuth = ({onAuthChange}: { onAuthChange: (authPayload: AuthPayload) => void }) => {
    const [email, setEmail] = useState("");
    const [allowMailAlert, setAllowMailAlert] = useState(false);
    const [savedCred, setSavedCred] = useState<boolean>(false);

    const areFieldsValid = useCallback(() => {
        return !!email;
    }, [email]);

    const isCredSaved = useCallback((): boolean => {
        return savedCred;
    }, [savedCred]);

    useEffect(() => {
        getAllCred().catch(error => console.log(error));
    }, []);

    useEffect(() => {
        onAuthChange({
            authType: AuthType.Public,
            email: email,
            allowMailAlert: allowMailAlert,
            isAllRequiredFieldsHaveValue: areFieldsValid(),
            isCredentialsSaved: isCredSaved()
        });
    }, [email, allowMailAlert, onAuthChange, areFieldsValid, isCredSaved]);

    const getAllCred = async () => {
        try {
            const credentials = await getCredentials(AuthType.Public);
            if (credentials) {
                setEmail(credentials["email"]);
                setAllowMailAlert(credentials["allowMailAlert"]);
                setSavedCred(true);
            } else {
                console.error('Failed to fetch credentials');
            }
        } catch (error) {
            console.error('Error fetching credentials:', error);
        }
    }

    return (
        <form>
            <div>
                <div
                    className="auth-form">
                    <div className="form-group">
                        <label htmlFor="email">Email<span className="mandatory-asterisk">*</span></label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="Enter Work or School Email Id"
                        />
                    </div>

                    <div className="form-group">
                        <div className="checkbox-label-container">
                            <input
                                type="checkbox"
                                id="allowMailAlert"
                                checked={allowMailAlert}
                                onChange={(e) => setAllowMailAlert(e.target.checked)}
                            />
                            <label htmlFor="allowMailAlert">Allow Mail Alert</label>
                        </div>

                    </div>
                </div>
            </div>
        </form>
    );
};

export default PublicAuth;

