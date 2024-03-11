import {AuthPayload} from "../components/AuthPage";

export const testConnection = async (formPayload: AuthPayload): Promise<any> => {
    const url = "../testConnection";
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(formPayload),
    });
    return response.json();
};

export const saveCredentials = async (formPayload: AuthPayload): Promise<any> => {
    const url = "../saveCredentials";
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(formPayload),
    });
    return response.json();
};

export const getCredentials = async (authType: string): Promise<any> => {
    const url = `../getCredentials?authType=${authType}`;
    const response = await fetch(url, {
        method: "GET",
    });
    return response.json();
};

export const getAuthKey = async (): Promise<any> => {
    const url = "../getAuthKey";
    const response = await fetch(url, {
        method: "GET",
    });
    const data = await response.json();
    return data;
};
