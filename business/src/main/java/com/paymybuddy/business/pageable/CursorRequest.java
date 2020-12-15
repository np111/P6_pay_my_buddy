package com.paymybuddy.business.pageable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Requests parameters for a {@link CursorFetcher}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = false)
public class CursorRequest extends AbstractRequest {
    private String cursor;
}
