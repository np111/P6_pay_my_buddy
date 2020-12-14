import {notification as antdNotification} from 'antd';
import {NotificationApi} from 'antd/lib/notification';

const notification = {} as NotificationApi;

for (const k in antdNotification) {
    if (Object.prototype.hasOwnProperty.call(antdNotification, k)) {
        notification[k] = antdNotification[k].bind(antdNotification);
    }
}

for (const k of ['success', 'error', 'info', 'warn', 'warning', 'open']) {
    const origMethod: any = notification[k];
    notification[k] = (...args) => {
        if (typeof args[0] === 'object' && typeof args[0].placement === 'undefined') {
            args[0].placement = 'bottomRight';
        }
        return origMethod.apply(undefined, args);
    };
}

export default notification;
