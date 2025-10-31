import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Note {
    private String text;
    private LocalDateTime createdAt;

    public Note(String text) {
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    public Note(String text, LocalDateTime createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return text + " | " + createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
