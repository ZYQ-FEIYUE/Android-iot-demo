package com.example.feiyue.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.feiyue.R;
import com.example.feiyue.ui.DeviceActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;

public class LinkDeviceAdapter extends RecyclerView.Adapter<LinkDeviceAdapter.ViewHolder> {

    private Context mContext;
    private List<LinkDevice> mLinkDeviceList;
    private OnLongClickCallbackBlock longClickCallBack;
    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView deviceIV;
        TextView deviceNameTV;
        TextView deviceModeTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            deviceIV = itemView.findViewById(R.id.device_image);
            deviceNameTV = itemView.findViewById(R.id.device_name);
            deviceModeTV = itemView.findViewById(R.id.device_mode);
        }
    }
    public LinkDeviceAdapter(List<LinkDevice> linkDeviceList) {
        mLinkDeviceList = linkDeviceList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.device_item,
                parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                LinkDevice linkDevice = mLinkDeviceList.get(position);
                Intent intent = new Intent(mContext, DeviceActivity.class);
                intent.putExtra(DeviceActivity.DEVICE_MAC, linkDevice.getDeviceBssid());       //把设备ssid传递给设备界面
                mContext.startActivity(intent);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                if (longClickCallBack != null)
                    longClickCallBack.callBack(v, position);
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LinkDevice linkDevice = mLinkDeviceList.get(position);
        holder.deviceNameTV.setText(linkDevice.getName());
        holder.deviceModeTV.setText(linkDevice.getMode().equals("my") ? "我的" : "共享");
        Glide.with(mContext).load(linkDevice.getImageId()).into(holder.deviceIV);
    }

    @Override
    public int getItemCount() {
        return mLinkDeviceList.size();
    }

    public interface OnLongClickCallbackBlock {
        void callBack(View v, int position);
    }
    public void setLongClick(OnLongClickCallbackBlock longClickCallBack) {
        this.longClickCallBack = longClickCallBack;
    }
}
