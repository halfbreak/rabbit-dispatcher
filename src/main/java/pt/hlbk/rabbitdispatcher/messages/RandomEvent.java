package pt.hlbk.rabbitdispatcher.messages;

import java.util.Objects;

public class RandomEvent {
    private String orderId;
    private String name;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RandomEvent{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomEvent that = (RandomEvent) o;
        return Objects.equals(getOrderId(), that.getOrderId()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId(), getName());
    }
}
