package com.jaslong.util.android.view.group;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/**
 * Adapter with functionality for groups.
 */
public interface GroupListAdapter extends ListAdapter {

    Object getGroup(int position);

    View getGroupView(Object group, View convertView, ViewGroup parent);

}
