package org.toptaxi.ataxibooking.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.data.PayType;
import org.toptaxi.ataxibooking.R;


public class PayTypesAdapter extends RecyclerView.Adapter<PayTypesAdapter.PayTypeHolder>{
    //private static String TAG = "#########" + PayTypesAdapter.class.getName();
    private static OnPayTypeClickListener onPayTypeClickListener;

    public interface OnPayTypeClickListener{
        void PayTypeClick(PayType payType);
    }

    public void setOnPayTypeClickListener(OnPayTypeClickListener PayTypeClickListener) {
        onPayTypeClickListener = PayTypeClickListener;
    }

    public PayTypesAdapter() {
    }

    @Override
    public int getItemCount(){
        return MainApplication.getInstance().getPreferences().getPayTypes().size();
    }

    @Override
    public PayTypesAdapter.PayTypeHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_route_point, viewGroup, false);
        return new PayTypesAdapter.PayTypeHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(PayTypesAdapter.PayTypeHolder viewHolder, final int position){
        viewHolder.tvName.setText(MainApplication.getInstance().getPreferences().getPayTypes().get(position).getCardCaption());
        viewHolder.tvDescription.setText(MainApplication.getInstance().getPreferences().getPayTypes().get(position).getCardDescription());
        viewHolder.ivType.setImageResource(MainApplication.getInstance().getPreferences().getPayTypes().get(position).getCardImage());
        //Log.d(TAG, "onBindViewHolder pos = " + position + ";caption = " + MainApplication.getInstance().getPreferences().getPayTypes().get(position).getCardCaption());
    }



    static class PayTypeHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        ImageView ivType;
        CardView cardView;
        PayTypeHolder(View itemView) {
            super(itemView);
            tvName = (TextView)itemView.findViewById(R.id.tvRoutePointName);
            tvDescription = (TextView)itemView.findViewById(R.id.tvRoutePointDescription);
            ivType = (ImageView)itemView.findViewById(R.id.ivRoutePointType);
            cardView = (CardView)itemView.findViewById(R.id.cvOrderRoutePoint);
            itemView.findViewById(R.id.edRoutePointNote).setVisibility(View.GONE);
            itemView.findViewById(R.id.cbRoutePoint).setVisibility(View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PayType payType = MainApplication.getInstance().getPreferences().getPayTypes().get(getAdapterPosition());
                    if (payType.clickReturnType().equals("return")){
                        if (onPayTypeClickListener != null)
                            onPayTypeClickListener.PayTypeClick(payType);
                    }
                }
            });
        }
    }
}
