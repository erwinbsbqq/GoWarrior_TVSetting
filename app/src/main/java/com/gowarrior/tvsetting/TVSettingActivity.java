package com.gowarrior.tvsetting;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by GoWarrior on 2015/6/25.
 */
public class TVSettingActivity extends Activity {
    private final static String LOGTAG = TVSettingActivity.class.getSimpleName();

    private int mOptionSel;
    private int mValueSel;
    private ListView mOptionList;
    private ListView mValueList;
    private View mOptionFocusView;
    private View mValueFocusView;

    private ArrayList<String> mResolutionList;
    private ArrayList<String> mSpdifList;
    private Context mContext;

    private View mBackground;
    private View mRegionTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tv_setting_activity);
        mBackground = findViewById(R.id.tv_setting_bg);
        mRegionTip = findViewById(R.id.tv_setting_region_tip);
        mContext = this;
        initListData();
        initOptionList();
        initValueList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.v(LOGTAG, "dispatchKeyEvent: code=" + event.getKeyCode()
                + " action=" + event.getAction());
        final int keyCode = event.getKeyCode();

        switch(keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(mValueList.isFocused()) {
                    mOptionList.requestFocus();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(mOptionList.isFocused()) {
                    mValueList.requestFocus();
                }
                return true;

            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    private void initListData() {
        mResolutionList = new ArrayList<String>();
        final String[] resList = {"720P50", "720P60", "1080P50", "1080P60"};
        for (String item : resList) {
            mResolutionList.add(item);
        }

        mSpdifList = new ArrayList<String>();
        final String[] spdifList = getResources().getStringArray(
                R.array.tv_setting_digital_audio_output);
        for (String item : spdifList) {
            mSpdifList.add(item);
        }
    }

    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v == mOptionList) {
                Log.v(LOGTAG, "mOptionList.onFocusChange: hasFocus=" + hasFocus);
                if (hasFocus) {
                    showFocusAnimation(mOptionFocusView, R.anim.tv_setting_avsys_item_focus_in);
                } else {
                    showFocusAnimation(mOptionFocusView, R.anim.tv_setting_avsys_item_focus_out);
                }
            } else if (v == mValueList) {
                Log.v(LOGTAG, "mValueList.onFocusChange: hasFocus=" + hasFocus);
                if (hasFocus) {
                    showFocusAnimation(mValueFocusView, R.anim.tv_setting_avsys_item_focus_in);
                } else {
                    showFocusAnimation(mValueFocusView, R.anim.tv_setting_avsys_item_focus_out);
                }
            }
        }
    };

    private void initOptionList() {
        mOptionList = (ListView)findViewById(R.id.tv_setting_avsys_option_list);
        mOptionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOGTAG, "mOptionList.onItemSelected: position=" + position);

                if (mOptionList.isFocused()) {
                    showFocusAnimation(mOptionFocusView,
                            R.anim.tv_setting_avsys_item_focus_out);
                    showFocusAnimation(view,
                            R.anim.tv_setting_avsys_item_focus_in);

                    //update value list(or background) when the selected item of option list changed
                    if (0 == position) {
                        mBackground.setBackgroundResource(R.drawable.tv_setting_bg);
                        mRegionTip.setVisibility(View.INVISIBLE);
                        mValueList.setVisibility(View.VISIBLE);
                        updateValueListAdapter(mResolutionList, 0);
                    } else if (1 == position) {
                        mBackground.setBackgroundResource(R.drawable.tv_setting_region);
                        mRegionTip.setVisibility(View.VISIBLE);
                        mValueList.setVisibility(View.INVISIBLE);
                    } else if (2 == position) {
                        mBackground.setBackgroundResource(R.drawable.tv_setting_bg);
                        mRegionTip.setVisibility(View.INVISIBLE);
                        mValueList.setVisibility(View.VISIBLE);
                        updateValueListAdapter(mSpdifList, 0);
                    }
                }

                mOptionFocusView = view;
                mOptionSel = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.v(LOGTAG, "mOptionList.onNothingSelected");
            }
        });
        mOptionList.setOnFocusChangeListener(mOnFocusChangeListener);

        updateOptionListAdapter(getOptionList(), 0);
        mOptionList.requestFocus();
    }

    private ArrayList<String> getOptionList() {
        ArrayList<String> data = new ArrayList<String>();
        data.add(getResources().getString(R.string.label_tv_setting_av_resolution));
        data.add(getResources().getString(R.string.label_tv_setting_av_display_area));
        data.add(getResources().getString(R.string.label_tv_setting_av_audio_output));
        return data;
    }

    private void updateOptionListAdapter(ArrayList<String> listData, int selPosition) {
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < listData.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", listData.get(i));
            listItem.add(map);
        }

        SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,
                R.layout.tv_setting_option_item,
                new String[] { "name" }, new int[] {
                R.id.tv_setting_option_text });
        mOptionList.setSelection(selPosition);
        mOptionList.setAdapter(listItemAdapter);
        mOptionSel = selPosition;
    }

    private void initValueList() {
        mValueList = (ListView)findViewById(R.id.tv_setting_avsys_value_list);
        mValueList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOGTAG, "mValueList.onItemSelected: position=" + position);
                if (mValueList.isFocused()) {
                    showFocusAnimation(mValueFocusView,
                            R.anim.tv_setting_avsys_item_focus_out);
                    showFocusAnimation(view,
                            R.anim.tv_setting_avsys_item_focus_in);
                }

                mValueFocusView = view;
                mValueSel = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.v(LOGTAG, "mValueList.onNothingSelected");
            }
        });
        mValueList.setOnFocusChangeListener(mOnFocusChangeListener);

        updateValueListAdapter(mResolutionList, 0);
        mValueSel = 0;
    }

    private void updateValueListAdapter(ArrayList<String> listData, int selPosition) {
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < listData.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("selected",
                    (i == selPosition) ? R.drawable.common_item_selected : 0);
            map.put("name", listData.get(i));
            listItem.add(map);
        }

        SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,
                R.layout.tv_setting_value_item,
                new String[] { "selected", "name" }, new int[] {
                R.id.tv_setting_value_selected, R.id.tv_setting_value_name });

        mValueList.setSelection(selPosition);
        if (mValueList.getSelectedView() != null)
            mValueList.getSelectedView().setSelected(true);
        mValueList.setAdapter(listItemAdapter);
        mValueSel = selPosition;
    }

    private void showFocusAnimation(View view, int animId) {
        if (null != view) {
            Animator mAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                    mContext, animId);
            mAnimatorSet.setTarget(view);
            mAnimatorSet.start();
        }
    }
}
