package com.project.scan_on.veriationadapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.project.scan_on.R;

import java.util.ArrayList;
import java.util.List;

public class VeriationAdapter extends BaseAdapter {


    Context context;
    String veriationcolororsizelist[];
    LayoutInflater inflter;
    RadioGroup group;
    private List<RadioButton> arrayList;

    public VeriationAdapter(Context applicationContext, String[] countryList) {
        this.context = context;
        this.veriationcolororsizelist = countryList;
        inflter = (LayoutInflater.from(applicationContext));
        group = new RadioGroup(applicationContext);
        arrayList = new ArrayList<>();

    }

    @Override
    public int getCount() {
        return veriationcolororsizelist.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflter.inflate(R.layout.veriation_dialog_list_item, null,false);
          RadioGroup radioGroupid = (RadioGroup)  convertView.findViewById(R.id.radioGroup);
          radioGroupid.setOrientation(LinearLayout.VERTICAL);
//          if (position == veriationcolororsizelist.length -1) {
//              for (int row = 0; row < 1; row++) {
//                  RadioGroup group = new RadioGroup(parent.getContext());
//                  group.setOrientation(LinearLayout.VERTICAL);
//
//                  for (int i = 1; i <= veriationcolororsizelist.length; i++) {
//                        RadioButton rdbtn = new RadioButton(parent.getContext());
//                      int j = i-1;
//                      rdbtn.setId(View.generateViewId());
//                      rdbtn.setText(veriationcolororsizelist[j]);
//
//                      group.addView(rdbtn);
//                  }
//                  radioGroupid.addView(group);
//              }
//          }
        RadioButton rdbtn = new RadioButton(parent.getContext());
        rdbtn.setId(View.generateViewId());
        rdbtn.setText(veriationcolororsizelist[position]);
        rdbtn.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
        arrayList.add(rdbtn);
        if (position == veriationcolororsizelist.length -1) {
            for (int x=0;x<arrayList.size();x++){
                radioGroupid.addView(arrayList.get(x        ));
            }

        }



        return convertView;
    }
}
