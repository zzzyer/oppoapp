package com.example.oppoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class fed_activity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_trans2;
    private TextView tv_fed2;
    private TextView tv_epoch;
    private TextView tv_loss;
    private EditText mip;
    private EditText mport;
    private Button request;
    private Button bn_train2;
    private Button bn_test2;
    private Button add_data2;
    private Button bn_tran2;
    private Button bn_con2;
    private Button down_mod2;
    private Button up_mod2;

    private NetUtils netUtils;
    private Utils utils = new Utils();
    private String network_name = "MobileNetV2";
    private String network_file_name = network_name + ".tflite";
    private GlobalApp globalApp;
    private String DEVICE_NUMBER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fed);
        globalApp = ((GlobalApp) getApplicationContext());


        if (DEVICE_NUMBER == null) {
            DEVICE_NUMBER = globalApp.getDeviceNumber();
        }
        netUtils = new NetUtils(DEVICE_NUMBER);

        String modelFilePath = getCacheDir().getAbsolutePath() + "/model/download/" + network_file_name;
        System.out.println(modelFilePath);
        loadModel(modelFilePath);

        tv_trans2 = (TextView) findViewById(R.id.tv_trans2);
        tv_fed2 = (TextView) findViewById(R.id.tv_fed2);


        tv_epoch = findViewById(R.id.epoch_2);
        tv_loss = findViewById(R.id.loss_2);
        tv_trans2.setSelected(false);
        tv_fed2.setSelected(true);
        tv_trans2.setOnClickListener(this);
        request = findViewById(R.id.request);
        mip = findViewById(R.id.ip);
        mport = findViewById(R.id.port);
        bn_train2 = findViewById(R.id.bn_train_2);
        bn_test2 = findViewById(R.id.bn_test_2);
        bn_con2 = findViewById(R.id.bn_con_2);
        bn_tran2 = findViewById(R.id.bn_trans_2);
        add_data2 = findViewById(R.id.add_data_2);
        down_mod2 = findViewById(R.id.model_down_2);
        up_mod2 = findViewById(R.id.model_up_2);
        request.setOnClickListener(this);
        bn_train2.setOnClickListener(this);
        bn_test2.setOnClickListener(this);
        bn_con2.setOnClickListener(this);
        bn_train2.setOnClickListener(this);
        add_data2.setOnClickListener(this);
        down_mod2.setOnClickListener(this);
        up_mod2.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_trans2:
                Intent intent = new Intent();
                intent.setClass(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.request:
                CreateRequestDialog rdialog = new CreateRequestDialog(fed_activity.this,"??????");
                rdialog.setOnDialogClickListener(new CreateRequestDialog.OnDialogClickListener() {
                    @Override
                    public void onSureCLickListener(EditText mip,EditText mport) {

                        System.out.println("mip:"+mip.getText());
                        System.out.println("mport:"+mport.getText());
                    }

                });
                rdialog.show();
                break;
            case R.id.bn_train_2:
                //??????????????????,????????????????????????
                fedTrain();
                break;
            case R.id.bn_test_2:
                //??????????????????
                Intent inferIntent = new Intent(fed_activity.this, CameraActivity.class);
                startActivity(inferIntent);
                break;
            case R.id.add_data_2:
                //????????????
                try {
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
                    getData();
                } catch (FileNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bn_trans_2:
                //????????????
                break;
            case R.id.bn_con_2:
                //????????????
                break;
            case R.id.model_down_2:
                //????????????
                Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
                doRegisterAndDownload();
                break;
            case R.id.model_up_2:
                //????????????
                break;
            default:
                break;
        }
    }

    public void doRegisterAndDownload() {
        String cacheDir = getCacheDir().getAbsolutePath();
        AtomicBoolean flag = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                flag.set(netUtils.doRegisterAndDownload(cacheDir));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (flag.get()) {
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
        }

        String modelFilePath = getCacheDir().getAbsolutePath() + "/model/download/" + network_file_name;
        loadModel(modelFilePath);
        Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
    }

    private void loadModel(String modelFilePath) {
        if (!globalApp.isNull()){
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        if (new File(modelFilePath).exists()) {
            String parentDir = getCacheDir().getAbsolutePath();
            List<String> list = Arrays.asList("paper", "rock", "scissors");
            try {
                globalApp.setTlModel(new TransferLearningModelWrapper(parentDir, list));
                Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                throw new RuntimeException("?????????????????????", e);
            }
        } else {
            Toast.makeText(this, "???????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    private void getData() throws FileNotFoundException, InterruptedException {
        String dataPath = getCacheDir().getAbsolutePath() + "/rps";
        if (!new File(dataPath).exists()) {
            new File(dataPath).mkdirs();
        }

        String downDataUrl = "http://112.124.109.236/rps_64.zip";
        Thread thread = new Thread(() -> {
            if (!new File(dataPath + "/rps_64").exists()) {
                netUtils.getData(dataPath, downDataUrl);
            }
            try {
                globalApp.getTlModel().addBatchSample(dataPath + "/rps_64");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
    }

    //??????????????????
    public void fedTrain() {
        String ckptDirPath = getCacheDir() + "/ckpt";
        File file = new File(ckptDirPath);
        if (!file.exists()) {
            file.mkdir();
        }
        new Thread(() -> {
            netUtils.doConnect();

            for (int i = 0; i < 50; i++) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                System.out.println(i);
                String ckptFilePath = ckptDirPath + "/checkpoint_" + i + ".ckpt";

                int finalI = i;
                globalApp.getTlModel().fedTraining((epoch, loss) -> {
                    System.out.println("epoch: " + finalI + " ----- loss:" + loss);

                    tv_epoch.setText(finalI + "");
                    tv_loss.setText(loss + "");
                }, (acc) -> {
                    System.out.println("acc------" + acc);
//            tv_acc.setText(acc + "");
                });
                // ????????????
                globalApp.getTlModel().saveModel(ckptFilePath);
//                Toast.makeText(this, "???"+i+"epoch???????????????????????????......", Toast.LENGTH_SHORT).show();
                // ????????????????????????????????????
                netUtils.doUpAndDownLoadParam(ckptFilePath, countDownLatch);
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!new File(ckptFilePath).exists()) {
//                    Toast.makeText(this, "ckpt???????????????", Toast.LENGTH_SHORT).show();
                    System.out.println("ckpt???????????????");
                    return;
                }
                globalApp.getTlModel().restoreModel(ckptFilePath);
//                Toast.makeText(this, "???"+i+"epoch?????????????????????", Toast.LENGTH_SHORT).show();

            }
        }).start();

    }
}