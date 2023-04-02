package com.example.smsincognito;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.android.billingclient.api.SkuDetailsResult;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuyActivity extends AppCompatActivity {
    BillingClient billingClient;
    String user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user_id = extras.getString("user_id");
        } else {
            create_alerte("Please restart application, if the probleme persists please contact timothee.escandell@gmail.com.", "Ok");
        }

        billingClient = BillingClient.newBuilder(this)
                        .enablePendingPurchases()
                        .setListener(new PurchasesUpdatedListener() {
                            @Override
                            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                    for (Purchase purchase: list) {
                                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                            verify_paiement_backend(purchase);
                                        }
                                    }
                                }
                            }
                        })
                       .build();

        connect_google_play_billing();
    }

    @Override
    protected void onResume() {
        super.onResume();
        billingClient.queryPurchasesAsync(
                BillingClient.ProductType.INAPP,
                new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            for (Purchase purchase: list) {
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                    verify_paiement_backend(purchase);
                                }
                            }
                        }
                    }
                }
        );
    }

    private void connect_google_play_billing() {
        billingClient.startConnection(
                new BillingClientStateListener() {
                    @Override
                    public void onBillingServiceDisconnected() {
                        connect_google_play_billing();
                    }

                    @Override
                    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            get_product_details();
                        }
                    }
                }
        );
    }

    private void get_product_details() {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("credit_1").setProductType(BillingClient.ProductType.INAPP).build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("credit_5").setProductType(BillingClient.ProductType.INAPP).build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("credit_10").setProductType(BillingClient.ProductType.INAPP).build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("credit_50").setProductType(BillingClient.ProductType.INAPP).build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("credit_150").setProductType(BillingClient.ProductType.INAPP).build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("credit_300").setProductType(BillingClient.ProductType.INAPP).build()))
                        .build();
        Activity activity = this;
        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                            Button credit_1_button = findViewById(R.id.buy1credit);
                            Button credit_5_button = findViewById(R.id.buy5credit);
                            Button credit_10_button = findViewById(R.id.buy10credit);
                            Button credit_50_button = findViewById(R.id.buy50credit);
                            Button credit_150_button = findViewById(R.id.buy150credit);
                            Button credit_300_button = findViewById(R.id.buy300credit);

                            ImmutableList productDetailsParamsList_1 =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetailsList.get(0))
                                                    .build()
                                    );
                            BillingFlowParams billingFlowParams_1 = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList_1)
                                    .build();
                            credit_1_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    billingClient.launchBillingFlow(activity, billingFlowParams_1);
                                }
                            });
                            ImmutableList productDetailsParamsList_5 =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetailsList.get(4))
                                                    .build()
                                    );
                            BillingFlowParams billingFlowParams_5 = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList_5)
                                    .build();
                            credit_5_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    billingClient.launchBillingFlow(activity, billingFlowParams_5);
                                }
                            });
                            ImmutableList productDetailsParamsList_10 =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetailsList.get(1))
                                                    .build()
                                    );
                            BillingFlowParams billingFlowParams_10 = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList_10)
                                    .build();
                            credit_10_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    billingClient.launchBillingFlow(activity, billingFlowParams_10);
                                }
                            });
                            ImmutableList productDetailsParamsList_50 =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetailsList.get(5))
                                                    .build()
                                    );
                            BillingFlowParams billingFlowParams_50 = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList_50)
                                    .build();
                            credit_50_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    billingClient.launchBillingFlow(activity, billingFlowParams_50);
                                }
                            });
                            ImmutableList productDetailsParamsList_150 =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetailsList.get(2))
                                                    .build()
                                    );
                            BillingFlowParams billingFlowParams_150 = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList_150)
                                    .build();
                            credit_150_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    billingClient.launchBillingFlow(activity, billingFlowParams_150);
                                }
                            });
                            ImmutableList productDetailsParamsList_300 =
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetailsList.get(3))
                                                    .build()
                                    );
                            BillingFlowParams billingFlowParams_300 = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList_300)
                                    .build();
                            credit_300_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    billingClient.launchBillingFlow(activity, billingFlowParams_300);
                                }
                            });
                        }
                    }
                }
        );
    }

    private void verify_paiement_backend(Purchase purchase) {
        JSONObject data = new JSONObject();

        try {
            data.put("token", user_id);
            data.put("purchase_token", purchase.getPurchaseToken());
            data.put("product_id", purchase.getProducts().get(0));
            data.put("payement_id", purchase.getOrderId());
        } catch (JSONException e) {
            return ;
        }

        JsonObjectRequest json_request = new JsonObjectRequest(Request.Method.POST, "http://tim-web.fr:7777/add_credit", data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //return_status = response.toString();
                        try {
                            String return_status = response.get("success").toString();

                            if (return_status == "true") {
                                ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                                billingClient.consumeAsync(consumeParams,
                                        new ConsumeResponseListener() {
                                            @Override
                                            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                                    create_alerte("YES ?", "YES");
                                                }
                                            }
                                        });
                            } else {
                                String msg = response.get("message").toString();

                                if (msg.length() != 0) {
                                    create_alerte("Payement failed.", "Ok");
                                    create_alerte(msg, "Ok");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String return_status = error.getMessage();
                create_alerte("Server down, please try again later.", "OK");
            }
        });

        RequestQueue request_queue;
        request_queue = Volley.newRequestQueue(getApplicationContext());
        request_queue.add(json_request);

        Handler handler;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //create_alerte();
            }
        }, 300);
        return ;
    }


    private void create_alerte(String str, String str_button) {
        AlertDialog.Builder alert_builder;
        AlertDialog alert;

        alert_builder = new AlertDialog.Builder(this);

        alert_builder.setMessage(str);
        alert_builder.setCancelable(false);
        alert_builder.setNeutralButton(str_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert = alert_builder.create();
        alert.show();
    }

}