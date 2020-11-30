export const handleAsyncError = (component: React.Component, err: any) => {
    component.setState(() => {
        /*
        if (component.componentDidCatch) {
            component.componentDidCatch(err, undefined);
            return;
        }
        */
        throw err;
    });
};
