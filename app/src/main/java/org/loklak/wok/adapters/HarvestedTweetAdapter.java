
package org.loklak.wok.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.loklak.wok.model.harvest.Status;
import org.loklak.wok.model.harvest.User;
import org.loklak.wok.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HarvestedTweetAdapter
        extends RecyclerView.Adapter<HarvestedTweetAdapter.HarvestedTweetViewHolder> {

    private List<Status> mHarvestedTweetList;

    public HarvestedTweetAdapter(List<Status> harvestedTweets) {
        this.mHarvestedTweetList = harvestedTweets;
    }

    @Override
    public HarvestedTweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_harvested_tweet, parent, false);
        return new HarvestedTweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HarvestedTweetViewHolder holder, int position) {
        holder.bind(mHarvestedTweetList.get(position));
    }

    @Override
    public int getItemCount() {
        return mHarvestedTweetList.size();
    }

    public void addHarvestedTweets(List<Status> harvestedTweetList) {
        int count = mHarvestedTweetList.size();
        mHarvestedTweetList.addAll(harvestedTweetList);
        notifyItemRangeInserted(getItemCount(), count);
    }

    public ArrayList<Status> getHarvestedTweetList() {
        return (ArrayList<Status>) mHarvestedTweetList;
    }

    public void clearAdapter() {
        mHarvestedTweetList.clear();
        notifyDataSetChanged();
    }

    class HarvestedTweetViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_fullname)
        TextView userFullname;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.tweet_date)
        TextView tweetDate;
        @BindView(R.id.harvested_tweet_text)
        TextView harvestedTweetTextView;

        public HarvestedTweetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Status harvestedTweet) {
            User user = harvestedTweet.getUser();
            userFullname.setText(user.getName());
            username.setText("@" + user.getScreenName());
            tweetDate.setText(getReadableDate(harvestedTweet.getCreatedAt()));
            harvestedTweetTextView.setText(harvestedTweet.getText());
        }

        private String getReadableDate(Long miliseconds) {

            CharSequence formatted = DateUtils.getRelativeTimeSpanString(miliseconds);
            return formatted.toString();
        }
    }
}
