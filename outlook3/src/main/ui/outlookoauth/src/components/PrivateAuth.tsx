import React, {useCallback, useEffect, useState} from "react";
import {AuthPayload, AuthType} from "./AuthPage";
import {getCredentials} from "../api";

const PrivateAuth = ({onAuthChange}: {
    onAuthChange: (authPayload: AuthPayload) => void
}) => {
    const [email, setEmail] = useState("");
    const [clientId, setClientId] = useState("");
    const [clientSecret, setClientSecret] = useState("");
    const [tenantId, setTenantId] = useState("");
    const [allowMailAlert, setAllowMailAlert] = useState(false);
    const [savedCred, setSavedCred] = useState<boolean>(false);

    const areFieldsValid = useCallback(() => {
        return !!email && !!clientId && !!clientSecret && !!tenantId;
    }, [email, clientId, clientSecret, tenantId]);

    const isCredSaved = useCallback((): boolean => {
        return savedCred;
    }, [savedCred]);

    useEffect(() => {
        getAllCred().catch(error => console.log(error));
    }, []);

    useEffect(() => {
        onAuthChange({
            authType: AuthType.Private,
            email: email,
            clientId: clientId,
            clientSecret: clientSecret,
            tenantId: tenantId,
            allowMailAlert: allowMailAlert,
            isAllRequiredFieldsHaveValue: areFieldsValid(),
            isCredentialsSaved: isCredSaved()
        });
    }, [email, clientId, clientSecret, tenantId, allowMailAlert, onAuthChange, areFieldsValid, isCredSaved]);

    const getAllCred = async () => {
        try {
            const credentials = await getCredentials(AuthType.Private);
            if (credentials) {
                setEmail(credentials["email"]);
                setClientId(credentials["clientId"]);
                setClientSecret(credentials["clientSecret"]);
                setTenantId(credentials["tenantId"]);
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
                <div className="auth-form">
                    <div className="form-group">
                        <label htmlFor="email">Email<span className="mandatory-asterisk">*</span></label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="Enter School or Work Email Id"
                            autoComplete="username"
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="client-id">Client ID<span className="mandatory-asterisk">*</span></label>
                        <input
                            type="text"
                            id="client-id"
                            value={clientId}
                            onChange={(e) => setClientId(e.target.value)}
                            placeholder="Enter Client ID"
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="client-secret">Client Secret<span
                            className="mandatory-asterisk">*</span></label>
                        <input
                            type="password"
                            id="client-secret"
                            value={clientSecret}
                            onChange={(e) => setClientSecret(e.target.value)}
                            placeholder="Enter Client Secret"
                            autoComplete="current-password"
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="tenant-id">Tenant ID<span className="mandatory-asterisk">*</span></label>
                        <input
                            type="text"
                            id="tenant-id"
                            value={tenantId}
                            onChange={(e) => setTenantId(e.target.value)}
                            placeholder="Enter Tenant ID"
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

export default PrivateAuth;

