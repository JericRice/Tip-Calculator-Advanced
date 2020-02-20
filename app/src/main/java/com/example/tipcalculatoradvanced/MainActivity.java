package com.example.tipcalculatoradvanced;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;



import android.os.Bundle;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {

    private EditText billAmountEditText;
    private EditText percentEditText;
    private SeekBar percentSeekBar;
    private EditText tipEditText;
    private EditText totalEditText;
    private RadioGroup roundingRadioGroup;
    private RadioButton roundNoneRadioButton;
    private RadioButton roundTipRadioButton;
    private RadioButton roundTotalRadioButton;
    private Spinner splitSpinner;
    private TextView perPersonLabel;
    private EditText perPersonEditText;

    private SharedPreferences savedValues;

    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;
    private String billAmountString = "";
    private float tipPercent = .15f;
    private int rounding = ROUND_NONE;
    private int split;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billAmountEditText = (EditText) findViewById(R.id.billAmountEditText);
        percentEditText = (EditText) findViewById(R.id.percentTextView);
        percentSeekBar = (SeekBar) findViewById(R.id.percentSeekBar);
        tipEditText = (EditText) findViewById(R.id.tipTextView);
        totalEditText = (EditText) findViewById(R.id.totalTextView);

        roundingRadioGroup = (RadioGroup) findViewById(R.id.roundingRadioGroup);
        roundNoneRadioButton = (RadioButton) findViewById(R.id.roundNoneRadioButton);
        roundTipRadioButton = (RadioButton) findViewById(R.id.roundTipRadioButton);
        roundTotalRadioButton = (RadioButton) findViewById(R.id.roundTotalRadioButton);

        splitSpinner = (Spinner) findViewById(R.id.splitSpinner);
        perPersonEditText = (EditText) findViewById(R.id.perPersonTextView);
        perPersonLabel = (TextView) findViewById(R.id.perPersonLabel);


        // Adapter code setting Array Adapter

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        splitSpinner.setAdapter(adapter);

        //set listeners

        billAmountEditText.setOnEditorActionListener(editTextListener);
        billAmountEditText.setOnKeyListener(keyListener);
        percentSeekBar.setOnSeekBarChangeListener(seekBarListener);
        percentSeekBar.setOnKeyListener(keyListener);
        roundingRadioGroup.setOnCheckedChangeListener(radioGroupListener);
        roundingRadioGroup.setOnKeyListener(keyListener);
        splitSpinner.setOnItemSelectedListener(spinnerListener);


        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);


    }

    @Override
    public void onPause(){
        super.onPause();
        Editor editor = savedValues.edit();
        editor.putString("BillAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.putInt("rounding", rounding);
        editor.putInt("split", split);
        editor.commit();

    }

    @Override
    public void onResume() {
        super.onResume();
        billAmountString = savedValues.getString("BillAmountString", "");
        tipPercent = savedValues.getFloat("tipPercent", 0.15f);
        rounding = savedValues.getInt("rounding", ROUND_NONE);
        split = savedValues.getInt("split", 1);

        billAmountEditText.setText(billAmountString);

        int progress = Math.round(tipPercent * 100);
        percentSeekBar.setProgress(progress);

        if (rounding == ROUND_NONE) {
            roundNoneRadioButton.setChecked(true);
        } else if (rounding == ROUND_TIP) {
            roundTipRadioButton.setChecked(true);
        } else if (rounding == ROUND_TOTAL) {
            roundTotalRadioButton.setChecked(true);
        }

        int position = split - 1;
        splitSpinner.setSelection(position);
    }

    public void calculateAndDisplay() {
        billAmountString = billAmountEditText.getText().toString();
        float billAmount;
        if (billAmountString.equals("")) {
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAmountString);
        }

        int progress = percentSeekBar.getProgress();
        tipPercent = (float) progress / 100;

        float tipAmount = 0;
        float totalAmount = 0;

        if (rounding == ROUND_NONE) {
            tipAmount = billAmount * tipPercent;
            totalAmount = billAmount + tipAmount;
        } else if (rounding == ROUND_TIP) {
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = billAmount + tipAmount;
        } else if (rounding == ROUND_TOTAL) {
            float tipNotRounded = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipNotRounded);
            tipAmount = totalAmount - billAmount;
        }

        float splitAmount = 0;
        if (split == 1) {
            perPersonLabel.setVisibility(View.GONE);
            perPersonEditText.setVisibility(View.GONE);
        } else {
            splitAmount = totalAmount / split;
            perPersonLabel.setVisibility(View.VISIBLE);
            perPersonEditText.setVisibility(View.VISIBLE);
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipEditText.setText(currency.format(tipAmount));
        totalEditText.setText(currency.format(totalAmount));
        perPersonEditText.setText(currency.format(splitAmount));

        NumberFormat percent = NumberFormat.getPercentInstance();
        percentEditText.setText(percent.format(tipPercent));

    }

    private OnEditorActionListener editTextListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                calculateAndDisplay();
            }
            return false;
        }
    };

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    calculateAndDisplay();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(billAmountEditText.getWindowToken(), 0);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (view.getId() == R.id.percentSeekBar) {
                        calculateAndDisplay();
                    }
                    break;
            }

            return false;
        }
    };

    private OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            percentEditText.setText(progress + "%");

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            calculateAndDisplay();

        }
    };

    private RadioGroup.OnCheckedChangeListener radioGroupListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.roundNoneRadioButton:
                    rounding = ROUND_NONE;
                    break;
                case R.id.roundTipRadioButton:
                    rounding = ROUND_TIP;
                    break;
                case R.id.roundTotalRadioButton:
                    rounding = ROUND_TOTAL;
                    break;
            }
            calculateAndDisplay();
        }
    };

    private OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            split = position + 1;
            calculateAndDisplay();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

}
