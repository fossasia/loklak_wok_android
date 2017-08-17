package org.loklak.wok.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.loklak.wok.model.suggest.Query;
import org.loklak.wok.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.SuggestViewHolder> {

    private List<Query> mQueries = new ArrayList<>();
    private OnSuggestionClickListener mSuggestionClickListener;

    public SuggestAdapter(List<Query> queries, OnSuggestionClickListener clickListener) {
        mQueries = queries;
        mSuggestionClickListener = clickListener;
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClicked(Query query);
    }

    @Override
    public SuggestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_tweet_search_suggest, parent, false);
        return new SuggestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SuggestViewHolder holder, int position) {
        holder.bind(mQueries.get(position));
    }

    @Override
    public int getItemCount() {
        return mQueries.size();
    }

    public void setQueries(List<Query> queries) {
        mQueries = queries;
        notifyDataSetChanged();
    }

    public List<Query> getQueries() {
        return mQueries;
    }

    class SuggestViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.suggest_query)
        TextView suggestQuery;

        SuggestViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> {
                int position = getLayoutPosition();
                Query query = mQueries.get(position);
                mSuggestionClickListener.onSuggestionClicked(query);
            });
        }

        void bind(Query query) {
            suggestQuery.setText(query.getQuery());
        }
    }
}
