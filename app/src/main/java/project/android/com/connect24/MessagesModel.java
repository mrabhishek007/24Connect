package project.android.com.connect24;

public class MessagesModel
{

   public String message,type;
   public String from;
   public long time;
   public boolean seen;


    public MessagesModel()
    {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;

    }

    public void setFrom(String from) {
        this.from = from;
    }
}
