package com.kalixia.ha.devices.weather.wunderground;

import com.kalixia.ha.model.User;
import com.kalixia.ha.model.devices.Device;
import com.kalixia.ha.model.devices.DeviceMetadata;
import com.kalixia.ha.model.sensors.SensorMetadata;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class WundergroundDeviceMetadata implements DeviceMetadata {

    @Override
    public String getName() {
        return "Weather Underground";
    }

    @Override
    public String getType() {
        return "wunderground";
    }

    @Override
    public String getLogo() {
        return "wunderground.png";
    }

    @Override
    public Set<SensorMetadata> getSensorsMetadata() {
        return Collections.emptySet();  // TODO: expose Wunderground sensors!
    }

    @Override
    public Device createDevice(User owner, String name) {
        return new WundergroundDevice(UUID.randomUUID(), name, owner);
    }

}
