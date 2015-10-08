package com.gowarrior.tvsetting;

import android.alisdk.AliSettings;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.gowarrior.settinglibrary.CommonDialog;
import com.gowarrior.settinglibrary.OnDialogCountDownListener;
import com.gowarrior.settinglibrary.OnDialogDoneListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by GoWarrior on 2015/6/25.
 */
public class TVSettingActivity extends Activity implements OnDialogDoneListener,
        OnDialogCountDownListener {
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

    private AliSettings mAliSettings;
    private int mSettedResolution = 0;
    private int mTobeSetResolution = 0;
    private int mScaleSize = 0;
    private int mSpdifSel = 0;
    private SharedPreferences mSharePreferences;
    private final static String NAME = "TVSetting";
    private final static String KEY = "ShowTips";
    private final static String TAG_DIALOG_CHANGE_RESOLUTION = "change-resolution";
    private final static String TAG_DIALOG_CONFIRM_RESOLUTION = "confirm-resolution";
    private boolean mShowTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tv_setting_activity);
        mBackground = findViewById(R.id.tv_setting_bg);
        mRegionTip = findViewById(R.id.tv_setting_region_tip);
        mContext = this;

        try {
            mAliSettings = new AliSettings();
        } catch (Exception e) {
            Log.e(LOGTAG, "new AliSettings failed!");
        }

        try {
            int res = mAliSettings.getResolution();
            switch (res) {
                case AliSettings.RESOLUTION_720P50:
                    mSettedResolution = 0;
                    break;
                case AliSettings.RESOLUTION_720P60:
                    mSettedResolution = 1;
                    break;
                case AliSettings.RESOLUTION_1080P50:
                    mSettedResolution = 2;
                    break;
                case AliSettings.RESOLUTION_1080P60:
                    mSettedResolution = 3;
                    break;
                default:
                    mSettedResolution = 0;
                    break;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "get resolution failed!");
        }

        try {
            mScaleSize = mAliSettings.getScaleRatio();
        } catch (Exception e) {
            Log.e(LOGTAG, "get Scale ratio failed");
        }

        try {
            int mSpdif = mAliSettings.getDigitalMode();
            switch (mSpdif) {
                case AliSettings.DIGITAL_MODE_PCM:
                    mSpdifSel = 0;
                    break;
                case AliSettings.DIGITAL_MODE_BS:
                    mSpdifSel = 1;
                    break;
                default:
                    mSpdifSel = 0;
                    break;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "get digital mode failed");
        }

        mSharePreferences = mContext.getSharedPreferences(NAME, Activity.MODE_PRIVATE);
        mShowTips = mSharePreferences.getBoolean(KEY, true);

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
                if(mOptionList.isFocused() && 1 == mOptionList.getSelectedItemPosition()) {
                    if(KeyEvent.ACTION_DOWN == event.getAction()) {
                        changeScaleSize(-1);
                    }
                    return true;
                }
                if(mValueList.isFocused()) {
                    mOptionList.requestFocus();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(mOptionList.isFocused() && 1 == mOptionList.getSelectedItemPosition()) {
                    if(KeyEvent.ACTION_DOWN == event.getAction()) {
                        changeScaleSize(1);
                    }
                    return true;
                }
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

    @Override
    public void onDialogDone(String tag, int id, String message) {
        if (tag.equals(TAG_DIALOG_CHANGE_RESOLUTION)) {
            if (0 == id) {
                mShowTips = false;
                mSharePreferences.edit().putBoolean(KEY, mShowTips);
                mSharePreferences.edit().commit();
            }
            if (id >= 0) {
                Log.i(LOGTAG, "apply the resolution after show tips");
                try {
                    setAliRes(mTobeSetResolution);
                } catch (Exception e ) {
                    Log.e(LOGTAG, "set resolution failed" + mTobeSetResolution);
                }
                CommonDialog.showDialog(R.string.msg_tv_setting_confirm_resolution,
                        R.array.tv_setting_confirm_resolution_buttons, 1, R.style.SmallDialog,
                        15000, 1000, TAG_DIALOG_CONFIRM_RESOLUTION, this);
            }
        } else if (tag.equals(TAG_DIALOG_CONFIRM_RESOLUTION)) {
            if (0 == id) {
                //save the resolution
                Log.i(LOGTAG, "save the resolution");
                mSettedResolution = mTobeSetResolution;
            } else {
                //discard the resolution
                Log.i(LOGTAG, "discard the resolution");
                try {
                    setAliRes(mSettedResolution);
                } catch (Exception e) {
                    Log.e(LOGTAG, "restore previous resolution failed");
                }
                setValueListCheckItem(mTobeSetResolution, mSettedResolution);
            }
        }
    }

    @Override
    public void onDialogCountDown(String tag, int remainTime, CommonDialog dialog) {
        Log.v(LOGTAG, "onDialogCountDown: tag=" + tag + " id=" + remainTime);
        if (tag.equals(TAG_DIALOG_CONFIRM_RESOLUTION)) {
            String message = getResources().getString(R.string.msg_tv_setting_confirm_resolution);
            message += " " + remainTime/1000 + getResources().
                    getString(R.string.msg_tv_setting_restore_resolution);
            dialog.setMessage(message);
        }
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
                        updateValueListAdapter(mResolutionList, getResolutionSel());
                    } else if (1 == position) {
                        mBackground.setBackgroundResource(R.drawable.tv_setting_region);
                        mRegionTip.setVisibility(View.VISIBLE);
                        mValueList.setVisibility(View.INVISIBLE);
                    } else if (2 == position) {
                        mBackground.setBackgroundResource(R.drawable.tv_setting_bg);
                        mRegionTip.setVisibility(View.INVISIBLE);
                        mValueList.setVisibility(View.VISIBLE);
                        updateValueListAdapter(mSpdifList, getSpdifSel());
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
        mValueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (0 == mOptionSel) {
                    setResolutionSel(position);
                } else if (2 == mOptionSel) {
                    setSpdifSel(position);
                }
            }
        });

        updateValueListAdapter(mResolutionList, getResolutionSel());
        mValueSel = getResolutionSel();
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

    /**
     * Get current selected resolution
     */
    private int getResolutionSel() {
        return mSettedResolution;
    }

    /**
     * Set selected resolution
     */
    private void setResolutionSel(int sel) {
        setValueListCheckItem(mSettedResolution, sel);
        mTobeSetResolution = sel;

        if (mShowTips) {
            CommonDialog.showDialog(R.string.msg_tv_setting_change_resolution,
                    R.array.tv_setting_change_resolution_buttons, 1,
                    TAG_DIALOG_CHANGE_RESOLUTION, this);
        } else {
            Log.i(LOGTAG, "apply the resolution without show tips");
            try {
                setAliRes(mTobeSetResolution);
            } catch (Exception e) {
                Log.e(LOGTAG, "set resolution failed!" + mTobeSetResolution);
            }
            CommonDialog.showDialog(R.string.msg_tv_setting_confirm_resolution,
                    R.array.tv_setting_confirm_resolution_buttons, 1, R.style.SmallDialog,
                    15000, 1000, TAG_DIALOG_CONFIRM_RESOLUTION, this);
        }
    }

    /**
     * Set resolution
     */
    private void setAliRes(int pos) {
        int res = -1;
        switch (pos) {
            case 0:
                res = AliSettings.RESOLUTION_720P50;
                break;
            case 1:
                res = AliSettings.RESOLUTION_720P60;
                break;
            case 2:
                res = AliSettings.RESOLUTION_1080P50;
                break;
            case 3:
                res = AliSettings.RESOLUTION_1080P60;
                break;
            default:
                res = AliSettings.RESOLUTION_720P50;
                break;
        }
        mAliSettings.setResolution(res);
        mAliSettings.resetScreen(0);
        mAliSettings.setScaleRatio(mScaleSize, 5);
    }

    //set selected picture when item is selected
    private void setValueListCheckItem(int oldPos, int newPos) {
        HashMap<String, Object> map = (HashMap<String, Object>)mValueList.getItemAtPosition(oldPos);
        map.put("selected", 0);
        map = (HashMap<String, Object>)mValueList.getItemAtPosition(newPos);
        map.put("selected", R.drawable.common_item_selected);

        ((BaseAdapter)mValueList.getAdapter()).notifyDataSetChanged();
    }

    /**
     * change scale of output
     */
    private void changeScaleSize(int adjustment) {
        int scaleSize = mScaleSize + adjustment;
        if ((scaleSize >= 80) && (scaleSize <= 100)) {
            mAliSettings.setScaleRatio(scaleSize, 5);
            mScaleSize = scaleSize;
        }
    }

    /**
     * get current spdif mode
     */
    private int getSpdifSel() {
        return mSpdifSel;
    }

    /**
     * set selected spdif
     */
    private void setSpdifSel(int sel) {
        Log.i(LOGTAG, "Set digital mode to " + sel);
        setAliDigitalMode(sel);
        setValueListCheckItem(mSpdifSel, sel);
        mSpdifSel = sel;
    }

    /**
     * Set Spdif
     */
    private void setAliDigitalMode(int sel) {
        int mDigitalMode = 0;
        switch (sel) {
            case 0:
                mDigitalMode = AliSettings.DIGITAL_MODE_PCM;
                break;
            case 1:
                mDigitalMode = AliSettings.DIGITAL_MODE_BS;
                break;
            default:
                mDigitalMode = AliSettings.DIGITAL_MODE_PCM;
                break;
        }
        mAliSettings.setDigitalMode(mDigitalMode);
    }
}
