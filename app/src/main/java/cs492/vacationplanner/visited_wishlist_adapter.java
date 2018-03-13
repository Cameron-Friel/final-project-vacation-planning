package cs492.vacationplanner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by liyon on 3/3/2018.
 */

public class visited_wishlist_adapter extends RecyclerView.Adapter<visited_wishlist_adapter.ListViewHolder>{
    // DB data list
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
        //holder.bind(DBList.get(positoin));

    }

    public interface OnListItemClickListener {
        // void onListItemClick()
    }

    @Override
    public int getItemCount() {
        // Db data list item count
        return 0;
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView mListTV;

        public ListViewHolder(View itemView) {
            super(itemView);
            mListTV = (TextView)itemView.findViewById(R.id.tv_list_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String country = "US"; // DB country field
                    // void onListItemClick(country)
                }
            });

        }

        public void bind(){ // param DB info
            mListTV.setText("country name from DB");
        }
    }
}
