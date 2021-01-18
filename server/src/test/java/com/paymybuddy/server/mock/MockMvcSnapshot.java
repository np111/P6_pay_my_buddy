package com.paymybuddy.server.mock;

import au.com.origin.snapshots.SnapshotMatcher;
import com.google.common.collect.Iterators;
import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

@UtilityClass
public class MockMvcSnapshot {
    public static void toMatchSnapshot(MvcResult firstObject, MvcResult... objects) throws Exception {
        SnapshotMatcher.expect(asResponse(firstObject), asResponses(objects)).toMatchSnapshot();
    }

    private static Object asResponse(MvcResult result) throws Exception {
        StringBuilder sb = new StringBuilder().append("-\n");

        MockHttpServletRequest request = result.getRequest();
        sb.append("> ").append(request.getMethod()).append(" ").append(request.getPathInfo());
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }
        Iterators.forEnumeration(request.getHeaderNames()).forEachRemaining(headerName -> {
            Iterators.forEnumeration(request.getHeaders(headerName)).forEachRemaining(headerValue -> {
                sb.append("\n> ").append(headerName).append(": ").append(headerValue);
            });
        });
        String requestContent = request.getContentAsString();
        if (requestContent != null) {
            sb.append("\n").append(requestContent);
        }

        MockHttpServletResponse response = result.getResponse();
        sb.append("\n< HTTP ").append(response.getStatus());
        response.getHeaderNames().forEach(headerName -> {
            response.getHeaderValues(headerName).forEach(headerValue -> {
                sb.append("\n< ").append(headerName).append(": ").append(headerValue);
            });
        });
        sb.append("\n").append(response.getContentAsString());

        return sb.toString();
    }

    private static Object[] asResponses(MvcResult[] results) throws Exception {
        Object[] ret = new Object[results.length];
        for (int i = 0; i < results.length; ++i) {
            ret[i] = asResponse(results[i]);
        }
        return ret;
    }
}
