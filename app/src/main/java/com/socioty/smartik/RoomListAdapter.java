package com.socioty.smartik;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.socioty.smartik.model.Room;

import java.util.List;

/**
 * Created by serhiipianykh on 2017-04-17.
 */

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

    private final List<Room> rooms;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImage;
        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.room_name);
            mImage = (ImageView) view.findViewById(R.id.room_image);
        }
    }

    public RoomListAdapter(final List<Room> rooms) {
        this.rooms = rooms;
    }

    @Override
    public RoomListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.room_adapter, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Room room = rooms.get(position);
        final byte[] imageBytes = room.getImageBytes();
        if (imageBytes != null) {
            final Bitmap bitMap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.mImage.setImageBitmap(bitMap);
        }
        holder.mTextView.setText(room.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(v.getContext(), RoomDetailsActivity.class);
                final Bundle data = new Bundle();
                data.putSerializable("roomName", rooms.get(position).getName());
                intent.putExtras(data);

                v.getContext().startActivity(intent);
            }
        });
    }
}
