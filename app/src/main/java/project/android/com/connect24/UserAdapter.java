package project.android.com.connect24;

public class UserAdapter
{
    public  String image,name,status,thumb_image,online;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserAdapter(String image, String name, String status, String thumb_image, String online) {
        this.image = image;
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
        this.online = online;
    }

    public String getOnline_status()
    {
        return online;

    }

    public void setOnline_status(String online_status) {
        this.online = online_status;
    }

    public UserAdapter()
    {
    }

}
