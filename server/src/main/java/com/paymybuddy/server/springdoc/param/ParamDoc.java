package com.paymybuddy.server.springdoc.param;

import com.paymybuddy.business.pageable.AbstractRequestParser;
import com.paymybuddy.business.pageable.CursorRequestParser;
import com.paymybuddy.business.pageable.PageRequestParser;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
@Scope("singleton")
public class ParamDoc {
    public void customize(Operation operation, HandlerMethod handlerMethod) {
        {
            ApiPageRequestParameter annotation = handlerMethod.getMethod().getAnnotation(ApiPageRequestParameter.class);
            if (annotation != null) {
                addPageRequestParameter(operation, handlerMethod, annotation);
            }
        }
        {
            ApiCursorRequestParameter annotation = handlerMethod.getMethod().getAnnotation(ApiCursorRequestParameter.class);
            if (annotation != null) {
                addCursorRequestParameter(operation, handlerMethod, annotation);
            }
        }
    }

    private void addAbstractPageRequestParameter(Operation operation) {
        operation.addParametersItem(new Parameter()
                .name(AbstractRequestParser.PAGE_SIZE_PARAM_NAME)
                .in("query")
                .description("Number of records to return per page.")
                .schema(new Schema<IntegerSchema>().type("integer").format("int32")));

        operation.addParametersItem(new Parameter()
                .name(AbstractRequestParser.PAGE_SORT_PARAM_NAME)
                .in("query")
                .description("Property to sort."
                        + " Prefix the property with \"-\" to reverse."
                        + " Multiple values can be separated by a comma.")
                .schema(new Schema<IntegerSchema>().type("string")));
    }

    private void addPageRequestParameter(Operation operation, HandlerMethod handlerMethod, ApiPageRequestParameter annotation) {
        addAbstractPageRequestParameter(operation);

        operation.addParametersItem(new Parameter()
                .name(PageRequestParser.PAGE_PARAM_NAME)
                .in("query")
                .description("Index of the page to return (zero for the first page).")
                .schema(new Schema<IntegerSchema>().type("integer").format("int32")));
    }

    private void addCursorRequestParameter(Operation operation, HandlerMethod handlerMethod, ApiCursorRequestParameter annotation) {
        addAbstractPageRequestParameter(operation);

        operation.addParametersItem(new Parameter()
                .name(CursorRequestParser.CURSOR_PARAM_NAME)
                .in("query")
                .description("Cursor of the page to return (empty for the first page,"
                        + " then use the prevCursor/nextCursor values returned in the previous response).")
                .schema(new Schema<IntegerSchema>().type("string")));
    }
}
