package project.android.com.connect24;

public class UserListFriendsModel
{
    private  String date;

    public UserListFriendsModel() {
    }

    public UserListFriendsModel(String date)
    {
        this.date = date;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
