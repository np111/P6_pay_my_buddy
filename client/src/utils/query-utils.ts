export function queryStringify(query: any, prefix?: string) {
    const pairs = [];
    for (const key in query) {
        if (!Object.prototype.hasOwnProperty.call(query, key)) {
            continue;
        }

        const value = query[key];
        if (value === undefined) {
            continue;
        }

        let keyStr = encodeURIComponent(key);
        if (prefix) {
            keyStr = prefix + '[' + keyStr + ']';
        }

        let pair;
        if (typeof value === 'object') {
            pair = queryStringify(value, keyStr);
        } else {
            pair = keyStr + '=' + encodeURIComponent(value);
        }
        pairs.push(pair);
    }
    return !pairs.length ? '' : '?' + pairs.join('&');
}
