/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

import Container from '@material-ui/core/Container';
import Typography from '@material-ui/core/Typography';
import { useEffect, useRef } from 'react';

export default function Dashboard() {
    const hystrixDashboard = useRef();
    useEffect(() => {
        if (hystrixDashboard.current) {
            // eslint-disable-next-line no-console
            console.log('Hystrix Dashboard Mounted');
            // eslint-disable-next-line no-console
            console.log(window.HystrixCommandMonitor);
            hystrixDashboard.current.id = 'content';
            window.loadHystrixCommand(window);
            const hystrixMonitor = new window.HystrixCommandMonitor(0, 'content', { includeDetailIcon: false });
            // start the EventSource which will open a streaming connection to the server
            const source = new EventSource('https://localhost:10019/metrics-service/application/hystrix.stream');
            // add the listener that will process incoming events
            source.addEventListener('message', hystrixMonitor.eventSourceMessageListener, false);
        }
    }, []);
    return (
        <>
            <Typography id="name" variant="h2" component="h1" gutterBottom align="center">
                Metrics Service
            </Typography>
            <Container ref={hystrixDashboard} />
        </>
    );
}
