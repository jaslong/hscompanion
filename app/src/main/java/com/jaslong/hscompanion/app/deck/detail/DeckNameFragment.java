package com.jaslong.hscompanion.app.deck.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jaslong.hscompanion.R;
import com.jaslong.util.android.app.BaseDialogFragment;

public class DeckNameFragment extends BaseDialogFragment {

    public interface Callback {
        void onDeckName(String name);
    }

    public static final String TAG = "deck_name";

    private static final String STATE_CURRENT_DECK_NAME = "current_deck_name";

    public static DeckNameFragment create(String currentDeckName) {
        DeckNameFragment fragment = new DeckNameFragment();
        Bundle args = new Bundle();
        args.putString(STATE_CURRENT_DECK_NAME, currentDeckName);
        fragment.setArguments(args);
        return fragment;
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback) getParentFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.deck_name_fragment, null);
        final EditText editText = (EditText) view.findViewById(R.id.deck_name_edit);
        editText.setText(getState().getString(STATE_CURRENT_DECK_NAME));
        editText.selectAll();
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.deck_name_title)
                .setView(view)
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onDeckName(editText.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

}
