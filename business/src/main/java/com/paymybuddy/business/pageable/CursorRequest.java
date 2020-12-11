package com.paymybuddy.business.pageable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = false)
public class CursorRequest extends AbstractRequest {
    private String cursor;
}
