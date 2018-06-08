package project.android.com.connect24;

public class ConvModel
{
    public boolean isSeen()
    {
        return seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean seen;
   public long timestamp;

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ConvModel(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public ConvModel() {
    }
}
