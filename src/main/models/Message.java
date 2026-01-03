package main.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;

public class Message {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty threadId = new SimpleIntegerProperty();
    private final StringProperty sender = new SimpleStringProperty();
    private final StringProperty content = new SimpleStringProperty();
    private final BooleanProperty isRead = new SimpleBooleanProperty();

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getThreadId() { return threadId.get(); }
    public void setThreadId(int value) { threadId.set(value); }
    public IntegerProperty threadIdProperty() { return threadId; }

    public String getSender() { return sender.get(); }
    public void setSender(String value) { sender.set(value); }
    public StringProperty senderProperty() { return sender; }

    public String getContent() { return content.get(); }
    public void setContent(String value) { content.set(value); }
    public StringProperty contentProperty() { return content; }

    public boolean isRead() { return isRead.get(); }
    public void setRead(boolean value) { isRead.set(value); }
    public BooleanProperty isReadProperty() { return isRead; }
}
