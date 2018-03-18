package cs492.vacationplanner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by liyon on 3/3/2018.
 */

public class visited_wishlist_adapter extends RecyclerView.Adapter<visited_wishlist_adapter.ListViewHolder>{
    private ArrayList<String> mlistLocation;
    OnListItemClickListener mListItemClickListener;

    visited_wishlist_adapter(OnListItemClickListener ListItemClickListener){
        mListItemClickListener = ListItemClickListener;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.db_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        holder.bind(mlistLocation.get(position));

    }

    public interface OnListItemClickListener {
        void onListItemClick(String location);
    }

    @Override
    public int getItemCount() {
        if(mlistLocation != null){
            return mlistLocation.size();
        } else {
            return 0;
        }
    }

    public void updateVWList(ArrayList<String> locationList){
        mlistLocation = locationList;
        notifyDataSetChanged();

    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView mListTV;

        public ListViewHolder(View itemView) {
            super(itemView);
            mListTV = (TextView)itemView.findViewById(R.id.tv_db_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String location = mlistLocation.get(getAdapterPosition());
                    mListItemClickListener.onListItemClick(location);
                }
            });

        }

        public void bind(String s) {
            mListTV.setText(s);
        }
    }
}
