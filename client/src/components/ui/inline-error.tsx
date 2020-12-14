import Alert from 'antd/lib/alert';
import 'antd/lib/alert/style/index.less';
import React from 'react';

export interface InlineErrorProps {
    error: any;
}

export function InlineError({error}: InlineErrorProps) {
    return (
        <Alert
            type='error'
            showIcon={true}
            message='An unexpected error occurred'
            description={<pre style={{maxHeight: 200}}>{stringifyError(error)}</pre>}
        />
    );

}

function stringifyError(error: any) {
    if (typeof error !== 'object' || !error) {
        return typeof error;
    }
    return error.stack + '\n' + JSON.stringify(error, (key, value) => {
        switch (key) {
            case 'name':
            case 'message':
            case 'stack':
            case 'config':
            case 'request':
                return undefined;
            default:
                return value;
        }
    }, 2);
}
