package lars.com.model;

public class DialogueLine {

    public final CharacterId speaker;
    public final String text;

    public DialogueLine(CharacterId speaker, String text) {
        this.speaker = speaker;
        this.text    = text;
    }
}
