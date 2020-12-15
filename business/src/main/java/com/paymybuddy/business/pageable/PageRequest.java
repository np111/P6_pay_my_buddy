package com.paymybuddy.business.pageable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Requests parameters for a {@link PageFetcher}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = false)
public class PageRequest extends AbstractRequest {
    private int page;
}
