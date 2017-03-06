package cn.yuan.leopard.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.yuan.leopardkit.LeopardHttp;
import com.yuan.leopardkit.interfaces.UploadIProgress;
import com.yuan.leopardkit.upload.FileUploadEnetity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.yuan.leopard.R;
import cn.yuan.leopard.adapters.UploadAdapter;
import cn.yuan.leopard.model.UploadModel;


public class UploadFragment extends Fragment implements UploadAdapter.IUploadGetPic {

    public static int RESULT_LOAD_IMAGE = 1000;

    private RecyclerView recyclerView;
    private UploadAdapter adapter;
    List<UploadModel> data = new ArrayList<>();

    private Button uploadBtn;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.upload_recyclerview);
        uploadBtn = (Button) view.findViewById(R.id.upload_btn);
        adapter = new UploadAdapter(data, this, getActivity());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setAdapter(adapter);

        //dialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("正在上传中");
    }

    private void initListener(){
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.size() <=1){
                    Toast.makeText(getActivity(),"未选择图片！",Toast.LENGTH_SHORT).show();
                    return ;
                }
                List<File> fileList = new ArrayList<File>();
                for (int i =0;i<data.size();i++){
                    if (i!=0){
                        fileList.add(data.get(i).getFile());
                    }
                }
                progressDialog.show();
                LeopardHttp.UPLOAD(new FileUploadEnetity("http://wxwusy.applinzi.com/leopardWeb/app/sample/upload.php",fileList), new UploadIProgress() {

                    @Override
                    public void onProgress(long progress, long total, int index, boolean done) {
                        Log.i("yuan","upload state: "+progress + " - "+total);
                        if (done){
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"所有图片上传成功！！",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSucess(String result) {
                        Toast.makeText(getActivity(),result,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Throwable e, String reason) {
                        Toast.makeText(getActivity(),reason,Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }


    private void initData() {
        if (data.size() <= 0)
            for (int i = 0; i < 1; i++) {
                File file = new File("");
                UploadModel model = new UploadModel(file);
                data.add(model);
            }

        adapter.notifyDataSetChanged();
    }

    private void addData(String filePath){
        File file = new File(filePath);
        UploadModel model = new UploadModel(file);
        data.add(model);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.i("yuan", "" + picturePath);
            addData(picturePath);
        }
    }

        @Override
        public void getPic () {
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }
