import {AuthPayload} from "../components/AuthPage";

const fetchRequest = async (url: string, method: string, body?: any): Promise<any> => {
    const response = await fetch(url, {
        method,
        headers: {
            "Content-Type": "application/json",
        },
        body: body ? JSON.stringify(body) : undefined,
    });
    return response.json();
};
export const testConnection = async (formPayload: AuthPayload): Promise<any> => {
    return fetchRequest("../testConnection", "POST", formPayload)
};

export const saveCredentials = async (formPayload: AuthPayload): Promise<any> => {
    return fetchRequest("../saveCredentials", "POST", formPayload);
};

export const getCredentials = async (authType: string): Promise<any> => {
    return fetchRequest(`../getCredentials?authType=${authType}`, "GET", null);
};

export const getAuthKey = async (): Promise<any> => {
    return fetchRequest("../getAuthKey", "GET", null);
};
