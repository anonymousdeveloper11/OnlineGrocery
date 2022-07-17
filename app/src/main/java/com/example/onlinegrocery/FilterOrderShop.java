package com.example.onlinegrocery;

import android.widget.Filter;

import com.example.onlinegrocery.adapters.AdapterOrderShop;
import com.example.onlinegrocery.adapters.AdapterProductSeller;
import com.example.onlinegrocery.models.ModelOrderShop;
import com.example.onlinegrocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {
    private AdapterOrderShop adapterOrderShop;
    private ArrayList<ModelOrderShop> orderList;

    public FilterOrderShop(AdapterOrderShop adapterOrderShop, ArrayList<ModelOrderShop> orderShopList) {
        this.adapterOrderShop = adapterOrderShop;
        this.orderList = orderShopList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results =  new FilterResults();
        //validate data for the search query
        if(constraint !=null && constraint.length()>0){
            //search field not empty, searching something , perform search
            //change to uppercase  to make case insensitive
            constraint = constraint.toString().toUpperCase();

            //add filtered data to list
          ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
          for(int i=0; i<orderList.size(); i++){

              //check search by title and category
              if(orderList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                  //add filtered data to list
                  filteredModels.add(orderList.get(i));

              }
          }
          results.count = filteredModels.size();
          results.values = filteredModels;


        }
        else{
            //search field empty not searching return original all/ complete list
            results.count = orderList.size();
            results.values = orderList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

       adapterOrderShop.orderShopList = (ArrayList<ModelOrderShop>) results.values;
        //refresh adapter
        adapterOrderShop.notifyDataSetChanged();

    }
}
