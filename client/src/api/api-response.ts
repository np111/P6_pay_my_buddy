export type ApiResponse<T = any> = {
    success: true;
    result: T;
} | {
    success: false;
    error: ApiError;
};

export interface ApiError {
    type: ErrorType;
    code: ErrorCode;
    message: string;
    metadata?: { [key: string]: any };
}

export type ErrorType = 'CLIENT' | 'SERVICE' | 'UNKNOWN';

export type ErrorCode =
// Client-side implementation
    '_ABORTED'
    // Type: UNKNOWN
    | 'SERVER_EXCEPTION'
    // Type: CLIENT
    | 'BAD_REQUEST'
    | 'VALIDATION_FAILED'
    | 'ACCESS_DENIED'
    // Type: SERVICE
    | 'EMAIL_ALREADY_EXISTS'
    | 'INVALID_CREDENTIALS'
    ;
