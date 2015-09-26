package com.het.greedimagesearch.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.het.greedimagesearch.R;
import com.het.greedimagesearch.SQLite.DatabaseHelper;
import com.het.greedimagesearch.models.Filter;


public class FilterDialog extends DialogFragment
        implements Button.OnClickListener {

    private Spinner spImageSize;
    private Spinner spColor;
    private Spinner spType;
    private EditText etSite;
    private Button btnOk;
    private Button btnCancel;
    public Filter filter = new Filter();

    public interface FilterDialogListener {
        void onFinishFilterDialogListener(Filter filter);
    }

    public FilterDialog() {
        // Empty constructor required for DialogFragment
    }

    public static FilterDialog newInstance(Filter filter) {
        FilterDialog frag = new FilterDialog();
        Bundle args = new Bundle();
        args.putString("size", filter.size);
        args.putString("color", filter.color);
        args.putString("type", filter.type);
        args.putString("site", filter.site);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container);
        getDialog().setTitle("Advanced Filters");

        spImageSize = (Spinner) view.findViewById(R.id.spImageSize);
        spColor = (Spinner) view.findViewById(R.id.spColor);
        spType = (Spinner) view.findViewById(R.id.spType);
        etSite = (EditText) view.findViewById(R.id.etSite);

        btnOk = (Button) view.findViewById(R.id.btnOk);
        btnCancel = (Button) view.findViewById(R.id.btnCancel);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.image_size, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterColor = ArrayAdapter.createFromResource(getContext(),
                R.array.color, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(getContext(),
                R.array.type, android.R.layout.simple_spinner_item);

// Apply the adapter to the spinner
        spImageSize.setAdapter(adapter);
        spColor.setAdapter(adapterColor);
        spType.setAdapter(adapterType);

//        set saved filter
        for (int i = 0; i < adapter.getCount(); i++) {
            if (getArguments().getString("size", "any").equals(adapter.getItem(i).toString())) {
                spImageSize.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < adapterColor.getCount(); i++) {
            if (getArguments().getString("color", "any").equals(adapterColor.getItem(i).toString())) {
                spColor.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < adapterType.getCount(); i++) {
            if (getArguments().getString("type", "any").equals(adapterType.getItem(i).toString())) {
                spType.setSelection(i);
                break;
            }
        }

        etSite.setText(getArguments().getString("site", ""));
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOk:
                filter.size = spImageSize.getSelectedItem().toString();
                filter.color = spColor.getSelectedItem().toString();
                filter.type = spType.getSelectedItem().toString();
                filter.site = etSite.getText().toString();

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());
                databaseHelper.updateFilter(filter);

                FilterDialogListener listener = (FilterDialogListener) getActivity();
                listener.onFinishFilterDialogListener(filter);

            case R.id.btnCancel:
                dismiss();
                break;
        }
    }
}