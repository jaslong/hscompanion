package com.jaslong.util.android.widget;

import android.widget.MultiAutoCompleteTextView;

/**
 * Tokenizer for a character.
 */
public class CharacterTokenizer implements MultiAutoCompleteTextView.Tokenizer {

    private final char mDelimiter;

    public CharacterTokenizer(char delimiter) {
        mDelimiter = delimiter;
    }

    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        int i = cursor;
        while (i > 0 && text.charAt(i - 1) != mDelimiter) {
            --i;
        }
        return i;
    }

    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();
        while (i < len) {
            if (text.charAt(i - 1) == mDelimiter) {
                return i;
            } else {
                ++i;
            }
        }
        return len;
    }

    @Override
    public CharSequence terminateToken(CharSequence text) {
        if (Character.isWhitespace(text.charAt(text.length() - 1))) {
            return text;
        } else {
            return new StringBuilder(text).append(mDelimiter).toString();
        }
    }

}
