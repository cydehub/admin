package com.stockzeno.wms.jobs;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stockzeno.jobs")
public class JobProperties {

    private int nearExpiryDays = 30;
    private Duration lowStockCooldown = Duration.ofHours(24);

    public int getNearExpiryDays() {
        return nearExpiryDays;
    }

    public void setNearExpiryDays(int nearExpiryDays) {
        this.nearExpiryDays = nearExpiryDays;
    }

    public Duration getLowStockCooldown() {
        return lowStockCooldown;
    }

    public void setLowStockCooldown(Duration lowStockCooldown) {
        this.lowStockCooldown = lowStockCooldown;
    }
}
