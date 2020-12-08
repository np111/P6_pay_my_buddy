package com.paymybuddy.server.jpa.util;

import java.util.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AbstractRequest {
    private int pageSize;
    private Collection<String> pageSort;
}
