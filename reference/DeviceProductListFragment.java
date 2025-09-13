package com.jwlryk.ogkiosk;

import android.opengl.Visibility;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jwlryk.ogkiosk.API.ApiHelper;
import com.jwlryk.ogkiosk.API.Device;
import com.jwlryk.ogkiosk.API.Product;
import com.jwlryk.ogkiosk.Util.Dlog;

import java.util.List;

public class DeviceProductListFragment extends Fragment {

    private static final String ARG_DEVICE = "device";
    private GridView gridViewProducts; // GridView for displaying products
    private RecyclerView recyclerViewProducts;
    private RecyclerView recyclerViewProductsSub;


    // 새로운 Device 객체를 전달받아 Fragment를 생성하는 메서드
    public static DeviceProductListFragment newInstance(Device device) {
        DeviceProductListFragment fragment = new DeviceProductListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DEVICE, device);  // Device 객체를 번들로 전달
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_product_list, container, false);

//        gridViewProducts = view.findViewById(R.id.gridViewProducts); // GridView 초기화
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        recyclerViewProductsSub = view.findViewById(R.id.recyclerViewProductsSub);

        /* DEVMODE_info */
        // UI 요소 초기화
        LinearLayout DEVMODE_DeviceInfo = view.findViewById(R.id.DEVMODE_DeviceInfo);
        TextView deviceCodeTextView = view.findViewById(R.id.deviceCodeTextView);
        TextView deviceNameTextView = view.findViewById(R.id.deviceNameTextView);
        TextView deviceTypeTextView = view.findViewById(R.id.deviceTypeTextView);
        TextView deviceOpt00TextView = view.findViewById(R.id.deviceOpt00TextView);
        TextView deviceStatusTextView = view.findViewById(R.id.deviceStatusTextView);

        /* DEVMODE Control */
        DEVMODE_DeviceInfo.setVisibility(View.GONE);

        // 번들에서 Device 객체를 받아옴
        if (getArguments() != null) {
            Device device = (Device) getArguments().getSerializable(ARG_DEVICE);

            // Device 정보 화면에 표시
            if (device != null) {
                deviceCodeTextView.setText("Code: " + device.getCode());
                deviceNameTextView.setText("Name: " + device.getName());
                deviceTypeTextView.setText("Type: " + device.getType());
                deviceOpt00TextView.setText("Opt00: " + device.getOpt00());
                deviceStatusTextView.setText("Status: " + device.getStatus());

                loadProducts(device.getCode());

            }
        }

        return view;
    }
    // 제품 목록을 로드하는 함수
    // 제품 목록을 로드하는 함수
//    private void loadProducts(String deviceCode) {
//        ApiHelper apiHelper = new ApiHelper();
//        apiHelper.fetchProductsByDeviceCode(deviceCode, new ApiHelper.ApiCallback<List<Product>>() {
//            @Override
//            public void onSuccess(List<Product> products) {
//                int numColumns = products.size() == 42 ? 8 : (products.size() == 36 ? 4 : 2);
//                gridViewProducts.setNumColumns(numColumns);
//
//                // 어댑터 설정
//                ProductAdapter productAdapter = new ProductAdapter(getActivity(), products);
//                gridViewProducts.setAdapter(productAdapter);  // GridView에 어댑터를 설정
//                Dlog.d("Products loaded successfully: " + products.toString());
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                Toast.makeText(getActivity(), "Failed to load products: " + errorMessage, Toast.LENGTH_SHORT).show();
//                Dlog.e("Failed to load products: " + errorMessage);
//            }
//        });
//    }

    // 제품 목록을 로드하는 함수
    private void loadProducts(String deviceCode) {
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.fetchProductsByDeviceCode(deviceCode, new ApiHelper.ApiCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                setupRecyclerView(products);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getActivity(), "Failed to load products: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // RecyclerView 설정
    private void setupRecyclerView(List<Product> products) {
        // 기본 레이아웃 설정
        GridLayoutManager gridLayoutManagerMain = null;
        GridLayoutManager gridLayoutManagerSub = null;

        if (products.size() == 42) {
            // 42개일 경우 main에 8*4=32개, sub에 5*2=10개 할당
            gridLayoutManagerMain = new GridLayoutManager(getActivity(), 8);  // 8열 설정
            recyclerViewProducts.setLayoutManager(gridLayoutManagerMain);

            List<Product> firstSet = products.subList(0, 32);  // 첫 32개
            ProductAdapter productAdapterMain = new ProductAdapter(getActivity(), firstSet);
            recyclerViewProducts.setAdapter(productAdapterMain);

            gridLayoutManagerSub = new GridLayoutManager(getActivity(), 5);  // 5열 설정
            recyclerViewProductsSub.setLayoutManager(gridLayoutManagerSub);

            List<Product> secondSet = products.subList(32, 42);  // 나머지 10개
            ProductAdapter productAdapterSub = new ProductAdapter(getActivity(), secondSet);
            recyclerViewProductsSub.setAdapter(productAdapterSub);

        } else if (products.size() == 36) {
            // 36개일 경우 main에 4*9=36개 모두 할당, sub 비활성화
            gridLayoutManagerMain = new GridLayoutManager(getActivity(), 4);  // 4열 설정
            recyclerViewProducts.setLayoutManager(gridLayoutManagerMain);

            ProductAdapter productAdapterMain = new ProductAdapter(getActivity(), products);  // 모든 제품을 main에 할당
            recyclerViewProducts.setAdapter(productAdapterMain);

            recyclerViewProductsSub.setVisibility(View.GONE);  // sub 비활성화

        } else if (products.size() == 20) {
            // 20개일 경우 main에 3*4=12개, sub에 4*2=8개 할당
            gridLayoutManagerMain = new GridLayoutManager(getActivity(), 3);  // 3열 설정
            recyclerViewProducts.setLayoutManager(gridLayoutManagerMain);

            List<Product> firstSet = products.subList(0, 12);  // 첫 12개
            ProductAdapter productAdapterMain = new ProductAdapter(getActivity(), firstSet);
            recyclerViewProducts.setAdapter(productAdapterMain);

            gridLayoutManagerSub = new GridLayoutManager(getActivity(), 4);  // 4열 설정
            recyclerViewProductsSub.setLayoutManager(gridLayoutManagerSub);

            List<Product> secondSet = products.subList(12, 20);  // 나머지 8개
            ProductAdapter productAdapterSub = new ProductAdapter(getActivity(), secondSet);
            recyclerViewProductsSub.setAdapter(productAdapterSub);

        } else {
            // 기타 경우 기본 8열 설정 (모든 제품을 main에 할당)
            gridLayoutManagerMain = new GridLayoutManager(getActivity(), 8);
            recyclerViewProducts.setLayoutManager(gridLayoutManagerMain);

            ProductAdapter productAdapterMain = new ProductAdapter(getActivity(), products);
            recyclerViewProducts.setAdapter(productAdapterMain);

            recyclerViewProductsSub.setVisibility(View.GONE);  // sub 비활성화
        }
    }



}
