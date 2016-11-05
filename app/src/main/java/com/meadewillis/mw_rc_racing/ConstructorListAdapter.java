package com.meadewillis.mw_rc_racing;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class ConstructorListAdapter extends ArrayAdapter<Players> {
	private int				resource;
	private LayoutInflater	inflater;
	private Context 		context;
	ConstructorListAdapter(Context ctx, int resourceId, List<Players> objects) {
		super( ctx, resourceId, objects );
		resource = resourceId;
		inflater = LayoutInflater.from( ctx );
		context = ctx;
	}
	@NonNull
	@Override
	public View getView (int position, View convertView, @NonNull ViewGroup parent ) {
		convertView = inflater.inflate( resource, null );
		Players Legend = getItem( position );
				TextView legendName = (TextView) convertView.findViewById(R.id.tvInfo1);
		assert Legend != null;
		legendName.setText(Legend.getName() + " #" + Legend.getId());

		TextView legendBorn = (TextView) convertView.findViewById(R.id.tvInfo2);
		legendBorn.setText("NextGate: " + Legend.getNextGate() + " Laps: " + Legend.getTotalLaps() + "\nKills: " + Legend.getTotalKills() + " Deaths: " + Legend.getTotalDeaths());

		ImageView legendImage = (ImageView) convertView.findViewById(R.id.imageView1);
		//legendImage.setColorFilter(Color.rgb(Legend.getTruckColorR(), Legend.getTruckColorG(), Legend.getTruckColorB()));
		String uri = "drawable/image" + Legend.getId();
		int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
		Drawable image = context.getDrawable(imageResource);
        legendImage.setImageDrawable(image);

		return convertView;
	}
}

