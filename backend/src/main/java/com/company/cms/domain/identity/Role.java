package com.company.cms.domain.identity;

import com.company.cms.domain.common.CmsEnums.RoleCode;
import java.util.Set;

public record Role(
    String id,
    RoleCode code,
    String name,
    Set<String> permissions,
    boolean systemManaged
) {
}
