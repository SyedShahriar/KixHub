package syedshahriar.com.KixHub.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import syedshahriar.com.KixHub.R;
import syedshahriar.com.KixHub.SearchActivity;
import syedshahriar.com.KixHub.SearchFragment;
import syedshahriar.com.KixHub.WatchListFragment;
import syedshahriar.com.KixHub.models.Post;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    private static final String TAG = "PostListAdapter";
    private static final int NUM_GRID_COLUMNS = 3;

    private Context mContext;
    private ArrayList<Post> mPosts;


    public class  ViewHolder extends RecyclerView.ViewHolder{
        SquareImageView mPostImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mPostImage = (SquareImageView) itemView.findViewById(R.id.post_image);
            int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            int imageWidth = gridWidth/NUM_GRID_COLUMNS;
            mPostImage.setMaxHeight(imageWidth);
            mPostImage.setMaxWidth(imageWidth);
        }
    }

    public PostListAdapter(Context mContext, ArrayList<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_view_post,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UniversalImageLoader.setImage(mPosts.get(position).getImage(),holder.mPostImage);
        final int pos = position;
        holder.mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: selected a post");
                //TODO

                //View post in detail
                Fragment fragment = (Fragment)((SearchActivity)mContext).getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                ((SearchActivity)mContext).mViewPager.getCurrentItem());

                if (fragment!=null){
                    if(fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":0")){
                        Log.d(TAG, "onClick: switching to: " + mContext.getString(R.string.fragment_view_post));

                        SearchFragment searchFragment = (SearchFragment)((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((SearchActivity)mContext).mViewPager.getCurrentItem());

                        searchFragment.viewPost(mPosts.get(pos).getPost_id());
                    }
                    else if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":1")){
                        Log.d(TAG, "onClick: switching to: " + mContext.getString(R.string.fragment_watch_list));
                        WatchListFragment watchListFragment = (WatchListFragment)((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((SearchActivity)mContext).mViewPager.getCurrentItem());

                        watchListFragment.viewPost(mPosts.get(pos).getPost_id());
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }



}
