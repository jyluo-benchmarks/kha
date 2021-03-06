package com.kalixia.ha.model.sensors;

import com.google.common.base.Objects;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.joda.time.Instant;

import javax.measure.Measurable;
import javax.measure.quantity.Quantity;
import java.io.Serializable;

/**
 * The main purpose of this class is to encapsulate both the value of a sensor and the time when the measurement was
 * done.
 * @param <Q> the type of value
 */
@ApiModel("DataPoint")
public class DataPoint<Q extends Quantity> implements Serializable {
    @ApiModelProperty("value of the datapoint")
    private final Measurable<Q> value;

    @ApiModelProperty("date of the datapoint")
    private final Instant at;

    public DataPoint(Measurable<Q> value) {
        this(value, Instant.now());
    }

    public DataPoint(Measurable<Q> value, Instant at) {
        this.value = value;
        this.at = at;
    }

    public Measurable<Q> getValue() {
        return value;
    }

    public Instant getAt() {
        return at;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("value", value)
                .add("at", at)
                .toString();
    }
}
