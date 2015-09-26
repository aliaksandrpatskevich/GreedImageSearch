package com.het.greedimagesearch.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.het.greedimagesearch.R;
import com.het.greedimagesearch.models.ImageResult;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageResultsAdapter extends ArrayAdapter<ImageResult> {

    public ImageResultsAdapter(Context context, List<ImageResult> objects) {
        super(context, R.layout.item_image_result, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ImageResult imageInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_image_result, parent, false);
        }
        // Lookup view for data population
        ImageView ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);

        // Populate the data into the template view using the data object
        tvTitle.setText(Html.fromHtml(imageInfo.title));
        ivImage.setImageResource(0); //reset

        //        insert image using picasso
        Picasso.with(getContext()).load(imageInfo.thumbUrl)
//        Picasso.with(getContext()).load(imageInfo.fullUrl)
                .placeholder(R.drawable.placeholder)
                .into(ivImage);

        // Return the completed view to render on screen
        return convertView;
    }
}
