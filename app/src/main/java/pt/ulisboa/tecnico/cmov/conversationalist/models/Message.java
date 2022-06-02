package pt.ulisboa.tecnico.cmov.conversationalist.models;

public class Message implements Comparable<Message> {
    private User sender;
    private MediaType media;
    private String value;
    private long timestamp;

    public Message(User sender, MediaType media, String value, long timestamp) {
        this.sender = sender;
        this.media = media;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getValue() {
        return this.value;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public int compareTo(Message o) {
        if(getTimestamp() == o.getTimestamp()) {
            return 0;
        }else if(getTimestamp() > o.getTimestamp()) {
            return 1;
        }
        return -1;
    }
}
