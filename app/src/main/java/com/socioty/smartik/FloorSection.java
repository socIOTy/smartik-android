package com.socioty.smartik;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.socioty.smartik.model.Room;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by Willian on 2017-04-19.
 */

public class FloorSection extends StatelessSection {
    private final ManageRoomFragment manageRoomFragment;
    private final FragmentManager fragmentManager;
    private final int floorNumber;
    private final List<Room> rooms;


    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public Button mButton;

        public HeaderViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.text_floor_section_header);
            mButton = (Button) view.findViewById(R.id.button_add_device);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView mImage;
        public ItemViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.room_name);
            mImage = (ImageView) view.findViewById(R.id.room_image);
        }
    }

    public FloorSection(final ManageRoomFragment manageRoomFragment, final FragmentManager fragmentManager, final int floorNumber, final List<Room> rooms) {
        super(R.layout.floor_section_header, R.layout.room_adapter);
        this.manageRoomFragment = manageRoomFragment;
        this.fragmentManager = fragmentManager;
        this.floorNumber = floorNumber;
        this.rooms = rooms;
    }

    @Override
    public int getContentItemsTotal() {
        return rooms.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder uncastedHolder, final int position) {
        final ItemViewHolder holder = (ItemViewHolder) uncastedHolder;
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

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        final String floorStr = String.format(holder.itemView.getResources().getString(R.string.floor_number), floorNumber);
        headerHolder.mTextView.setText(floorStr);

        headerHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Bundle bundle = new Bundle();
                bundle.putInt(ManageRoomFragment.KEY_FLOOR_NUMBER, floorNumber - 1);
                manageRoomFragment.setArguments(bundle);
                manageRoomFragment.show(fragmentManager, "BLA");
            }
        });

    }
}
