package project.android.com.connect24;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionPageAdapter  extends FragmentPagerAdapter
{
    Context context;
    int no_of_tabs = 3;

    public SectionPageAdapter(Context context, FragmentManager fragmentManager)
    {
        super(fragmentManager);
        this.context = context;

    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0 :
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1 :
                ChatsFragment chatsFragment = new ChatsFragment();
                return  chatsFragment;

            case 2 :
                FriendsFragment friendsFragment = new FriendsFragment();
                return  friendsFragment;

                default:
                   return  null;
        }

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
                default:
                    return null;

        }

    }

    @Override
    public int getCount()
    {
        return no_of_tabs;
    }
}
