package github.kasuminova.network.message;

public interface MessageProcessor<T> {
    void process(T message);
}
