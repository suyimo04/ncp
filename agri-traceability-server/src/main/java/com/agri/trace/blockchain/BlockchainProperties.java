package com.agri.trace.blockchain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "webase")
public class BlockchainProperties {
    private String frontUrl;
    private String groupId;
    private String user;
    private Boolean mockMode = true;
}
