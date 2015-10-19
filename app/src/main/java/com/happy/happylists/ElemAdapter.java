package com.happy.happylists;

import java.util.ArrayList;

import com.happy.happylists.R;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ElemAdapter extends BaseExpandableListAdapter {

	static final String TAG = "myLogs";

	private Activity _context;
	private ArrayList<String> groups;
	private ArrayList<ArrayList<Elements>> elems;
	private LayoutInflater inflater;
	private Typeface faceHN;

	public ElemAdapter(Activity context,
					   ArrayList<String> groups, ArrayList<ArrayList<Elements>> elems ) {
		this._context = context;
		this.groups = groups;
		this.elems = elems;
		inflater = LayoutInflater.from(_context );
		faceHN = Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.helveticaNeueLight));
	}

	public Object getChild(int groupPosition, int childPosition) {
		return elems.get( groupPosition ).get( childPosition );
	}

	public long getChildId(int groupPosition, int childPosition) {
		return (long)( groupPosition*1024+childPosition );  // Max 1024 children per group     
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
							 ViewGroup parent) {
		View v = null;
		if( convertView != null )
			v = convertView;
		else
			v = inflater.inflate(R.layout.nastrlist, parent, false);
		Elements c = (Elements)getChild( groupPosition, childPosition );
		TextView elem = (TextView)v.findViewById( R.id.tvNT );
		elem.setTypeface(faceHN);
		if( elem != null ) elem.setText( c.getElem() );
		CheckBox cb = (CheckBox)v.findViewById( R.id.chbnas );
		switch (groupPosition) {
			case 0:
				cb.setVisibility(convertView.VISIBLE);
				break;
			case 1:
				cb.setVisibility(convertView.GONE);
				break;
			case 2:
				if (childPosition==1)
					cb.setVisibility(convertView.VISIBLE);
				else
					cb.setVisibility(convertView.GONE);
				break;
		}
		cb.setChecked( c.getState() );
		cb.setClickable(false);
		return v;
	}

	public int getChildrenCount(int groupPosition) {
		return elems.get( groupPosition ).size();
	}

	public Object getGroup(int groupPosition) {
		return groups.get( groupPosition );
	}

	public int getGroupCount() {
		return groups.size();
	}

	public long getGroupId(int groupPosition) {
		return (long)( groupPosition*1024 );  // To be consistent with getChildId     
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View v = null;
		if( convertView != null )
			v = convertView;
		else
			v = inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
		String gt = (String)getGroup( groupPosition );
		TextView nmGroup = (TextView)v.findViewById( android.R.id.text1 );
		nmGroup.setTypeface(faceHN);
		if( gt != null ) nmGroup.setText( gt );
		return v;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void onGroupCollapsed (int groupPosition) {

	}

	public void onGroupExpanded(int groupPosition) {

	}


} 


