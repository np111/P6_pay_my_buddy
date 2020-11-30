import {ApiError} from './api-response';

export class ApiException extends Error {
    error?: ApiError;
    cause?: Error;

    constructor(message: string, error?: ApiError, cause?: Error) {
        super(message);
        this.error = error;
        this.cause = cause;
    }
}

export class AccessDeniedError extends ApiException {
    constructor(error: ApiError) {
        super('Access denied (authenticated required/missing authorization)', error);
    }

    get invalidToken() {
        return this.error && this.error.metadata && !!this.error.metadata.invalidToken;
    }
}

export class UnhandledApiError extends ApiException {
    constructor(error: ApiError) {
        super('Unhandled service error', error);
    }
}
